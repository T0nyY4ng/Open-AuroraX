package espada.spacex.aurora.managers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class OnGroundManager {
   private boolean onGround;

   public OnGroundManager() {
      MeteorClient.EVENT_BUS.subscribe(this);
      this.onGround = false;
   }

   @EventHandler(
      priority = 200
   )
   private void onPacket(PacketEvent.Send event) {
      if (event.packet instanceof PlayerMoveC2SPacket) {
         this.onGround = ((PlayerMoveC2SPacket)event.packet).isOnGround();
      }

   }

   public boolean isOnGround() {
      return this.onGround;
   }
}
