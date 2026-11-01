package vendredi.soir.nofy.graphics.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SpriteLoader {
  private static final Map<String, Texture> TEXTURE_CACHE = new HashMap<>();

  public static List<TextureRegion> loadSpriteSet(
      String spriteSetFilePath,
      int frameWidth,
      int frameHeight,
      int firstFrameX,
      int firstFrameY,
      int frameAmount) {
    List<TextureRegion> spriteSet = new ArrayList<>();

    Texture spriteSetAll = getOrLoadTexture(spriteSetFilePath);
    for (int i = 0; i < frameAmount; i++) {
      int x = firstFrameX + i * frameWidth;
      int y = firstFrameY;

      TextureRegion frame = new TextureRegion(spriteSetAll, x, y, frameWidth, frameHeight);
      spriteSet.add(frame);
    }

    return spriteSet;
  }

  /** A region covering a whole image, for rig parts - each is already its own trimmed file. */
  public static TextureRegion loadRegion(String filePath) {
    return new TextureRegion(getOrLoadTexture(filePath));
  }

  private static Texture getOrLoadTexture(String path) {
    return TEXTURE_CACHE.computeIfAbsent(path, Texture::new);
  }

  public static void clearCache() {
    TEXTURE_CACHE.clear();
  }
}
