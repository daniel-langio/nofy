package vendredi.soir.nofy.game;

import java.util.Map;
import vendredi.soir.nofy.data.ActionDefinition;
import vendredi.soir.nofy.data.EntityDefinition;
import vendredi.soir.nofy.data.RigDefinition;
import vendredi.soir.nofy.factory.ActionLoader;
import vendredi.soir.nofy.factory.EntityDefinitionLoader;
import vendredi.soir.nofy.factory.RigLoader;
import vendredi.soir.nofy.factory.SceneLoader;

public class MyGame extends Game {
  private static final String ACTIONS_DIR = "data/actions";
  private static final String ENTITIES_DIR = "data/entities";
  private static final String RIGS_DIR = "data/rigs";
  private static final String SCENE_FILE = "data/scenes/demo.json";

  public MyGame() {
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
