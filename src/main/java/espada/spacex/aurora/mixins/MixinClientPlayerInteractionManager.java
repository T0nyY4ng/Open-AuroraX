package espada.spacex.aurora.mixins;

import espada.spacex.aurora.modules.combatplus.automine.AuroraMine;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.network.packet.Packet;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.SequencedPacketCreator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientPlayerInteractionManager.class})
public abstract class MixinClientPlayerInteractionManager {
   @Shadow
   @Final
   private MinecraftClient client;
   @Shadow
   private float blockBreakingSoundCooldown;
   @Shadow
   private float currentBreakingProgress;
   @Shadow
   private ItemStack selectedStack;
   @Shadow
   private BlockPos currentBreakingPos;
   @Shadow
   private boolean breakingBlock;
   private Direction dir;
   @Unique
   private BlockPos position = null;

   @Shadow
   public abstract void sendSequencedPacket(ClientWorld var1, SequencedPacketCreator var2);

   @Shadow
   public abstract boolean breakBlock(BlockPos var1);

   @Shadow
   public abstract int getBlockBreakingProgress();

   @Inject(
      method = {"attackBlock"},
      at = {@At("HEAD")}
   )
   private void onAttack(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
      this.dir = direction;
      this.position = pos;
   }

   @Redirect(
      method = {"attackBlock"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V",
   ordinal = 1
)
   )
   private void onStart(ClientPlayerInteractionManager instance, ClientWorld world, SequencedPacketCreator packetCreator) {
      AuroraMine autoMine = (AuroraMine)Modules.get().get(AuroraMine.class);
      if (!autoMine.isActive()) {
         this.sendSequencedPacket(world, packetCreator);
      } else {
         BlockState blockState = world.getBlockState(this.position);
         boolean bl = !blockState.isAir();
         if (bl && this.currentBreakingProgress == 0.0F) {
            blockState.onBlockBreakStart(this.client.world, this.position, this.client.player);
         }

         if (bl && blockState.calcBlockBreakingDelta(this.client.player, this.client.player.getWorld(), this.position) >= 1.0F) {
            this.breakBlock(this.position);
         } else {
            this.breakingBlock = true;
            this.currentBreakingPos = this.position;
            this.selectedStack = this.client.player.getMainHandStack();
            this.currentBreakingProgress = 0.0F;
            this.blockBreakingSoundCooldown = 0.0F;
            this.client.world.setBlockBreakingInfo(this.client.player.getId(), this.currentBreakingPos, this.getBlockBreakingProgress());
         }

         autoMine.onStart(this.position, this.dir);
      }
   }

   @Redirect(
      method = {"attackBlock"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V",
   ordinal = 0
)
   )
   private void onAbort(ClientPlayNetworkHandler instance, Packet<?> packet) {
      AuroraMine autoMine = (AuroraMine)Modules.get().get(AuroraMine.class);
      if (!autoMine.isActive()) {
         instance.sendPacket(packet);
      } else {
         autoMine.onAbort(this.position);
      }
   }

   @Inject(
      method = {"updateBlockBreakingProgress"},
      at = {@At("HEAD")}
   )
   private void onUpdateProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
      this.position = pos;
      this.dir = direction;
   }

   @Redirect(
      method = {"updateBlockBreakingProgress"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V",
   ordinal = 1
)
   )
   private void onStop(ClientPlayerInteractionManager instance, ClientWorld world, SequencedPacketCreator packetCreator) {
      AuroraMine autoMine = (AuroraMine)Modules.get().get(AuroraMine.class);
      if (!autoMine.isActive()) {
         this.sendSequencedPacket(world, packetCreator);
      } else {
         autoMine.onStop();
      }
   }

   @Redirect(
      method = {"cancelBlockBreaking"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
)
   )
   private void cancel(ClientPlayNetworkHandler instance, Packet<?> packet) {
      AuroraMine autoMine = (AuroraMine)Modules.get().get(AuroraMine.class);
      if (!autoMine.isActive()) {
         instance.sendPacket(packet);
      } else {
         autoMine.onAbort(this.currentBreakingPos);
      }
   }
}
