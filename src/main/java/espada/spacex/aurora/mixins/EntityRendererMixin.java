package espada.spacex.aurora.mixins;

import espada.spacex.aurora.modules.renderplus.NewNameTags;
import meteordevelopment.meteorclient.mixininterface.IEntityRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityRenderer.class})
public abstract class EntityRendererMixin<T extends Entity> implements IEntityRenderer {
   @Inject(
      method = {"renderLabelIfPresent"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderLabel(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo info) {
      if (entity instanceof PlayerEntity) {
         if (((NewNameTags)Modules.get().get(NewNameTags.class)).playerNametags() && (EntityUtils.getGameMode((PlayerEntity)entity) != null || !((NewNameTags)Modules.get().get(NewNameTags.class)).excludeBots())) {
            info.cancel();
         }

      }
   }
}
