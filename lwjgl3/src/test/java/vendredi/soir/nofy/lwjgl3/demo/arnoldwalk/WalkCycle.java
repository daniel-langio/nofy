package vendredi.soir.nofy.lwjgl3.demo.arnoldwalk;

import com.badlogic.gdx.math.MathUtils;
import vendredi.soir.nofy.entity.RiggedEntity;

/**
 * A walk, computed from a single phase angle rather than played back from keyframes.
 *
 * <p>This is a stand-in for the clip system, not a replacement for it: it exists because a rig can
 * be posed today and nothing can be authored yet. Procedural motion of this kind stays genuinely
 * useful afterwards though - it composes with whatever clip is playing, which is what idle
 * breathing or a head turning to follow the player need.
 *
 * <p>Limbs are driven in antiphase - near leg forward with far arm - and the whole pose is scaled
 * by a blend factor that eases in and out, so stopping settles into a stand rather than freezing
 * mid-stride.
 */
final class WalkCycle {
  /** Radians per second. One full cycle is two steps, so this is about 2.5 steps a second. */
  static final float CADENCE = 8f;

  static final float THIGH_SWING = 22f;
  private static final float KNEE_BEND = 36f;
  private static final float ARM_SWING = 22f;
  private static final float ELBOW_BEND = 14f;

  /**
   * How much of the leg's rotation the ankle cancels. A foot is a child of the shin, so without
   * this it inherits the whole leg's swing and points at the sky at the ends of a stride.
   */
  private static final float ANKLE_LEVELLING = 0.7f;

  /** How far the hips drop at the bottom of a stride, in world units. */
  private static final float HIP_BOB = 0.5f;

  /**
   * Where in the cycle the knee folds. A leg is straight through its stance - planted, with the
   * body travelling over it - and folds through its swing, while it is off the ground coming
   * forward. With speed matched to stride, the planted foot is world-stationary around phase 0, so
   * stance is the half-cycle centred there and the fold belongs to the other half: -PI/2, nudged
   * slightly early so the knee breaks at toe-off rather than at mid-swing.
   *
   * <p>Getting this half a cycle out bends the knee of whichever leg is carrying the weight, and
   * the walk reads as running backwards.
   */
  private static final float KNEE_LAG = -1.3f;

  /** Per second; how quickly the walk pose eases in when starting and out when stopping. */
  private static final float BLEND_RATE = 9f;

  /** 0 is a pure sine swing, 1 a pure triangle wave. */
  private static final float TRIANGLE_BLEND = 0.7f;

  private float phase;
  private float blend;

  void update(RiggedEntity character, float deltaTime, boolean walking) {
    if (walking) {
      phase += deltaTime * CADENCE;
    }
    blend = MathUtils.lerp(blend, walking ? 1f : 0f, Math.min(1f, deltaTime * BLEND_RATE));

    float near = swing(phase);
    float far = -near;

    float thighNear = THIGH_SWING * near;
    float thighFar = THIGH_SWING * far;
    // A knee folds backwards - the heel comes up towards the buttock. Arnold is drawn facing
    // left, so behind him is +x, and a positive rotation is what carries the foot there. Negating
    // this throws the shin out in front of the knee instead, which reads as a bird's leg.
    float shinNear = KNEE_BEND * knee(phase);
    float shinFar = KNEE_BEND * knee(phase + MathUtils.PI);

    character.setBoneRotation("thigh_near", blend * thighNear);
    character.setBoneRotation("thigh_far", blend * thighFar);
    character.setBoneRotation("shin_near", blend * shinNear);
    character.setBoneRotation("shin_far", blend * shinFar);
    character.setBoneRotation("foot_near", blend * -(thighNear + shinNear) * ANKLE_LEVELLING);
    character.setBoneRotation("foot_far", blend * -(thighFar + shinFar) * ANKLE_LEVELLING);

    // Arms counter-swing against the legs on the same side - that opposition is most of what
    // makes a walk read as a walk rather than a shuffle.
    character.setBoneRotation("upperarm_near", blend * ARM_SWING * far);
    character.setBoneRotation("upperarm_far", blend * ARM_SWING * near);
    character.setBoneRotation("forearm_near", blend * -ELBOW_BEND * (1f + far) / 2f);
    character.setBoneRotation("forearm_far", blend * -ELBOW_BEND * (1f + near) / 2f);

    // Two dips per cycle: the body drops once per step, not once per stride.
    character.bone("pelvis").setY(blend * -HIP_BOB * Math.abs(near));
  }

  /**
   * The thigh's swing curve, a sine flattened towards a triangle wave.
   *
   * <p>A pure sine's rate of change peaks mid-stance and falls to nothing at the ends, so a planted
   * foot slides backwards through the middle of a step and forwards at either end. A triangle wave
   * has a constant rate of change, which is exactly what keeps a foot still while the body travels
   * over it - but it turns at the extremes like a hinge, so only part of the way there.
   */
  private static float swing(float phase) {
    float sine = MathUtils.sin(phase);
    float triangle = (2f / MathUtils.PI) * (float) Math.asin(sine);
    return MathUtils.lerp(sine, triangle, TRIANGLE_BLEND);
  }

  private static float knee(float phase) {
    return Math.max(0f, MathUtils.sin(phase + KNEE_LAG));
  }
}
