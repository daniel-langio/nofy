package vendredi.soir.nofy.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;
import vendredi.soir.nofy.data.CharacterKit;
import vendredi.soir.nofy.data.ClipDefinition;
import vendredi.soir.nofy.factory.CharacterKitLoader;

class RigCheckTest {
  private static final CharacterKit KIT =
      CharacterKitLoader.load(new FileHandle(new File("../assets/data/character-kit")));

  @Test
  void everyPoseOnEveryCharacterPasses() {
    for (String c : KIT.getCatalog().getCharacters().keySet()) {
      CharacterRig rig = new CharacterRig(KIT, c);
      for (String id : KIT.getPoses().keySet()) {
        List<String> w = RigCheck.check(KIT, rig, rig.resolve(id));
        assertTrue(w.isEmpty(), c + "/" + id + ": " + w);
      }
    }
  }

  @Test
  void everyClipFramePasses() {
    for (String c : KIT.getCatalog().getCharacters().keySet()) {
      CharacterRig rig = new CharacterRig(KIT, c);
      for (ClipDefinition clip : KIT.getClips().values()) {
        ClipPlayer player = new ClipPlayer(rig, clip);
        for (int f = clip.getStartFrame(); f <= clip.getEndFrame(); f++) {
          List<String> w = RigCheck.check(KIT, rig, player.poseAt(f), isKey(clip, f));
          assertTrue(w.isEmpty(), c + "/" + clip.getId() + "@" + f + ": " + w);
        }
      }
    }
  }

  private static boolean isKey(ClipDefinition clip, int frame) {
    return clip.getKeys().stream().anyMatch(k -> k.getFrame() == frame);
  }

  @Test
  void flagsAFlippedKneeAndAFloatingFigure() {
    CharacterRig rig = new CharacterRig(KIT, "nori");
    ResolvedPose p = rig.resolve("crouch");
    p.limbs[ResolvedPose.LEG_R].bend = 1; // b stays +85, so the knee now bends against it
    assertTrue(RigCheck.check(KIT, rig, p).stream().anyMatch(s -> s.startsWith("Flip: legR")));

    ResolvedPose q = rig.resolve("stand");
    q.rootY = -19; // just under the airborne cutoff, with placement not re-grounding it
    assertFalse(RigCheck.check(KIT, rig, rig.resolve("stand")).iterator().hasNext());
    assertEquals(0, RigCheck.check(KIT, rig, rig.resolve("jump")).size());
  }
}
