package espada.spacex.aurora.mixins;

import espada.spacex.aurora.events.Render2DEvent;
import espada.spacex.aurora.modules.renderplus.AttackIndicator;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InGameHud.class})
public class MixinInGameHud {
   @Inject(
      method = {"render"},
      at = {@At("TAIL")}
   )
   private void onRenderTail(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      Render2DEvent.Render2DEvent(context);
      MeteorClient.EVENT_BUS.post(Render2DEvent.Render2DEvent(context));
      AttackIndicator attackIndicator = (AttackIndicator)Modules.get().get(AttackIndicator.class);
      if (attackIndicator.isActive()) {
         MinecraftClient mc = MinecraftClient.getInstance();
         attackIndicator.render(context, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
      }

   }
}
