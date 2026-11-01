package vendredi.soir.nofy.entity;

import com.badlogic.gdx.math.Rectangle;
import java.util.List;
import vendredi.soir.nofy.data.ActionRule;

/**
 * An actor in the world: something GameWorld animates and draws, and that the collision and trigger
 * systems can measure, position and drive.
 *
 * <p>This exists because a rigged character is not one Sprite and so cannot extend TexturedEntity.
 * AnimatedEntity (one sprite, one flipbook) and RiggedEntity (a bone hierarchy of parts) share no
 * class hierarchy, only this surface.
 */
public interface Entity extends Drawable {
  String getName();

  float getX();

  float getY();

  float getWidth();

  float getHeight();

  void setX(float x);

  void setY(float y);

  void setPosition(float x, float y);

  void translate(float xAmount, float yAmount);

  Rectangle getBoundingRectangle();

  void animate(float deltaTime);

  void play(String actionId);

  String getDefaultActionId();

  List<ActionRule> getActionRules();

  void setActionRules(List<ActionRule> actionRules);

  boolean isSolid();

  void setSolid(boolean solid);

  boolean isPositionDrivenByTrigger();

  void setPositionDrivenByTrigger(boolean positionDrivenByTrigger);
}
