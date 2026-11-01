package vendredi.soir.nofy.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import java.io.File;
import org.junit.jupiter.api.Test;
import vendredi.soir.nofy.data.CharacterKit;
import vendredi.soir.nofy.data.ClipDefinition;
import vendredi.soir.nofy.data.ClipKey;

class CharacterKitLoaderTest {
  static CharacterKit kit() {
    // Gradle runs core tests with the module as working directory.
    return CharacterKitLoader.load(new FileHandle(new File("../assets/data/character-kit")));
  }

  @Test
  void loadsEveryFile_andClipKeysReferenceExistingPoses() {
    CharacterKit kit = kit();

    assertEquals(11, kit.getPoses().size(), "base + 10 named poses");
    assertEquals(3, kit.getCatalog().getCharacters().size());
    assertEquals(12, kit.getExpressions().getExpressions().size());
    assertEquals(9, kit.getGestures().size());
    assertNotNull(kit.getManifest().getLimbs().get("legL"));

    assertTrue(kit.getClips().containsKey("hop-and-wave"));
    for (ClipDefinition clip : kit.getClips().values()) {
      for (ClipKey key : clip.getKeys()) {
        assertTrue(kit.getPoses().containsKey(key.getPose()), clip.getId() + ": " + key.getPose());
      }
    }
    assertEquals(6, kit.getClips().get("hop-and-wave").getTracks().get("armR.wrist").length);
  }
}
