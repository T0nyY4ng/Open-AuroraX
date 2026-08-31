package espada.spacex.aurora.utils.meteor;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.renderer.Mesh;
import meteordevelopment.meteorclient.utils.render.ByteTexture;
import meteordevelopment.meteorclient.utils.render.ByteTexture.Filter;
import meteordevelopment.meteorclient.utils.render.ByteTexture.Format;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackRange;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

public class FontFix {
   public ByteTexture texture;
   private final int height;
   private final float scale;
   private final float ascent;
   private final Int2ObjectOpenHashMap<CharData> charMap = new Int2ObjectOpenHashMap();
   private static final int size = 2048;
   private final ByteBuffer buffer;
   private final STBTTFontinfo fontInfo;
   private final ByteBuffer bitmap;
   private final STBTTPackContext packContext;
   private final Int2ObjectOpenHashMap<STBTTPackedchar> packedChars = new Int2ObjectOpenHashMap();
   private long loadTimer = 0L;
   private int loadCount = 0;
   private final int loadSpeedLimit = 7;

   public FontFix(ByteBuffer buffer, int height) {
      this.buffer = buffer;
      this.height = height;
      this.fontInfo = STBTTFontinfo.create();
      STBTruetype.stbtt_InitFont(this.fontInfo, buffer);
      this.bitmap = BufferUtils.createByteBuffer(4194304);
      this.packContext = STBTTPackContext.create();
      STBTruetype.stbtt_PackBegin(this.packContext, this.bitmap, 2048, 2048, 0, 1);
      this.texture = new ByteTexture(2048, 2048, this.bitmap, Format.A, Filter.Linear, Filter.Linear);
      this.scale = STBTruetype.stbtt_ScaleForPixelHeight(this.fontInfo, (float)height);
      MemoryStack stack = MemoryStack.stackPush();

      try {
         IntBuffer ascent = stack.mallocInt(1);
         STBTruetype.stbtt_GetFontVMetrics(this.fontInfo, ascent, (IntBuffer)null, (IntBuffer)null);
         this.ascent = (float)ascent.get(0);
      } catch (Throwable var7) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (stack != null) {
         stack.close();
      }

      this.preloadAsciiCharacters();
   }

   private void preloadAsciiCharacters() {
      STBTTPackedchar.Buffer cdata = STBTTPackedchar.create(128);
      STBTTPackRange.Buffer packRange = STBTTPackRange.create(1);
      packRange.put(STBTTPackRange.create().set((float)this.height, 32, (IntBuffer)null, 128, cdata, (byte)2, (byte)2));
      packRange.flip();
      STBTruetype.stbtt_PackFontRanges(this.packContext, this.buffer, 0, packRange);

      for(int i = 0; i < cdata.capacity(); ++i) {
         STBTTPackedchar packedChar = (STBTTPackedchar)cdata.get(i);
         this.putCharData(i + 32, packedChar);
      }

      this.createTexture();
   }

   private void loadCharacter(List<Integer> codePoints) {
      if (System.currentTimeMillis() - this.loadTimer > 100L) {
         this.loadTimer = System.currentTimeMillis();
         this.loadCount = 0;
      }

      if (this.loadCount < 7) {
         for(Integer codePoint : codePoints) {
            this.loadCharacter(codePoint);
         }

         this.createTexture();
         ++this.loadCount;
      }
   }

   private void loadCharacter(int codePoint) {
      if (!this.charMap.containsKey(codePoint)) {
         STBTTPackedchar.Buffer cdata = STBTTPackedchar.create(1);
         STBTTPackRange.Buffer packRange = STBTTPackRange.create(1);
         packRange.put(STBTTPackRange.create().set((float)this.height, codePoint, (IntBuffer)null, 1, cdata, (byte)2, (byte)2));
         packRange.flip();
         STBTruetype.stbtt_PackFontRanges(this.packContext, this.buffer, 0, packRange);
         STBTTPackedchar packedChar = (STBTTPackedchar)cdata.get(0);
         this.putCharData(codePoint, packedChar);
         this.packedChars.put(codePoint, packedChar);
      }
   }

   private void putCharData(int codePoint, STBTTPackedchar packedChar) {
      float ipw = 4.8828125E-4F;
      float iph = 4.8828125E-4F;
      this.charMap.put(codePoint, new CharData(packedChar.xoff(), packedChar.yoff(), packedChar.xoff2(), packedChar.yoff2(), (float)packedChar.x0() * ipw, (float)packedChar.y0() * iph, (float)packedChar.x1() * ipw, (float)packedChar.y1() * iph, packedChar.xadvance()));
   }

   private void createTexture() {
      this.texture = new ByteTexture(2048, 2048, this.bitmap, Format.A, Filter.Linear, Filter.Linear);
   }

   public double getWidth(String string, int length) {
      double width = (double)0.0F;
      if (this.tryLoadString(string)) {
         return width;
      } else {
         for(int i = 0; i < length; ++i) {
            int cp = string.charAt(i);
            CharData c = (CharData)this.charMap.get(cp);
            if (c != null) {
               width += (double)c.xAdvance;
            }
         }

         return width;
      }
   }

   public int getHeight() {
      return this.height;
   }

   private boolean tryLoadString(String s) {
      boolean isLoading = false;
      List<Integer> charPoints = null;

      for(int i = 0; i < s.length(); ++i) {
         int cp = s.charAt(i);
         CharData c = (CharData)this.charMap.get(cp);
         if (c == null) {
            if (charPoints == null) {
               charPoints = new ArrayList();
            }

            charPoints.add(cp);
            isLoading = true;
         }
      }

      if (charPoints != null) {
         this.loadCharacter(charPoints);
      }

      return isLoading;
   }

   public double render(Mesh mesh, String string, double x, double y, Color color, double scale) {
      if (this.tryLoadString(string)) {
         return x;
      } else {
         y += (double)(this.ascent * this.scale) * scale;

         for(int i = 0; i < string.length(); ++i) {
            int cp = string.charAt(i);
            CharData c = (CharData)this.charMap.get(cp);
            if (c != null) {
               mesh.quad(mesh.vec2(x + (double)c.x0 * scale, y + (double)c.y0 * scale).vec2((double)c.u0, (double)c.v0).color(color).next(), mesh.vec2(x + (double)c.x0 * scale, y + (double)c.y1 * scale).vec2((double)c.u0, (double)c.v1).color(color).next(), mesh.vec2(x + (double)c.x1 * scale, y + (double)c.y1 * scale).vec2((double)c.u1, (double)c.v1).color(color).next(), mesh.vec2(x + (double)c.x1 * scale, y + (double)c.y0 * scale).vec2((double)c.u1, (double)c.v0).color(color).next());
               x += (double)c.xAdvance * scale;
            }
         }

         return x;
      }
   }

   private static record CharData(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, float xAdvance) {
   }
}
