package vendredi.soir.nofy.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import vendredi.soir.nofy.entity.Entity;
import vendredi.soir.nofy.graphics.sprites.SpriteLoader;

@Getter
@RequiredArgsConstructor
public class GameWorld {
  private List<Entity> entities = new ArrayList<>();
  @Setter private Entity cameraTarget;
  @Setter private float simulationRange = Float.MAX_VALUE;
  private final Set<String> activeEvents = new HashSet<>();

  public void addEntity(Entity entity) {
    entities.add(entity);
  }

  public void animate(float deltaTime) {
    entities.stream()
        .filter(this::isWithinSimulationRange)
        .forEach(entity -> entity.animate(deltaTime));
  }

  // Physics gating only - trigger/action evaluation (TriggerSystem) still runs on every entity
  // regardless of range, since a rule-driven action (e.g. a schedule) must stay correct on query
  // whether or not the entity is currently being physically simulated.
  private boolean isWithinSimulationRange(Entity entity) {
    if (cameraTarget == null || entity == cameraTarget || simulationRange == Float.MAX_VALUE) {
      return true;
    }
    float dx = entity.getX() - cameraTarget.getX();
    float dy = entity.getY() - cameraTarget.getY();
    return Math.sqrt(dx * dx + dy * dy) <= simulationRange;
  }

  public void raiseEvent(String name) {
    activeEvents.add(name);
  }

  public boolean isEventActive(String name) {
    return activeEvents.contains(name);
  }

  public void clearEvents() {
    activeEvents.clear();
  }

  public void dispose() {
    Set<Texture> texturesToDispose = new HashSet<>();
    entities.forEach(entity -> texturesToDispose.addAll(entity.getTexturesToDispose()));

    texturesToDispose.forEach(Texture::dispose);
    SpriteLoader.clearCache();
  }

  public void draw(SpriteBatch batch) {
    entities.forEach(entity -> entity.draw(batch));
  }
}
