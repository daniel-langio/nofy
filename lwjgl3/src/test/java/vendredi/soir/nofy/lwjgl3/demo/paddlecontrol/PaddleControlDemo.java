package vendredi.soir.nofy.lwjgl3.demo.paddlecontrol;

import java.util.Map;
import vendredi.soir.nofy.data.ActionDefinition;
import vendredi.soir.nofy.data.EntityDefinition;
import vendredi.soir.nofy.data.RigDefinition;
import vendredi.soir.nofy.factory.ActionLoader;
import vendredi.soir.nofy.factory.EntityDefinitionLoader;
import vendredi.soir.nofy.factory.RigLoader;
import vendredi.soir.nofy.factory.SceneLoader;
import vendredi.soir.nofy.game.Game;

/**
 * Interactive preview of the "paddle-control" scenario: a mouse-controlled paddle that deflects a
 * bouncing ball on contact. Lives under src/test alongside its launcher, same convention as
 * demo/ballbounce.
 */
public class PaddleControlDemo extends Game {
  private static final String ACTIONS_DIR = "data/actions";
  private static final String ENTITIES_DIR = "data/entities";
  private static final String RIGS_DIR = "data/rigs";
  private static final String SCENE_FILE = "data/scenes/paddle-control-scene.json";

  public PaddleControlDemo() {
    super();
  }

  @Override
  public void init() {
    Map<String, ActionDefinition> actions = ActionLoader.loadAll(ACTIONS_DIR);
    Map<String, EntityDefinition> entityDefinitions = EntityDefinitionLoader.loadAll(ENTITIES_DIR);
    Map<String, RigDefinition> rigs = RigLoader.loadAll(RIGS_DIR);
    SceneLoader.populate(world, SCENE_FILE, entityDefinitions, rigs, actions);
  }
}
