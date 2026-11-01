package vendredi.soir.nofy.character;

import java.util.LinkedHashMap;
import java.util.Map;
import vendredi.soir.nofy.entity.RiggedEntity;

/**
 * Turns a resolved pose into Nofy bone rotations (degrees, counter-clockwise, relative to the
 * parent). a and b keep their sign; spine, neck, root.rot and wrist are clockwise in the prototype,
 * so they flip.
 */
public final class PoseApplier {
  private PoseApplier() {}

  /**
   * Poses the entity: bone rotations, facing, and the root offset that places it on its contact.
   */
  public static void apply(RiggedEntity entity, CharacterRig rig, ResolvedPose p) {
    rotations(p).forEach(entity::setBoneRotation);
    entity.setFlipped("right".equals(p.facing));
    // Body-frame units, y down; the character scale was applied to the art, not to root.x/y.
    float dx = p.rootX * rig.scale() * rig.canvasScale();
    float dy = (p.rootY * rig.scale() + rig.placement(p)) * rig.canvasScale();
    entity.setRootOffset(dx, dy);
  }

  public static Map<String, Float> rotations(ResolvedPose p) {
    Map<String, Float> r = new LinkedHashMap<>();
    r.put("pelvis", -p.rootRot);
    r.put("torso", -p.spine);
    r.put("neck", 0f);
    r.put("head", -p.neck);
    limb(r, p.limbs[ResolvedPose.ARM_L], "upperarm_far", "forearm_far");
    r.put("hand_far", -p.limbs[ResolvedPose.ARM_L].wrist);
    limb(r, p.limbs[ResolvedPose.ARM_R], "upperarm_near", "forearm_near");
    r.put("hand_near", -p.limbs[ResolvedPose.ARM_R].wrist);
    leg(r, p.limbs[ResolvedPose.LEG_L], "thigh_far", "shin_far", "foot_far");
    leg(r, p.limbs[ResolvedPose.LEG_R], "thigh_near", "shin_near", "foot_near");
    return r;
  }

  private static void limb(Map<String, Float> r, ResolvedPose.Limb l, String upper, String lower) {
    r.put(upper, l.a);
    r.put(lower, l.b);
  }

  private static void leg(
      Map<String, Float> r, ResolvedPose.Limb l, String thigh, String shin, String foot) {
    limb(r, l, thigh, shin);
    // ponytail: toe stays 1 even for a backward knee; flipping the shoe needs a per-part flipX
    // (phase 5). Add it then and use toe = l.bend == 1 ? -1 : 1.
    r.put(foot, -(0.15f * (l.a + l.b) + l.foot));
  }
}
