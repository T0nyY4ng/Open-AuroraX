package espada.spacex.aurora.mixins;

import espada.spacex.aurora.utils.meteor.FontFix;
import java.nio.ByteBuffer;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Mesh;
import meteordevelopment.meteorclient.renderer.text.CustomTextRenderer;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(
   value = {CustomTextRenderer.class},
   remap = false
)
public abstract class CustomTextRendererMixin implements TextRenderer {
   @Shadow
   @Final
   private Mesh mesh;
   @Shadow
   private boolean building;
   @Shadow
   private boolean scaleOnly;
   @Shadow
   private double fontScale = (double)1.0F;
   @Shadow
   private double scale = (double)1.0F;
   @Unique
   FontFix[] fixedFonts = new FontFix[5];
   @Unique
   private FontFix fixedFont;

   @Inject(
      method = {"<init>"},
      at = {@At("RETURN")},
      locals = LocalCapture.CAPTURE_FAILHARD
   )
   public void onInit(FontFace fontFace, CallbackInfo ci, byte[] bytes, ByteBuffer buffer) {
      for(int i = 0; i < this.fixedFonts.length; ++i) {
         this.fixedFonts[i] = new FontFix(buffer, (int)Math.round((double)27.0F * ((double)i * (double)0.5F + (double)1.0F)));
      }

   }

   @Overwrite
   public double getWidth(String text, int length, boolean shadow) {
      if (text.isEmpty()) {
         return (double)0.0F;
      } else {
         FontFix font = this.building ? this.fixedFont : this.fixedFonts[0];
         return (font.getWidth(text, length) + (double)(shadow ? 1 : 0)) * this.scale / (double)1.5F;
      }
   }

   @Overwrite
   public double getHeight(boolean shadow) {
      FontFix font = this.building ? this.fixedFont : this.fixedFonts[0];
      return (double)(font.getHeight() + 1 + (shadow ? 1 : 0)) * this.scale / (double)1.5F;
   }

   @Overwrite
   public void begin(double scale, boolean scaleOnly, boolean big) {
      if (this.building) {
         throw new RuntimeException("CustomTextRenderer.begin() called twice");
      } else {
         if (!scaleOnly) {
            this.mesh.begin();
         }

         if (big) {
            this.fixedFont = this.fixedFonts[this.fixedFonts.length - 1];
         } else {
            double scaleA = Math.floor(scale * (double)10.0F) / (double)10.0F;
            int scaleI;
            if (scaleA >= (double)3.0F) {
               scaleI = 5;
            } else if (scaleA >= (double)2.5F) {
               scaleI = 4;
            } else if (scaleA >= (double)2.0F) {
               scaleI = 3;
            } else if (scaleA >= (double)1.5F) {
               scaleI = 2;
            } else {
               scaleI = 1;
            }

            this.fixedFont = this.fixedFonts[scaleI - 1];
         }

         this.building = true;
         this.scaleOnly = scaleOnly;
         this.fontScale = (double)this.fixedFont.getHeight() / (double)27.0F;
         this.scale = (double)1.0F + (scale - this.fontScale) / this.fontScale;
      }
   }

   @Overwrite
   public void end(MatrixStack matrices) {
      if (!this.building) {
         throw new RuntimeException("CustomTextRenderer.end() called without calling begin()");
      } else {
         if (!this.scaleOnly) {
            this.mesh.end();
            GL.bindTexture(this.fixedFont.texture.getGlId());
            this.mesh.render(matrices);
         }

         this.building = false;
         this.scale = (double)1.0F;
      }
   }

   @Overwrite
   public double render(String text, double x, double y, Color color, boolean shadow) {
      boolean wasBuilding = this.building;
      if (!wasBuilding) {
         this.begin();
      }

      double width;
      if (shadow) {
         int preShadowA = CustomTextRenderer.SHADOW_COLOR.a;
         CustomTextRenderer.SHADOW_COLOR.a = (int)((double)color.a / (double)255.0F * (double)preShadowA);
         width = this.fixedFont.render(this.mesh, text, x + this.fontScale * this.scale / (double)1.5F, y + this.fontScale * this.scale / (double)1.5F, CustomTextRenderer.SHADOW_COLOR, this.scale / (double)1.5F);
         this.fixedFont.render(this.mesh, text, x, y, color, this.scale / (double)1.5F);
         CustomTextRenderer.SHADOW_COLOR.a = preShadowA;
      } else {
         width = this.fixedFont.render(this.mesh, text, x, y, color, this.scale / (double)1.5F);
      }

      if (!wasBuilding) {
         this.end();
      }

      return width;
   }
}
