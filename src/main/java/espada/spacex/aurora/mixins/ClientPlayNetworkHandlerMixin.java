package espada.spacex.aurora.mixins;

import espada.spacex.aurora.events.SendCommandEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public abstract class ClientPlayNetworkHandlerMixin {
   @Inject(
      method = {"sendChatCommand"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendCommand(String message, CallbackInfo ci) {
      SendCommandEvent var10000 = (SendCommandEvent)MeteorClient.EVENT_BUS.post(SendCommandEvent.get(message));
   }
}
