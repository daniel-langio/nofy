package vendredi.soir.nofy.data;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A pose reached at a frame. ease shapes the segment arriving at this key: linear, in, out, inOut
 * (default), hold. set is deep-merged onto the named pose.
 */
@Getter
@NoArgsConstructor
public class ClipKey {
  private int frame;
  private String pose;
  private String ease;
  private PoseDefinition set;
}
