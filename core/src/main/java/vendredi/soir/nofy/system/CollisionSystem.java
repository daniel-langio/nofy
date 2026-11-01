package vendredi.soir.nofy.system;

import com.badlogic.gdx.math.Rectangle;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import vendredi.soir.nofy.entity.AnimatedEntity;
import vendredi.soir.nofy.entity.Entity;
import vendredi.soir.nofy.game.GameWorld;
import vendredi.soir.nofy.graphics.sprites.SpriteAnimation;

/**
 * Pairwise bounding-box collision among "solid" entities. O(n^2) - fine at demo scale, not meant to
 * scale to large entity counts. Also counts distinct collision events (a pair transitioning from
 * separated to overlapping, not every frame they happen to stay in contact), for callers that want
 * to react to or stop after N collisions.
 */
public class CollisionSystem {
  @Getter private int collisionEventCount = 0;

  private final Set<Map.Entry<Entity, Entity>> previouslyOverlapping = new HashSet<>();

  public void resolve(GameWorld world) {
    List<Entity> solidEntities = new ArrayList<>();
    for (Entity entity : world.getEntities()) {
      if (entity.isSolid()) {
        solidEntities.add(entity);
      }
    }

    Set<Map.Entry<Entity, Entity>> stillOverlapping = new HashSet<>();

    for (int i = 0; i < solidEntities.size(); i++) {
      for (int j = i + 1; j < solidEntities.size(); j++) {
        Entity a = solidEntities.get(i);
        Entity b = solidEntities.get(j);

        if (!resolvePair(a, b)) {
          continue;
        }

        Map.Entry<Entity, Entity> pairKey = new AbstractMap.SimpleEntry<>(a, b);
        stillOverlapping.add(pairKey);
        if (!previouslyOverlapping.contains(pairKey)) {
          collisionEventCount++;
        }
      }
    }

    previouslyOverlapping.clear();
    previouslyOverlapping.addAll(stillOverlapping);
  }

  private boolean resolvePair(Entity a, Entity b) {
    Rectangle boundsA = a.getBoundingRectangle();
    Rectangle boundsB = b.getBoundingRectangle();
    if (!boundsA.overlaps(boundsB)) {
      return false;
    }

    float overlapX =
        Math.min(boundsA.x + boundsA.width, boundsB.x + boundsB.width)
            - Math.max(boundsA.x, boundsB.x);
    float overlapY =
        Math.min(boundsA.y + boundsA.height, boundsB.y + boundsB.height)
            - Math.max(boundsA.y, boundsB.y);

    if (overlapX < overlapY) {
      separateAndBounce(a, b, overlapX, true, boundsA.x < boundsB.x);
    } else {
      separateAndBounce(a, b, overlapY, false, boundsA.y < boundsB.y);
    }
    return true;
  }

  private void separateAndBounce(
      Entity a, Entity b, float overlap, boolean xAxis, boolean aIsLower) {
    float push = overlap / 2f;
    float aSign = aIsLower ? -1f : 1f;

    if (xAxis) {
      a.setX(a.getX() + aSign * push);
      b.setX(b.getX() - aSign * push);
      flipVx(a);
      flipVx(b);
    } else {
      a.setY(a.getY() + aSign * push);
      b.setY(b.getY() - aSign * push);
      flipVy(a);
      flipVy(b);
    }
  }

  // A controlled entity's position is driven by the mouse, not its own velocity - don't perturb
  // the velocity it'll resume with once released. Velocity itself lives on a sprite entity's
  // current action, so a rigged character is separated but has nothing to reverse.
  private void flipVx(Entity entity) {
    if (entity.isPositionDrivenByTrigger() || !(entity instanceof AnimatedEntity animated)) {
      return;
    }
    SpriteAnimation animation = animated.getCurrentAnimation();
    animation.setVx(-animation.getVx());
  }

  private void flipVy(Entity entity) {
    if (entity.isPositionDrivenByTrigger() || !(entity instanceof AnimatedEntity animated)) {
      return;
    }
    SpriteAnimation animation = animated.getCurrentAnimation();
    animation.setVy(-animation.getVy());
  }
}
