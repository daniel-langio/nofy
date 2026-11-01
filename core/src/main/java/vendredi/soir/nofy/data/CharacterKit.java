package vendredi.soir.nofy.data;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Everything under assets/data/character-kit, loaded. Pose ids include "base". */
@Getter
@RequiredArgsConstructor
public class CharacterKit {
  private final RigManifest manifest;
  private final CharacterCatalog catalog;
  private final Map<String, PoseDefinition> poses;
  private final Map<String, ClipDefinition> clips;
  private final Map<String, GestureDefinition> gestures;
  private final ExpressionSet expressions;
}
