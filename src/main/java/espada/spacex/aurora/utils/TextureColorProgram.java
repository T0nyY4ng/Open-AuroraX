package espada.spacex.aurora.utils;

import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.render.VertexFormats;

public class TextureColorProgram extends GlProgram {
   public TextureColorProgram() {
      super("position_tex_color2", VertexFormats.POSITION);
   }

   public void setParameters(float x, float y, float width, float height, float radius, Color color) {
      int i = (Integer)this.mc.options.getGuiScale().getValue();
   }

   public void use() {
      super.use();
   }

   protected void setup() {
   }
}
