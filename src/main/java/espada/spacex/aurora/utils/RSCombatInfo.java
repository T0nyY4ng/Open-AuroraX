package espada.spacex.aurora.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class RSCombatInfo {
   public static Box getCrystalBox(BlockPos pos) {
      return new Box((double)pos.getX() - (double)0.5F, (double)pos.getY(), (double)pos.getZ() - (double)0.5F, (double)pos.getX() + (double)1.5F, (double)(pos.getY() + 2), (double)pos.getZ() + (double)1.5F);
   }

   public static Box getCrystalBox(Vec3d pos) {
      return new Box(pos.getX() - (double)1.0F, pos.getY(), pos.getZ() - (double)1.0F, pos.getX() + (double)1.0F, pos.getY() + (double)2.0F, pos.getZ() + (double)1.0F);
   }
}
