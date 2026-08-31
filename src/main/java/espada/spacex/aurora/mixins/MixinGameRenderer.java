package espada.spacex.aurora.mixins;

import espada.spacex.aurora.modules.renderplus.AspectRatio;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({GameRenderer.class})
public abstract class MixinGameRenderer {
   @Shadow
   private float zoom;
   @Shadow
   private float zoomX;
   @Shadow
   private float zoomY;
   @Shadow
   private float viewDistance;

   @Inject(
      method = {"getBasicProjectionMatrix"},
      at = {@At("TAIL")},
      cancellable = true
   )
   public void getBasicProjectionMatrixHook(double fov, CallbackInfoReturnable<Matrix4f> cir) {
      if (((AspectRatio)Modules.get().get(AspectRatio.class)).isActive()) {
         MatrixStack matrixStack = new MatrixStack();
         matrixStack.peek().getPositionMatrix().identity();
         if (this.zoom != 1.0F) {
            matrixStack.translate(this.zoomX, -this.zoomY, 0.0F);
            matrixStack.scale(this.zoom, this.zoom, 1.0F);
         }

         matrixStack.peek().getPositionMatrix().mul((new Matrix4f()).setPerspective((float)(fov * (double)((float)Math.PI / 180F)), ((Double)((AspectRatio)Modules.get().get(AspectRatio.class)).ratio.get()).floatValue(), 0.05F, this.viewDistance * 4.0F));
         cir.setReturnValue(matrixStack.peek().getPositionMatrix());
      }

   }
}
