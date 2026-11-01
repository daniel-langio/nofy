package vendredi.soir.nofy.factory;

import com.badlogic.gdx.Gdx;
import java.util.HashMap;
import java.util.Map;
import vendredi.soir.nofy.data.RigDefinition;

public final class RigLoader {
  private RigLoader() {}

  public static Map<String, RigDefinition> loadAll(String directoryPath) {
    Map<String, RigDefinition> rigs = new HashMap<>();

    for (String path : AssetManifest.listJsonFiles(directoryPath)) {
      RigDefinition rig =
          GameDataJson.instance().fromJson(RigDefinition.class, Gdx.files.internal(path));
      rigs.put(rig.getId(), rig);
    }

    return rigs;
  }
}
