package espada.spacex.aurora.modules.movementplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import java.util.List;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class StepPlus extends Modules {
   private final SettingGroup sgGeneral;
   public final Setting<Boolean> slow;
   private final Setting<Boolean> strict;
   public final Setting<Double> height;
   public final Setting<Double> cooldown;
   public boolean stepping;
   double targetY;
   public int index;
   public double[] currentOffsets;
   public long lastStep;

   public StepPlus() {
      super(Aurora.MovementPlus, "Step+", "Step but works.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.slow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Slow")).description("Moves up slowly to prevent lagbacks.")).defaultValue(false)).build());
      this.strict = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Strict")).description("Strict 2b2tpvp step.")).defaultValue(false)).build());
      this.height = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Height")).description("Starts stepping if target block can be reached in x movement ticks.")).defaultValue((double)2.5F).min(0.6).sliderRange(0.6, (double)2.5F).visible(() -> !(Boolean)this.strict.get())).build());
      this.cooldown = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Cooldown")).description("Waits x seconds between steps.")).defaultValue((double)0.25F).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).build());
      this.stepping = false;
      this.targetY = (double)0.0F;
      this.index = 0;
      this.currentOffsets = null;
      this.lastStep = 0L;
   }

   public void onActivate() {
      this.index = 0;
      this.stepping = false;
      this.currentOffsets = null;
      this.targetY = (double)0.0F;
   }

   public void slowStep(Entity entity, Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
      Box box = entity.getBoundingBox();
      List<VoxelShape> list = entity.getWorld().getEntityCollisions(entity, box.stretch(movement));
      Vec3d vec3d = movement.lengthSquared() == (double)0.0F ? movement : Entity.adjustMovementForCollisions(entity, movement, box, entity.getWorld(), list);
      if (movement.x != vec3d.x || movement.z != vec3d.z || this.stepping) {
         if (entity.isOnGround() && !this.stepping && (double)(System.currentTimeMillis() - this.lastStep) > (Double)this.cooldown.get() * (double)1000.0F) {
            Vec3d vec3d2 = Entity.adjustMovementForCollisions(entity, new Vec3d(movement.x, (Double)this.height.get(), movement.z), box, entity.getWorld(), list);
            Vec3d vec3d3 = Entity.adjustMovementForCollisions(entity, new Vec3d((double)0.0F, (Double)this.height.get(), (double)0.0F), box.stretch(movement.x, (double)0.0F, movement.z), entity.getWorld(), list);
            if (vec3d3.y < (Double)this.height.get()) {
               Vec3d vec3d4 = Entity.adjustMovementForCollisions(entity, new Vec3d(movement.x, (double)0.0F, movement.z), box.offset(vec3d3), entity.getWorld(), list).add(vec3d3);
               if (vec3d4.horizontalLengthSquared() > vec3d2.horizontalLengthSquared()) {
                  vec3d2 = vec3d4;
               }
            }

            if (vec3d2.horizontalLengthSquared() > vec3d.horizontalLengthSquared()) {
               Vec3d vec = vec3d2.add(Entity.adjustMovementForCollisions(entity, new Vec3d((double)0.0F, -vec3d2.y + movement.y, (double)0.0F), box.offset(vec3d2), entity.getWorld(), list));
               double[] o = this.getOffsets(vec.y);
               if (o != null) {
                  this.lastStep = System.currentTimeMillis();
                  this.currentOffsets = o;
                  this.targetY = this.mc.player.getY() + vec.y;
                  this.stepping = true;
                  this.index = -1;
               }
            }
         }

         if (this.stepping && this.currentOffsets != null) {
            ++this.index;
            double offset = (double)0.0F;
            if (this.index < this.currentOffsets.length) {
               offset = this.currentOffsets[this.index];
            }

            if (this.index >= this.currentOffsets.length) {
               if (!(Boolean)this.strict.get()) {
                  offset = this.targetY - this.mc.player.getY();
               }

               this.stepping = false;
            }

            Vec3d vec3d4;
            if ((Boolean)this.strict.get() && this.index <= 1) {
               vec3d4 = Entity.adjustMovementForCollisions(entity, new Vec3d((double)0.0F, offset, (double)0.0F), box.stretch((double)0.0F, (double)0.0F, (double)0.0F), entity.getWorld(), list);
            } else {
               Vec3d vec3d3 = Entity.adjustMovementForCollisions(entity, new Vec3d((double)0.0F, offset, (double)0.0F), box.stretch((double)0.0F, (double)0.0F, (double)0.0F), entity.getWorld(), list);
               vec3d4 = Entity.adjustMovementForCollisions(entity, new Vec3d(movement.x, (double)0.0F, movement.z), box.offset(vec3d3), entity.getWorld(), list).add(vec3d3);
            }

            cir.setReturnValue(vec3d4);
            return;
         }
      }

      cir.setReturnValue(vec3d);
   }

   public double[] getOffsets(double step) {
      if ((Boolean)this.strict.get()) {
         return step > 0.6 && step <= 1.000001 ? new double[]{0.424, 0.33712, 0.25197759999999997} : null;
      } else if (step > 2.019) {
         return new double[]{0.425, 0.39599999999999996, -0.122, -0.09999999999999998, 0.42300000000000004, 0.3500000000000001, 0.2799999999999998, 0.21700000000000008, 0.15000000000000013, -0.10000000000000009};
      } else if (step > (double)1.5F) {
         return new double[]{0.42, 0.36000000000000004, -0.15000000000000002, -0.12, 0.39, 0.30999999999999994, 0.24, -0.020000000000000018};
      } else if (step > 1.015) {
         return new double[]{0.42, 0.3332, 0.25680000000000003, 0.08299999999999996, -0.07800000000000007};
      } else {
         return step > 0.6 ? new double[]{0.42, 0.3332} : null;
      }
   }

   public void step(double[] offsets) {
      if (offsets != null) {
         double offset = (double)0.0F;

         for(double v : offsets) {
            offset += v;
            this.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + offset, this.mc.player.getZ(), false));
         }

         this.lastStep = System.currentTimeMillis();
      }
   }

   private boolean i(Box b) {
      return OLEPOSSUtils.inside(this.mc.player, b);
   }
}
