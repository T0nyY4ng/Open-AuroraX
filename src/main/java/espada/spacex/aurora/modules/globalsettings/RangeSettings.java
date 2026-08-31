package espada.spacex.aurora.modules.globalsettings;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.RotationUtils;
import espada.spacex.aurora.utils.SettingUtils;
import java.util.Objects;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class RangeSettings extends Modules {
   private final SettingGroup sgPlace;
   private final SettingGroup sgAttack;
   private final SettingGroup sgMining;
   public final Setting<Double> placeRange;
   public final Setting<Double> placeRangeWalls;
   private final Setting<FromMode> placeRangeFrom;
   private final Setting<PlaceRangeMode> placeRangeMode;
   private final Setting<Double> blockWidth;
   private final Setting<Double> blockHeight;
   private final Setting<Double> placeHeight;
   public final Setting<Double> attackRange;
   public final Setting<Double> attackRangeWalls;
   public final Setting<Boolean> reduce;
   public final Setting<Double> reduceAmount;
   public final Setting<Double> reduceStep;
   private final Setting<FromMode> attackRangeFrom;
   private final Setting<AttackRangeMode> attackRangeMode;
   private final Setting<Double> closestAttackWidth;
   private final Setting<Double> closestAttackHeight;
   private final Setting<Double> attackHeight;
   public final Setting<Double> miningRange;
   public final Setting<Double> miningRangeWalls;
   private final Setting<FromMode> miningRangeFrom;
   private final Setting<MiningRangeMode> miningRangeMode;
   private final Setting<Double> closestMiningWidth;
   private final Setting<Double> closestMiningHeight;
   private final Setting<Double> miningHeight;
   public double rangeMulti;

   public RangeSettings() {
      super(Aurora.Settings, "Range", "Global range settings for every aurora module.");
      this.sgPlace = this.settings.createGroup("Placing");
      this.sgAttack = this.settings.createGroup("Attacking");
      this.sgMining = this.settings.createGroup("Mining");
      this.placeRange = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Place Range")).description("Range for placing.")).defaultValue(5.2).range((double)0.0F, (double)6.0F).sliderRange((double)0.0F, (double)6.0F).build());
      this.placeRangeWalls = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Place Range Walls")).description("Range for placing behind blocks.")).defaultValue(5.2).range((double)0.0F, (double)6.0F).sliderRange((double)0.0F, (double)6.0F).build());
      this.placeRangeFrom = this.sgPlace.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Place Range From")).description("Where to calculate place ranges from.")).defaultValue(RangeSettings.FromMode.Eyes)).build());
      this.placeRangeMode = this.sgPlace.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Place Range Mode")).description("Where to calculate place ranges from.")).defaultValue(RangeSettings.PlaceRangeMode.NCP)).build());
      this.blockWidth = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Block Width")).description("How wide should the box be for closest range.")).defaultValue((double)2.0F).min((double)0.0F).sliderRange((double)0.0F, (double)3.0F).visible(() -> ((PlaceRangeMode)this.placeRangeMode.get()).equals(RangeSettings.PlaceRangeMode.CustomBox))).build());
      this.blockHeight = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Block Height")).description("How tall should the box be for closest range.")).defaultValue((double)2.0F).min((double)0.0F).sliderRange((double)0.0F, (double)3.0F).visible(() -> ((PlaceRangeMode)this.placeRangeMode.get()).equals(RangeSettings.PlaceRangeMode.CustomBox))).build());
      this.placeHeight = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Place Height")).description("The height to calculate ranges from.")).defaultValue((double)0.5F).sliderRange((double)0.0F, (double)1.0F).visible(() -> ((PlaceRangeMode)this.placeRangeMode.get()).equals(RangeSettings.PlaceRangeMode.Height))).build());
      this.attackRange = this.sgAttack.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Attack Range")).description("Range for attacking entities.")).defaultValue(4.8).range((double)0.0F, (double)6.0F).sliderRange((double)0.0F, (double)6.0F).build());
      this.attackRangeWalls = this.sgAttack.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Attack Range Walls")).description("Range for attacking entities behind blocks.")).defaultValue(4.8).range((double)0.0F, (double)6.0F).sliderRange((double)0.0F, (double)6.0F).build());
      this.reduce = this.sgAttack.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Reduce")).description("Reduces range on every hit by reduce step until it reaches (range - reduce amount).")).defaultValue(false)).build());
      SettingGroup var10001 = this.sgAttack;
      DoubleSetting.Builder var10002 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Reduce Amount")).description("Check description from 'Reduce' setting.")).defaultValue(0.8).range((double)0.0F, (double)6.0F).sliderRange((double)0.0F, (double)6.0F);
      Setting<Boolean> var10003 = this.reduce;
      Objects.requireNonNull(var10003);
      this.reduceAmount = var10001.add(((DoubleSetting.Builder)var10002.visible(var10003::get)).build());
      var10001 = this.sgAttack;
      var10002 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Reduce Step")).description("Check description from 'Reduce' setting.")).defaultValue(0.14).range((double)0.0F, (double)6.0F).sliderRange((double)0.0F, (double)6.0F);
      var10003 = this.reduce;
      Objects.requireNonNull(var10003);
      this.reduceStep = var10001.add(((DoubleSetting.Builder)var10002.visible(var10003::get)).build());
      this.attackRangeFrom = this.sgAttack.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Attack Range From")).description("Where to calculate ranges from.")).defaultValue(RangeSettings.FromMode.Eyes)).build());
      this.attackRangeMode = this.sgAttack.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Attack Range Mode")).description("Where to calculate ranges from.")).defaultValue(RangeSettings.AttackRangeMode.NCP)).build());
      this.closestAttackWidth = this.sgAttack.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Closest Attack Width")).description("How wide should the box be for closest range.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)3.0F).visible(() -> ((AttackRangeMode)this.attackRangeMode.get()).equals(RangeSettings.AttackRangeMode.CustomBox))).build());
      this.closestAttackHeight = this.sgAttack.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Closest Attack Height")).description("How tall should the box be for closest range.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)3.0F).visible(() -> ((AttackRangeMode)this.attackRangeMode.get()).equals(RangeSettings.AttackRangeMode.CustomBox))).build());
      this.attackHeight = this.sgAttack.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Attack Height")).description("The height above feet to calculate ranges from.")).defaultValue((double)1.0F).sliderRange((double)-2.0F, (double)2.0F).visible(() -> ((AttackRangeMode)this.attackRangeMode.get()).equals(RangeSettings.AttackRangeMode.Height))).build());
      this.miningRange = this.sgMining.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Mining Range")).description("Range for mining blocks.")).defaultValue(4.8).range((double)0.0F, (double)6.0F).sliderRange((double)0.0F, (double)6.0F).build());
      this.miningRangeWalls = this.sgMining.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Mining Range Walls")).description("Range for mining blocks behind other blocks.")).defaultValue(4.8).range((double)0.0F, (double)6.0F).sliderRange((double)0.0F, (double)6.0F).build());
      this.miningRangeFrom = this.sgMining.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mining Range From")).description("Where to calculate mining ranges from.")).defaultValue(RangeSettings.FromMode.Eyes)).build());
      this.miningRangeMode = this.sgMining.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mining Range Mode")).description("Where to calculate mining ranges from.")).defaultValue(RangeSettings.MiningRangeMode.NCP)).build());
      this.closestMiningWidth = this.sgMining.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Closest Mining Width")).description("How wide should the box be for closest range.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)3.0F).visible(() -> ((MiningRangeMode)this.miningRangeMode.get()).equals(RangeSettings.MiningRangeMode.CustomBox))).build());
      this.closestMiningHeight = this.sgMining.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Closest Mining Height")).description("How tall should the box be for closest range.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)3.0F).visible(() -> ((MiningRangeMode)this.miningRangeMode.get()).equals(RangeSettings.MiningRangeMode.CustomBox))).build());
      this.miningHeight = this.sgMining.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Mining Height")).description("The height above block bottom to calculate ranges from.")).defaultValue((double)0.5F).sliderRange((double)0.0F, (double)1.0F).visible(() -> ((MiningRangeMode)this.miningRangeMode.get()).equals(RangeSettings.MiningRangeMode.Height))).build());
      this.rangeMulti = (double)0.0F;
   }

   public boolean inPlaceRange(BlockPos pos, Vec3d from) {
      if (this.mc.player == null) {
         return false;
      } else {
         double dist = this.placeRangeTo(pos, from);
         return dist >= (double)0.0F && (SettingUtils.placeTrace(pos) ? dist <= (Double)this.placeRange.get() : dist <= (Double)this.placeRangeWalls.get());
      }
   }

   public boolean inPlaceRangeNoTrace(BlockPos pos, Vec3d from) {
      if (this.mc.player == null) {
         return false;
      } else {
         double dist = this.placeRangeTo(pos, from);
         return dist >= (double)0.0F && dist <= Math.max((Double)this.placeRange.get(), (Double)this.placeRangeWalls.get());
      }
   }

   public double placeRangeTo(BlockPos pos, Vec3d from) {
      Box pBB = this.mc.player.getBoundingBox();
      if (from == null) {
         from = this.mc.player.getEyePos();
         Vec3d pPos = this.mc.player.getPos();
         switch (((FromMode)this.placeRangeFrom.get()).ordinal()) {
            case 1 -> ((IVec3d)from).set((pBB.minX + pBB.maxX) / (double)2.0F, (pBB.minY + pBB.maxY) / (double)2.0F, (pBB.minZ + pBB.maxZ) / (double)2.0F);
            case 2 -> ((IVec3d)from).set(pPos.x, pPos.y, pPos.z);
         }
      }

      Vec3d feet = new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY(), (double)pos.getZ() + (double)0.5F);
      switch (((PlaceRangeMode)this.placeRangeMode.get()).ordinal()) {
         case 0 -> {
            return this.getRange(from, feet.add((double)0.0F, (double)0.5F, (double)0.0F));
         }
         case 1 -> {
            return this.getRange(from, feet.add((double)0.0F, (Double)this.placeHeight.get(), (double)0.0F));
         }
         case 2 -> {
            return this.getRange(from, OLEPOSSUtils.getClosest(this.mc.player.getEyePos(), feet, (double)1.0F, (double)1.0F));
         }
         case 3 -> {
            return this.getRange(from, OLEPOSSUtils.getClosest(this.mc.player.getEyePos(), feet, (Double)this.blockWidth.get(), (Double)this.blockHeight.get()));
         }
         default -> {
            return (double)-1.0F;
         }
      }
   }

   public boolean inAttackRange(Box bb, Vec3d from) {
      return this.inAttackRange(bb, this.getFeet(bb), from);
   }

   public boolean inAttackRange(Box bb, Vec3d feet, Vec3d from) {
      if (this.mc.player == null) {
         return false;
      } else if (SettingUtils.attackTrace(bb)) {
         return this.attackRangeTo(bb, feet, from, true) < (Double)this.attackRange.get();
      } else {
         return this.attackRangeTo(bb, feet, from, false) < (Double)this.attackRangeWalls.get();
      }
   }

   public boolean inAttackRangeNoTrace(Box bb, Vec3d feet, Vec3d from) {
      if (this.mc.player == null) {
         return false;
      } else {
         return this.attackRangeTo(bb, feet, from, true) <= Math.max((Double)this.attackRange.get(), (Double)this.attackRangeWalls.get());
      }
   }

   public double attackRangeTo(Box bb, Vec3d feet, Vec3d from, boolean countReduce) {
      Box pBB = this.mc.player.getBoundingBox();
      if (from == null) {
         from = this.mc.player.getEyePos();
         switch (((FromMode)this.attackRangeFrom.get()).ordinal()) {
            case 1 -> ((IVec3d)from).set((pBB.minX + pBB.maxX) / (double)2.0F, (pBB.minY + pBB.maxY) / (double)2.0F, (pBB.minZ + pBB.maxZ) / (double)2.0F);
            case 2 -> from = this.mc.player.getPos();
         }
      } else {
         switch (((FromMode)this.attackRangeFrom.get()).ordinal()) {
            case 0 -> from = from.add((double)0.0F, (double)this.mc.player.getEyeHeight(this.mc.player.getPose()), (double)0.0F);
            case 1 -> from = from.add((double)0.0F, (double)(this.mc.player.getEyeHeight(this.mc.player.getPose()) / 2.0F), (double)0.0F);
         }
      }

      double var10000;
      switch (((AttackRangeMode)this.attackRangeMode.get()).ordinal()) {
         case 0 -> var10000 = this.getRange(from, new Vec3d(feet.x, Math.min(Math.max(from.getY(), bb.minY), bb.maxY), feet.z));
         case 1 -> var10000 = this.getRange(from, new Vec3d(feet.x, Math.min(Math.max(from.getY(), bb.minY), bb.maxY), feet.z)) - this.getDistFromCenter(bb, feet, from);
         case 2 -> var10000 = this.getRange(from, feet.add((double)0.0F, (Double)this.attackHeight.get(), (double)0.0F));
         case 3 -> var10000 = this.getRange(from, OLEPOSSUtils.getClosest(this.mc.player.getEyePos(), feet, Math.abs(bb.minX - bb.maxX), Math.abs(bb.minY - bb.maxY)));
         case 4 -> var10000 = this.getRange(from, new Vec3d((bb.minX + bb.maxX) / (double)2.0F, (bb.minY + bb.maxY) / (double)2.0F, (bb.minZ + bb.maxZ) / (double)2.0F));
         case 5 -> var10000 = this.getRange(from, OLEPOSSUtils.getClosest(this.mc.player.getEyePos(), feet, Math.abs(bb.minX - bb.maxX) * (Double)this.closestAttackWidth.get(), Math.abs(bb.minY - bb.maxY) * (Double)this.closestAttackHeight.get()));
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      double dist = var10000;
      return dist * (countReduce && (Boolean)this.reduce.get() ? this.rangeMulti : (double)1.0F);
   }

   public double getDistFromCenter(Box bb, Vec3d feet, Vec3d from) {
      Vec3d startPos = new Vec3d(feet.x, Math.min(Math.max(from.getY(), bb.minY), bb.maxY), feet.z);
      Vec3d rangePos = new Vec3d(feet.x, Math.min(Math.max(from.getY(), bb.minY), bb.maxY), feet.z);
      double halfWidth = Math.abs(bb.minX - bb.maxX) / (double)2.0F;
      if (from.x == rangePos.x && from.z == rangePos.z) {
         return (double)0.0F;
      } else {
         Vec3d dist = new Vec3d(from.x - rangePos.x, (double)0.0F, from.z - rangePos.z);
         if (this.getDistXZ(dist) < halfWidth * Math.sqrt((double)2.0F)) {
            return (double)0.0F;
         } else {
            if (dist.getZ() > (double)0.0F) {
               ((IVec3d)rangePos).setXZ(rangePos.x, rangePos.z + halfWidth);
            } else if (dist.getZ() < (double)0.0F) {
               ((IVec3d)rangePos).setXZ(rangePos.x, rangePos.z - halfWidth);
            } else if (dist.getX() > (double)0.0F) {
               ((IVec3d)rangePos).setXZ(rangePos.x + halfWidth, rangePos.z);
            } else {
               ((IVec3d)rangePos).setXZ(rangePos.x - halfWidth, rangePos.z);
            }

            Vec3d vec2 = rangePos.subtract(startPos);
            double angle = RotationUtils.radAngle(new Vec2f((float)dist.x, (float)dist.z), new Vec2f((float)vec2.x, (float)vec2.z));
            if (angle > (Math.PI / 4D)) {
               angle = (Math.PI / 2D) - angle;
            }

            return angle >= (double)0.0F && angle <= (Math.PI / 4D) ? halfWidth / Math.cos(angle) : (double)0.0F;
         }
      }
   }

   private double getRange(Vec3d from, Vec3d to) {
      double x = Math.abs(from.x - to.x);
      double y = Math.abs(from.y - to.y);
      double z = Math.abs(from.z - to.z);
      return Math.sqrt(x * x + y * y + z * z);
   }

   private Vec3d getFeet(Box bb) {
      return new Vec3d((bb.minX + bb.maxX) / (double)2.0F, bb.minY, (bb.minZ + bb.maxZ) / (double)2.0F);
   }

   public boolean inMineRange(BlockPos pos) {
      if (this.mc.player == null) {
         return false;
      } else {
         double dist = this.miningRangeTo(pos, (Vec3d)null);
         return dist >= (double)0.0F && (SettingUtils.placeTrace(pos) ? dist <= (Double)this.miningRange.get() : dist <= (Double)this.miningRangeWalls.get());
      }
   }

   public boolean inMineRangeNoTrace(BlockPos pos) {
      if (this.mc.player == null) {
         return false;
      } else {
         double dist = this.miningRangeTo(pos, (Vec3d)null);
         return dist >= (double)0.0F && dist <= Math.max((Double)this.miningRange.get(), (Double)this.miningRangeWalls.get());
      }
   }

   public double miningRangeTo(BlockPos pos, Vec3d from) {
      Box pBB = this.mc.player.getBoundingBox();
      Vec3d pPos = this.mc.player.getPos();
      if (from == null) {
         from = this.mc.player.getEyePos();
         switch (((FromMode)this.miningRangeFrom.get()).ordinal()) {
            case 1 -> ((IVec3d)from).set((pBB.minX + pBB.maxX) / (double)2.0F, (pBB.minY + pBB.maxY) / (double)2.0F, (pBB.minX + pBB.maxX) / (double)2.0F);
            case 2 -> ((IVec3d)from).set(pPos.x, pPos.y, pPos.z);
         }
      }

      Vec3d feet = new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY(), (double)pos.getZ() + (double)0.5F);
      switch (((MiningRangeMode)this.miningRangeMode.get()).ordinal()) {
         case 0 -> {
            return this.getRange(from, feet.add((double)0.0F, (double)0.5F, (double)0.0F));
         }
         case 1 -> {
            return this.getRange(from, feet.add((double)0.0F, (Double)this.miningHeight.get(), (double)0.0F));
         }
         case 2 -> {
            return this.getRange(from, OLEPOSSUtils.getClosest(this.mc.player.getEyePos(), feet, (double)1.0F, (double)1.0F));
         }
         case 3 -> {
            return this.getRange(from, OLEPOSSUtils.getClosest(this.mc.player.getEyePos(), feet, (Double)this.closestMiningWidth.get(), (Double)this.closestMiningHeight.get()));
         }
         default -> {
            return (double)-1.0F;
         }
      }
   }

   private double getDistXZ(Vec3d vec) {
      return Math.sqrt(vec.x * vec.x + vec.z * vec.z);
   }

   public void registerAttack(Box bb) {
      if (this.attackRangeTo(bb, this.getFeet(bb), (Vec3d)null, false) <= (Double)this.attackRange.get() - (Double)this.reduceAmount.get()) {
         this.rangeMulti = Math.min(this.rangeMulti + (Double)this.reduceStep.get(), (double)1.0F);
      } else {
         this.rangeMulti = Math.max(this.rangeMulti - (Double)this.reduceStep.get(), ((Double)this.attackRange.get() - (Double)this.reduceStep.get() / (Double)this.attackRange.get()) / (Double)this.attackRange.get());
      }

   }

   public static enum PlaceRangeMode {
      NCP,
      Height,
      Vanilla,
      CustomBox;

      // $FF: synthetic method
      private static PlaceRangeMode[] $values() {
         return new PlaceRangeMode[]{NCP, Height, Vanilla, CustomBox};
      }
   }

   public static enum AttackRangeMode {
      NCP,
      UpdatedNCP,
      Height,
      Vanilla,
      Middle,
      CustomBox;

      // $FF: synthetic method
      private static AttackRangeMode[] $values() {
         return new AttackRangeMode[]{NCP, UpdatedNCP, Height, Vanilla, Middle, CustomBox};
      }
   }

   public static enum MiningRangeMode {
      NCP,
      Height,
      Vanilla,
      CustomBox;

      // $FF: synthetic method
      private static MiningRangeMode[] $values() {
         return new MiningRangeMode[]{NCP, Height, Vanilla, CustomBox};
      }
   }

   public static enum FromMode {
      Eyes,
      Middle,
      Feet;

      // $FF: synthetic method
      private static FromMode[] $values() {
         return new FromMode[]{Eyes, Middle, Feet};
      }
   }
}
