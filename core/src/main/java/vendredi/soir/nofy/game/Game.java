package vendredi.soir.nofy.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import vendredi.soir.nofy.entity.Entity;
import vendredi.soir.nofy.system.CollisionSystem;
import vendredi.soir.nofy.system.TriggerSystem;

public abstract class Game {
  public static final int WORLD_WIDTH = 200;
  public static final int WORLD_HEIGHT = 200;

  protected final SpriteBatch spriteBatch;
  protected final Viewport viewport;
  protected final GameWorld world;
  protected final TriggerSystem triggerSystem;
  protected final CollisionSystem collisionSystem;

  protected double upTime = 0.0;

  public Game() {
    this.spriteBatch = new SpriteBatch();
    this.viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
    this.triggerSystem = new TriggerSystem();
    this.collisionSystem = new CollisionSystem();

    this.world = new GameWorld();
    init();
  }

  // create entities and do settings here
  public abstract void init();

  public void input() {
    triggerSystem.update(world, viewport, upTime);
  }

  public void logic() {
    final float deltaTime = Gdx.graphics.getDeltaTime();

    upTime += deltaTime;
    world.animate(deltaTime);
    collisionSystem.resolve(world);
    world.clearEvents();
  }

  public void draw() {
    followCameraTarget();

    ScreenUtils.clear(Color.valueOf("069f66"));
    spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
    spriteBatch.begin();

    world.draw(spriteBatch);

    spriteBatch.end();
  }

  private void followCameraTarget() {
    Entity target = world.getCameraTarget();
    if (target == null) {
      return;
    }
    viewport
        .getCamera()
        .position
        .set(target.getX() + target.getWidth() / 2f, target.getY() + target.getHeight() / 2f, 0);
    viewport.getCamera().update();
  }

  public void dispose() {
    world.dispose();
  }

  public void resize(int width, int height) {
    viewport.update(width, height, true);
  }
}
