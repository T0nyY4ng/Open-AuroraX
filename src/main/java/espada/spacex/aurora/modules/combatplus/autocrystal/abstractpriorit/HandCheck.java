package espada.spacex.aurora.modules.combatplus.autocrystal.abstractpriorit;

import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.utils.Util;
import java.util.function.Predicate;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;

public class HandCheck {
   public static Hand getHand(Predicate<ItemStack> predicate) {
      return predicate.test(Managers.HOLDING.getStack()) ? Hand.MAIN_HAND : (predicate.test(Util.mc.player.getOffHandStack()) ? Hand.OFF_HAND : null);
   }
}
