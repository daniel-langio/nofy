package vendredi.soir.nofy.data;

import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A deterministic animation: pose keys with easing plus per-control override tracks. Any frame can
 * be evaluated on its own, in any order.
 *
 * <p>tracks maps a control path such as "armR.wrist" to keyframes that override the interpolated
 * pose after it has been computed.
 */
@Getter
@NoArgsConstructor
public class ClipDefinition {
  private String id;
  private float fps = 30f;
  private int startFrame;
  private int endFrame;
  private String rigVersion;
  private List<ClipKey> keys;
  private HashMap<String, TrackKey[]> tracks;

  @Getter
  @NoArgsConstructor
  public static class TrackKey {
    private int frame;
    private float value;
    private String ease;
  }
}
