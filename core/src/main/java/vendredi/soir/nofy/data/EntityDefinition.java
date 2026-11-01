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

  private List<AnimationBinding> animations;
}
