package vendredi.soir.nofy.data;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * One (Trigger, Action) rule on an entity instance. An entity's current action is whichever rule
 * has the highest priority among those whose trigger currently holds, falling back to the
 * entity's own default action when none do.
 */
@Getter
@NoArgsConstructor
public class ActionRule {
  private TriggerDefinition trigger;
  private String actionId;
  private int priority = 0;
  private boolean followTrigger = false; // if true and this rule wins, position tracks the trigger source (mouse)
}
