package vendredi.soir.nofy.factory;

import com.badlogic.gdx.utils.Json;
import vendredi.soir.nofy.data.ActionRule;
import vendredi.soir.nofy.data.AnimationBinding;
import vendredi.soir.nofy.data.BoneDefinition;
import vendredi.soir.nofy.data.EntityDefinition;
import vendredi.soir.nofy.data.EntityInstanceDefinition;
import vendredi.soir.nofy.data.RigDefinition;
import vendredi.soir.nofy.data.SceneDefinition;

public final class GameDataJson {
  private static final Json JSON = new Json();

  static {
    JSON.setElementType(EntityDefinition.class, "animations", AnimationBinding.class);
    JSON.setElementType(SceneDefinition.class, "entities", EntityInstanceDefinition.class);
    JSON.setElementType(EntityInstanceDefinition.class, "actionRules", ActionRule.class);
    JSON.setElementType(RigDefinition.class, "bones", BoneDefinition.class);
  }

  private GameDataJson() {}

  public static Json instance() {
    return JSON;
  }
}
