package vendredi.soir.nofy.factory;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.List;
import java.util.Map;
import vendredi.soir.nofy.data.ActionDefinition;
import vendredi.soir.nofy.data.AnimationBinding;
import vendredi.soir.nofy.data.EntityDefinition;
import vendredi.soir.nofy.data.RigDefinition;
import vendredi.soir.nofy.entity.AnimatedEntity;
import vendredi.soir.nofy.entity.Entity;
import vendredi.soir.nofy.entity.RiggedEntity;
import vendredi.soir.nofy.graphics.sprites.SpriteAnimation;
import vendredi.soir.nofy.graphics.sprites.SpriteLoader;

public final class EntityFactory {
  private EntityFactory() {}

  public static Entity create(
      EntityDefinition definition,
      Map<String, ActionDefinition> actions,
      Map<String, RigDefinition> rigs,
      String instanceName,
      String initialActionId) {
    String name = instanceName != null ? instanceName : definition.getId();
    String defaultActionId =
        initialActionId != null ? initialActionId : definition.getDefaultActionId();

    if (definition.getRigId() != null) {
      return createRigged(definition, rigs, name, defaultActionId);
    }

    List<SpriteAnimation> spriteAnimations =
        definition.getAnimations().stream()
            .map(binding -> toSpriteAnimation(binding, actions))
            .toList();

    AnimatedEntity entity = new AnimatedEntity(name, spriteAnimations, defaultActionId);
    entity.setSize(definition.getWidth(), definition.getHeight());
    return entity;
  }

  private static RiggedEntity createRigged(
      EntityDefinition definition,
      Map<String, RigDefinition> rigs,
      String name,
      String defaultActionId) {
    RigDefinition rig = rigs.get(definition.getRigId());
    if (rig == null) {
      throw new IllegalArgumentException(
          String.format(
              "Rig %s referenced by entity %s is not defined",
              definition.getRigId(), definition.getId()));
    }

    // A rig is scaled to the entity's height; its width follows from the character's proportions,
    // so definition.getWidth() is not used here.
    return new RiggedEntity(
        name, rig, definition.getHeight(), defaultActionId, SpriteLoader::loadRegion);
  }

  private static SpriteAnimation toSpriteAnimation(
      AnimationBinding binding, Map<String, ActionDefinition> actions) {
    ActionDefinition action = actions.get(binding.getActionId());
    if (action == null) {
      throw new IllegalArgumentException(
          String.format("Action %s referenced by a binding is not defined", binding.getActionId()));
    }

    List<TextureRegion> frames =
        SpriteLoader.loadSpriteSet(
            binding.getSpriteSheetPath(),
            binding.getFrameWidth(),
            binding.getFrameHeight(),
            binding.getFirstFrameX(),
            binding.getFirstFrameY(),
            binding.getFrameCount());

    SpriteAnimation animation =
        new SpriteAnimation(0, binding.getFps(), binding.getActionId(), frames);
    animation.setLoop(action.isLoop());
    animation.setVx(action.getVx());
    animation.setVy(action.getVy());
    return animation;
  }
}
