package espada.spacex.aurora.mixins;

import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.modules.movementplus.TickShift;
import espada.spacex.aurora.modules.renderplus.SwingModifier;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import net.minecraft.util.Hand;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayerEntity.class})
public abstract class MixinClientPlayerEntity {
   @Shadow
   @Final
   public ClientPlayNetworkHandler networkHandler;
   @Unique
   private static boolean sent = false;

   @Inject(
      method = {"swingHand(Lnet/minecraft/util/Hand;)V"},
      at = {@At("HEAD")}
   )
   private void swingHand(Hand hand, CallbackInfo ci) {
      ((SwingModifier)Modules.get().get(SwingModifier.class)).startSwing(hand);
   }

   @Inject(
      method = {"sendMovementPackets"},
      at = {@At("HEAD")}
   )
   private void sendPacketsHead(CallbackInfo ci) {
      sent = false;
   }

   @Inject(
      method = {"sendMovementPackets"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
)}
   )
   private void onSendPacket(CallbackInfo ci) {
      sent = true;
   }

   @Inject(
      method = {"sendMovementPackets"},
      at = {@At("TAIL")}
   )
   private void sendPacketsTail(CallbackInfo ci) {
      TickShift tickShift = (TickShift)Modules.get().get(TickShift.class);
      if (tickShift.isActive() && !((Keybind)tickShift.Key.get()).isPressed()) {
         tickShift.unSent = Math.min((Integer)tickShift.packets.get(), tickShift.unSent + 1);
      }

   }

   @Redirect(
      method = {"sendMovementPackets"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V",
   ordinal = 2
)
   )
   private void sendPacketFull(ClientPlayNetworkHandler instance, Packet<?> packet) {
      this.networkHandler.sendPacket(Managers.ROTATION.onFull((PlayerMoveC2SPacket.Full)packet));
   }

   @Redirect(
      method = {"sendMovementPackets"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V",
   ordinal = 3
)
   )
   private void sendPacketPosGround(ClientPlayNetworkHandler instance, Packet<?> packet) {
      this.networkHandler.sendPacket(Managers.ROTATION.onPositionOnGround((PlayerMoveC2SPacket.PositionAndOnGround)packet));
   }

   @Redirect(
      method = {"sendMovementPackets"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V",
   ordinal = 4
)
   )
   private void sendPacketLookGround(ClientPlayNetworkHandler instance, Packet<?> packet) {
      PlayerMoveC2SPacket toSend = Managers.ROTATION.onLookAndOnGround((PlayerMoveC2SPacket.LookAndOnGround)packet);
      if (toSend != null) {
         this.networkHandler.sendPacket(toSend);
      }

   }

   @Redirect(
      method = {"sendMovementPackets"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V",
   ordinal = 5
)
   )
   private void sendPacketGround(ClientPlayNetworkHandler instance, Packet<?> packet) {
      this.networkHandler.sendPacket(Managers.ROTATION.onOnlyOnground((PlayerMoveC2SPacket.OnGroundOnly)packet));
   }
}
