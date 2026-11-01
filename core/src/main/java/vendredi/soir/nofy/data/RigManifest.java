package vendredi.soir.nofy.data;

import java.util.HashMap;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The rig contract shared by every character: pivots, limb lengths, preferred bend sides, angle
 * limits and control ranges. It drives clamping and the rig check.
 */
@Getter
@NoArgsConstructor
public class RigManifest {
  private String id;
  private String[] drawOrder;
  private HashMap<String, float[]> pivots;
  private HashMap<String, Limb> limbs;
  private HashMap<String, float[]> controls;
  private HashMap<String, String[]> replacements;

  @Getter
  @NoArgsConstructor
  public static class Limb {
    private float[] lengths;
    private int preferredBend;

    /** Keyed "a" and "b", each [min, max] degrees. */
    private HashMap<String, float[]> limits;
  }
}
