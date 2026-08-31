package espada.spacex.aurora.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class EntityInfo {
   public static BlockPos playerPos(PlayerEntity targetEntity) {
      return new BlockPos((int)Math.floor(targetEntity.getX()), (int)Math.round(targetEntity.getY()), (int)Math.floor(targetEntity.getZ()));
   }

   public static boolean CrystalCheck(BlockPos pos) {
      for(Entity entity : MeteorClient.mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
         if (entity instanceof EndCrystalEntity) {
            return true;
         }
      }

      return false;
   }
}
