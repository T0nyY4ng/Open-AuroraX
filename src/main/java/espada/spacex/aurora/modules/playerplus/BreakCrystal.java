package espada.spacex.aurora.modules.playerplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.SettingUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class BreakCrystal extends Modules {
   private Entity crystal;
   private final boolean autoDisable = false;

   public BreakCrystal() {
      super(Aurora.PlayerPlus, "BreakCrystal", "Automatically breaks nearby end crystals.");
   }

   @EventHandler(
      priority = 200
   )
   private void onTick(TickEvent.Pre event) {
      this.crystal = this.getBlocking();
      if (this.crystal != null) {
         this.sendPacket(PlayerInteractEntityC2SPacket.attack(this.crystal, this.mc.player.isSneaking()));
      }
   }

   private Entity getBlocking() {
      Entity crystal = null;
      double lowest = (double)1000.0F;

      for(Entity entity : this.mc.world.getEntities()) {
         if (entity instanceof EndCrystalEntity && !(this.mc.player.distanceTo(entity) > 5.0F) && SettingUtils.inAttackRange(entity.getBoundingBox())) {
            crystal = entity;
         }
      }

      return crystal;
   }
}
