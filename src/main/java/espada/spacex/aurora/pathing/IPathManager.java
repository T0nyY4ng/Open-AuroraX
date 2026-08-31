package espada.spacex.aurora.pathing;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Settings;
import net.minecraft.entity.Entity;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public interface IPathManager {
   String getName();

   boolean isPathing();

   void pause();

   void resume();

   void stop();

   default void moveTo(BlockPos pos) {
      this.moveTo(pos, false);
   }

   void moveTo(BlockPos var1, boolean var2);

   void moveInDirection(float var1);

   void mine(Block... var1);

   void follow(Predicate<Entity> var1);

   float getTargetYaw();

   float getTargetPitch();

   ISettings getSettings();

   public interface ISettings {
      Settings get();

      Setting<Boolean> getWalkOnWater();

      Setting<Boolean> getWalkOnLava();

      Setting<Boolean> getStep();

      Setting<Boolean> getNoFall();

      void save();
   }
}
