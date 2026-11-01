package vendredi.soir.nofy.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import vendredi.soir.nofy.data.ActionRule;
import vendredi.soir.nofy.graphics.sprites.SpriteAnimation;

@Getter
public class AnimatedEntity extends TexturedEntity implements Entity {
  private final String defaultActionId;
  private final List<SpriteAnimation> animations;
  private String currentActionId;
  @Setter private Rectangle bounceBounds;
  @Setter private List<ActionRule> actionRules = List.of();
  @Setter private boolean positionDrivenByTrigger = false;
  @Setter private boolean solid = false;

  public AnimatedEntity(String name, List<SpriteAnimation> animations, String defaultActionId) {
    super(name, resolveDefaultTexture(animations, defaultActionId));
    this.animations = validateAnimations(animations, defaultActionId);
    this.defaultActionId = defaultActionId;
    this.currentActionId = defaultActionId;
  }

  @Override
  public void play(String actionId) {
    if (!actionId.equals(currentActionId)) {
      resetCurrentAnimation();
      currentActionId = actionId;
    }
  }

  public void resetCurrentAnimation() {
    getCurrentAnimation().reset();
  }

  @Override
  public void draw(Batch batch) {
    batch.draw(getCurrentAnimation().getCurrentFrame(), getX(), getY(), getWidth(), getHeight());
  }

  @Override
  public void animate(float deltaTime) {
    SpriteAnimation currentAnimation = getCurrentAnimation();
    currentAnimation.animate(deltaTime);
    if (positionDrivenByTrigger) {
      // A trigger rule has full authority over position while active; only the flipbook still runs.
      return;
    }
    translate(currentAnimation.getVx() * deltaTime, currentAnimation.getVy() * deltaTime);
    if (bounceBounds != null) {
      bounceWithinBounds(currentAnimation);
    }
  }

  private void bounceWithinBounds(SpriteAnimation currentAnimation) {
    if (getX() < bounceBounds.x) {
      setX(bounceBounds.x);
      currentAnimation.setVx(Math.abs(currentAnimation.getVx()));
    } else if (getX() + getWidth() > bounceBounds.x + bounceBounds.width) {
      setX(bounceBounds.x + bounceBounds.width - getWidth());
      currentAnimation.setVx(-Math.abs(currentAnimation.getVx()));
    }

    if (getY() < bounceBounds.y) {
      setY(bounceBounds.y);
      currentAnimation.setVy(Math.abs(currentAnimation.getVy()));
    } else if (getY() + getHeight() > bounceBounds.y + bounceBounds.height) {
      setY(bounceBounds.y + bounceBounds.height - getHeight());
      currentAnimation.setVy(-Math.abs(currentAnimation.getVy()));
    }
  }

  @Override
  public Set<Texture> getTexturesToDispose() {
    Set<Texture> textures = new HashSet<>();
    for (SpriteAnimation animation : animations) {
      animation.getFrames().forEach(frame -> textures.add(frame.getTexture()));
    }
    return textures;
  }

  public Texture getDefaultTexture() {
    return getDefaultAnimation().getCurrentFrame().getTexture();
  }

  public SpriteAnimation getCurrentAnimation() {
    return findAnimation(animations, currentActionId);
  }

  public SpriteAnimation getDefaultAnimation() {
    return findAnimation(animations, defaultActionId);
  }

  private static Texture resolveDefaultTexture(
      List<SpriteAnimation> animations, String defaultActionId) {
    return findAnimation(animations, defaultActionId).getCurrentFrame().getTexture();
  }

  private static List<SpriteAnimation> validateAnimations(
      List<SpriteAnimation> animations, String defaultActionId) {
    if (animations.stream().noneMatch(a -> a.getActionId().equals(defaultActionId))) {
      throw new IllegalArgumentException(
          String.format("This entity requires an animation for : %s", defaultActionId));
    }
    return animations;
  }

  private static SpriteAnimation findAnimation(List<SpriteAnimation> animations, String actionId) {
    return animations.stream()
        .filter(a -> a.getActionId().equals(actionId))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException(String.format("Action %s not found", actionId)));
  }
}
