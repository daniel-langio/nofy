package vendredi.soir.nofy.factory;

import com.badlogic.gdx.Gdx;
import java.util.ArrayList;
import java.util.List;

/**
 * FileHandle.list() only enumerates real directory entries; it comes back empty for internal
 * assets bundled inside a packaged jar. assets.txt (generated at build time, listing every file
 * under assets/) is what lets directory-style lookups keep working once loaded from a jar.
 */
final class AssetManifest {
  private AssetManifest() {}

  static List<String> listJsonFiles(String directoryPath) {
    String prefix = directoryPath.endsWith("/") ? directoryPath : directoryPath + "/";
    List<String> paths = new ArrayList<>();

    for (String line : Gdx.files.internal("assets.txt").readString().split("\n")) {
      String path = line.trim();
      if (path.startsWith(prefix)
          && path.endsWith(".json")
          && path.indexOf('/', prefix.length()) == -1) {
        paths.add(path);
      }
    }

    return paths;
  }
}
