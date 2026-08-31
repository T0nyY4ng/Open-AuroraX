package espada.spacex.aurora.utils;

import espada.spacex.aurora.managers.Managers;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class BOInvUtils {
   private static int[] slots;
   public static int pickSlot = -1;

   public static boolean pickSwitch(int slot) {
      if (slot >= 0) {
         Managers.HOLDING.modifyStartTime = System.currentTimeMillis();
         pickSlot = slot;
         MeteorClient.mc.getNetworkHandler().sendPacket(new PickFromInventoryC2SPacket(slot));
         return true;
      } else {
         return false;
      }
   }

   public static void pickSwapBack() {
      if (pickSlot >= 0) {
         MeteorClient.mc.getNetworkHandler().sendPacket(new PickFromInventoryC2SPacket(pickSlot));
         pickSlot = -1;
      }

   }

   public static boolean invSwitch(int slot) {
      if (slot >= 0) {
         ScreenHandler handler = MeteorClient.mc.player.currentScreenHandler;
         Int2ObjectArrayMap<ItemStack> stack = new Int2ObjectArrayMap();
         stack.put(slot, handler.getSlot(slot).getStack());
         MeteorClient.mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(handler.syncId, handler.getRevision(), 36 + Managers.HOLDING.slot, slot, SlotActionType.SWAP, handler.getSlot(slot).getStack(), stack));
         MeteorClient.mc.player.getInventory().updateItems();
         slots = new int[]{slot, Managers.HOLDING.slot};
         return true;
      } else {
         return false;
      }
   }

   public static void swapBack() {
      ScreenHandler handler = MeteorClient.mc.player.currentScreenHandler;
      Int2ObjectArrayMap<ItemStack> stack = new Int2ObjectArrayMap();
      stack.put(slots[0], handler.getSlot(slots[0]).getStack());
      MeteorClient.mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(handler.syncId, handler.getRevision(), 36 + slots[1], slots[0], SlotActionType.SWAP, handler.getSlot(slots[0]).getStack().copy(), stack));
      MeteorClient.mc.player.getInventory().updateItems();
   }

   public static void invSwapBack() {
      ScreenHandler handler = MeteorClient.mc.player.currentScreenHandler;
      Int2ObjectArrayMap<ItemStack> stack = new Int2ObjectArrayMap();
      stack.put(slots[0], handler.getSlot(slots[0]).getStack());
      MeteorClient.mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(handler.syncId, handler.getRevision(), 36 + slots[1], slots[0], SlotActionType.SWAP, handler.getSlot(slots[0]).getStack().copy(), stack));
      MeteorClient.mc.player.getInventory().updateItems();
   }

   public static int findHotbarBlock(Block blockIn) {
      for(int i = 0; i < 9; ++i) {
         ItemStack stack = MeteorClient.mc.player.getInventory().getStack(i);
         if (stack != ItemStack.EMPTY && stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == blockIn) {
            return i;
         }
      }

      return -1;
   }

   public static void doSwap(int slot) {
      MeteorClient.mc.player.getInventory().selectedSlot = slot;
      MeteorClient.mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
      MeteorClient.mc.player.getInventory().updateItems();
   }
}
