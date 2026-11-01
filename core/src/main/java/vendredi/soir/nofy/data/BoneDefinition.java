package vendredi.soir.nofy.data;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * One joint of a rig, plus the single part pinned to it. All coordinates are in the rig's canvas
 * space (see RigDefinition).
 *
 * <p>trimX/trimY is where the part's image sat on the full canvas before it was cropped to its
 * opaque bounds; without it a trimmed part cannot be placed back relative to its pivot.
 *
 * <p>z is draw order, and it is deliberately not the bone tree: the far arm draws behind the torso
 * and the near arm in front, though both hang off the same bone.
 */
@Getter
@NoArgsConstructor
public class BoneDefinition {
  private String id;
  private String parent;
  private float pivotX;
  private float pivotY;
  private String part;
  private float trimX;
  private float trimY;
  private float partWidth;
  private float partHeight;
  private int z;
}
