package vendredi.soir.nofy.data;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * One arm or leg. Either a/b (direct angles) or t (an IK target) drives it.
 *
 * <p>a is the upper segment's direction from straight down, positive toward screen-right; b is the
 * extra bend of the lower segment. bend (1 or -1) is the stored elbow/knee side, so IK never flips
 * a limb. s1/s2 foreshorten the segments. foot tilts the shoe, wrist rotates the hand (arms only).
 *
 * <p>armL/legL are the FAR limbs of a left-facing character, armR/legR the NEAR ones.
 */
@Getter
@NoArgsConstructor
public class LimbPose {
  private Float a;
  private Float b;
  private float[] t;

  /** "body" when t is in body coordinates rather than torso coordinates. */
  private String frame;

  private Integer bend;
  private Float s1;
  private Float s2;
  private Float foot;
  private Float wrist;
  private String hand;
  private String layer;
  private GestureDefinition fingers;
}
