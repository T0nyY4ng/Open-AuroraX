package espada.spacex.aurora.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class MathUtil {
   public static Vec3d interpolateEntity(Entity entity, float time) {
      return new Vec3d(entity.lastRenderX + (entity.getX() - entity.lastRenderX) * (double)time, entity.lastRenderY + (entity.getY() - entity.lastRenderY) * (double)time, entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * (double)time);
   }
}
