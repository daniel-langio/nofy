package vendredi.soir.nofy.lwjgl3.demo.cast;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import vendredi.soir.nofy.Main;
import vendredi.soir.nofy.lwjgl3.StartupHelper;

/** Launches the interactive cast preview window. */
public class CastLauncher {
  public static void main(String[] args) {
    if (StartupHelper.startNewJvmIfRequired()) return;

    Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
    configuration.setTitle("Nofy - cast preview (1-0 poses, H clip, F flip)");
    configuration.useVsync(true);
    configuration.setWindowedMode(1000, 1000);

    new Lwjgl3Application(new Main(CastDemo::new), configuration);
  }
}
