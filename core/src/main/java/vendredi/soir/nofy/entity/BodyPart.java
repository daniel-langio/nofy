package vendredi.soir.nofy.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.Set;
import lombok.Getter;

/**
 * One drawable piece of a rigged character, pinned to a bone.
 *
 * <p>It deliberately does not extend TexturedEntity: a Sprite carries its own position, rotation
 * and scale, and for a body part all three belong to the bone. Nothing but the bone may move a
 * part, or the rig and whatever wrote the part directly will fight each other every frame.
 *
 * <p>It is a Drawable rather than a full Entity for now. Promoting it - so a part can carry its own
 * SpriteAnimation (an eye blinking on its own clock, independently of whatever the body is doing)
 * and its own action rules (hovering a hand) - only needs its own action state added here.
 */
@Getter
public class BodyPart implements Drawable {
  private final String name;
  private final Bone bone;
  private final TextureRegion region;
  private final int z;

  /** Offset from the bone's pivot to this part's lower-left corner, unrotated, in world units. */
  private final float offsetX;

  private final float offsetY;
  private final float width;
  private final float height;

  public BodyPart(
      String name,
      Bone bone,
      TextureRegion region,
      int z,
      float offsetX,
      float offsetY,
      float width,
      float height) {
    this.name = name;
    this.bone = bone;
    this.region = region;
    this.z = z;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.width = width;
    this.height = height;
  }

  @Override
  public void draw(Batch batch) {
    // Rotate and scale about the BONE PIVOT rather than the part's own corner. originX/originY are
    // measured from the draw position, so negating the offset puts the origin back on the pivot.
    batch.draw(
        region,
        bone.getWorldX() + offsetX,
        bone.getWorldY() + offsetY,
        -offsetX,
        -offsetY,
        width,
        height,
        bone.isFlipped() ? -bone.getWorldScale() : bone.getWorldScale(),
        bone.getWorldScale(),
        bone.getWorldRotation());
  }

  @Override
  public Set<Texture> getTexturesToDispose() {
    return Set.of(region.getTexture());
  }
}
