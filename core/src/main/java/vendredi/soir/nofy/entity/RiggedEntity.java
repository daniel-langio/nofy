package vendredi.soir.nofy.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import vendredi.soir.nofy.data.ActionRule;
import vendredi.soir.nofy.data.BoneDefinition;
import vendredi.soir.nofy.data.RigDefinition;

/**
 * A character assembled from a bone hierarchy, each bone drawing one body part - as opposed to
 * AnimatedEntity, which is a single sprite playing one flipbook.
 *
 * <p>This class owns the single conversion from the rig's canvas space (y-down, origin top-left,
 * authored resolution) to world space (y-up, scaled so the character stands targetHeight units
 * tall). Everything downstream of the constructor is already in world units.
 *
 * <p>Its parts are owned here and are deliberately NOT added to GameWorld: a hand in the world's
 * entity list would be treated as a free-standing actor by the collision and trigger systems.
 */
@Getter
public class RiggedEntity implements Entity {
  private final String name;
  private final String defaultActionId;

  /** Topologically ordered - every bone appears after its parent, so one pass updates them all. */
  private final List<Bone> bones;

  private final Map<String, Bone> bonesById;

  /** In draw order (the rig's z), which is not the bone tree. */
  private final List<BodyPart> parts;

  private final float width;
  private final float height;

  private float x = 0f;
  private float y = 0f;
  private String currentActionId;

  /** Mirrors the character horizontally, so it can face either way without a second set of art. */
  @Setter private boolean flipped = false;

  @Setter private List<ActionRule> actionRules = List.of();
  @Setter private boolean solid = false;
  @Setter private boolean positionDrivenByTrigger = false;

  private final Rectangle bounds = new Rectangle();

  /**
   * @param regionLoader resolves a rig part path to its texture region. Injected rather than called
   *     directly so the transform maths can be exercised without a GL context.
   */
  public RiggedEntity(
      String name,
      RigDefinition rig,
      float targetHeight,
      String defaultActionId,
      Function<String, TextureRegion> regionLoader) {
    this.name = name;
    this.defaultActionId = defaultActionId;
    this.currentActionId = defaultActionId;

    // The one canvas-to-world conversion. The character's opaque extent, not the canvas, is what
    // targetHeight measures - the canvas is mostly empty space.
    float scale = targetHeight / rig.getFigureHeight();
    this.width = rig.getFigureWidth() * scale;
    this.height = targetHeight;

    this.bones = buildBones(rig, scale);
    this.bonesById = new HashMap<>();
    bones.forEach(bone -> bonesById.put(bone.getId(), bone));
    this.parts = buildParts(rig, scale, bonesById, regionLoader);

    updateBones();
  }

  /** Poses one bone, in degrees counter-clockwise, relative to its parent. */
  public void setBoneRotation(String boneId, float degrees) {
    bone(boneId).setRotation(degrees);
  }

  public Bone bone(String boneId) {
    Bone bone = bonesById.get(boneId);
    if (bone == null) {
      throw new IllegalArgumentException(String.format("Bone %s not found", boneId));
    }
    return bone;
  }

  @Override
  public void animate(float deltaTime) {
    updateBones();
  }

  @Override
  public void draw(Batch batch) {
    // Draw order is the rig's flat z list, never the bone tree: the far arm draws behind the torso
    // and the near arm in front, though both are children of the same bone.
    parts.forEach(part -> part.draw(batch));
  }

  @Override
  public Rectangle getBoundingRectangle() {
    // A logical box, deliberately not the posed art's extent: bounds derived from swinging limbs
    // would make solid entities jitter and pop apart as the character moves.
    return bounds.set(x, y, width, height);
  }

  @Override
  public void setX(float x) {
    this.x = x;
  }

  @Override
  public void setY(float y) {
    this.y = y;
  }

  @Override
  public void setPosition(float x, float y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public void translate(float xAmount, float yAmount) {
    this.x += xAmount;
    this.y += yAmount;
  }

  /**
   * Records the requested action. Nothing consumes it yet - a rigged character plays keyframed
   * clips rather than flipbooks, and clips are not implemented.
   */
  @Override
  public void play(String actionId) {
    this.currentActionId = actionId;
  }

  @Override
  public Set<Texture> getTexturesToDispose() {
    Set<Texture> textures = new HashSet<>();
    parts.forEach(part -> textures.addAll(part.getTexturesToDispose()));
    return textures;
  }

  private void updateBones() {
    bones.forEach(bone -> bone.update(x, y));

    if (flipped) {
      // Mirror only once the whole chain is resolved: every bone reflects across the character's
      // own centre line, so it turns on the spot rather than drifting sideways.
      float axisX = x + width / 2f;
      bones.forEach(bone -> bone.mirrorAbout(axisX));
    }
  }

  /**
   * Builds bones parent-before-child. The rig file is written in draw order, so its bones are not
   * in hierarchy order and cannot simply be walked top to bottom.
   */
  private static List<Bone> buildBones(RigDefinition rig, float scale) {
    Map<String, BoneDefinition> definitionsById = new HashMap<>();
    rig.getBones().forEach(definition -> definitionsById.put(definition.getId(), definition));

    Map<String, Bone> built = new LinkedHashMap<>();
    List<BoneDefinition> pending = new ArrayList<>(rig.getBones());

    while (!pending.isEmpty()) {
      boolean progressed = false;

      for (Iterator<BoneDefinition> it = pending.iterator(); it.hasNext(); ) {
        BoneDefinition definition = it.next();
        String parentId = definition.getParent();

        if (parentId != null && !built.containsKey(parentId)) {
          if (!definitionsById.containsKey(parentId)) {
            throw new IllegalArgumentException(
                String.format(
                    "Bone %s references unknown parent %s", definition.getId(), parentId));
          }
          continue;
        }

        built.put(definition.getId(), toBone(definition, definitionsById, built, rig, scale));
        it.remove();
        progressed = true;
      }

      if (!progressed) {
        throw new IllegalArgumentException(
            String.format("Rig %s has a cycle in its bone hierarchy", rig.getId()));
      }
    }

    return List.copyOf(built.values());
  }

  private static Bone toBone(
      BoneDefinition definition,
      Map<String, BoneDefinition> definitionsById,
      Map<String, Bone> built,
      RigDefinition rig,
      float scale) {
    String parentId = definition.getParent();

    if (parentId == null) {
      return new Bone(
          definition.getId(),
          null,
          (definition.getPivotX() - rig.getFigureX()) * scale,
          // The y flip: canvas y grows downwards from the top, world y upwards from the entity's
          // lower-left corner.
          (rig.getFigureY() + rig.getFigureHeight() - definition.getPivotY()) * scale);
    }

    BoneDefinition parent = definitionsById.get(parentId);
    return new Bone(
        definition.getId(),
        built.get(parentId),
        (definition.getPivotX() - parent.getPivotX()) * scale,
        -(definition.getPivotY() - parent.getPivotY()) * scale);
  }

  private static List<BodyPart> buildParts(
      RigDefinition rig,
      float scale,
      Map<String, Bone> bonesById,
      Function<String, TextureRegion> regionLoader) {
    return rig.getBones().stream()
        .sorted(Comparator.comparingInt(BoneDefinition::getZ))
        .map(definition -> toBodyPart(definition, scale, bonesById, regionLoader))
        .toList();
  }

  private static BodyPart toBodyPart(
      BoneDefinition definition,
      float scale,
      Map<String, Bone> bonesById,
      Function<String, TextureRegion> regionLoader) {
    return new BodyPart(
        definition.getId(),
        bonesById.get(definition.getId()),
        regionLoader.apply(definition.getPart()),
        definition.getZ(),
        // Where the part's image sits relative to its bone pivot. trimX/trimY is the image's
        // top-left on the canvas; the world offset is to its lower-left, hence the part height.
        (definition.getTrimX() - definition.getPivotX()) * scale,
        -(definition.getTrimY() + definition.getPartHeight() - definition.getPivotY()) * scale,
        definition.getPartWidth() * scale,
        definition.getPartHeight() * scale);
  }
}
