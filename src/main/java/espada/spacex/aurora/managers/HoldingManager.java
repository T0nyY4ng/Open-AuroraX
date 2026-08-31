package espada.spacex.aurora.managers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class HoldingManager {
   public int slot;
   public long modifyStartTime = 0L;

   public HoldingManager() {
      MeteorClient.EVENT_BUS.subscribe(this);
      this.slot = 0;
   }

   @EventHandler(
      priority = 200
   )
   private void onSend(PacketEvent.Send event) {
      Packet var3 = event.packet;
      if (var3 instanceof UpdateSelectedSlotC2SPacket packet) {
         if (packet.getSelectedSlot() >= 0 && packet.getSelectedSlot() <= 8) {
            this.slot = packet.getSelectedSlot();
         }
      }

   }

   public ItemStack getStack() {
      return MeteorClient.mc.player == null ? null : MeteorClient.mc.player.getInventory().getStack(this.slot);
   }

   public int getSlot() {
      return this.slot;
   }

   public boolean isHolding(Item... items) {
      ItemStack stack = this.getStack();
      if (stack == null) {
         return false;
      } else {
         for(Item item : items) {
            if (item.equals(stack.getItem())) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isHolding(Item item) {
      ItemStack stack = this.getStack();
      return stack == null ? false : stack.getItem().equals(item);
   }
}
