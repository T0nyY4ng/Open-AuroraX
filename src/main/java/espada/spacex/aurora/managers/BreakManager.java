package espada.spacex.aurora.managers;

import espada.spacex.aurora.modules.combatplus.automine.AuroraMine;
import java.util.HashMap;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;

public class BreakManager {
   public final Map<String, BlockPos> map = new HashMap();

   public BreakManager() {
      MeteorClient.EVENT_BUS.subscribe(this);
   }

   public boolean isMine(BlockPos pos, boolean self) {
      for(Map.Entry<String, BlockPos> block : this.map.entrySet()) {
         if (((BlockPos)block.getValue()).equals(pos)) {
            return true;
         }
      }

      return self && Modules.get().isActive(AuroraMine.class) && pos.equals(((AuroraMine)Modules.get().get(AuroraMine.class)).targetPos());
   }

   @EventHandler(
      priority = 200
   )
   private void onReceive(PacketEvent.Receive event) {
      Packet var3 = event.packet;
      if (var3 instanceof BlockBreakingProgressS2CPacket p) {
         Entity entity = MeteorClient.mc.world.getEntityById(p.getEntityId());
         PlayerEntity breaker = entity == null ? null : (PlayerEntity)entity;
         if (breaker == null) {
            return;
         }

         this.map.put(breaker.getGameProfile().getName(), p.getPos());
      }

   }
}
