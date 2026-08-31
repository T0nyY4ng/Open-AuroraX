package espada.spacex.aurora.mixins;

import net.minecraft.util.math.BlockPos;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({ClientPlayerInteractionManager.class})
public interface IClientPlayerInteractionManager {
   @Accessor("currentBreakingProgress")
   float getBreakingProgress();

   @Accessor("currentBreakingProgress")
   void setCurrentBreakingProgress(float var1);

   @Accessor("currentBreakingPos")
   BlockPos getCurrentBreakingBlockPos();

   @Invoker("sendSequencedPacket")
   void aurora$sendSequencedPacket(ClientWorld world, SequencedPacketCreator packetCreator);
}
