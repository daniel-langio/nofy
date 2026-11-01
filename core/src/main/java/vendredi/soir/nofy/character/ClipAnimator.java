package vendredi.soir.nofy.character;

import java.util.Map;
import vendredi.soir.nofy.data.ActionDefinition;
import vendredi.soir.nofy.data.CharacterKit;
import vendredi.soir.nofy.data.ClipDefinition;
import vendredi.soir.nofy.entity.RiggedEntity;

/**
 * Plays character kit clips and poses on one rigged entity. An action id with a clip of the same
 * name starts it (looping per the action's loop flag); any other action leaves the pose alone.
 */
public class ClipAnimator {
  private final CharacterKit kit;
  private final CharacterRig rig;
  private final Map<String, ActionDefinition> actions;

  private ClipDefinition clip;
  private ClipPlayer player;
  private boolean loop;
  private float elapsed;
  private ResolvedPose held;
  private boolean mirrored;

  public ClipAnimator(CharacterKit kit, String characterId, Map<String, ActionDefinition> actions) {
    this.kit = kit;
    this.rig = new CharacterRig(kit, characterId);
    this.actions = actions;
  }

  public void play(String actionId) {
    ClipDefinition next = kit.getClips().get(actionId);
    if (next == null) return;
    clip = next;
    held = null;
    player = new ClipPlayer(rig, next);
    ActionDefinition action = actions.get(actionId);
    loop = action == null || action.isLoop();
    elapsed = 0f;
  }

  /** Holds a library pose, dropping any clip that was playing. */
  public void showPose(RiggedEntity entity, String poseId) {
    clip = null;
    player = null;
    held = rig.resolve(poseId);
    PoseApplier.apply(entity, rig, held);
  }

  /** Turns the character to face right whatever its poses say; false leaves each pose's facing. */
  public void setMirrored(boolean mirrored) {
    this.mirrored = mirrored;
  }

  public void update(RiggedEntity entity, float deltaTime) {
    if (player == null) {
      if (held != null) apply(entity, held);
      return;
    }
    elapsed += deltaTime;
    float length = (clip.getEndFrame() - clip.getStartFrame()) / clip.getFps();
    if (loop && length > 0) {
      elapsed %= length;
    } else {
      elapsed = Math.min(elapsed, length);
    }
    int frame = clip.getStartFrame() + Math.round(elapsed * clip.getFps());
    apply(entity, player.poseAt(frame));
  }

  private void apply(RiggedEntity entity, ResolvedPose pose) {
    if (mirrored) pose.facing = "right";
    PoseApplier.apply(entity, rig, pose);
  }
}
