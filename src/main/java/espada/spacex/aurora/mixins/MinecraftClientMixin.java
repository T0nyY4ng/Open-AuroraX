package espada.spacex.aurora.mixins;

import espada.spacex.aurora.modules.globalsettings.RSRClientPlusTitle;
import espada.spacex.aurora.modules.playerplus.MultiTasks;
import meteordevelopment.meteorclient.mixininterface.IMinecraftClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
   value = {MinecraftClient.class},
   priority = 1001
)
public abstract class MinecraftClientMixin implements IMinecraftClient {
   @ModifyArg(
      method = {"updateWindowTitle"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/util/Window;setTitle(Ljava/lang/String;)V"
)
   )
   private String setTitle(String original) {
      String var10000 = Modules.get() != null && Modules.get().get(RSRClientPlusTitle.class) != null && ((RSRClientPlusTitle)Modules.get().get(RSRClientPlusTitle.class)).isActive() ? "Welcome use RSRClientPlus UserName : " : "Welcome use Aurora User version UserName : ";
      return var10000 + MinecraftClient.getInstance().getSession().getUsername();
   }

   @Redirect(
      method = {"handleBlockBreaking"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"
),
      require = 0
   )
   public boolean breakBlock(ClientPlayerEntity clientPlayer) {
      MultiTasks multiTasks = (MultiTasks)Modules.get().get(MultiTasks.class);
      return multiTasks.isActive() ? false : clientPlayer.isUsingItem();
   }
}
