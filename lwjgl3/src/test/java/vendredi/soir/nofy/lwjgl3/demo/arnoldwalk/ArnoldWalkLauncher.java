package vendredi.soir.nofy.lwjgl3.demo.arnoldwalk;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import vendredi.soir.nofy.Main;
import vendredi.soir.nofy.lwjgl3.StartupHelper;

/** Launches the interactive Arnold walk preview window. */
public class ArnoldWalkLauncher {
  public static void main(String[] args) {
    if (StartupHelper.startNewJvmIfRequired()) return;

    Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
    configuration.setTitle("Nofy - Arnold walk preview (left/right arrows or A/D)");
    configuration.useVsync(true);
    // Square to match the square world, and larger than the other previews so a 24-unit-tall
    // character is big enough to judge the animation by.
    configuration.setWindowedMode(1000, 1000);

    new Lwjgl3Application(new Main(ArnoldWalkDemo::new), configuration);
  }
}
