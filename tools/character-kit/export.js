#!/usr/bin/env node
// Exports Nori, Bastien and Eric as 16 rigid parts each, in an I-pose bind pose (every limb
// straight down), into assets-src/characters/<id>/{rig.json,parts/NN_<bone>.svg}.
// Then run tools/build-character.sh <id> to rasterize.
//
//   node tools/character-kit/export.js [id...]
//
// Geometry comes from kit.js (the prototype's own drawing code). Each limb segment is a straight
// capsule from its pivot to its child's pivot, so parent and child overlap fully at any angle.
const fs = require('fs');
const path = require('path');
const makeKit = require('./kit.js');

const ROOT = path.resolve(__dirname, '../..');
const DATA = path.join(ROOT, 'assets/data/character-kit');
const read = (f) => JSON.parse(fs.readFileSync(path.join(DATA, f), 'utf8'));
const catalog = read('characters.json');
const gestures = read('gestures.json');
const exprs = read('expressions.json');

// Body frame (300 wide, y down) to the 1024 canvas: X = 512 + (x - 150) * s, ground at GROUND_Y.
// Runtime pose offsets use the same factor (canvasScale in characters.json).
const S = catalog.canvasScale;
const GROUND_Y = 940;
const SOLE_TO_ANKLE = 36; // the shoe turns about the bottom of the sock, 36 below the leg end

// bone, parent, draw order (z = file number, same as Arnold)
const BONES = [
  ['foot_far', 'shin_far'], ['shin_far', 'thigh_far'], ['thigh_far', 'pelvis'], ['pelvis', null],
  ['foot_near', 'shin_near'], ['shin_near', 'thigh_near'], ['thigh_near', 'pelvis'],
  ['upperarm_far', 'torso'], ['forearm_far', 'upperarm_far'], ['hand_far', 'forearm_far'],
  ['torso', 'pelvis'], ['neck', 'torso'], ['head', 'neck'],
  ['upperarm_near', 'torso'], ['forearm_near', 'upperarm_near'], ['hand_near', 'forearm_near'],
];

function exportCharacter(id) {
  const ch = catalog.characters[id];
  const b = ch.build;
  const COL = ch.colors;
  const k = makeKit(id, COL, gestures, exprs.expressions, exprs.lookOffsets);
  const { f, ln, tube, handSVG, crotch, torsoSVG, headSVG } = k;
  const [H0, N] = [catalog.h0, catalog.n];
  const y0 = 19 + (catalog.adultLengths.leg[0] + catalog.adultLengths.leg[1]) - (b.leg[0] + b.leg[1]);
  const a = S * b.scale;
  const tx = 512 - 150 * a;
  const ty = GROUND_Y + S * (y0 - 598) * b.scale;
  const pt = (p) => [tx + a * p[0], ty + a * p[1]];
  const wrap = (inner) =>
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="1024" height="1024">` +
    `<g transform="matrix(${a} 0 0 ${a} ${tx} ${ty})">${inner}</g></svg>\n`;

  // straight limbs: S, elbow/knee, wrist/ankle
  const limbs = {};
  for (const key of ['armL', 'armR', 'legL', 'legR']) {
    const r = catalog.bindRig[key];
    const [l1, l2] = key.startsWith('arm') ? b.arm : b.leg;
    const s = r.S;
    limbs[key] = { r, S: s, E: [s[0], s[1] + l1], H: [s[0], s[1] + l1 + l2] };
  }

  // capsule between two points on a vertical line, with its own radius at each end
  const cap = (x, y0_, r0, y1, r1, fill, bottomCap = true) =>
    `<circle cx="${x}" cy="${y0_}" r="${r0}" fill="${fill}"/>` +
    (bottomCap ? `<circle cx="${x}" cy="${y1}" r="${r1}" fill="${fill}"/>` : '') +
    `<path d="M${x - r0} ${y0_} L${x + r0} ${y0_} L${x + r1} ${y1} L${x - r1} ${y1}Z" fill="${fill}"/>`;

  const svg = {};

  for (const [side, key] of [['far', 'legL'], ['near', 'legR']]) {
    const L = limbs[key];
    const w = L.r.w;
    const T = [L.S[0], L.S[1] - 12];
    svg['thigh_' + side] = cap(L.S[0], T[1], w[0] - 0.5, L.E[1], w[1], COL.pants);
    const A = [L.H[0], L.H[1] - 8];
    svg['shin_' + side] =
      `<g transform="translate(${f(L.H)})"><rect x="-11" y="-4" width="22" height="40" fill="${COL.sock}"/>` +
      `<rect x="-25" y="-18" width="50" height="20" rx="6" fill="${COL.cuff}"/></g>` +
      cap(L.E[0], L.E[1], w[1], A[1], w[2], COL.pants, false) +
      `<circle cx="${L.E[0]}" cy="${L.E[1]}" r="${w[1]}" fill="${COL.pants}"/>`;
    svg['foot_' + side] =
      `<g transform="translate(${f(L.H)})"><path d="M15 52 L-30 52 Q-34 52 -34 48 Q-34 34 -19 33 L8 33 Q15 33 15 41Z" fill="${COL.shoe}"/></g>`;
  }

  svg.pelvis =
    crotch(limbs.legL, limbs.legR) +
    `<circle cx="${H0[0]}" cy="${H0[1] + 16}" r="24" fill="${COL.pants}"/>` +
    `<path d="M101 318 L199 318 Q206 340 199 354 Q190 366 168 368 L132 368 Q110 366 101 354 Q94 340 101 318Z" fill="${COL.pants}"/>` +
    `<path d="M108 330 L132 343 M192 330 L168 343" ${ln(3.5, COL.cuff)}/>`;

  svg.torso = torsoSVG();
  svg.neck = `<path d="M150 208 L150 182" ${ln(30, COL.skinS)}/>`;
  const hdy = b.headDy || 0;
  const headT = `translate(${N[0]} ${N[1] + hdy}) scale(${b.head}) translate(${-N[0]} ${-N[1]})`;
  const face = { look: 'ahead', expr: 'calm' };
  svg.head = `<g transform="${headT}">${headSVG(face, 'back')}${headSVG(face, 'front')}</g>`;

  for (const [side, key] of [['far', 'armL'], ['near', 'armR']]) {
    const L = limbs[key];
    const far = side === 'far';
    const skin = far ? COL.skinS : COL.skin;
    const cloth = far ? COL.topS : COL.top;
    const x = L.S[0];
    let upper = cap(x, L.S[1], 13, L.E[1], 13, skin);
    let fore = cap(x, L.E[1], 13, L.H[1], 13, skin);
    if (ch.longSleeves) {
      const wy = L.E[1] + (L.H[1] - L.E[1]) * 0.82;
      upper += cap(x, L.S[1], 16.5, L.E[1], 16.5, cloth);
      fore += cap(x, L.E[1], 16.5, wy, 16.5, cloth);
      fore += `<path d="M${x} ${wy - (L.H[1] - L.E[1]) * 0.06} L${x} ${wy}" ${ln(35, COL.hem)}/>`;
    } else {
      const cy = L.S[1] + (L.E[1] - L.S[1]) * 0.5;
      upper += `<path d="M${x} ${L.S[1]} L${x} ${cy}" ${ln(36, cloth)}/>`;
    }
    svg['upperarm_' + side] = upper;
    svg['forearm_' + side] = fore;
    svg['hand_' + side] =
      `<g transform="translate(${f(L.H)})">${handSVG('relaxed', 0, null).replaceAll(COL.skin, skin)}</g>`;
  }

  // pivots, canvas space
  const P = {
    pelvis: H0, torso: H0, neck: N, head: N,
  };
  for (const side of ['far', 'near']) {
    const A = limbs[side === 'far' ? 'armL' : 'armR'];
    const Lg = limbs[side === 'far' ? 'legL' : 'legR'];
    P['upperarm_' + side] = A.S; P['forearm_' + side] = A.E; P['hand_' + side] = A.H;
    P['thigh_' + side] = Lg.S; P['shin_' + side] = Lg.E;
    P['foot_' + side] = [Lg.H[0], Lg.H[1] + SOLE_TO_ANKLE];
  }

  const outDir = path.join(ROOT, 'assets-src/characters', id);
  fs.mkdirSync(path.join(outDir, 'parts'), { recursive: true });
  const rig = { canvas: [1024, 1024], png_scale: 2, note: `${ch.name}: I-pose bind pose, exported by tools/character-kit/export.js.`, bones: {} };
  BONES.forEach(([bone, parent], i) => {
    const part = String(i + 1).padStart(2, '0') + '_' + bone;
    fs.writeFileSync(path.join(outDir, 'parts', part + '.svg'), wrap(svg[bone]));
    rig.bones[bone] = { pivot: pt(P[bone]).map((v) => Math.round(v * 10) / 10), parent, part };
  });
  fs.writeFileSync(path.join(outDir, 'rig.json'), JSON.stringify(rig, null, 2) + '\n');
  console.log('exported', id);
}

const ids = process.argv.slice(2);
(ids.length ? ids : Object.keys(catalog.characters)).forEach(exportCharacter);
