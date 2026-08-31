package espada.spacex.aurora.utils;

import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import espada.spacex.aurora.modules.globalsettings.FacingSettings;
import espada.spacex.aurora.modules.globalsettings.RangeSettings;
import espada.spacex.aurora.modules.globalsettings.RaytraceSettings;
import espada.spacex.aurora.modules.globalsettings.RotationSettings;
import espada.spacex.aurora.modules.globalsettings.ServerSettings;
import espada.spacex.aurora.modules.globalsettings.SwingSettings;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class SettingUtils {
   private static final FacingSettings facing = (FacingSettings)Modules.get().get(FacingSettings.class);
   private static final RangeSettings range = (RangeSettings)Modules.get().get(RangeSettings.class);
   private static final RaytraceSettings raytrace = (RaytraceSettings)Modules.get().get(RaytraceSettings.class);
   private static final RotationSettings rotation = (RotationSettings)Modules.get().get(RotationSettings.class);
   private static final ServerSettings server = (ServerSettings)Modules.get().get(ServerSettings.class);
   private static final SwingSettings swing = (SwingSettings)Modules.get().get(SwingSettings.class);

   public static void registerAttack(Box bb) {
      range.registerAttack(bb);
   }

   public static double getPlaceRange() {
      return (Double)range.placeRange.get();
   }

   public static double getPlaceWallsRange() {
      return (Double)range.placeRangeWalls.get();
   }

   public static double getAttackRange() {
      return (Double)range.attackRange.get();
   }

   public static double getAttackWallsRange() {
      return (Double)range.attackRangeWalls.get();
   }

   public static double getMineRange() {
      return (Double)range.miningRange.get();
   }

   public static double getMineWallsRange() {
      return (Double)range.miningRangeWalls.get();
   }

   public static double placeRangeTo(BlockPos pos) {
      return range.placeRangeTo(pos, (Vec3d)null);
   }

   public static boolean inPlaceRange(BlockPos pos) {
      return range.inPlaceRange(pos, (Vec3d)null);
   }

   public static boolean inPlaceRange(BlockPos pos, Vec3d from) {
      return range.inPlaceRange(pos, from);
   }

   public static boolean inPlaceRangeNoTrace(BlockPos pos) {
      return range.inPlaceRangeNoTrace(pos, (Vec3d)null);
   }

   public static boolean inPlaceRangeNoTrace(BlockPos pos, Vec3d from) {
      return range.inPlaceRangeNoTrace(pos, from);
   }

   public static boolean inAttackRange(Box bb) {
      return range.inAttackRange(bb, (Vec3d)null);
   }

   public static boolean inAttackRange(Box bb, Vec3d from) {
      return range.inAttackRange(bb, from);
   }

   public static double mineRangeTo(BlockPos pos) {
      return range.miningRangeTo(pos, (Vec3d)null);
   }

   public static boolean inMineRange(BlockPos pos) {
      return range.inMineRange(pos);
   }

   public static boolean inMineRangeNoTrace(BlockPos pos) {
      return range.inMineRangeNoTrace(pos);
   }

   public static boolean inAttackRangeNoTrace(Box bb, double eyeHeight, Vec3d feet) {
      return range.inAttackRangeNoTrace(bb, feet, (Vec3d)null);
   }

   public static boolean inAttackRangeNoTrace(Box bb, double eyeHeight, Vec3d feet, Vec3d from) {
      return range.inAttackRangeNoTrace(bb, feet, from);
   }

   public static double attackRangeTo(Box bb, Vec3d feet) {
      return range.attackRangeTo(bb, feet, (Vec3d)null, true);
   }

   public static boolean startMineRot() {
      return rotation.startMineRot();
   }

   public static boolean endMineRot() {
      return rotation.endMineRot();
   }

   public static boolean shouldVanillaRotate() {
      return (Boolean)rotation.vanillaRotation.get();
   }

   public static boolean shouldRotate(RotationType type) {
      return rotation.shouldRotate(type);
   }

   public static boolean rotationCheck(Box box, RotationType type) {
      return rotation.rotationCheck(box, type);
   }

   public static void swing(SwingState state, SwingType type, Hand hand) {
      swing.swing(state, type, hand);
   }

   public static void mineSwing(SwingSettings.MiningSwingState state) {
      swing.mineSwing(state);
   }

   public static boolean shouldAirPlace() {
      return (Boolean)facing.airPlace.get();
   }

   public static PlaceData getPlaceData(BlockPos pos) {
      return facing.getPlaceData(pos, true);
   }

   public static PlaceData getPlaceDataANDDir(BlockPos pos, Predicate<Direction> predicate) {
      return facing.getPlaceDataAND(pos, predicate, (Predicate)null, true);
   }

   public static PlaceData getPlaceDataANDPos(BlockPos pos, Predicate<BlockPos> predicate) {
      return facing.getPlaceDataAND(pos, (Predicate)null, predicate, true);
   }

   public static PlaceData getPlaceDataAND(BlockPos pos, Predicate<Direction> predicateDir, Predicate<BlockPos> predicate) {
      return facing.getPlaceDataAND(pos, predicateDir, predicate, true);
   }

   public static PlaceData getPlaceDataOR(BlockPos pos, Predicate<BlockPos> predicate) {
      return facing.getPlaceDataOR(pos, predicate, true);
   }

   public static PlaceData getPlaceData(BlockPos pos, boolean ignoreContainers) {
      return facing.getPlaceData(pos, ignoreContainers);
   }

   public static PlaceData getPlaceDataANDDir(BlockPos pos, Predicate<Direction> predicate, boolean ignoreContainers) {
      return facing.getPlaceDataAND(pos, predicate, (Predicate)null, ignoreContainers);
   }

   public static PlaceData getPlaceDataANDPos(BlockPos pos, Predicate<BlockPos> predicate, boolean ignoreContainers) {
      return facing.getPlaceDataAND(pos, (Predicate)null, predicate, ignoreContainers);
   }

   public static PlaceData getPlaceDataAND(BlockPos pos, Predicate<Direction> predicateDir, Predicate<BlockPos> predicate, boolean ignoreContainers) {
      return facing.getPlaceDataAND(pos, predicateDir, predicate, ignoreContainers);
   }

   public static PlaceData getPlaceDataOR(BlockPos pos, Predicate<BlockPos> predicate, boolean ignoreContainers) {
      return facing.getPlaceDataOR(pos, predicate, ignoreContainers);
   }

   public static Direction getPlaceOnDirection(BlockPos pos) {
      return facing.getPlaceOnDirection(pos);
   }

   public static boolean shouldPlaceTrace() {
      return (Boolean)raytrace.placeTrace.get();
   }

   public static boolean shouldAttackTrace() {
      return (Boolean)raytrace.attackTrace.get();
   }

   public static boolean placeTrace(BlockPos pos) {
      return !shouldPlaceTrace() || raytrace.placeTrace(pos);
   }

   public static boolean attackTrace(Box bb) {
      return !shouldAttackTrace() || raytrace.attackTrace(bb);
   }

   public static boolean oldDamage() {
      return (Boolean)server.oldVerDamage.get();
   }

   public static boolean oldCrystals() {
      return (Boolean)server.oldVerCrystals.get();
   }

   public static boolean cc() {
      return (Boolean)server.cc.get();
   }
}
