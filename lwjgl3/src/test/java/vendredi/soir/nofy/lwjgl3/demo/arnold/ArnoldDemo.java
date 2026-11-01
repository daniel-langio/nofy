package vendredi.soir.nofy.lwjgl3.demo.arnold;

import com.badlogic.gdx.math.MathUtils;
import java.util.Map;
import vendredi.soir.nofy.data.ActionDefinition;
import vendredi.soir.nofy.data.EntityDefinition;
import vendredi.soir.nofy.data.RigDefinition;
import vendredi.soir.nofy.entity.RiggedEntity;
import vendredi.soir.nofy.factory.ActionLoader;
import vendredi.soir.nofy.factory.EntityDefinitionLoader;
import vendredi.soir.nofy.factory.RigLoader;
import vendredi.soir.nofy.factory.SceneLoader;
import vendredi.soir.nofy.game.Game;

/**
 * Interactive preview of Arnold, the first rig-based character: sixteen parts assembled from a bone
 * hierarchy rather than a sprite sheet.
 *
 * <p>At rest he stands in the rig's bind pose, which should match the kit's own preview render.
 */
public class ArnoldDemo extends Game {
  private static final String ACTIONS_DIR = "data/actions";
  private static final String ENTITIES_DIR = "data/entities";
  private static final String RIGS_DIR = "data/rigs";
  private static final String SCENE_FILE = "data/scenes/arnold-scene.json";

  private static final float SWING_DEGREES = 40f;
  private static final float SWING_SPEED = 2f;

  private RiggedEntity arnold;

  public ArnoldDemo() {
    super();
  }

  @Override
  public void init() {
    Map<String, ActionDefinition> actions = ActionLoader.loadAll(ACTIONS_DIR);
    Map<String, EntityDefinition> entityDefinitions = EntityDefinitionLoader.loadAll(ENTITIES_DIR);
    Map<String, RigDefinition> rigs = RigLoader.loadAll(RIGS_DIR);
    SceneLoader.populate(world, SCENE_FILE, entityDefinitions, rigs, actions);

    arnold =
        world.getEntities().stream()
            .filter(RiggedEntity.class::isInstance)
            .map(RiggedEntity.class::cast)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No rigged entity in " + SCENE_FILE));
  }

  /**
   * Swings one shoulder and nothing else. The forearm and hand following it is the whole point - it
   * is the cheapest demonstration that the bone chain composes, ahead of any clip support.
   */
  @Override
  public void logic() {
    arnold.setBoneRotation(
        "upperarm_near", MathUtils.sin((float) upTime * SWING_SPEED) * SWING_DEGREES);
    super.logic();
  }
}
