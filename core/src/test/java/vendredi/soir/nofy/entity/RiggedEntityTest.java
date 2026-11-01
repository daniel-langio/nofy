package vendredi.soir.nofy.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vendredi.soir.nofy.data.RigDefinition;
import vendredi.soir.nofy.factory.GameDataJson;

/**
 * Exercises the canvas-to-world conversion and the bone chain without a GL context: the rig is
 * parsed from JSON exactly as it would be at runtime, and parts are built with no texture, since
 * none of this touches one.
 *
 * <p>The fixture is proportioned so the maths stays checkable by hand - a 200-tall figure rendered
 * 200 units tall means a scale of exactly 1, leaving only the y flip to verify.
 */
class RiggedEntityTest {
  private static final float TOLERANCE = 1e-4f;
  private static final float FIGURE_HEIGHT = 200f;

  /**
   * Bones are deliberately listed in neither hierarchy nor draw order, and z deliberately
   * contradicts the tree: foot draws first though it hangs off root, and root draws last.
   */
  private static final String RIG_JSON =
      """
{
  "id": "test", "canvas": 1000,
  "figureX": 0, "figureY": 0, "figureWidth": 100, "figureHeight": 200,
  "bones": [
    { "id": "hand", "parent": "arm",  "pivotX": 70, "pivotY": 20,
      "part": "hand.png", "trimX": 45, "trimY": 10,  "partWidth": 10, "partHeight": 10, "z": 3 },
    { "id": "root", "parent": null,   "pivotX": 50, "pivotY": 100,
      "part": "root.png", "trimX": 40, "trimY": 90,  "partWidth": 40, "partHeight": 20, "z": 5 },
    { "id": "foot", "parent": "root", "pivotX": 50, "pivotY": 180,
      "part": "foot.png", "trimX": 45, "trimY": 170, "partWidth": 10, "partHeight": 20, "z": 0 },
    { "id": "arm",  "parent": "root", "pivotX": 50, "pivotY": 60,
      "part": "arm.png",  "trimX": 45, "trimY": 20,  "partWidth": 10, "partHeight": 40, "z": 2 }
  ]
}
""";

  @Test
  void bindPose_bonesLandOnTheirCanvasPivots() {
    RiggedEntity entity = entity(FIGURE_HEIGHT);

    // Canvas y grows downwards, world y upwards, so a pivot at canvas y=100 on a 200-tall figure
    // sits 100 units up from the entity's foot - and one at y=180 sits only 20 up.
    assertBoneAt(entity, "root", 50f, 100f);
    assertBoneAt(entity, "arm", 50f, 140f);
    assertBoneAt(entity, "hand", 70f, 180f);
    assertBoneAt(entity, "foot", 50f, 20f);
  }

  @Test
  void bindPose_followsTheEntityPositionAndHeight() {
    RiggedEntity entity = entity(FIGURE_HEIGHT);
    entity.setPosition(7f, 13f);
    entity.animate(0f);

    assertBoneAt(entity, "root", 57f, 113f);
    assertBoneAt(entity, "hand", 77f, 193f);

    // Twice as tall means every offset from the entity's corner doubles, the figure's own width
    // included - the rig is authored once and scaled, not re-authored per size.
    RiggedEntity twiceAsTall = entity(FIGURE_HEIGHT * 2f);
    assertBoneAt(twiceAsTall, "root", 100f, 200f);
    assertEquals(200f, twiceAsTall.getWidth(), TOLERANCE);
    assertEquals(400f, twiceAsTall.getHeight(), TOLERANCE);
  }

  @Test
  void rotatingArm_carriesTheHandWithIt() {
    RiggedEntity entity = entity(FIGURE_HEIGHT);

    entity.setBoneRotation("arm", 90f);
    entity.animate(0f);

    // The arm's own pivot does not move - a bone turns about itself.
    assertBoneAt(entity, "arm", 50f, 140f);
    // The hand sits 40 up and 20 across from the arm at rest, so a quarter turn counter-clockwise
    // takes it 40 to the left and 20 up instead. Getting this wrong - composing in the child's
    // frame rather than the parent's - leaves the hand unmoved.
    assertBoneAt(entity, "hand", 10f, 160f);
    assertEquals(90f, entity.bone("hand").getWorldRotation(), TOLERANCE);

    // A sibling chain is untouched by it.
    assertBoneAt(entity, "foot", 50f, 20f);
  }

  @Test
  void flipping_reflectsTheRigAboutTheCharactersOwnCentre() {
    RiggedEntity upright = posed(false);
    RiggedEntity mirrored = posed(true);

    // The figure is 100 wide at x=0, so it turns about x=50 - on the spot, not drifting sideways.
    float axis = 50f;

    for (String boneId : List.of("root", "arm", "hand", "foot")) {
      Bone before = upright.bone(boneId);
      Bone after = mirrored.bone(boneId);

      assertEquals(2f * axis - before.getWorldX(), after.getWorldX(), TOLERANCE, boneId + " x");
      assertEquals(before.getWorldY(), after.getWorldY(), TOLERANCE, boneId + " y is unchanged");
      // Reflecting a rotation inverts it; the part is then drawn with a negative horizontal scale.
      assertEquals(
          -before.getWorldRotation(), after.getWorldRotation(), TOLERANCE, boneId + " rotation");
      assertTrue(after.isFlipped(), boneId + " is flipped");
    }
  }

  /** Posed rather than at rest, so the reflection is checked against a non-symmetric character. */
  private static RiggedEntity posed(boolean flipped) {
    RiggedEntity entity = entity(FIGURE_HEIGHT);
    entity.setBoneRotation("arm", 30f);
    entity.setFlipped(flipped);
    entity.animate(0f);
    return entity;
  }

  @Test
  void drawOrder_followsPartZ_notTheBoneTree() {
    RiggedEntity entity = entity(FIGURE_HEIGHT);

    assertEquals(
        List.of("foot", "arm", "hand", "root"),
        entity.getParts().stream().map(BodyPart::getName).toList());
  }

  @Test
  void partOffsets_placeTrimmedArtRelativeToItsPivot() {
    BodyPart root =
        entity(FIGURE_HEIGHT).getParts().stream()
            .filter(part -> part.getName().equals("root"))
            .findFirst()
            .orElseThrow();

    // The part was cropped from canvas (40,90) at 40x20 and its bone pivots at (50,100), so its
    // lower-left corner is 10 left of and 10 below the pivot once y is flipped.
    assertEquals(-10f, root.getOffsetX(), TOLERANCE);
    assertEquals(-10f, root.getOffsetY(), TOLERANCE);
    assertEquals(40f, root.getWidth(), TOLERANCE);
    assertEquals(20f, root.getHeight(), TOLERANCE);
  }

  @Test
  void bonesReferencingAnUnknownParent_areRejected() {
    String orphaned = RIG_JSON.replace("\"parent\": \"arm\"", "\"parent\": \"nonexistent\"");

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new RiggedEntity(
                "broken",
                GameDataJson.instance().fromJson(RigDefinition.class, orphaned),
                FIGURE_HEIGHT,
                "idle",
                path -> null));
  }

  private static RiggedEntity entity(float targetHeight) {
    return new RiggedEntity(
        "test",
        GameDataJson.instance().fromJson(RigDefinition.class, RIG_JSON),
        targetHeight,
        "idle",
        path -> null);
  }

  private static void assertBoneAt(RiggedEntity entity, String boneId, float x, float y) {
    Bone bone = entity.bone(boneId);
    assertEquals(x, bone.getWorldX(), TOLERANCE, boneId + " world x");
    assertEquals(y, bone.getWorldY(), TOLERANCE, boneId + " world y");
  }
}
