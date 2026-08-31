package espada.spacex.aurora.pathing;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Settings;
import net.minecraft.entity.Entity;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class NopPathManager implements IPathManager {
   private final NopSettings settings = new NopSettings();

   public String getName() {
      return "none";
   }

   public boolean isPathing() {
      return false;
   }

   public void pause() {
   }

   public void resume() {
   }

   public void stop() {
   }

   public void moveTo(BlockPos pos, boolean ignoreY) {
   }

   public void moveInDirection(float yaw) {
   }

   public void mine(Block... blocks) {
   }

   public void follow(Predicate<Entity> entity) {
   }

   public float getTargetYaw() {
      return 0.0F;
   }

   public float getTargetPitch() {
      return 0.0F;
   }

   public IPathManager.ISettings getSettings() {
      return this.settings;
   }

   private static class NopSettings implements IPathManager.ISettings {
      private final Settings settings = new Settings();
      private final Setting<Boolean> setting = (new BoolSetting.Builder()).build();

      public Settings get() {
         return this.settings;
      }

      public Setting<Boolean> getWalkOnWater() {
         this.setting.reset();
         return this.setting;
      }

      public Setting<Boolean> getWalkOnLava() {
         this.setting.reset();
         return this.setting;
      }

      public Setting<Boolean> getStep() {
         this.setting.reset();
         return this.setting;
      }

      public Setting<Boolean> getNoFall() {
         this.setting.reset();
         return this.setting;
      }

      public void save() {
      }
   }
}
