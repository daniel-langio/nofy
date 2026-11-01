package vendredi.soir.nofy.data;

import java.util.HashMap;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** One character's proportions and palette on the shared skeleton. */
@Getter
@NoArgsConstructor
public class CharacterBuild {
  private String name;
  private boolean longSleeves;
  private Build build;
  private HashMap<String, String> colors;

  @Getter
  @NoArgsConstructor
  public static class Build {
    private float scale = 1f;
    private float head = 1f;
    private float headDy;

    /** Added to every pose's spine rotation. */
    private float stoop;

    /** Upper and lower segment length. */
    private float[] arm;

    private float[] leg;
  }
}
