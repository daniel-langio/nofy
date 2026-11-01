package vendredi.soir.nofy.lwjgl3.demo.arnold;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import vendredi.soir.nofy.Main;
import vendredi.soir.nofy.lwjgl3.StartupHelper;

/** Launches the interactive Arnold rig preview window. */
public class ArnoldLauncher {
  public static void main(String[] args) {
    if (StartupHelper.startNewJvmIfRequired()) return;

    Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
    configuration.setTitle("Nofy - Arnold rig preview");
    configuration.useVsync(true);
    // Square window matching the square world, so FitViewport needs no side bands.
    configuration.setWindowedMode(600, 600);

    new Lwjgl3Application(new Main(ArnoldDemo::new), configuration);
  }
}
