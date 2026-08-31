package espada.spacex.aurora.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ArmorUtil {
   public static boolean checkThreshold(ItemStack i, double threshold) {
      return getDamage(i) <= threshold;
   }

   public static double getDamage(ItemStack i) {
      return (double)(i.getMaxDamage() - i.getDamage()) / (double)i.getMaxDamage() * (double)100.0F;
   }

   public static ItemStack getArmor(int slot) {
      return (ItemStack)MeteorClient.mc.player.getInventory().armor.get(slot);
   }

   public static boolean isHelm(ItemStack itemStack) {
      if (itemStack == null) {
         return false;
      } else if (itemStack.getItem() == Items.NETHERITE_HELMET) {
         return true;
      } else if (itemStack.getItem() == Items.DIAMOND_HELMET) {
         return true;
      } else if (itemStack.getItem() == Items.GOLDEN_HELMET) {
         return true;
      } else if (itemStack.getItem() == Items.IRON_HELMET) {
         return true;
      } else if (itemStack.getItem() == Items.CHAINMAIL_HELMET) {
         return true;
      } else {
         return itemStack.getItem() == Items.LEATHER_HELMET;
      }
   }

   public static boolean isChest(ItemStack itemStack) {
      if (itemStack == null) {
         return false;
      } else if (itemStack.getItem() == Items.NETHERITE_CHESTPLATE) {
         return true;
      } else if (itemStack.getItem() == Items.DIAMOND_CHESTPLATE) {
         return true;
      } else if (itemStack.getItem() == Items.GOLDEN_CHESTPLATE) {
         return true;
      } else if (itemStack.getItem() == Items.IRON_CHESTPLATE) {
         return true;
      } else if (itemStack.getItem() == Items.CHAINMAIL_CHESTPLATE) {
         return true;
      } else {
         return itemStack.getItem() == Items.LEATHER_CHESTPLATE;
      }
   }

   public static boolean isLegs(ItemStack itemStack) {
      if (itemStack == null) {
         return false;
      } else if (itemStack.getItem() == Items.NETHERITE_LEGGINGS) {
         return true;
      } else if (itemStack.getItem() == Items.DIAMOND_LEGGINGS) {
         return true;
      } else if (itemStack.getItem() == Items.GOLDEN_LEGGINGS) {
         return true;
      } else if (itemStack.getItem() == Items.IRON_LEGGINGS) {
         return true;
      } else if (itemStack.getItem() == Items.CHAINMAIL_LEGGINGS) {
         return true;
      } else {
         return itemStack.getItem() == Items.LEATHER_LEGGINGS;
      }
   }

   public static boolean isBoots(ItemStack itemStack) {
      if (itemStack == null) {
         return false;
      } else if (itemStack.getItem() == Items.NETHERITE_BOOTS) {
         return true;
      } else if (itemStack.getItem() == Items.DIAMOND_BOOTS) {
         return true;
      } else if (itemStack.getItem() == Items.GOLDEN_BOOTS) {
         return true;
      } else if (itemStack.getItem() == Items.IRON_BOOTS) {
         return true;
      } else if (itemStack.getItem() == Items.CHAINMAIL_BOOTS) {
         return true;
      } else {
         return itemStack.getItem() == Items.LEATHER_BOOTS;
      }
   }
}
