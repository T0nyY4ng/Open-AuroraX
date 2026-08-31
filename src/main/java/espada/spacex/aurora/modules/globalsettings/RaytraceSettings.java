package espada.spacex.aurora.modules.globalsettings;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.mixins.IRaycastContext;
import espada.spacex.aurora.utils.meteor.BODamageUtils;
import java.util.Objects;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class RaytraceSettings extends Modules {
   private final SettingGroup sgPlace;
   private final SettingGroup sgAttack;
   public final Setting<Boolean> placeTrace;
   private final Setting<PlaceTraceMode> placeMode;
   private final Setting<Double> placeHeight;
   private final Setting<Double> placeHeight1;
   private final Setting<Double> placeHeight2;
   private final Setting<Double> exposure;
   public final Setting<Boolean> attackTrace;
   private final Setting<AttackTraceMode> attackMode;
   private final Setting<Double> attackHeight;
   private final Setting<Double> attackHeight1;
   private final Setting<Double> attackHeight2;
   private final Setting<Double> attackExposure;
   private final Vec3d vec;
   public RaycastContext raycastContext;
   public BlockHitResult result;
   public int hit;

   public RaytraceSettings() {
      super(Aurora.Settings, "Raytrace", "Global raytrace settings for every aurora module.");
      this.sgPlace = this.settings.createGroup("Placing");
      this.sgAttack = this.settings.createGroup("Attacking");
      this.placeTrace = this.sgPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Place Traces")).description("Raytraces when placing.")).defaultValue(false)).build());
      SettingGroup var10001 = this.sgPlace;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Place Mode")).description("Place trace mode.")).defaultValue(RaytraceSettings.PlaceTraceMode.SinglePoint);
      Setting<Boolean> var10003 = this.placeTrace;
      Objects.requireNonNull(var10003);
      this.placeMode = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.placeHeight = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Place Height")).description("Raytraces to x blocks above the bottom.")).defaultValue((double)0.5F).sliderRange((double)-2.0F, (double)2.0F).visible(() -> this.placeMode.get() == RaytraceSettings.PlaceTraceMode.SinglePoint && (Boolean)this.placeTrace.get())).build());
      this.placeHeight1 = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Place Height 1")).description("Raytraces to x blocks above the bottom.")).defaultValue((double)0.25F).sliderRange((double)-2.0F, (double)1.5F).visible(() -> this.placeMode.get() == RaytraceSettings.PlaceTraceMode.DoublePoint && (Boolean)this.placeTrace.get())).build());
      this.placeHeight2 = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Place Height 2")).description("Raytraces to x blocks above the bottom.")).defaultValue((double)0.75F).sliderRange((double)-2.0F, (double)2.0F).visible(() -> this.placeMode.get() == RaytraceSettings.PlaceTraceMode.DoublePoint && (Boolean)this.placeTrace.get())).build());
      this.exposure = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Place Exposure")).description("How many % of the block should be seen.")).defaultValue((double)50.0F).range((double)0.0F, (double)100.0F).sliderRange((double)0.0F, (double)100.0F).visible(() -> this.placeMode.get() == RaytraceSettings.PlaceTraceMode.Exposure && (Boolean)this.placeTrace.get())).build());
      this.attackTrace = this.sgAttack.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Attack Traces")).description("Raytraces when attacking.")).defaultValue(false)).build());
      var10001 = this.sgAttack;
      var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Attack Mode")).description("Attack trace mode.")).defaultValue(RaytraceSettings.AttackTraceMode.SinglePoint);
      var10003 = this.attackTrace;
      Objects.requireNonNull(var10003);
      this.attackMode = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.attackHeight = this.sgAttack.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Attack Height")).description("Raytraces to x blocks above the bottom.")).defaultValue((double)1.5F).sliderRange((double)-2.0F, (double)2.0F).visible(() -> ((AttackTraceMode)this.attackMode.get()).equals(RaytraceSettings.AttackTraceMode.SinglePoint) && (Boolean)this.attackTrace.get())).build());
      this.attackHeight1 = this.sgAttack.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Attack Height 1")).description("Raytraces to x * hitbox height above the bottom.")).defaultValue((double)0.5F).sliderRange((double)-2.0F, (double)2.0F).visible(() -> ((AttackTraceMode)this.attackMode.get()).equals(RaytraceSettings.AttackTraceMode.DoublePoint) && (Boolean)this.attackTrace.get())).build());
      this.attackHeight2 = this.sgAttack.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Attack Height 2")).description("Raytraces to x * hitbox height above the bottom.")).defaultValue((double)0.5F).sliderRange((double)-2.0F, (double)2.0F).visible(() -> ((AttackTraceMode)this.attackMode.get()).equals(RaytraceSettings.AttackTraceMode.DoublePoint) && (Boolean)this.attackTrace.get())).build());
      this.attackExposure = this.sgAttack.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Attack Exposure")).description("How many % of the entity should be seen.")).defaultValue((double)50.0F).range((double)0.0F, (double)100.0F).sliderRange((double)0.0F, (double)100.0F).visible(() -> this.placeMode.get() == RaytraceSettings.PlaceTraceMode.Exposure && (Boolean)this.attackTrace.get())).build());
      this.vec = new Vec3d((double)0.0F, (double)0.0F, (double)0.0F);
      this.hit = 0;
   }

   public boolean placeTrace(BlockPos pos) {
      if (!(Boolean)this.placeTrace.get()) {
         return true;
      } else {
         this.updateContext();
         switch (((PlaceTraceMode)this.placeMode.get()).ordinal()) {
            case 0:
               ((IRaycastContext)this.raycastContext).setEnd(new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY() + (Double)this.placeHeight.get(), (double)pos.getZ() + (double)0.5F));
               this.result = BODamageUtils.raycast(this.raycastContext);
               return this.result.getBlockPos().equals(pos);
            case 1:
               ((IRaycastContext)this.raycastContext).setEnd(new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY() + (Double)this.placeHeight1.get(), (double)pos.getZ() + (double)0.5F));
               this.result = BODamageUtils.raycast(this.raycastContext);
               if (this.result.getBlockPos().equals(pos)) {
                  return true;
               }

               ((IRaycastContext)this.raycastContext).setEnd(new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY() + (Double)this.placeHeight2.get(), (double)pos.getZ() + (double)0.5F));
               this.result = BODamageUtils.raycast(this.raycastContext);
               return this.result.getBlockPos().equals(pos);
            case 2:
               ((IVec3d)this.vec).set((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F);

               for(Direction dir : Direction.values()) {
                  ((IRaycastContext)this.raycastContext).setEnd(this.vec.add((double)((float)dir.getOffsetX() / 2.0F), (double)((float)dir.getOffsetY() / 2.0F), (double)((float)dir.getOffsetZ() / 2.0F)));
                  this.result = BODamageUtils.raycast(this.raycastContext);
                  if (this.result.getBlockPos().equals(pos)) {
                     return true;
                  }
               }
               break;
            case 3:
               ((IVec3d)this.vec).set((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
               this.hit = 0;

               for(int x = 0; x <= 2; ++x) {
                  for(int y = 0; y <= 2; ++y) {
                     for(int z = 0; z <= 2; ++z) {
                        ((IRaycastContext)this.raycastContext).setEnd(this.vec.add(0.1 + (double)x * 0.4, 0.1 + (double)y * 0.4, 0.1 + (double)z * 0.4));
                        this.result = BODamageUtils.raycast(this.raycastContext);
                        if (this.result.getBlockPos().equals(pos)) {
                           ++this.hit;
                           if ((double)this.hit >= (Double)this.exposure.get() / (double)100.0F * (double)27.0F) {
                              return true;
                           }
                        }
                     }
                  }
               }
               break;
            case 4:
               ((IVec3d)this.vec).set((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
               this.hit = 0;

               for(int x = 0; x <= 2; ++x) {
                  for(int y = 0; y <= 2; ++y) {
                     for(int z = 0; z <= 2; ++z) {
                        ((IRaycastContext)this.raycastContext).setEnd(this.vec.add(0.1 + (double)x * 0.4, 0.1 + (double)y * 0.4, 0.1 + (double)z * 0.4));
                        this.result = BODamageUtils.raycast(this.raycastContext);
                        if (this.result.getBlockPos().equals(pos)) {
                           return true;
                        }
                     }
                  }
               }
         }

         return false;
      }
   }

   public boolean attackTrace(Box box) {
      if (!(Boolean)this.attackTrace.get()) {
         return true;
      } else {
         this.updateContext();
         switch (((AttackTraceMode)this.attackMode.get()).ordinal()) {
            case 0:
               ((meteordevelopment.meteorclient.mixininterface.IRaycastContext)BODamageUtils.raycastContext).set(this.mc.player.getEyePos(), new Vec3d((box.minX + box.maxX) / (double)2.0F, box.minY + (Double)this.attackHeight.get(), (box.minZ + box.maxZ) / (double)2.0F), ShapeType.COLLIDER, FluidHandling.NONE, this.mc.player);
               return BODamageUtils.raycast(BODamageUtils.raycastContext).getType() != Type.BLOCK;
            case 1:
               ((meteordevelopment.meteorclient.mixininterface.IRaycastContext)BODamageUtils.raycastContext).set(this.mc.player.getEyePos(), new Vec3d((box.minX + box.maxX) / (double)2.0F, box.minY + (Double)this.attackHeight1.get(), (box.minZ + box.maxZ) / (double)2.0F), ShapeType.COLLIDER, FluidHandling.NONE, this.mc.player);
               if (BODamageUtils.raycast(BODamageUtils.raycastContext).getType() != Type.BLOCK) {
                  return true;
               }

               ((meteordevelopment.meteorclient.mixininterface.IRaycastContext)BODamageUtils.raycastContext).set(this.mc.player.getEyePos(), new Vec3d((box.minX + box.maxX) / (double)2.0F, box.minY + (Double)this.attackHeight2.get(), (box.minZ + box.maxZ) / (double)2.0F), ShapeType.COLLIDER, FluidHandling.NONE, this.mc.player);
               return BODamageUtils.raycast(BODamageUtils.raycastContext).getType() != Type.BLOCK;
            case 2:
               ((IVec3d)this.vec).set(box.minX, box.minY, box.minZ);
               double xw = box.maxX - box.minX;
               double yh = box.maxY - box.minY;
               double zw = box.maxZ - box.minZ;
               this.hit = 0;

               for(int x = 0; x <= 2; ++x) {
                  for(int y = 0; y <= 2; ++y) {
                     for(int z = 0; z <= 2; ++z) {
                        ((IRaycastContext)this.raycastContext).setEnd(this.vec.add(MathHelper.lerp((double)((float)x / 2.0F), 0.1, xw - 0.1), MathHelper.lerp((double)((float)y / 2.0F), (double)0.0F, yh - 0.1), MathHelper.lerp((double)((float)z / 2.0F), 0.1, zw - 0.1)));
                        this.result = BODamageUtils.raycast(this.raycastContext);
                        if (this.result.getType() != Type.BLOCK) {
                           ++this.hit;
                           if ((double)this.hit >= (Double)this.attackExposure.get() / (double)100.0F * (double)27.0F) {
                              return true;
                           }
                        }
                     }
                  }
               }
               break;
            case 3:
               ((IVec3d)this.vec).set(box.minX, box.minY, box.minZ);
               double xw3 = box.maxX - box.minX;
               double yh3 = box.maxY - box.minY;
               double zw3 = box.maxZ - box.minZ;

               for(int x = 0; x <= 2; ++x) {
                  for(int y = 0; y <= 2; ++y) {
                     for(int z = 0; z <= 2; ++z) {
                        ((IRaycastContext)this.raycastContext).setEnd(this.vec.add(MathHelper.lerp((double)((float)x / 2.0F), 0.1, xw3 - 0.1), MathHelper.lerp((double)((float)y / 2.0F), (double)0.0F, yh3 - 0.1), MathHelper.lerp((double)((float)z / 2.0F), 0.1, zw3 - 0.1)));
                        this.result = BODamageUtils.raycast(this.raycastContext);
                        if (this.result.getType() != Type.BLOCK) {
                           return true;
                        }
                     }
                  }
               }
         }

         return false;
      }
   }

   private void updateContext() {
      if (this.raycastContext == null) {
         this.raycastContext = new RaycastContext(this.mc.player.getEyePos(), (Vec3d)null, ShapeType.COLLIDER, FluidHandling.ANY, this.mc.player);
      } else {
         ((IRaycastContext)this.raycastContext).setStart(this.mc.player.getEyePos());
      }

   }

   public static enum PlaceTraceMode {
      SinglePoint,
      DoublePoint,
      Sides,
      Exposure,
      Any;

      // $FF: synthetic method
      private static PlaceTraceMode[] $values() {
         return new PlaceTraceMode[]{SinglePoint, DoublePoint, Sides, Exposure, Any};
      }
   }

   public static enum AttackTraceMode {
      SinglePoint,
      DoublePoint,
      Exposure,
      Any;

      // $FF: synthetic method
      private static AttackTraceMode[] $values() {
         return new AttackTraceMode[]{SinglePoint, DoublePoint, Exposure, Any};
      }
   }
}
