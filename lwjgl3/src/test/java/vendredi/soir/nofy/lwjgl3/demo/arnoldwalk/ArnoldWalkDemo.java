package vendredi.soir.nofy.lwjgl3.demo.arnoldwalk;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
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
 * Arnold walking under keyboard control: left/right arrows or A/D, and he faces the way he is
 * going. Releasing the keys eases him back to a stand.
 *
 * <p>The walk is generated procedurally (see WalkCycle) because the rig has no keyframed clips yet
 * - this demo drives bone rotations directly, which is the whole interface a clip will eventually
 * sit behind.
 */
public class ArnoldWalkDemo extends Game {
  private static final String ACTIONS_DIR = "data/actions";
  private static final String ENTITIES_DIR = "data/entities";
  private static final String RIGS_DIR = "data/rigs";
  private static final String SCENE_FILE = "data/scenes/arnold-walk-scene.json";

  /**
   * Hip pivot to the ground, in world units, for Arnold at his authored height. Measured from the
   * rig: the thigh pivots at canvas y=515 and his feet reach y=864, over a 738-tall figure drawn 24
   * units tall.
   */
  private static final float LEG_LENGTH = (864f - 515f) / 738f * 24f;

  /**
   * Derived rather than chosen, so the feet do not skate: a step carries the foot 2 * LEG_LENGTH *
   * sin(THIGH_SWING) along the ground, and the cycle takes two steps, so the body has to travel
   * exactly that far in that time.
   */
  private static final float WALK_SPEED =
      2f
          * LEG_LENGTH
          * MathUtils.sinDeg(WalkCycle.THIGH_SWING)
          * (WalkCycle.CADENCE / MathUtils.PI);

  private RiggedEntity arnold;
  private final WalkCycle walkCycle = new WalkCycle();

  public ArnoldWalkDemo() {
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

  @Override
  public void logic() {
    float deltaTime = Gdx.graphics.getDeltaTime();
    int direction = walkDirection();

    if (direction != 0) {
      // Arnold is drawn facing left, so only walking right needs the rig mirrored.
      arnold.setFlipped(direction > 0);
      arnold.translate(direction * WALK_SPEED * deltaTime, 0f);
      arnold.setX(MathUtils.clamp(arnold.getX(), 0f, WORLD_WIDTH - arnold.getWidth()));
    }

    walkCycle.update(arnold, deltaTime, direction != 0);

    // Poses are set before the world animates, which is what recomputes the bone chain from them.
    super.logic();
  }

  /** Zero when neither or both directions are held, so pressing both stands still. */
  private int walkDirection() {
    boolean left = Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A);
    boolean right = Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D);

    if (left == right) {
      return 0;
    }
    return left ? -1 : 1;
  }
}
