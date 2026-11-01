package vendredi.soir.nofy.lwjgl3;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import org.jcodec.api.awt.AWTSequenceEncoder;
import vendredi.soir.nofy.data.ActionDefinition;
import vendredi.soir.nofy.data.EntityDefinition;
import vendredi.soir.nofy.data.RigDefinition;
import vendredi.soir.nofy.data.VideoExportConfig;
import vendredi.soir.nofy.entity.Entity;
import vendredi.soir.nofy.factory.ActionLoader;
import vendredi.soir.nofy.factory.EntityDefinitionLoader;
import vendredi.soir.nofy.factory.RigLoader;
import vendredi.soir.nofy.factory.SceneLoader;
import vendredi.soir.nofy.game.Game;
import vendredi.soir.nofy.game.GameWorld;
import vendredi.soir.nofy.system.CollisionSystem;
import vendredi.soir.nofy.system.TriggerSystem;

/**
 * Drives the same GameWorld/AnimatedEntity/SceneLoader pipeline as the interactive game, but with a
 * fixed timestep (not wall-clock time) so playback speed is deterministic, capturing each frame to
 * a video file instead of presenting it on screen.
 */
public class VideoExportApplication implements ApplicationListener {
  private final VideoExportConfig config;

  private SpriteBatch spriteBatch;
  private Viewport viewport;
  private GameWorld world;
  private final CollisionSystem collisionSystem = new CollisionSystem();
  private final TriggerSystem triggerSystem = new TriggerSystem();
  private AWTSequenceEncoder encoder;
  private int totalFrames;
  private int framesEncoded;
  private float fixedDeltaTime;
  private int tailFrames;
  private Integer stopFrame;
  private double upTime = 0;

  public VideoExportApplication(VideoExportConfig config) {
    this.config = config;
  }

  @Override
  public void create() {
    spriteBatch = new SpriteBatch();
    viewport = new FitViewport(Game.WORLD_WIDTH, Game.WORLD_HEIGHT);

    world = new GameWorld();
    Map<String, ActionDefinition> actions = ActionLoader.loadAll(config.getActionsDir());
    Map<String, EntityDefinition> entityDefinitions =
        EntityDefinitionLoader.loadAll(config.getEntitiesDir());
    Map<String, RigDefinition> rigs = RigLoader.loadAll(config.getRigsDir());
    SceneLoader.populate(world, config.getSceneFile(), entityDefinitions, rigs, actions);

    fixedDeltaTime = 1f / config.getFps();
    totalFrames = config.getFps() * config.getDurationSeconds();
    tailFrames = config.getFps(); // ~1s of extra footage after an early stop condition fires

    try {
      File outputFile = new File(config.getOutputPath());
      File outputDir = outputFile.getParentFile();
      if (outputDir != null) {
        outputDir.mkdirs();
      }
      encoder = AWTSequenceEncoder.createSequenceEncoder(outputFile, config.getFps());
    } catch (IOException e) {
      throw new RuntimeException("Failed to open video output for encoding", e);
    }
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, true);
  }

  @Override
  public void render() {
    if (framesEncoded >= totalFrames || (stopFrame != null && framesEncoded >= stopFrame)) {
      finishAndExit();
      return;
    }

    upTime += fixedDeltaTime;
    triggerSystem.update(world, viewport, upTime);
    world.animate(fixedDeltaTime);
    collisionSystem.resolve(world);
    world.clearEvents();

    Integer stopAfterCollisionCount = config.getStopAfterCollisionCount();
    if (stopFrame == null
        && stopAfterCollisionCount != null
        && collisionSystem.getCollisionEventCount() >= stopAfterCollisionCount) {
      stopFrame = framesEncoded + tailFrames;
    }

    followCameraTarget();
    ScreenUtils.clear(Color.valueOf("069f66"));
    spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
    spriteBatch.begin();
    world.draw(spriteBatch);
    spriteBatch.end();

    Pixmap frame = ScreenUtils.getFrameBufferPixmap(0, 0, config.getWidth(), config.getHeight());
    try {
      encoder.encodeImage(toBufferedImage(frame));
    } catch (IOException e) {
      throw new RuntimeException("Failed to encode video frame", e);
    } finally {
      frame.dispose();
    }

    framesEncoded++;
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

  private void finishAndExit() {
    try {
      encoder.finish();
    } catch (IOException e) {
      throw new RuntimeException("Failed to finalize video output", e);
    }
    world.dispose();
    Gdx.app.exit();
  }

  /** Framebuffer rows are bottom-up; BufferedImage rows are top-down, so rows are flipped. */
  private static BufferedImage toBufferedImage(Pixmap pixmap) {
    int width = pixmap.getWidth();
    int height = pixmap.getHeight();
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    ByteBuffer pixels = pixmap.getPixels();
    pixels.clear();
    for (int y = 0; y < height; y++) {
      int flippedY = height - 1 - y;
      for (int x = 0; x < width; x++) {
        int r = pixels.get() & 0xFF;
        int g = pixels.get() & 0xFF;
        int b = pixels.get() & 0xFF;
        pixels.get(); // alpha, unused for video output
        image.setRGB(x, flippedY, (r << 16) | (g << 8) | b);
      }
    }
    return image;
  }

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void dispose() {}
}
