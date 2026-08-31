package espada.spacex.aurora.modules.globalsettings;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.managers.RotationManager;
import espada.spacex.aurora.utils.NCPRaytracer;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.RotationUtils;
import java.util.List;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.BlockState;

public class RotationSettings extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgInteract;
   private final SettingGroup sgBlockPlace;
   private final SettingGroup sgMining;
   private final SettingGroup sgAttack;
   private final SettingGroup sgUse;
   public final Setting<Boolean> vanillaRotation;
   public final Setting<Double> yawStep;
   public final Setting<Double> pitchStep;
   public final Setting<Double> yawRandomization;
   public final Setting<Double> pitchRandomization;
   private final Setting<Boolean> interactRotate;
   public final Setting<Double> interactTime;
   public final Setting<RotationCheckMode> interactMode;
   public final Setting<Double> interactYawAngle;
   public final Setting<Double> interactPitchAngle;
   public final Setting<Integer> interactMemory;
   private final Setting<Boolean> blockRotate;
   public final Setting<Double> blockTime;
   public final Setting<RotationCheckMode> blockMode;
   public final Setting<Double> blockYawAngle;
   public final Setting<Double> blockPitchAngle;
   public final Setting<Integer> blockMemory;
   private final Setting<Boolean> mineRotate;
   public final Setting<Double> mineTime;
   public final Setting<RotationCheckMode> mineMode;
   public final Setting<MiningRotMode> mineTiming;
   public final Setting<Double> mineYawAngle;
   public final Setting<Double> minePitchAngle;
   public final Setting<Integer> mineMemory;
   private final Setting<Boolean> attackRotate;
   public final Setting<Double> attackTime;
   public final Setting<RotationCheckMode> attackMode;
   public final Setting<Double> attackYawAngle;
   public final Setting<Double> attackPitchAngle;
   public final Setting<Integer> attackMemory;
   public final Setting<Double> useTime;
   public final Vec3d vec;

   public RotationSettings() {
      super(Aurora.Settings, "Rotate", "Global rotation settings for every aurora module.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgInteract = this.settings.createGroup("Interact");
      this.sgBlockPlace = this.settings.createGroup("Block Place");
      this.sgMining = this.settings.createGroup("Mining");
      this.sgAttack = this.settings.createGroup("Attack");
      this.sgUse = this.settings.createGroup("Use");
      this.vanillaRotation = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Vanilla Rotation")).description("Turns your head.")).defaultValue(false)).build());
      this.yawStep = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Yaw Step")).description("How many yaw degrees should be rotated each packet.")).defaultValue((double)90.0F).range((double)0.0F, (double)180.0F).sliderRange((double)0.0F, (double)180.0F).build());
      this.pitchStep = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Pitch Step")).description("How many pitch degrees should be rotated each packet.")).defaultValue((double)45.0F).range((double)0.0F, (double)180.0F).sliderRange((double)0.0F, (double)180.0F).build());
      this.yawRandomization = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Yaw Randomization")).description(".")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.pitchRandomization = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Pitch Randomization")).description(".")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.interactRotate = this.rotateSetting("Interact", "interacting with a block", this.sgInteract);
      this.interactTime = this.timeSetting("Interact", this.sgInteract);
      this.interactMode = this.modeSetting("Interact", this.sgInteract);
      this.interactYawAngle = this.yawAngleSetting("Interact", this.sgInteract, () -> this.interactMode.get() == RotationSettings.RotationCheckMode.Angle);
      this.interactPitchAngle = this.pitchAngleSetting("Interact", this.sgInteract, () -> this.interactMode.get() == RotationSettings.RotationCheckMode.Angle);
      this.interactMemory = this.memorySetting("Interact", this.sgInteract);
      this.blockRotate = this.rotateSetting("Block Place", "placing a block", this.sgBlockPlace);
      this.blockTime = this.timeSetting("Block Place", this.sgBlockPlace);
      this.blockMode = this.modeSetting("Block Place", this.sgBlockPlace);
      this.blockYawAngle = this.yawAngleSetting("Block Place", this.sgBlockPlace, () -> this.blockMode.get() == RotationSettings.RotationCheckMode.Angle);
      this.blockPitchAngle = this.pitchAngleSetting("Block Place", this.sgBlockPlace, () -> this.blockMode.get() == RotationSettings.RotationCheckMode.Angle);
      this.blockMemory = this.memorySetting("Block Place", this.sgBlockPlace);
      this.mineRotate = this.rotateSetting("Mining", "mining a block", this.sgMining);
      this.mineTime = this.timeSetting("Mining", this.sgMining);
      this.mineMode = this.modeSetting("Mining", this.sgMining);
      this.mineTiming = this.sgMining.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mining Rotate Timing")).description(".")).defaultValue(RotationSettings.MiningRotMode.End)).build());
      this.mineYawAngle = this.yawAngleSetting("Mining", this.sgMining, () -> this.mineMode.get() == RotationSettings.RotationCheckMode.Angle);
      this.minePitchAngle = this.pitchAngleSetting("Mining", this.sgMining, () -> this.mineMode.get() == RotationSettings.RotationCheckMode.Angle);
      this.mineMemory = this.memorySetting("Mining", this.sgMining);
      this.attackRotate = this.rotateSetting("Attack", "attacking an entity", this.sgAttack);
      this.attackTime = this.timeSetting("Attack", this.sgAttack);
      this.attackMode = this.modeSetting("Attack", this.sgAttack);
      this.attackYawAngle = this.yawAngleSetting("Attack", this.sgAttack, () -> this.attackMode.get() == RotationSettings.RotationCheckMode.Angle);
      this.attackPitchAngle = this.pitchAngleSetting("Attack", this.sgAttack, () -> this.attackMode.get() == RotationSettings.RotationCheckMode.Angle);
      this.attackMemory = this.memorySetting("Attack", this.sgAttack);
      this.useTime = this.timeSetting("Use", this.sgUse);
      this.vec = new Vec3d((double)0.0F, (double)0.0F, (double)0.0F);
   }

   private Setting<Boolean> rotateSetting(String type, String verb, SettingGroup sg) {
      return sg.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name(type + " Rotate")).description("Rotates when + " + verb)).defaultValue(false)).build());
   }

   private Setting<Double> timeSetting(String type, SettingGroup sg) {
      return sg.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name(type + " Rotation Time")).description("Keeps the rotation for x seconds after ending.")).defaultValue((double)0.5F).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).build());
   }

   private Setting<RotationCheckMode> modeSetting(String type, SettingGroup sg) {
      return sg.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name(type + " Rotation Mode")).description(".")).defaultValue(RotationSettings.RotationCheckMode.Raytrace)).build());
   }

   private Setting<Double> yawAngleSetting(String type, SettingGroup sg, IVisible visible) {
      return sg.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name(type + " Yaw Angle")).description("Accepts rotation if yaw angle to target is under this.")).defaultValue((double)90.0F).range((double)0.0F, (double)180.0F).sliderRange((double)0.0F, (double)180.0F).visible(visible)).build());
   }

   private Setting<Double> pitchAngleSetting(String type, SettingGroup sg, IVisible visible) {
      return sg.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name(type + " Pitch Angle")).description("Accepts rotation if pitch angle to target is under this.")).defaultValue((double)45.0F).range((double)0.0F, (double)180.0F).sliderRange((double)0.0F, (double)180.0F).visible(visible)).build());
   }

   private Setting<Integer> memorySetting(String type, SettingGroup sg) {
      return sg.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name(type + " Memory")).description("Accepts rotation if looked at it x packets earlier.")).defaultValue(1)).range(1, 20).sliderRange(1, 20).build());
   }

   public boolean rotationCheck(Box box, RotationType type) {
      List<RotationManager.Rotation> history = RotationManager.history;
      if (box == null) {
         return false;
      } else {
         switch (this.mode(type).ordinal()) {
            case 0:
               for(int r = 0; r < this.memory(type) && history.size() > r; ++r) {
                  RotationManager.Rotation rot = (RotationManager.Rotation)history.get(r);
                  if (this.raytraceCheck(rot.vec(), rot.yaw(), rot.pitch(), box)) {
                     return true;
                  }
               }
               break;
            case 1:
               for(int r = 0; r < this.memory(type) && history.size() > r; ++r) {
                  RotationManager.Rotation rot = (RotationManager.Rotation)history.get(r);
                  double range = (double)7.0F;
                  Vec3d end = (new Vec3d(range * Math.cos(Math.toRadians(rot.yaw() + (double)90.0F)) * Math.abs(Math.cos(Math.toRadians(rot.pitch()))), range * -Math.sin(Math.toRadians(rot.pitch())), range * Math.sin(Math.toRadians(rot.yaw() + (double)90.0F)) * Math.abs(Math.cos(Math.toRadians(rot.pitch()))))).add(rot.vec());
                  if (NCPRaytracer.raytrace(rot.vec(), end, box)) {
                     return true;
                  }
               }
               break;
            case 2:
               for(int r = 0; r < this.memory(type) && history.size() > r; ++r) {
                  RotationManager.Rotation rot = (RotationManager.Rotation)history.get(r);
                  if (this.angleCheck(rot.vec(), rot.yaw(), rot.pitch(), box, type)) {
                     return true;
                  }
               }
         }

         return false;
      }
   }

   public boolean shouldRotate(RotationType type) {
      boolean var10000;
      switch (type) {
         case Interact -> var10000 = (Boolean)this.interactRotate.get();
         case BlockPlace -> var10000 = (Boolean)this.blockRotate.get();
         case Attacking -> var10000 = (Boolean)this.attackRotate.get();
         case Mining -> var10000 = (Boolean)this.mineRotate.get();
         default -> var10000 = true;
      }

      return var10000;
   }

   public RotationCheckMode mode(RotationType type) {
      RotationCheckMode var10000;
      switch (type) {
         case Interact -> var10000 = (RotationCheckMode)this.interactMode.get();
         case BlockPlace -> var10000 = (RotationCheckMode)this.blockMode.get();
         case Attacking -> var10000 = (RotationCheckMode)this.attackMode.get();
         case Mining -> var10000 = (RotationCheckMode)this.mineMode.get();
         default -> var10000 = RotationSettings.RotationCheckMode.Raytrace;
      }

      return var10000;
   }

   public double time(RotationType type) {
      double var10000;
      switch (type) {
         case Interact -> var10000 = (Double)this.interactTime.get();
         case BlockPlace -> var10000 = (Double)this.blockTime.get();
         case Attacking -> var10000 = (Double)this.attackTime.get();
         case Mining -> var10000 = (Double)this.mineTime.get();
         case Use -> var10000 = (Double)this.useTime.get();
         case Other -> var10000 = (double)1.0F;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public int memory(RotationType type) {
      int var10000;
      switch (type) {
         case Interact -> var10000 = (Integer)this.interactMemory.get();
         case BlockPlace -> var10000 = (Integer)this.blockMemory.get();
         case Attacking -> var10000 = (Integer)this.attackMemory.get();
         case Mining -> var10000 = (Integer)this.mineMemory.get();
         default -> var10000 = 1;
      }

      return var10000;
   }

   public double yawStep(RotationType type) {
      double var10000;
      switch (type) {
         case Use:
         case Other:
            var10000 = (double)42069.0F;
            break;
         default:
            var10000 = (Double)this.yawStep.get() + (Math.random() - (double)0.5F) * (double)2.0F * (Double)this.yawRandomization.get();
      }

      return var10000;
   }

   public double pitchStep(RotationType type) {
      double var10000;
      switch (type) {
         case Use:
         case Other:
            var10000 = (double)42069.0F;
            break;
         default:
            var10000 = (Double)this.pitchStep.get() + (Math.random() - (double)0.5F) * (double)2.0F * (Double)this.pitchRandomization.get();
      }

      return var10000;
   }

   public double yawAngle(RotationType type) {
      double var10000;
      switch (type) {
         case Interact -> var10000 = (Double)this.interactYawAngle.get();
         case BlockPlace -> var10000 = (Double)this.blockYawAngle.get();
         case Attacking -> var10000 = (Double)this.attackYawAngle.get();
         case Mining -> var10000 = (Double)this.mineYawAngle.get();
         default -> var10000 = (double)0.0F;
      }

      return var10000;
   }

   public double pitchAngle(RotationType type) {
      double var10000;
      switch (type) {
         case Interact -> var10000 = (Double)this.interactPitchAngle.get();
         case BlockPlace -> var10000 = (Double)this.blockPitchAngle.get();
         case Attacking -> var10000 = (Double)this.attackPitchAngle.get();
         case Mining -> var10000 = (Double)this.minePitchAngle.get();
         default -> var10000 = (double)0.0F;
      }

      return var10000;
   }

   public boolean angleCheck(Vec3d pos, double y, double p, Box box, RotationType type) {
      return RotationUtils.yawAngle(y, RotationUtils.getYaw(pos, box.getCenter())) <= this.yawAngle(type) && Math.abs(p - RotationUtils.getPitch(pos, box.getCenter())) <= this.pitchAngle(type);
   }

   public boolean raytraceCheck(Vec3d pos, double y, double p, Box box) {
      double range = pos.distanceTo(OLEPOSSUtils.getMiddle(box)) + (double)3.0F;
      Vec3d end = (new Vec3d(range * Math.cos(Math.toRadians(y + (double)90.0F)) * Math.abs(Math.cos(Math.toRadians(p))), range * -Math.sin(Math.toRadians(p)), range * Math.sin(Math.toRadians(y + (double)90.0F)) * Math.abs(Math.cos(Math.toRadians(p))))).add(pos);

      for(float i = 0.0F; i < 1.0F; i = (float)((double)i + 0.01)) {
         if (box.contains(pos.x + (end.x - pos.x) * (double)i, pos.y + (end.y - pos.y) * (double)i, pos.z + (end.z - pos.z) * (double)i)) {
            return true;
         }
      }

      return false;
   }

   private double lerp(double from, double to, double delta) {
      return from + (to - from) * delta;
   }

   public boolean validForCheck(BlockPos pos, BlockState state) {
      if (state.isSolid()) {
         return true;
      } else if (state.getBlock() instanceof FluidBlock) {
         return false;
      } else if (state.getBlock() instanceof StairsBlock) {
         return false;
      } else {
         return state.hasBlockEntity() ? false : state.isFullCube(this.mc.world, pos);
      }
   }

   public boolean endMineRot() {
      if (!(Boolean)this.mineRotate.get()) {
         return false;
      } else {
         return this.mineTiming.get() == RotationSettings.MiningRotMode.End || this.mineTiming.get() == RotationSettings.MiningRotMode.Double;
      }
   }

   public boolean startMineRot() {
      if (!(Boolean)this.mineRotate.get()) {
         return false;
      } else {
         return this.mineTiming.get() == RotationSettings.MiningRotMode.Start || this.mineTiming.get() == RotationSettings.MiningRotMode.Double;
      }
   }

   public static enum MiningRotMode {
      Start,
      End,
      Double;

      // $FF: synthetic method
      private static MiningRotMode[] $values() {
         return new MiningRotMode[]{Start, End, Double};
      }
   }

   public static enum RotationCheckMode {
      Raytrace,
      StrictRaytrace,
      Angle;

      // $FF: synthetic method
      private static RotationCheckMode[] $values() {
         return new RotationCheckMode[]{Raytrace, StrictRaytrace, Angle};
      }
   }
}
