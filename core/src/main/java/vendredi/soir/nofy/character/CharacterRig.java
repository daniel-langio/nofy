package vendredi.soir.nofy.character;

import vendredi.soir.nofy.data.CharacterBuild;
import vendredi.soir.nofy.data.CharacterCatalog;
import vendredi.soir.nofy.data.CharacterKit;
import vendredi.soir.nofy.data.LimbPose;
import vendredi.soir.nofy.data.PoseDefinition;

/**
 * One character's skeleton in the prototype's 300-wide body frame: resolves named poses (base +
 * pose + IK + stoop) and works out where the feet and hips land (placement).
 */
public class CharacterRig {
  /** Body-frame ground line, and the shoe's height from ankle to sole at adult scale. */
  public static final float GROUND = 598f;

  private static final float SOLE = 50f;

  private final CharacterKit kit;
  private final CharacterBuild.Build build;
  private final float[] h0;
  private final float y0;
  private final float[][] lengths = new float[4][];

  public CharacterRig(CharacterKit kit, String characterId) {
    this.kit = kit;
    CharacterCatalog cat = kit.getCatalog();
    this.build = cat.getCharacters().get(characterId).getBuild();
    this.h0 = cat.getH0();
    float[] al = cat.getAdultLengths().get("leg");
    // Y0 puts the shoes on the ground at GROUND.
    this.y0 = 19f + (al[0] + al[1]) - (build.getLeg()[0] + build.getLeg()[1]);
    lengths[ResolvedPose.ARM_L] = build.getArm();
    lengths[ResolvedPose.ARM_R] = build.getArm();
    lengths[ResolvedPose.LEG_L] = build.getLeg();
    lengths[ResolvedPose.LEG_R] = build.getLeg();
  }

  /** Canvas units per body-frame unit, for turning pose offsets into rig offsets. */
  public float canvasScale() {
    return kit.getCatalog().getCanvasScale();
  }

  public float scale() {
    return build.getScale();
  }

  /** Base pose, then the named pose on top. */
  public ResolvedPose resolve(String poseId) {
    PoseDefinition base = kit.getPoses().get("base");
    PoseDefinition spec = kit.getPoses().get(poseId);
    ResolvedPose p = new ResolvedPose();
    p.overlay(base);
    for (ResolvedPose.Limb l : p.limbs) {
      l.bend = 0; // unset until the pose or the manifest says which way the joint bends
    }
    p.overlay(spec);
    LimbPose[] defs = {spec.getArmL(), spec.getArmR(), spec.getLegL(), spec.getLegR()};
    for (int i = 0; i < 4; i++) {
      ResolvedPose.Limb l = p.limbs[i];
      LimbPose s = defs[i];
      int preferred =
          kit.getManifest().getLimbs().get(ResolvedPose.LIMB_NAMES[i]).getPreferredBend();
      if (s != null) {
        l.bend = s.getBend() != null ? s.getBend() : preferred;
        if (i < 2) l.wrist = s.getWrist() != null ? s.getWrist() : 0;
        if (s.getT() != null) solveTarget(p, i, s);
      }
    }
    // Stoop is added after IK on purpose: targets are authored against the unstooped spine.
    p.spine += build.getStoop();
    p.neck -= build.getStoop() * 0.6f;
    for (int i = 0; i < 4; i++) {
      if (p.limbs[i].bend == 0) {
        p.limbs[i].bend =
            kit.getManifest().getLimbs().get(ResolvedPose.LIMB_NAMES[i]).getPreferredBend();
      }
    }
    return p;
  }

  private void solveTarget(ResolvedPose p, int i, LimbPose s) {
    CharacterCatalog.BindLimb r = kit.getCatalog().getBindRig().get(ResolvedPose.LIMB_NAMES[i]);
    ResolvedPose.Limb l = p.limbs[i];
    float l1 = lengths[i][0] * l.s1;
    float l2 = lengths[i][1] * l.s2;
    float[] adult = kit.getCatalog().getAdultLengths().get(i < 2 ? "arm" : "leg");
    float reach = (lengths[i][0] + lengths[i][1]) / (adult[0] + adult[1]);
    float[] t = s.getT();
    if (i < 2 && "body".equals(s.getFrame())) {
      t = rotAbout(t, h0, -p.spine);
    }
    float[] sh = r.getS();
    float tx = sh[0] + (t[0] - sh[0]) * reach;
    float ty = sh[1] + (t[1] - sh[1]) * reach;
    double vx = tx - sh[0];
    double vy = ty - sh[1];
    double d = Math.min(l1 + l2 - 0.5, Math.max(Math.abs(l1 - l2) + 1, Math.hypot(vx, vy)));
    double at = Math.toDegrees(Math.atan2(vx, vy));
    double al = Math.toDegrees(Math.acos((l1 * l1 + d * d - l2 * l2) / (2 * l1 * d)));
    double ga = Math.toDegrees(Math.acos((l1 * l1 + l2 * l2 - d * d) / (2 * l1 * l2)));
    double a = at + l.bend * al;
    while (a > 180) a -= 360;
    while (a < -180) a += 360;
    double out = Math.hypot(vx, vy) - l1 - l2;
    if (out > 1) {
      p.warnings.add(
          ResolvedPose.LIMB_NAMES[i] + ": target is " + Math.round(out) + "px out of reach");
    }
    l.a = (float) a;
    l.b = (float) (-l.bend * (180 - ga));
  }

  /** Elbow/knee and hand/ankle points of a limb in the body frame, before root transform. */
  public float[][] limbPoints(ResolvedPose p, int i) {
    ResolvedPose.Limb l = p.limbs[i];
    float[] s = kit.getCatalog().getBindRig().get(ResolvedPose.LIMB_NAMES[i]).getS();
    double a = Math.toRadians(l.a);
    double e = Math.toRadians(l.a + l.b);
    float ex = (float) (s[0] + lengths[i][0] * l.s1 * Math.sin(a));
    float ey = (float) (s[1] + lengths[i][0] * l.s1 * Math.cos(a));
    float hx = (float) (ex + lengths[i][1] * l.s2 * Math.sin(e));
    float hy = (float) (ey + lengths[i][1] * l.s2 * Math.cos(e));
    return new float[][] {s, {ex, ey}, {hx, hy}};
  }

  /** A body-frame point after root transform, character scale, and before placement. */
  public float[] toWorldRaw(ResolvedPose p, float[] pt) {
    float[] q = rotAbout(pt, h0, p.rootRot);
    float wx = q[0] + p.rootX;
    float wy = q[1] + p.rootY + y0;
    float sc = build.getScale();
    return new float[] {150 + (wx - 150) * sc, GROUND + (wy - GROUND) * sc};
  }

  /**
   * Contact ownership: grounded poses are moved until the lowest shoe rests on the ground, seated
   * ones until the hips rest on the seat; airborne and lying poses keep their own height.
   */
  public float placement(ResolvedPose p) {
    if (p.rootY <= -20 || Math.abs(p.rootRot) >= 30) return 0;
    float sc = build.getScale();
    if ("none".equals(p.seat)) {
      float lowest = Float.NEGATIVE_INFINITY;
      for (int i = ResolvedPose.LEG_L; i <= ResolvedPose.LEG_R; i++) {
        lowest = Math.max(lowest, toWorldRaw(p, limbPoints(p, i)[2])[1] + SOLE * sc);
      }
      return GROUND - lowest;
    }
    CharacterCatalog.Seat seat = kit.getCatalog().getSeats().get(p.seat);
    return seat.getTop() - (toWorldRaw(p, h0)[1] + seat.getOff() * sc);
  }

  /** Foot gap to the ground: positive = lifted, negative = sunk. For tests and the rig check. */
  public float footGap(ResolvedPose p, int leg, float surface) {
    return surface
        - (toWorldRaw(p, limbPoints(p, leg)[2])[1] + placement(p) + SOLE * build.getScale());
  }

  public float[] h0() {
    return h0;
  }

  static float[] rotAbout(float[] p, float[] c, float deg) {
    double a = Math.toRadians(deg);
    double cos = Math.cos(a);
    double sin = Math.sin(a);
    double x = p[0] - c[0];
    double y = p[1] - c[1];
    return new float[] {(float) (c[0] + x * cos - y * sin), (float) (c[1] + x * sin + y * cos)};
  }
}
