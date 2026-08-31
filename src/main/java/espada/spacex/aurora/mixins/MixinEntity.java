package espada.spacex.aurora.mixins;

import espada.spacex.aurora.events.UpdateVelocityEvent;
import espada.spacex.aurora.modules.movementplus.StepPlus;
import espada.spacex.aurora.modules.renderplus.AntiCrawl;
import espada.spacex.aurora.modules.renderplus.ForceSneak;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.LivingEntityMoveEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.EntityPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public abstract class MixinEntity {
   @Inject(
      method = {"updateVelocity"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void updateVelocityHook(float speed, Vec3d movementInput, CallbackInfo ci) {
      if (MeteorClient.mc.player != null && MeteorClient.mc.world != null) {
         if ((Object)this == MeteorClient.mc.player) {
            UpdateVelocityEvent event = new UpdateVelocityEvent(movementInput, speed, MeteorClient.mc.player.getYaw(), movementInputToVelocity(movementInput, speed, MeteorClient.mc.player.getYaw()));
            MeteorClient.EVENT_BUS.post(event);
            if (event.isCancelled()) {
               ci.cancel();
               MeteorClient.mc.player.setVelocity(MeteorClient.mc.player.getVelocity().add(event.getVelocity()));
            }
         }

      }
   }

   @Shadow
   private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
      double d = movementInput.lengthSquared();
      if (d < 1.0E-7) {
         return Vec3d.ZERO;
      } else {
         Vec3d vec3d = (d > (double)1.0F ? movementInput.normalize() : movementInput).multiply((double)speed);
         float f = MathHelper.sin(yaw * ((float)Math.PI / 180F));
         float g = MathHelper.cos(yaw * ((float)Math.PI / 180F));
         return new Vec3d(vec3d.x * (double)g - vec3d.z * (double)f, vec3d.y, vec3d.z * (double)g + vec3d.x * (double)f);
      }
   }

   @Shadow
   public abstract boolean isInPose(EntityPose var1);

   @Shadow
   public abstract Text getName();

   @Shadow
   public abstract World getWorld();

   @Shadow
   public abstract ActionResult interact(PlayerEntity var1, Hand var2);

   @Shadow
   protected abstract void fall(double var1, boolean var3, BlockState var4, BlockPos var5);

   @Shadow
   protected abstract boolean stepOnBlock(BlockPos var1, BlockState var2, boolean var3, boolean var4, Vec3d var5);

   @Shadow
   public abstract float getStepHeight();

   @Shadow
   public abstract boolean isOnGround();

   @Shadow
   public abstract Box getBoundingBox();

   @Inject(
      method = {"adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void inject(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
      StepPlus step = (StepPlus)Modules.get().get(StepPlus.class);
      Entity entity = (Entity)(Object)this;
      boolean active = step.isActive() && entity == MeteorClient.mc.player;
      if (active && (Boolean)step.slow.get()) {
         step.slowStep(entity, movement, cir);
      } else {
         active = active && (double)(System.currentTimeMillis() - step.lastStep) > (Double)step.cooldown.get() * (double)1000.0F;
         Box box = this.getBoundingBox();
         List<VoxelShape> list = this.getWorld().getEntityCollisions(entity, box.stretch(movement));
         Vec3d vec3d = movement.lengthSquared() == (double)0.0F ? movement : Entity.adjustMovementForCollisions(entity, movement, box, this.getWorld(), list);
         boolean bl = movement.x != vec3d.x;
         boolean bl2 = movement.y != vec3d.y;
         boolean bl3 = movement.z != vec3d.z;
         boolean bl4 = this.isOnGround() || !active && bl2 && movement.y < (double)0.0F;
         if ((active ? (Double)step.height.get() : (double)this.getStepHeight()) > (double)0.0F && bl4 && (bl || bl3)) {
            Vec3d vec3d2 = Entity.adjustMovementForCollisions(entity, new Vec3d(movement.x, active ? (Double)step.height.get() : (double)this.getStepHeight(), movement.z), box, this.getWorld(), list);
            Vec3d vec3d3 = Entity.adjustMovementForCollisions(entity, new Vec3d((double)0.0F, active ? (Double)step.height.get() : (double)this.getStepHeight(), (double)0.0F), box.stretch(movement.x, (double)0.0F, movement.z), this.getWorld(), list);
            if (vec3d3.y < (active ? (Double)step.height.get() : (double)this.getStepHeight())) {
               Vec3d vec3d4 = Entity.adjustMovementForCollisions(entity, new Vec3d(movement.x, (double)0.0F, movement.z), box.offset(vec3d3), this.getWorld(), list).add(vec3d3);
               if (vec3d4.horizontalLengthSquared() > vec3d2.horizontalLengthSquared()) {
                  vec3d2 = vec3d4;
               }
            }

            if (vec3d2.horizontalLengthSquared() > vec3d.horizontalLengthSquared()) {
               Vec3d v = vec3d2.add(Entity.adjustMovementForCollisions(entity, new Vec3d((double)0.0F, -vec3d2.y + movement.y, (double)0.0F), box.offset(vec3d2), entity.getWorld(), list));
               if (active) {
                  step.step(step.getOffsets(v.y));
               }

               cir.setReturnValue(v);
               return;
            }
         }

         cir.setReturnValue(vec3d);
      }
   }

   @Inject(
      method = {"isInSneakingPose"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void isSneaking(CallbackInfoReturnable<Boolean> cir) {
      if (MeteorClient.mc.player == null || this.getName() != MeteorClient.mc.player.getName()) {
         cir.setReturnValue(((ForceSneak)Modules.get().get(ForceSneak.class)).isActive() || this.isInPose(EntityPose.CROUCHING));
      }

   }

   @Inject(
      method = {"doesNotCollide(Lnet/minecraft/util/math/Box;)Z"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void poseNotCollide(Box box, CallbackInfoReturnable<Boolean> cir) {
      if (Modules.get().isActive(AntiCrawl.class)) {
         cir.setReturnValue(true);
      }

   }

   @Inject(
      method = {"move"},
      at = {@At("HEAD")}
   )
   private void onMove(MovementType type, Vec3d movement, CallbackInfo info) {
      if ((Object)this == MeteorClient.mc.player) {
         MeteorClient.EVENT_BUS.post(PlayerMoveEvent.get(type, movement));
      } else if ((Object)this instanceof LivingEntity) {
         MeteorClient.EVENT_BUS.post(LivingEntityMoveEvent.get((LivingEntity)(Object)this, movement));
      }

   }
}
