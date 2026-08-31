package espada.spacex.aurora.modules.combatplus.autocrystal.abstractpriorit;

import espada.spacex.aurora.utils.Util;
import net.minecraft.block.AirBlock;
import net.minecraft.util.math.BlockPos;

public class AirCheck {
   public static boolean air(BlockPos pos) {
      return Util.mc.world.getBlockState(pos).getBlock() instanceof AirBlock;
   }
}
