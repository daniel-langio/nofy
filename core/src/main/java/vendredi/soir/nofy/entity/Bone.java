package vendredi.soir.nofy.entity;

import com.badlogic.gdx.math.MathUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * One joint of a rig: the pose set on it (local, relative to its parent) plus the world transform
 * recomputed from the whole parent chain each frame.
 *
 * <p>Bind offsets arrive already converted to world units and y-up, so nothing here knows about
 * canvas space - see RiggedEntity, which owns that one conversion.
 *
 * <p>Bones carry rotation and UNIFORM scale only. A parent's non-uniform scale combined with a
 * rotated child produces shear, and Batch.draw cannot express shear - it offers scale-then-rotate,
 * five degrees of freedom, where a general affine needs six. Allowing it would mean leaving
 * SpriteBatch for PolygonSpriteBatch or hand-built vertices.
 */
@Getter
public class Bone {
  private final String id;
  private final Bone parent;

  /** Offset from the parent's pivot to this bone's, in world units, in the rig's bind pose. */
  private final float bindOffsetX;

  private final float bindOffsetY;

  @Setter private float rotation = 0f;
  @Setter private float x = 0f;
  @Setter private float y = 0f;
  @Setter private float scale = 1f;

  private float worldX;
  private float worldY;
  private float worldRotation;
  private float worldScale = 1f;
  private boolean flipped;

  public Bone(String id, Bone parent, float bindOffsetX, float bindOffsetY) {
    this.id = id;
    this.parent = parent;
    this.bindOffsetX = bindOffsetX;
    this.bindOffsetY = bindOffsetY;
  }

  /**
   * Recomputes this bone's world transform. Callers must update bones parent-before-child, which
   * RiggedEntity guarantees by ordering them topologically once at construction.
   *
   * <p>originX/originY is the owning entity's position - only the root bone is placed against it.
   */
  void update(float originX, float originY) {
    float offsetX = bindOffsetX + x;
    float offsetY = bindOffsetY + y;

    flipped = false;

    if (parent == null) {
      // Rotating the root turns the whole character about the root pivot, so the pivot itself
      // stays put - the root's own rotation must not move it.
      worldRotation = rotation;
      worldScale = scale;
      worldX = originX + offsetX;
      worldY = originY + offsetY;
      return;
    }

    float parentRadians = parent.worldRotation * MathUtils.degreesToRadians;
    float cos = MathUtils.cos(parentRadians);
    float sin = MathUtils.sin(parentRadians);

    worldRotation = parent.worldRotation + rotation;
    worldScale = parent.worldScale * scale;
    worldX = parent.worldX + parent.worldScale * (offsetX * cos - offsetY * sin);
    worldY = parent.worldY + parent.worldScale * (offsetX * sin + offsetY * cos);
  }

  /**
   * Reflects this bone's world transform across a vertical line, turning a character to face the
   * other way. Applied after the whole chain has been updated, so every bone mirrors against the
   * same axis rather than each against its parent.
   *
   * <p>Reflecting a rotation inverts it - S(-1,1)R(a) equals R(-a)S(-1,1) - which is why the angle
   * is negated here and the part is drawn with a negative horizontal scale.
   */
  void mirrorAbout(float axisX) {
    worldX = 2f * axisX - worldX;
    worldRotation = -worldRotation;
    flipped = true;
  }
}
