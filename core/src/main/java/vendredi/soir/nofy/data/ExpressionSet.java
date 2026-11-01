package vendredi.soir.nofy.data;

import java.util.HashMap;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** expressions.json: face slot choices per expression, and gaze offsets per look direction. */
@Getter
@NoArgsConstructor
public class ExpressionSet {
  private HashMap<String, Expression> expressions;
  private HashMap<String, float[]> lookOffsets;

  @Getter
  @NoArgsConstructor
  public static class Expression {
    private String eyes;
    private String mouth;
    private String brows;
  }
}
