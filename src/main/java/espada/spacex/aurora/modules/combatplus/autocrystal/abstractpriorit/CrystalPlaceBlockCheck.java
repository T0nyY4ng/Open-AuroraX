package espada.spacex.aurora.modules.combatplus.autocrystal.abstractpriorit;

import espada.spacex.aurora.utils.Util;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class CrystalPlaceBlockCheck {
   public static boolean crystalBlock(BlockPos pos) {
      return Util.mc.world.getBlockState(pos).getBlock().equals(Blocks.OBSIDIAN) || Util.mc.world.getBlockState(pos).getBlock().equals(Blocks.BEDROCK);
   }
}
