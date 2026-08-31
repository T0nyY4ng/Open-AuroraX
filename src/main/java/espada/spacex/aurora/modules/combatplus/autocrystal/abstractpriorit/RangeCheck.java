package espada.spacex.aurora.modules.combatplus.autocrystal.abstractpriorit;

import espada.spacex.aurora.utils.SettingUtils;
import net.minecraft.util.math.BlockPos;

public class RangeCheck {
   public static boolean inPlaceRange(BlockPos pos) {
      return SettingUtils.inPlaceRange(pos);
   }
}
