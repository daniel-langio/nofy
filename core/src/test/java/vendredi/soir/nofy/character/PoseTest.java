package vendredi.soir.nofy.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.File;
import java.util.Map;
import org.junit.jupiter.api.Test;
import vendredi.soir.nofy.data.CharacterKit;
import vendredi.soir.nofy.data.PoseDefinition;
import vendredi.soir.nofy.factory.CharacterKitLoader;
import vendredi.soir.nofy.factory.GameDataJson;

class PoseTest {
  private static final FileHandle DIR = new FileHandle(new File("../assets/data/character-kit"));
  private static final CharacterKit KIT = CharacterKitLoader.load(DIR);
  private static final String[] CHARS = {"nori", "bastien", "eric"};

  private static void eq(float expected, float actual, float tol, String what) {
    assertEquals(expected, actual, tol, what);
  }

  /** Every pose x character against the prototype's own output: angles within 0.5 degrees. */
  @Test
  void resolvedPosesMatchGolden() {
    JsonValue golden = new JsonReader().parse(DIR.child("golden/resolved-poses.json"));
    int n = 0;
    for (JsonValue ch : golden) {
      CharacterRig rig = new CharacterRig(KIT, ch.name);
      for (JsonValue entry : ch) {
        PoseDefinition g =
            GameDataJson.instance().readValue(PoseDefinition.class, entry.get("pose"));
        ResolvedPose p = rig.resolve(entry.name);
        String id = ch.name + "/" + entry.name;
        eq(g.getSpine(), p.spine, 0.5f, id + " spine");
        eq(g.getNeck(), p.neck, 0.5f, id + " neck");
        eq(g.getRoot().getRot(), p.rootRot, 0.5f, id + " root.rot");
        eq(g.getRoot().getY(), p.rootY, 0.5f, id + " root.y");
        eq(entry.getFloat("placementDy"), rig.placement(p), 1f, id + " placement");
        var gl =
            new vendredi.soir.nofy.data.LimbPose[] {
              g.getArmL(), g.getArmR(), g.getLegL(), g.getLegR()
            };
        for (int i = 0; i < 4; i++) {
          String lid = id + " " + ResolvedPose.LIMB_NAMES[i];
          eq(gl[i].getA(), p.limbs[i].a, 0.5f, lid + ".a");
          eq(gl[i].getB(), p.limbs[i].b, 0.5f, lid + ".b");
          assertEquals(gl[i].getBend(), p.limbs[i].bend, lid + ".bend");
          if (i < 2) eq(gl[i].getWrist(), p.limbs[i].wrist, 0.5f, lid + ".wrist");
          else eq(gl[i].getFoot(), p.limbs[i].foot, 0.5f, lid + ".foot");
        }
        n++;
      }
    }
    assertEquals(30, n);
  }

  @Test
  void signConversion() {
    Map<String, Float> wave = PoseApplier.rotations(new CharacterRig(KIT, "nori").resolve("wave"));
    eq(150, wave.get("upperarm_near"), 0.5f, "wave upperarm");
    eq(30, wave.get("forearm_near"), 0.5f, "wave forearm");

    Map<String, Float> crouch =
        PoseApplier.rotations(new CharacterRig(KIT, "nori").resolve("crouch"));
    eq(-45, crouch.get("thigh_near"), 0.5f, "crouch thigh");
    eq(85, crouch.get("shin_near"), 0.5f, "crouch shin");

    // lean: spine -22 -> torso +22; Bastien's -5 stoop is added to every pose.
    eq(
        22,
        PoseApplier.rotations(new CharacterRig(KIT, "nori").resolve("lean")).get("torso"),
        0.5f,
        "nori lean");
    eq(
        27,
        PoseApplier.rotations(new CharacterRig(KIT, "bastien").resolve("lean")).get("torso"),
        0.5f,
        "bastien lean");
  }

  @Test
  void groundedPosesRestOnTheGround() {
    for (String c : CHARS) {
      CharacterRig rig = new CharacterRig(KIT, c);
      for (String id : KIT.getPoses().keySet()) {
        ResolvedPose p = rig.resolve(id);
        if (!"none".equals(p.seat) || p.rootY <= -20 || Math.abs(p.rootRot) >= 30) continue;
        float lowest =
            Math.max(
                rig.footGap(p, ResolvedPose.LEG_L, 598), rig.footGap(p, ResolvedPose.LEG_R, 598));
        // Gap is measured to the ground: the lower foot must sit within 1 unit of it.
        assertTrue(
            Math.abs(
                    Math.min(
                        rig.footGap(p, ResolvedPose.LEG_L, 598),
                        rig.footGap(p, ResolvedPose.LEG_R, 598)))
                <= 1f,
            c + "/" + id + " gap " + lowest);
      }
    }
  }

  @Test
  void clipEvaluationIsOrderIndependent() {
    CharacterRig rig = new CharacterRig(KIT, "nori");
    var clip = KIT.getClips().get("hop-and-wave");
    ClipPlayer sequential = new ClipPlayer(rig, clip);
    float[] expected = new float[131];
    for (int f = 0; f <= 130; f++)
      expected[f] = PoseApplier.rotations(sequential.poseAt(f)).get("upperarm_near");

    ClipPlayer shuffled = new ClipPlayer(rig, clip);
    java.util.Random rnd = new java.util.Random(7);
    for (int i = 0; i < 400; i++) {
      int f = rnd.nextInt(131);
      assertEquals(
          expected[f],
          PoseApplier.rotations(shuffled.poseAt(f)).get("upperarm_near"),
          0f,
          "frame " + f);
    }
  }

  @Test
  void discreteFieldsSwitchOnKeyAndTrackOverridesPose() {
    ClipPlayer player =
        new ClipPlayer(new CharacterRig(KIT, "nori"), KIT.getClips().get("hop-and-wave"));
    // stand -> wave keys at 58 and 80: the wave hand appears exactly at frame 80, not before.
    assertEquals("relaxed", player.poseAt(79).limbs[ResolvedPose.ARM_R].hand);
    assertEquals("wave", player.poseAt(80).limbs[ResolvedPose.ARM_R].hand);
    // Track armR.wrist: 26 at frame 88, regardless of the pose's own wrist (0).
    eq(26, player.poseAt(88).limbs[ResolvedPose.ARM_R].wrist, 1e-4f, "wrist track");
  }
}
