package espada.spacex.aurora.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import espada.spacex.aurora.utils.RenderUtils;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public class GameRendererMixin {
   @Inject(
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/render/Camera;FLorg/joml/Matrix4f;)V"
)},
      method = {"renderWorld"}
   )
   void render3dHook(RenderTickCounter tickCounter, CallbackInfo ci) {
      RenderUtils.lastProjMat.set(RenderSystem.getProjectionMatrix());
      RenderUtils.lastModMat.set(RenderSystem.getModelViewMatrix());
   }
}
