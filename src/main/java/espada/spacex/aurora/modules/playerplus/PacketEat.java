package espada.spacex.aurora.modules.playerplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;

public class PacketEat extends Modules {
   private ItemStack PackEatItemStack;

   public PacketEat() {
      super(Aurora.PlayerPlus, "PacketEat", "PackEat");
   }

   @EventHandler
   public void onTick(TickEvent.Post event) {
      if (this.mc.player != null && this.mc.player.isUsingItem()) {
         this.PackEatItemStack = this.mc.player.getActiveItem();
      }

   }

   @EventHandler
   public void onPacket(PacketEvent.Send event) {
      try {
         Packet var3 = event.packet;
         if (var3 instanceof PlayerActionC2SPacket packet) {
            if (packet.getAction() == Action.RELEASE_USE_ITEM && this.PackEatItemStack != null && this.PackEatItemStack.get(DataComponentTypes.FOOD) != null && ((FoodComponent)this.PackEatItemStack.get(DataComponentTypes.FOOD)).canAlwaysEat()) {
               event.cancel();
            }
         }
      } catch (Exception var4) {
      }

   }
}
