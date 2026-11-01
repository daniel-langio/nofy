package vendredi.soir.nofy.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import vendredi.soir.nofy.data.ActionRule;
import vendredi.soir.nofy.data.TriggerDefinition;
import vendredi.soir.nofy.entity.Entity;
import vendredi.soir.nofy.game.GameWorld;

/**
 * Evaluates each entity's ordered (Trigger, Action) rules (highest priority first, already sorted
 * by SceneLoader) and switches its current action to the first whose trigger currently holds,
 * falling back to the entity's own default action when none do. A winning rule may also be marked
 * to drive the entity's position from the trigger's source (only the mouse, for now, and only one
 * entity may be so driven at a time).
 */
public class TriggerSystem {
  private static final float DAY_LENGTH_SECONDS = 120f;

  private Entity following;
  private Entity clickOriginEntity;

  public void update(GameWorld world, Viewport viewport, double upTime) {
    Vector2 mouseWorld = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
    boolean leftButtonPressed = Gdx.input.isButtonPressed(Buttons.LEFT);
    boolean leftJustPressed = Gdx.input.isButtonJustPressed(Buttons.LEFT);
    float hourOfDay = hourOfDay(upTime);

    if (!leftButtonPressed) {
      clickOriginEntity = null;
    }

    for (Entity entity : world.getEntities()) {
      if (entity.getActionRules().isEmpty()) {
        continue;
      }
      evaluate(entity, world, mouseWorld, leftJustPressed, leftButtonPressed, hourOfDay);
    }
  }

  private void evaluate(
      Entity entity,
      GameWorld world,
      Vector2 mouseWorld,
      boolean leftJustPressed,
      boolean leftButtonPressed,
      float hourOfDay) {
    ActionRule winner = null;
    for (ActionRule rule : entity.getActionRules()) {
      if (isSatisfied(
          rule.getTrigger(),
          entity,
          world,
          mouseWorld,
          leftJustPressed,
          leftButtonPressed,
          hourOfDay)) {
        winner = rule;
        break;
      }
    }

    if (winner != null && winner.isFollowTrigger() && (following == null || following == entity)) {
      following = entity;
      entity.setPositionDrivenByTrigger(true);
      entity.setPosition(
          mouseWorld.x - entity.getWidth() / 2f, mouseWorld.y - entity.getHeight() / 2f);
      entity.play(winner.getActionId());
      return;
    }

    if (following == entity) {
      following = null;
    }
    entity.setPositionDrivenByTrigger(false);
    entity.play(winner != null ? winner.getActionId() : entity.getDefaultActionId());
  }

  private boolean isSatisfied(
      TriggerDefinition trigger,
      Entity entity,
      GameWorld world,
      Vector2 mouseWorld,
      boolean leftJustPressed,
      boolean leftButtonPressed,
      float hourOfDay) {
    Rectangle bounds = entity.getBoundingRectangle();
    return switch (trigger.getType()) {
      case CLICK ->
          isClickSatisfied(entity, bounds, mouseWorld, leftJustPressed, leftButtonPressed);
      case HOVER -> bounds.contains(mouseWorld);
      case PROXIMITY -> isWithinProximity(entity, world, trigger.getRadius());
      case TIME_OF_DAY ->
          isWithinHourRange(hourOfDay, trigger.getStartHour(), trigger.getEndHour());
      case ENVIRONMENT_EVENT -> world.isEventActive(trigger.getEventName());
    };
  }

  // True from the frame the button is pressed down over this entity until it's released,
  // regardless of where the mouse moves in between - classic click-and-drag, not a one-frame blip.
  private boolean isClickSatisfied(
      Entity entity,
      Rectangle bounds,
      Vector2 mouseWorld,
      boolean leftJustPressed,
      boolean leftButtonPressed) {
    if (!leftButtonPressed) {
      return false;
    }
    if (entity == clickOriginEntity) {
      return true;
    }
    if (leftJustPressed && clickOriginEntity == null && bounds.contains(mouseWorld)) {
      clickOriginEntity = entity;
      return true;
    }
    return false;
  }

  private boolean isWithinProximity(Entity entity, GameWorld world, Float radius) {
    Entity target = world.getCameraTarget();
    if (target == null || target == entity || radius == null) {
      return false;
    }
    float dx = entity.getX() - target.getX();
    float dy = entity.getY() - target.getY();
    return Math.sqrt(dx * dx + dy * dy) <= radius;
  }

  private boolean isWithinHourRange(float hour, Float start, Float end) {
    if (start == null || end == null) {
      return false;
    }
    if (start <= end) {
      return hour >= start && hour < end;
    }
    return hour >= start || hour < end; // wraps past midnight, e.g. 22 -> 6
  }

  private float hourOfDay(double upTime) {
    float progress = (float) ((upTime % DAY_LENGTH_SECONDS) / DAY_LENGTH_SECONDS);
    return progress * 24f;
  }
}
