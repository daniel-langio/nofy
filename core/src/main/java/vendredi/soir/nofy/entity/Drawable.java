package vendredi.soir.nofy.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import java.util.Set;

/** Something that can be drawn, and whose textures must eventually be freed. */
public interface Drawable {
  void draw(Batch batch);

  Set<Texture> getTexturesToDispose();
}
