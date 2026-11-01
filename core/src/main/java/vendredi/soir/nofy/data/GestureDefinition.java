package vendredi.soir.nofy.data;

import lombok.Getter;
import lombok.NoArgsConstructor;

/** A parametric hand: finger spread, per-finger curl (0-1), thumb angle and curl. */
@Getter
@NoArgsConstructor
public class GestureDefinition {
  private float spread;
  private float[] curl;
  private float thumb;
  private float tcurl;
}
