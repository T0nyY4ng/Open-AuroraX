package espada.spacex.aurora.utils;

import espada.spacex.aurora.enums.HoleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Hole {
   public final BlockPos pos;
   public final HoleType type;
   public final BlockPos[] positions;
   public final Vec3d middle;

   public Hole(BlockPos pos, HoleType type) {
      this.pos = pos;
      this.type = type;
      switch (type) {
         case Single:
            this.positions = new BlockPos[]{pos};
            this.middle = new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY(), (double)pos.getZ() + (double)0.5F);
            break;
         case DoubleX:
            this.positions = new BlockPos[]{pos, pos.add(1, 0, 0)};
            this.middle = new Vec3d((double)(pos.getX() + 1), (double)pos.getY(), (double)pos.getZ() + (double)0.5F);
            break;
         case DoubleZ:
            this.positions = new BlockPos[]{pos, pos.add(0, 0, 1)};
            this.middle = new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY(), (double)(pos.getZ() + 1));
            break;
         case Quad:
            this.positions = new BlockPos[]{pos, pos.add(1, 0, 0), pos.add(0, 0, 1), pos.add(1, 0, 1)};
            this.middle = new Vec3d((double)(pos.getX() + 1), (double)pos.getY(), (double)(pos.getZ() + 1));
            break;
         default:
            this.positions = new BlockPos[0];
            this.middle = new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY(), (double)pos.getZ() + (double)0.5F);
      }

   }

   public BlockPos[] positions() {
      return this.positions;
   }
}
