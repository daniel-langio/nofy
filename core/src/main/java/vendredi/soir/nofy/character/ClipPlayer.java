package vendredi.soir.nofy.character;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vendredi.soir.nofy.data.ClipDefinition;
import vendredi.soir.nofy.data.ClipKey;

/**
 * Evaluates a clip at any frame, in any order: the result depends only on (clip, frame), never on
 * what was evaluated before.
 */
public class ClipPlayer {
  private final CharacterRig rig;
  private final ClipDefinition clip;
  private final Map<Integer, ResolvedPose> keyPoses = new HashMap<>();

  public ClipPlayer(CharacterRig rig, ClipDefinition clip) {
    this.rig = rig;
    this.clip = clip;
  }

  public ResolvedPose poseAt(int frame) {
    List<ClipKey> keys = clip.getKeys();
    int i = 0;
    while (i < keys.size() - 1 && keys.get(i + 1).getFrame() <= frame) i++;
    ResolvedPose p;
    if (i >= keys.size() - 1 || frame <= keys.get(i).getFrame()) {
      p = keyPose(i).copy();
    } else {
      ClipKey k0 = keys.get(i);
      ClipKey k1 = keys.get(i + 1);
      float t = (frame - k0.getFrame()) / (float) (k1.getFrame() - k0.getFrame());
      p = ResolvedPose.lerp(keyPose(i), keyPose(i + 1), ease(k1.getEase(), t));
    }
    if (clip.getTracks() != null) {
      clip.getTracks().forEach((path, track) -> p.set(path, trackValue(track, frame)));
    }
    return p;
  }

  private ResolvedPose keyPose(int index) {
    return keyPoses.computeIfAbsent(
        index,
        i -> {
          ClipKey k = clip.getKeys().get(i);
          ResolvedPose p = rig.resolve(k.getPose());
          if (k.getSet() != null) p.overlay(k.getSet());
          return p;
        });
  }

  static float trackValue(ClipDefinition.TrackKey[] keys, int frame) {
    if (frame <= keys[0].getFrame()) return keys[0].getValue();
    for (int i = 1; i < keys.length; i++) {
      if (frame <= keys[i].getFrame()) {
        ClipDefinition.TrackKey a = keys[i - 1];
        ClipDefinition.TrackKey b = keys[i];
        float t = ease(b.getEase(), (frame - a.getFrame()) / (float) (b.getFrame() - a.getFrame()));
        return a.getValue() + (b.getValue() - a.getValue()) * t;
      }
    }
    return keys[keys.length - 1].getValue();
  }

  static float ease(String name, float t) {
    return switch (name == null ? "inOut" : name) {
      case "linear" -> t;
      case "in" -> t * t;
      case "out" -> 1 - (1 - t) * (1 - t);
      case "hold" -> t >= 1 ? 1 : 0;
      case "inOut" -> t < 0.5f ? 2 * t * t : 1 - (float) Math.pow(-2 * t + 2, 2) / 2;
      default -> throw new IllegalArgumentException("Unknown ease: " + name);
    };
  }
}
