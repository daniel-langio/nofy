package vendredi.soir.nofy.data;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A body pose in the character kit's prototype conventions (see docs/rigged-characters.md).
 *
 * <p>Every field is optional: a named pose lists only what it changes from base.json, and absent
 * fields stay null so the resolver can tell "unset" from 0. Numeric fields interpolate between clip
 * keys; discrete fields (facing, look, expr, seat) switch exactly on a key.
 *
 * <p>spine, neck and root.rot are clockwise-positive (SVG); the PoseApplier flips them for Nofy.
 */
@Getter
@NoArgsConstructor
public class PoseDefinition {
  private String name;
  private String facing;
  private String look;
  private String expr;
  private String seat;
  private Float spine;
  private Float neck;
  private RootPose root;
  private LimbPose armL;
  private LimbPose armR;
  private LimbPose legL;
  private LimbPose legR;
}
