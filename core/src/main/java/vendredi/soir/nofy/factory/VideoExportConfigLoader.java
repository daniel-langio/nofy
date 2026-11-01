package vendredi.soir.nofy.factory;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import vendredi.soir.nofy.data.VideoExportConfig;

public final class VideoExportConfigLoader {
  private VideoExportConfigLoader() {}

  /**
   * Reads a plain filesystem path, not a Gdx internal asset path: this is called before the
   * libGDX application (and thus Gdx.files) exists, since the resulting width/height are needed
   * to configure the application window itself.
   */
  public static VideoExportConfig load(String configFilePath) {
    try (Reader reader = new FileReader(configFilePath)) {
      return GameDataJson.instance().fromJson(VideoExportConfig.class, reader);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read video export config: " + configFilePath, e);
    }
  }
}
