package vendredi.soir.nofy.factory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import vendredi.soir.nofy.data.CharacterCatalog;
import vendredi.soir.nofy.data.CharacterKit;
import vendredi.soir.nofy.data.ClipDefinition;
import vendredi.soir.nofy.data.ExpressionSet;
import vendredi.soir.nofy.data.GestureDefinition;
import vendredi.soir.nofy.data.PoseDefinition;
import vendredi.soir.nofy.data.RigManifest;

public final class CharacterKitLoader {
  private static final String DIR = "data/character-kit";

  private CharacterKitLoader() {}

  /** Loads from the packaged assets; needs Gdx initialised. */
  public static CharacterKit loadInternal() {
    return load(Gdx.files::internal, DIR + "/", listed("poses"), listed("clips"));
  }

  /** Loads from a directory on disk (tests, tools); no Gdx needed. */
  public static CharacterKit load(FileHandle dir) {
    return load(dir::child, "", names(dir.child("poses")), names(dir.child("clips")));
  }

  private static CharacterKit load(
      Function<String, FileHandle> file, String prefix, String[] poseIds, String[] clipIds) {
    Json json = GameDataJson.instance();
    Map<String, PoseDefinition> poses = new HashMap<>();
    for (String id : poseIds) {
      poses.put(
          id, json.fromJson(PoseDefinition.class, file.apply(prefix + "poses/" + id + ".json")));
    }
    Map<String, ClipDefinition> clips = new HashMap<>();
    for (String id : clipIds) {
      clips.put(
          id, json.fromJson(ClipDefinition.class, file.apply(prefix + "clips/" + id + ".json")));
    }
    @SuppressWarnings("unchecked")
    Map<String, GestureDefinition> gestures =
        json.fromJson(HashMap.class, GestureDefinition.class, file.apply(prefix + "gestures.json"));
    return new CharacterKit(
        json.fromJson(RigManifest.class, file.apply(prefix + "rig-manifest.json")),
        json.fromJson(CharacterCatalog.class, file.apply(prefix + "characters.json")),
        poses,
        clips,
        gestures,
        json.fromJson(ExpressionSet.class, file.apply(prefix + "expressions.json")));
  }

  private static String[] listed(String sub) {
    return AssetManifest.listJsonFiles(DIR + "/" + sub).stream()
        .map(p -> p.substring(p.lastIndexOf('/') + 1, p.length() - ".json".length()))
        .toArray(String[]::new);
  }

  private static String[] names(FileHandle dir) {
    FileHandle[] files = dir.list(".json");
    String[] ids = new String[files.length];
    for (int i = 0; i < files.length; i++) {
      ids[i] = files[i].nameWithoutExtension();
    }
    return ids;
  }
}
