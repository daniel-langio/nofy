// Drawing functions for the Nori/Bastien/Eric characters, moved verbatim from the prototype
// (pose-studio.html) so the exporter reproduces its exact geometry. Everything is drawn in the
// 300-wide body frame, y down, hips at (150, 330).
//
// Only the first argument set changes per character: kit(char, COL, GESTURES, EXPR, LOOK).
module.exports = function kit(CHAR, COL, GESTURES, EXPR, LOOK) {
const pose = {};
const rad=d=>d*Math.PI/180, deg=r=>r*180/Math.PI;
const f=p=>p[0].toFixed(1)+' '+p[1].toFixed(1);
const clone=o=>JSON.parse(JSON.stringify(o));
const ln=(w,c)=>`fill="none" stroke="${c}" stroke-width="${w}" stroke-linecap="round" stroke-linejoin="round"`;
function tube(S,E,A,w0,w1,w2,crease){
  const sub=(a,b)=>[a[0]-b[0],a[1]-b[1]],len=v=>Math.hypot(v[0],v[1])||1,unit=v=>{const l=len(v);return[v[0]/l,v[1]/l];};
  const d1=len(sub(E,S)),d2=len(sub(A,E)),u1=unit(sub(E,S)),u2=unit(sub(A,E));
  const bend=Math.acos(Math.max(-1,Math.min(1,u1[0]*u2[0]+u1[1]*u2[1])));
  const r=Math.min(d1*.45,d2*.45,8+bend*w1*1.1);
  const K1=[E[0]-u1[0]*r,E[1]-u1[1]*r],K2=[E[0]+u2[0]*r,E[1]+u2[1]*r],C=[];
  const line=(P,Q,n)=>{for(let i=0;i<n;i++){const t=i/n;C.push({p:[P[0]+(Q[0]-P[0])*t,P[1]+(Q[1]-P[1])*t],t:unit(sub(Q,P))});}};
  line(S,K1,5);
  for(let i=0;i<10;i++){const t=i/10,u=1-t;C.push({p:[u*u*K1[0]+2*u*t*E[0]+t*t*K2[0],u*u*K1[1]+2*u*t*E[1]+t*t*K2[1]],t:unit([2*u*(E[0]-K1[0])+2*t*(K2[0]-E[0]),2*u*(E[1]-K1[1])+2*t*(K2[1]-E[1])])});}
  line(K2,A,5);C.push({p:A,t:u2});
  let s=0;C.forEach((c,i)=>{if(i)s+=len(sub(c.p,C[i-1].p));c.s=s;});
  const kS=C[10].s,tot=s,wAt=x=>x<kS?w0+(w1-w0)*x/kS:w1+(w2-w1)*(x-kS)/(tot-kS||1);
  const Lf=[],Rt=[];C.forEach(c=>{const n=[-c.t[1],c.t[0]],w=wAt(c.s);Lf.push([c.p[0]+n[0]*w,c.p[1]+n[1]*w]);Rt.push([c.p[0]-n[0]*w,c.p[1]-n[1]*w]);});
  let cr='';const dg=deg(bend);
  if(crease&&dg>14){
    const inner=unit([u2[0]-u1[0],u2[1]-u1[1]]),tx=unit([u1[0]+u2[0],u1[1]+u2[1]]),M=[(K1[0]+2*E[0]+K2[0])/4,(K1[1]+2*E[1]+K2[1])/4],op=Math.min(1,(dg-14)/35).toFixed(2);
    for(const k of[-5,4]){const a=[M[0]+inner[0]*w1*.85+tx[0]*k,M[1]+inner[1]*w1*.85+tx[1]*k],b=[a[0]-inner[0]*9+tx[0]*k*.4,a[1]-inner[1]*9+tx[1]*k*.4];
      cr+=`<path d="M${f(a)} L${f(b)}" ${ln(3,crease)} opacity="${op}"/>`;}
  }
  return{d:'M'+Lf.map(f).join(' L')+' L'+Rt.reverse().map(f).join(' L')+'Z',cr};
}

/* Parametric cartoon hand. Wrist at (0,0), fingers pointing down (+y).
   Everything is one skin colour; fingers are round-capped strokes tucked under a round palm,
   so "curl" just shortens a finger until only a knuckle bump is left. */
function handShape(g,o={}){
  const s=o.size||1,col=o.color||'#F0B49A',shade=o.shade||'#D28E78',side=o.side||1,turn=o.turn??0,lag=o.lag||[0,0,0,0];
  const cx=0,cy=9*s,rx=9*s,ry=8.6*s,fw=5.8*s,lens=[1,1.1,1,.8];
  let fingers='',thumb='',creases='';
  for(let i=0;i<4;i++){
    const a=(i-1.5)*g.spread+lag[i];
    const r=rad(a),bx=cx+side*rx*.62*Math.sin(r)*(1-turn*.6)+side*turn*(i-1.5)*-1.2*s,by=cy+ry*.55*Math.cos(r);
    const L=s*(12.5-10.5*g.curl[i])*lens[i];
    const ex=bx+side*Math.sin(r)*L*(1-turn*.5),ey=by+Math.cos(r)*L;
    fingers+=`<path d="M${bx.toFixed(1)} ${by.toFixed(1)} L${ex.toFixed(1)} ${ey.toFixed(1)}" stroke="${col}" stroke-width="${fw.toFixed(1)}" stroke-linecap="round"/>`;
    if(g.curl[i]>.75)creases+=`<path d="M${(bx-side*2.2*s).toFixed(1)} ${(ey-1.5*s).toFixed(1)} L${(bx+side*2.2*s).toFixed(1)} ${(ey-1.5*s).toFixed(1)}" stroke="${shade}" stroke-width="${(1.3*s).toFixed(1)}" stroke-linecap="round" opacity=".55"/>`;
  }
  const ta=rad(-side*g.thumb),tb=[cx-side*rx*.72,cy-ry*.25],tL=s*(10.5-6*g.tcurl);
  const te=[tb[0]+Math.sin(ta)*tL,tb[1]+Math.cos(ta)*tL];
  thumb=`<path d="M${tb[0].toFixed(1)} ${tb[1].toFixed(1)} L${te[0].toFixed(1)} ${te[1].toFixed(1)}" stroke="${col}" stroke-width="${(6.4*s).toFixed(1)}" stroke-linecap="round"/>`;
  const palm=`<ellipse cx="${cx}" cy="${cy.toFixed(1)}" rx="${(rx*(1-turn*.35)).toFixed(1)}" ry="${ry.toFixed(1)}" fill="${col}"/>`;
  return fingers+palm+creases+thumb;
}

/* hands: drawn pointing down (+y) from the wrist */
function handSVG(kind,upright,fingers){
  const sk=COL.skin;
  if(kind==='hidden')return'';
  if(kind==='clasp')return`<circle cx="0" cy="9" r="11" fill="${sk}"/><circle cx="-4" cy="14" r="8" fill="${sk}"/>`;
  if(kind==='cup')return`<g transform="rotate(${upright.toFixed(1)} 0 8)">
      <path d="M-11 -16 L11 -16 L8.5 22 Q8 25 5 25 L-5 25 Q-8 25 -8.5 22Z" fill="#E9F6FF" opacity=".9"/>
      <path d="M-10.2 -3 L10.2 -3 L8.5 22 Q8 25 5 25 L-5 25 Q-8 25 -8.5 22Z" fill="#6B3A1E"/>
      <rect x="-12.5" y="-19" width="25" height="5" rx="2.5" fill="#DDEFFC"/><path d="M3 -19 L-7 -36" ${ln(3,COL.accent)}/></g>
      <circle cx="0" cy="8" r="10" fill="${sk}"/><circle cx="-6" cy="4" r="5" fill="${sk}"/>`;
  if(kind==='bag')return`<g transform="rotate(${upright.toFixed(1)} 0 0)">
      <path d="M-12 6 Q-17 34 -19 48 M12 6 Q17 34 19 48" ${ln(3.5,'#F4EADA')}/>
      <path d="M-27 48 L27 48 L22 90 Q21 95 15 95 L-15 95 Q-21 95 -22 90Z" fill="#D9A15A"/>
      <path d="M-25 60 L25 60 M-24 72 L24 72 M-23 84 L23 84 M-12 48 L-10 95 M0 48 L0 95 M12 48 L10 95" ${ln(2,'#A56E2E')}/>
      <path d="M-17 53 Q0 36 17 53 L15 57 L-15 57Z" fill="#FFF7EA"/></g><circle cx="0" cy="7" r="10" fill="${sk}"/>`;
  return handShape(fingers||GESTURES[kind]||GESTURES.relaxed,{size:1.45,color:sk,shade:COL.skinS});
}
function crotch(Ll,Lr){
  // fabric fillet between the thighs: a curve that leaves each thigh's inner edge tangentially (back toward the hips),
  // so a lifted or splayed thigh never shows a corner, shelf or spike at the seat
  const ref=[150,372];
  // measured from the same root point the thigh tube starts at (12px above the hip joint), so the curve stays inside the tube edge
  const edge=L=>{const T=[L.S[0],L.S[1]-12],dx=L.E[0]-T[0],dy=L.E[1]-T[1],l=Math.hypot(dx,dy)||1,u=[dx/l,dy/l];let n=[-u[1],u[0]];
    if(n[0]*(ref[0]-T[0])+n[1]*(ref[1]-T[1])<0)n=[-n[0],-n[1]];
    return{p:[T[0]+u[0]*50+n[0]*24,T[1]+u[1]*50+n[1]*24],u};};
  const A=edge(Ll),B=edge(Lr),k=22;
  const c1=[A.p[0]-A.u[0]*k,A.p[1]-A.u[1]*k],c2=[B.p[0]-B.u[0]*k,B.p[1]-B.u[1]*k];
  return`<path d="M${f(Ll.S)} L${f(A.p)} C${f(c1)} ${f(c2)} ${f(B.p)} L${f(Lr.S)}Z" fill="${COL.pants}"/>`;
}
function torsoSVG(){
  if(CHAR==='eric')return`<path d="M102 248 Q104 210 130 204 L170 204 Q196 210 198 248 Q210 292 202 318 Q194 342 150 344 Q106 342 98 318 Q90 292 102 248Z" fill="${COL.top}"/>
      <path d="M104 236 Q150 230 196 236 M100 262 Q150 256 200 262 M99 288 Q150 282 201 288" ${ln(7,COL.stripe)}/>
      <path d="M118 270 Q150 262 182 270 L186 318 Q184 344 150 346 Q116 344 114 318Z" fill="${COL.pants}" stroke="${COL.pants}" stroke-width="6" stroke-linejoin="round"/>
      <path d="M122 272 Q120 240 126 212 M178 272 Q180 240 174 212" ${ln(9,COL.pants)}/>
      <circle cx="123" cy="270" r="5" fill="${COL.accent}"/><circle cx="177" cy="270" r="5" fill="${COL.accent}"/>
      <path d="M136 290 Q150 296 164 290 L163 306 Q150 312 137 306Z" fill="${COL.pantsS}"/>
      <path d="M130 205 Q150 222 170 205Z" fill="${COL.skin}"/><path d="M129 205 Q150 226 171 205" ${ln(6,COL.collar)}/>`;
  if(CHAR==='bastien'&&pose.view==='back')return`<path d="M92 306 Q94 344 128 344 L172 344 Q206 344 208 306Z" fill="${COL.hem}"/>
      <path d="M104 248 Q106 212 130 205 L170 205 Q194 212 196 248 Q216 286 210 312 Q204 336 176 338 L124 338 Q96 336 90 312 Q84 286 104 248Z" fill="${COL.top}"/>
      <path d="M132 206 Q150 216 168 206" ${ln(7,COL.hem)}/><path d="M150 214 L150 324" ${ln(2.4,COL.hem)}/>`;
  if(CHAR==='bastien')return`<path d="M92 306 Q94 344 128 344 L172 344 Q206 344 208 306Z" fill="${COL.hem}"/>
      <path d="M104 248 Q106 212 130 205 L170 205 Q194 212 196 248 Q216 286 210 312 Q204 336 176 338 L124 338 Q96 336 90 312 Q84 286 104 248Z" fill="${COL.top}"/>
      <path d="M128 206 Q130 250 150 258 Q170 250 172 206Z" fill="${COL.collar}" stroke="${COL.collar}" stroke-width="8" stroke-linejoin="round"/>
      <path d="M137 206 Q140 226 150 230 Q160 226 163 206Z" fill="${COL.skinS}"/>
      <path d="M150 262 L150 324" ${ln(3,COL.hem)}/>
      <circle cx="157" cy="276" r="3.6" fill="${COL.accent}"/><circle cx="157" cy="296" r="3.6" fill="${COL.accent}"/><circle cx="157" cy="316" r="3.6" fill="${COL.accent}"/>
      <path d="M112 300 L134 300 M166 300 L188 300" ${ln(3,COL.hem)}/>`;
  return`<path d="M100 300 Q100 334 128 334 L172 334 Q200 334 200 300Z" fill="${COL.hem}"/>
      <path d="M104 252 Q106 214 130 206 L170 206 Q194 214 196 252 L200 304 Q200 324 180 324 L120 324 Q100 324 100 304Z" fill="${COL.top}"/>
      <path d="M128 207 Q150 246 172 207Z" fill="${COL.skin}"/><path d="M128 205 Q150 218 172 205 L172 214 Q150 228 128 214Z" fill="${COL.skinS}"/>
      <path d="M127 206 Q150 250 173 206" ${ln(7,COL.collar)}/>`;
}
function headBastien(p,part,gx,gy,e){
  if(part==='back')return'';
  if(p.view==='back')return`<circle cx="106" cy="146" r="10" fill="${COL.skin}"/><circle cx="194" cy="146" r="10" fill="${COL.skin}"/>
      <rect x="108" y="96" width="84" height="94" rx="32" fill="${COL.skin}"/>
      <path d="M108 132 Q110 158 128 166 Q150 172 172 166 Q190 158 192 132 Q170 146 150 146 Q130 146 108 132Z" fill="${COL.hair}" stroke="${COL.hair}" stroke-width="4" stroke-linejoin="round"/>
      <path d="M122 104 Q140 96 160 98" ${ln(4,'#FFFFFF')} opacity=".22"/><path d="M107 140 L101 140 M193 140 L199 140" ${ln(2.6,COL.glass)}/>`;
  let o=`<circle cx="106" cy="146" r="10" fill="${COL.skin}"/><circle cx="194" cy="146" r="10" fill="${COL.skin}"/>`;
  o+=`<rect x="108" y="96" width="84" height="94" rx="32" fill="${COL.skin}"/>`;
  o+=`<ellipse cx="111" cy="140" rx="8" ry="15" transform="rotate(8 111 140)" fill="${COL.hair}"/><ellipse cx="189" cy="140" rx="8" ry="15" transform="rotate(-8 189 140)" fill="${COL.hair}"/>`;
  for(const[x,y]of[[131,142],[169,140]]){
    o+=`<ellipse cx="${x-2}" cy="${y+13}" rx="8" ry="4.5" fill="${COL.blush}"/>`;
    if(e.eyes==='squeeze')o+=`<path d="M${x-6} ${y-2} Q${x} ${y+4} ${x+6} ${y-2}" ${ln(3.4,COL.eye)}/>`;
    else if(e.eyes==='happy')o+=`<path d="M${x-6} ${y+2} Q${x} ${y-5} ${x+6} ${y+2}" ${ln(3.4,COL.eye)}/>`;
    else if(e.eyes==='half')o+=`<path d="M${x-6+gx} ${y} L${x+6+gx} ${y}" ${ln(3.4,COL.eye)}/><path d="M${x-5+gx} ${y+1} Q${x+gx} ${y+5} ${x+5+gx} ${y+1}" fill="${COL.eye}"/>`;
    else if(e.eyes==='closed')o+=`<path d="M${x-6} ${y+1} Q${x} ${y+4} ${x+6} ${y+1}" ${ln(3.4,COL.eye)}/>`;
    else if(e.eyes==='wide')o+=`<circle cx="${x+gx}" cy="${y+gy}" r="6.6" fill="${COL.eye}"/><circle cx="${x+gx+2}" cy="${y+gy-2}" r="2" fill="#fff"/>`;
    else if(e.eyes==='down')o+=`<circle cx="${x+gx}" cy="${y+3}" r="4.6" fill="${COL.eye}"/>`;
    else o+=`<circle cx="${(x+gx).toFixed(1)}" cy="${(y+gy).toFixed(1)}" r="5" fill="${COL.eye}"/>`;
    o+=`<circle cx="${x}" cy="${y}" r="12.5" fill="${COL.glass}" fill-opacity=".14" stroke="${COL.glass}" stroke-width="3.4"/>`;
  }
  o+=`<path d="M143 139 Q150 135 157 139" ${ln(3,COL.glass)}/>`;
  const BR={neutral:[[119,121,141,118],[159,117,181,119]],up:[[119,114,141,110],[159,109,181,113]],worried:[[119,120,141,112],[159,111,181,119]],
    angry:[[119,114,141,121],[159,120,181,113]],low:[[119,123,141,122],[159,121,181,122]],skeptic:[[119,121,141,118],[159,109,181,112]]}[e.brows||'neutral'];
  o+=BR.map(([a,b,c,d])=>`<path d="M${a} ${b} Q${(a+c)/2} ${Math.min(b,d)-3} ${c} ${d}" ${ln(6.5,COL.hair)}/>`).join('');
  o+=`<path d="M152 145 L150 160 Q146 163 141 159" ${ln(3.4,COL.skinS)}/>`;
  if(e.mouth==='laugh'||e.mouth==='grin')o+=`<path d="M136 172 Q150 169 164 171 Q162 190 150 191 Q138 190 136 172Z" fill="${COL.lips}"/><ellipse cx="150" cy="185" rx="7" ry="3.5" fill="${COL.tongue}"/>`;
  else if(e.mouth==='pout'||e.mouth==='sip')o+=`<ellipse cx="149" cy="176" rx="5" ry="4" fill="${COL.lips}"/>`;
  else if(e.mouth==='o')o+=`<ellipse cx="150" cy="180" rx="5.5" ry="7" fill="${COL.lips}"/>`;
  else if(e.mouth==='frown')o+=`<path d="M141 182 Q150 175 159 182" ${ln(3.2,COL.lips)}/>`;
  else if(e.mouth==='wobble')o+=`<path d="M140 179 Q145 175 150 179 Q155 183 160 179" ${ln(3,COL.lips)}/>`;
  else if(e.mouth==='flat')o+=`<path d="M142 178 L158 178" ${ln(3.2,COL.lips)}/>`;
  else o+=`<path d="M141 176 Q150 181 159 176" ${ln(3.2,COL.lips)}/>`;
  // the mustache carries the mouth's mood: it lifts for smiles and droops for frowns
  const md={laugh:-4,grin:-4,smile:-1,frown:3,wobble:2}[e.mouth]||0;
  o+=`<path d="M130 ${169+md} Q133 160 150 163 Q167 160 170 ${169+md} Q163 175 150 171 Q137 175 130 ${169+md}Z" fill="${COL.hair}" stroke="${COL.hair}" stroke-width="5" stroke-linejoin="round"/>`;
  // no hat: a bald top with a soft shine and a little wisp of hair
  o+=`<path d="M128 104 Q140 98 154 99" ${ln(4,'#FFFFFF')} opacity=".22"/><path d="M146 97 Q150 88 158 90" ${ln(3,COL.hair)}/>`;
  return o;
}
function headEric(p,part,gx,gy,e){
  if(part==='back')return'';
  let o=`<circle cx="104" cy="146" r="11" fill="${COL.skin}"/><circle cx="196" cy="146" r="11" fill="${COL.skin}"/>`;
  o+=`<rect x="104" y="88" width="92" height="102" rx="46" fill="${COL.skin}"/>`;
  // curly hair: a cap of overlapping round curls with one springy curl on top
  o+=[[118,104,15],[136,92,17],[158,89,17],[178,98,15],[190,114,10],[110,120,9]].map(([x,y,r])=>`<circle cx="${x}" cy="${y}" r="${r}" fill="${COL.hair}"/>`).join('');
  o+=`<path d="M150 76 Q158 62 168 70 Q174 78 164 82" ${ln(5.5,COL.hair)}/><path d="M128 92 Q140 84 152 86" ${ln(3,'#5A3A8A')}/>`;
  for(const[x,y]of[[132,140],[168,138]]){
    o+=`<circle cx="${x-2}" cy="${y+15}" r="8" fill="${COL.blush}" opacity=".55"/>`;
    if(e.eyes==='squeeze')o+=`<path d="M${x-7} ${y-2} Q${x} ${y+5} ${x+7} ${y-2}" ${ln(3.6,COL.eye)}/>`;
    else if(e.eyes==='happy')o+=`<path d="M${x-7} ${y+2} Q${x} ${y-6} ${x+7} ${y+2}" ${ln(3.6,COL.eye)}/>`;
    else if(e.eyes==='half')o+=`<path d="M${x-7+gx} ${y} L${x+7+gx} ${y}" ${ln(3.6,COL.eye)}/>`;
    else o+=`<circle cx="${(x+gx).toFixed(1)}" cy="${(y+gy).toFixed(1)}" r="7" fill="${COL.eye}"/><circle cx="${(x+gx+2.4).toFixed(1)}" cy="${(y+gy-2.4).toFixed(1)}" r="2.3" fill="#fff"/>`;
  }
  o+=`<path d="M126 124 Q132 121 138 123 M162 121 Q168 119 174 121" ${ln(4,COL.hair)}/>`;
  o+=`<ellipse cx="150" cy="154" rx="4.5" ry="3.5" fill="${COL.skinS}"/>`;
  if(e.mouth==='laugh'||e.mouth==='grin')o+=`<path d="M138 166 Q150 163 162 165 Q160 182 150 183 Q140 182 138 166Z" fill="${COL.lips}"/><ellipse cx="150" cy="177" rx="6" ry="3" fill="${COL.tongue}"/>`;
  else if(e.mouth==='pout'||e.mouth==='sip')o+=`<ellipse cx="150" cy="170" rx="4.5" ry="4" fill="${COL.lips}"/>`;
  else o+=`<path d="M141 167 Q150 175 159 167" ${ln(3.4,COL.lips)}/>`;
  return o;
}
function headSVG(p,part){
  if(CHAR==='eric'){const[gx,gy]=LOOK[p.look]||[0,0];return headEric(p,part,gx,gy,EXPR[p.expr]||EXPR.calm);}
  if(CHAR==='bastien'){const[gx,gy]=LOOK[p.look]||[0,0];return headBastien(p,part,gx,gy,EXPR[p.expr]||EXPR.calm);}
  const[gx,gy]=LOOK[p.look]||[0,0],e=EXPR[p.expr]||EXPR.calm;
  if(part==='back')return`<path d="M96 132 Q94 80 150 74 Q206 78 206 132 L208 182 Q206 196 190 196 L112 196 Q96 196 95 182Z" fill="${COL.hair}"/>`;
  let o=`<rect x="110" y="92" width="80" height="96" rx="26" fill="${COL.skin}"/>`;
  for(const[x,y]of[[132,141],[168,139]]){
    o+=`<ellipse cx="${x-3}" cy="${y+10}" rx="8" ry="4.5" fill="${COL.blush}"/>`;
    if(e.eyes==='squeeze')o+=`<path d="M${x-7} ${y-3} Q${x} ${y+4} ${x+7} ${y-3}" ${ln(3.6,COL.eye)}/>`;
    else if(e.eyes==='happy')o+=`<path d="M${x-6} ${y+2} Q${x} ${y-5} ${x+6} ${y+2}" ${ln(3.4,COL.eye)}/>`;
    else if(e.eyes==='half')o+=`<path d="M${x-6+gx} ${y} L${x+6+gx} ${y}" ${ln(3.4,COL.eye)}/>`;
    else o+=`<circle cx="${(x+gx).toFixed(1)}" cy="${(y+gy).toFixed(1)}" r="5.6" fill="${COL.eye}"/>`;
  }
  o+=`<path d="M124 125 Q132 121 140 123 M160 121 Q168 118 176 120" ${ln(5.5,COL.hairS)}/>`;
  o+=`<path d="M152 143 L150 158 Q146 161 141 157" ${ln(3.4,COL.skinS)}/>`;
  if(e.mouth==='laugh')o+=`<path d="M134 160 Q150 156 166 159 Q165 186 150 188 Q135 186 134 160Z" fill="${COL.lips}"/><path d="M136 160 Q150 157 164 159 L163 165 Q150 163 137 166Z" fill="#fff"/><ellipse cx="150" cy="181" rx="8" ry="4" fill="${COL.tongue}"/>`;
  else if(e.mouth==='grin')o+=`<path d="M138 166 Q150 163 162 165 Q160 180 150 181 Q140 180 138 166Z" fill="${COL.lips}"/><ellipse cx="150" cy="176" rx="6" ry="3" fill="${COL.tongue}"/>`;
  else if(e.mouth==='pout')o+=`<ellipse cx="149" cy="170" rx="5.5" ry="4.5" fill="${COL.lips}"/>`;
  else if(e.mouth==='sip')o+=`<ellipse cx="146" cy="170" rx="4" ry="3.4" fill="${COL.lips}"/>`;
  else o+=`<path d="M141 167 Q150 164 159 166 Q158 174 150 175 Q142 174 141 167Z" fill="${COL.lips}"/>`;
  o+=`<path d="M106 128 Q104 86 150 84 Q196 86 194 124 L188 124 Q184 110 172 110 L128 112 Q116 114 112 132Z" fill="${COL.hair}"/>`;
  return o;
}

return { rad, deg, f, ln, tube, handSVG, crotch, torsoSVG, headSVG };
};
