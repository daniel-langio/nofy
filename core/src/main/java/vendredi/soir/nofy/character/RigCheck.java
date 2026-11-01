package vendredi.soir.nofy.character;

import java.util.ArrayList;
import java.util.List;
import vendredi.soir.nofy.data.CharacterKit;
import vendredi.soir.nofy.data.RigManifest;

/**
 * QA for a resolved pose against the rig manifest: angle limits, limbs flipping against their
 * stored bend, IK targets out of reach, feet sinking or all floating, hips off the seat. Empty
 * result = pass.
 */
public final class RigCheck {
  private static final float STEP_SURFACE = 504f;

  private RigCheck() {}

  public static List<String> check(CharacterKit kit, CharacterRig rig, ResolvedPose p) {
    return check(kit, rig, p, true);
  }

  /**
   * flips=false skips the bend-direction test, for frames between keys: the stored bend only
   * switches on a key, so tweening between opposite bends legitimately passes through the wrong
   * side for a few frames.
   */
  public static List<String> check(
      CharacterKit kit, CharacterRig rig, ResolvedPose p, boolean flips) {
    RigManifest m = kit.getManifest();
    List<String> w = new ArrayList<>(p.warnings);
    control(w, m, "root.x", p.rootX);
    control(w, m, "root.y", p.rootY);
    control(w, m, "root.rot", p.rootRot);
    control(w, m, "spine", p.spine);
    control(w, m, "neck", p.neck);
    for (int i = 0; i < 4; i++) {
      String k = ResolvedPose.LIMB_NAMES[i];
      ResolvedPose.Limb l = p.limbs[i];
      RigManifest.Limb spec = m.getLimbs().get(k);
      float[] la = spec.getLimits().get("a");
      float[] lb = spec.getLimits().get("b");
      if (l.a < la[0] || l.a > la[1]) w.add(k + " a " + Math.round(l.a) + "° is past its limit");
      if (Math.abs(l.b) > Math.max(Math.abs(lb[0]), Math.abs(lb[1]))) {
        w.add(k + " b " + Math.round(l.b) + "° is past its limit");
      }
      if (flips && Math.abs(l.b) > 3 && Math.signum(l.b) != -l.bend) {
        w.add("Flip: " + k + " bends against its stored direction");
      }
    }
    if (p.rootY > -20 && Math.abs(p.rootRot) < 30) {
      float surface = "step".equals(p.seat) ? STEP_SURFACE : CharacterRig.GROUND;
      float gl = rig.footGap(p, ResolvedPose.LEG_L, surface);
      float gr = rig.footGap(p, ResolvedPose.LEG_R, surface);
      if (gl < -8) w.add("legL foot sinks " + Math.round(-gl) + "px");
      if (gr < -8) w.add("legR foot sinks " + Math.round(-gr) + "px");
      if ("none".equals(p.seat) && Math.abs(gl) > 8 && Math.abs(gr) > 8) {
        w.add("Neither foot touches the ground: the character floats");
      }
      if (!"none".equals(p.seat)) {
        var seat = kit.getCatalog().getSeats().get(p.seat);
        float hip = rig.toWorldRaw(p, rig.h0())[1] + rig.placement(p);
        float gap = seat.getTop() - (hip + seat.getOff() * rig.scale());
        if (Math.abs(gap) > 12)
          w.add("Seat contact: hips are " + Math.round(gap) + "px off the " + p.seat);
      }
    }
    return w;
  }

  private static void control(List<String> w, RigManifest m, String path, float v) {
    float[] lim = m.getControls().get(path);
    if (v < lim[0] || v > lim[1]) {
      w.add(path + " = " + Math.round(v) + " is outside its limit " + lim[0] + " to " + lim[1]);
    }
  }
}
