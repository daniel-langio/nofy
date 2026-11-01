package vendredi.soir.nofy.lwjgl3.demo.cast;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import java.util.List;
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
 * Nori, Bastien and Eric side by side. Keys 1-0 hold a library pose, H plays the hop-and-wave clip,
 * and F turns them to face the other way.
 */
public class CastDemo extends Game {
  private static final String SCENE_FILE = "data/scenes/cast-scene.json";

  /** Key order matches the pose studio: 1 = first pose ... 0 = tenth. */
  private static final List<String> POSES =
      List.of("stand", "think", "sit", "lean", "walk", "laugh", "crouch", "wave", "glide", "jump");

  private static final int[] KEYS = {
    Keys.NUM_1, Keys.NUM_2, Keys.NUM_3, Keys.NUM_4, Keys.NUM_5,
    Keys.NUM_6, Keys.NUM_7, Keys.NUM_8, Keys.NUM_9, Keys.NUM_0
  };

  private List<RiggedEntity> cast;
  private boolean facingRight;

  public CastDemo() {
    super();
  }

  @Override
  public void init() {
    Map<String, ActionDefinition> actions = ActionLoader.loadAll("data/actions");
    Map<String, EntityDefinition> entityDefinitions =
        EntityDefinitionLoader.loadAll("data/entities");
    Map<String, RigDefinition> rigs = RigLoader.loadAll("data/rigs");
    SceneLoader.populate(world, SCENE_FILE, entityDefinitions, rigs, actions);

    cast =
        world.getEntities().stream()
            .filter(RiggedEntity.class::isInstance)
            .map(RiggedEntity.class::cast)
            .toList();
  }

  @Override
  public void logic() {
    for (int i = 0; i < KEYS.length; i++) {
      if (Gdx.input.isKeyJustPressed(KEYS[i])) {
        String pose = POSES.get(i);
        cast.forEach(c -> c.getClipAnimator().showPose(c, pose));
      }
    }
    if (Gdx.input.isKeyJustPressed(Keys.H)) {
      cast.forEach(c -> c.play("hop-and-wave"));
    }
    if (Gdx.input.isKeyJustPressed(Keys.F)) {
      facingRight = !facingRight;
      cast.forEach(c -> c.getClipAnimator().setMirrored(facingRight));
    }
    super.logic();
  }
}
