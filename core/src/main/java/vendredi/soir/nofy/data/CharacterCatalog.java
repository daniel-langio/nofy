package vendredi.soir.nofy.data;

import java.util.HashMap;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** characters.json: every build plus the shared bind-rig constants. */
@Getter
@NoArgsConstructor
public class CharacterCatalog {
  /** Canvas units per body-frame unit the character art was exported at. */
  private float canvasScale = 1f;

  private HashMap<String, CharacterBuild> characters;
  private HashMap<String, float[]> adultLengths;
  private HashMap<String, BindLimb> bindRig;

  /** Hips centre and neck pivot in the 300-wide body frame. */
  private float[] h0;

  private float[] n;
  private HashMap<String, Seat> seats;

  @Getter
  @NoArgsConstructor
  public static class BindLimb {
    /** Shoulder or hip pivot. */
    private float[] S;

    private float L1;
    private float L2;
    private float[] w;
  }

  @Getter
  @NoArgsConstructor
  public static class Seat {
    private float top;
    private float off;
  }
}
