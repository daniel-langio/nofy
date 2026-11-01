package vendredi.soir.nofy.data;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A character's skeleton, as produced by tools/build-character.sh.
 *
 * <p>Every coordinate here is in the square canvas space the artwork was authored in - y-down,
 * origin top-left - and is independent of the resolution the parts were rasterized at. RiggedEntity
 * converts to world space (y-up, scaled to the entity's height) exactly once; keeping a single
 * conversion point is what stops the y-flip and the scale factor from being applied twice or not at
 * all.
 *
 * <p>figureX/Y/Width/Height is the union of every part's opaque bounds - the character's actual
 * extent on the canvas, which is what the entity's height is measured against.
 */
@Getter
@NoArgsConstructor
public class RigDefinition {
  private String id;
  private float canvas = 1024f;
  private float rasterScale = 1f;
  private float figureX;
  private float figureY;
  private float figureWidth;
  private float figureHeight;
  private List<BoneDefinition> bones;
}
