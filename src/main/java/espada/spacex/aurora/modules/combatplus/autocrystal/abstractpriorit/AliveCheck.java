package espada.spacex.aurora.modules.combatplus.autocrystal.abstractpriorit;

import espada.spacex.aurora.utils.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.Box;

public class AliveCheck {
   public static boolean isAlive(Box box) {
      if (box == null) {
         return true;
      } else {
         for(Entity en : Util.mc.world.getEntities()) {
            if (en instanceof EndCrystalEntity && bbEquals(box, en.getBoundingBox())) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean bbEquals(Box box1, Box box2) {
      return box1.minX == box2.minX && box1.minY == box2.minY && box1.minZ == box2.minZ && box1.maxX == box2.maxX && box1.maxY == box2.maxY && box1.maxZ == box2.maxZ;
   }
}
