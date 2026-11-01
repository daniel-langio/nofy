package vendredi.soir.nofy.data;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EntityDefinition {
  private String id;
  private float width = 32f;
  private float height = 32f;
  private String defaultActionId;

  /** When set, this entity is built from the named rig instead of sprite-sheet animations. */
  private String rigId;

  /** When set on a rigged entity, it is posed and animated from this character kit build. */
  private String characterId;

  private List<AnimationBinding> animations;
}
