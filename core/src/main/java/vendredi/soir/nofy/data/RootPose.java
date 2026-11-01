package vendredi.soir.nofy.data;

import lombok.Getter;
import lombok.NoArgsConstructor;

/** Hip translation (body-frame units, y down) and whole-body rotation (clockwise, degrees). */
@Getter
@NoArgsConstructor
public class RootPose {
  private Float x;
  private Float y;
  private Float rot;
}
