package espada.spacex.aurora.modules.combatplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.HoleType;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.timers.TimerList;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.ExtrapolationUtils;
import espada.spacex.aurora.utils.Hole;
import espada.spacex.aurora.utils.HoleUtils;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.RotationUtils;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.meteor.BOEntityUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class HoleFillPlus extends Modules {
   private final SettingGroup sgNear;
   private final SettingGroup sgWalking;
   private final SettingGroup sgLooking;
   private final SettingGroup sgSelf;
   private final SettingGroup sgPlacing;
   private final SettingGroup sgRender;
   private final SettingGroup sgHole;
   private final Setting<Boolean> near;
   private final Setting<Double> nearDistance;
   private final Setting<Integer> nearExt;
   private final Setting<Integer> selfExt;
   private final Setting<Integer> extSmooth;
   private final Setting<Boolean> efficient;
   private final Setting<Boolean> above;
   private final Setting<Boolean> iHole;
   private final Setting<Boolean> walking;
   private final Setting<Double> walkingDist;
   private final Setting<Integer> walkMemory;
   private final Setting<Boolean> look;
   private final Setting<Double> lookDist;
   private final Setting<Integer> lookMemory;
   private final Setting<Boolean> iSelfHole;
   private final Setting<Boolean> selfAbove;
   private final Setting<Double> selfDistance;
   private final Setting<Boolean> selfWalking;
   private final Setting<Double> selfWalkingDist;
   private final Setting<Integer> selfWalkMemory;
   private final Setting<SwitchMode> switchMode;
   private final Setting<SurroundPlus.PlaceDelayMode> placeDelayMode;
   private final Setting<Integer> placeDelayT;
   private final Setting<Double> placeDelayS;
   private final Setting<Integer> places;
   private final Setting<Double> delay;
   private final Setting<List<Block>> blocks;
   private final Setting<Integer> boxExt;
   private final Setting<Integer> boxExtSmooth;
   private final Setting<Boolean> single;
   private final Setting<Boolean> doubleHole;
   private final Setting<Boolean> quad;
   private final Setting<Boolean> placeSwing;
   private final Setting<SwingHand> placeHand;
   private final Setting<Double> renderTime;
   private final Setting<Double> fadeTime;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<SettingColor> lineColor;
   private final Setting<SettingColor> sideColor;
   private final List<BlockPos> holes;
   private final TimerList<BlockPos> timers;
   private final List<Render> render;
   private final Map<AbstractClientPlayerEntity, List<Movement>> walkAngles;
   private final Map<AbstractClientPlayerEntity, List<Look>> lookAngles;
   private final Map<AbstractClientPlayerEntity, Box> nearPosition;
   private final Map<AbstractClientPlayerEntity, Box> boxes;
   private boolean shouldUpdate;
   private Hand hand;
   private int blocksLeft;
   private int placesLeft;
   private FindItemResult result;
   private boolean switched;
   private int tickTimer;
   private long lastTime;
   public static boolean placing = false;

   public HoleFillPlus() {
      super(Aurora.CombatPlus, "Hole Fill Rewrite", "Automatically is a cunt to your enemies.");
      this.sgNear = this.settings.createGroup("Near");
      this.sgWalking = this.settings.createGroup("Walking");
      this.sgLooking = this.settings.createGroup("Looking");
      this.sgSelf = this.settings.createGroup("Self");
      this.sgPlacing = this.settings.createGroup("Placing");
      this.sgRender = this.settings.createGroup("Render");
      this.sgHole = this.settings.createGroup("Hole");
      this.near = this.sgNear.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Near")).description(".")).defaultValue(true)).build());
      this.nearDistance = this.sgNear.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Near Distance")).description(".")).defaultValue((double)3.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.nearExt = this.sgNear.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Extrapolation")).description(".")).defaultValue(5)).min(0).sliderRange(0, 20).build());
      this.selfExt = this.sgNear.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Self Extrapolation")).description(".")).defaultValue(2)).min(0).sliderRange(0, 20).build());
      this.extSmooth = this.sgNear.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Extrapolation Smoothening")).description(".")).defaultValue(2)).min(1).sliderRange(0, 20).build());
      this.efficient = this.sgNear.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Efficient")).description("Only places if the hole is closer to target.")).defaultValue(true)).build());
      this.above = this.sgNear.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Above")).description("Only places if target is above the hole.")).defaultValue(true)).build());
      this.iHole = this.sgNear.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Ignore Hole")).description("Doesn't place if enemy is in a hole.")).defaultValue(true)).build());
      this.walking = this.sgWalking.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Walking")).description(".")).defaultValue(true)).build());
      this.walkingDist = this.sgWalking.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Walking Dist")).description(".")).defaultValue((double)6.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.walkMemory = this.sgWalking.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Walk Memory")).description("Fills the hole is enemy was walking to it during previous x ticks.")).defaultValue(5)).min(0).sliderRange(0, 20).build());
      this.look = this.sgLooking.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Looking")).description(".")).defaultValue(true)).build());
      this.lookDist = this.sgLooking.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Look Dist")).description(".")).defaultValue((double)10.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.lookMemory = this.sgWalking.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Look Memory")).description("Fills the hole is enemy was looking at it during previous x ticks.")).defaultValue(5)).min(0).sliderRange(0, 20).build());
      this.iSelfHole = this.sgSelf.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Ignore Self Hole")).description("Doesn't check 'efficient' if you are in a hole.")).defaultValue(true)).build());
      this.selfAbove = this.sgSelf.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Self Above")).description("Only checks 'efficient' if you are above the hole.")).defaultValue(true)).build());
      this.selfDistance = this.sgSelf.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Self Distance")).description("Doesn't place if the block is this close to you.")).defaultValue((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      SettingGroup var10001 = this.sgSelf;
      BoolSetting.Builder var10002 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Self Walking")).description("Doesn't check 'efficient' if you are in a hole.")).defaultValue(true);
      Setting<Boolean> var10003 = this.efficient;
      Objects.requireNonNull(var10003);
      this.selfWalking = var10001.add(((BoolSetting.Builder)var10002.visible(var10003::get)).build());
      this.selfWalkingDist = this.sgSelf.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Self Walk Dist")).description(".")).defaultValue((double)3.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.selfWalkMemory = this.sgSelf.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Self Walk Memory")).description("Doesn't fill any hole you were walking to during past x ticks.")).defaultValue(2)).min(0).sliderRange(0, 20).build());
      this.switchMode = this.sgPlacing.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("Method of switching. Silent is the most reliable but delays crystals on some servers.")).defaultValue(HoleFillPlus.SwitchMode.Silent)).build());
      this.placeDelayMode = this.sgPlacing.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Place Delay Mode")).description(".")).defaultValue(SurroundPlus.PlaceDelayMode.Ticks)).build());
      this.placeDelayT = this.sgPlacing.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Place Tick Delay")).description("Tick delay between places.")).defaultValue(1)).min(1).sliderRange(0, 20).visible(() -> this.placeDelayMode.get() == SurroundPlus.PlaceDelayMode.Ticks)).build());
      this.placeDelayS = this.sgPlacing.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Place Delay")).description("Delay between places.")).defaultValue(0.1).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).visible(() -> this.placeDelayMode.get() == SurroundPlus.PlaceDelayMode.Seconds)).build());
      this.places = this.sgPlacing.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Places")).description("How many blocks to place each time.")).defaultValue(1)).min(1).sliderRange(0, 20).build());
      this.delay = this.sgPlacing.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Delay")).description("Waits x seconds before trying to place at the same position if there is more than 1 missing block.")).defaultValue(0.3).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).build());
      this.blocks = this.sgPlacing.add(((BlockListSetting.Builder)((BlockListSetting.Builder)(new BlockListSetting.Builder()).name("Blocks")).description("Blocks to use.")).defaultValue(new Block[]{Blocks.OBSIDIAN}).build());
      this.boxExt = this.sgPlacing.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Box Extrapolation")).description("Enemy hitbox extrapolation")).defaultValue(0)).min(0).sliderRange(0, 20).build());
      this.boxExtSmooth = this.sgPlacing.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Box Extrapolation Smoothening")).description(".")).defaultValue(2)).min(1).sliderRange(0, 20).build());
      this.single = this.sgHole.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Single")).description("Fills 1x1 holes")).defaultValue(true)).build());
      this.doubleHole = this.sgHole.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Double")).description("Fills 2x1 block holes")).defaultValue(true)).build());
      this.quad = this.sgHole.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Quad")).description("Fills 2x2 block holes")).defaultValue(true)).build());
      this.placeSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Swing")).description("Renders swing animation when placing a block.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      EnumSetting.Builder var2 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.placeSwing;
      Objects.requireNonNull(var10003);
      this.placeHand = var10001.add(((EnumSetting.Builder)var2.visible(var10003::get)).build());
      this.renderTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Render Time")).description("How long the box should remain in full alpha.")).defaultValue(0.3).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.fadeTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fade Time")).description("How long the fading should take.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description("Which parts of boxes should be rendered.")).defaultValue(ShapeMode.Both)).build());
      this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.holes = new ArrayList();
      this.timers = new TimerList<BlockPos>();
      this.render = new ArrayList();
      this.walkAngles = new HashMap();
      this.lookAngles = new HashMap();
      this.nearPosition = new HashMap();
      this.boxes = new HashMap();
      this.shouldUpdate = false;
      this.hand = null;
      this.blocksLeft = 0;
      this.placesLeft = 0;
      this.result = null;
      this.switched = false;
      this.tickTimer = 0;
      this.lastTime = 0L;
   }

   @EventHandler(
      priority = 200
   )
   private void onTick(TickEvent.Post event) {
      this.shouldUpdate = true;
   }

   @EventHandler(
      priority = 200
   )
   private void onRender(Render3DEvent event) {
      this.timers.update();
      if (this.mc.player != null && this.mc.world != null) {
         if (this.shouldUpdate) {
            this.update();
            this.shouldUpdate = false;
         }

         this.render.removeIf((r) -> System.currentTimeMillis() - r.time > 1000L);
         this.render.forEach((r) -> {
            double progress = (double)1.0F - Math.min((double)(System.currentTimeMillis() - r.time) + (Double)this.renderTime.get() * (double)1000.0F, (Double)this.fadeTime.get() * (double)1000.0F) / ((Double)this.fadeTime.get() * (double)1000.0F);
            event.renderer.box(r.pos, new Color(((SettingColor)this.sideColor.get()).r, ((SettingColor)this.sideColor.get()).g, ((SettingColor)this.sideColor.get()).b, (int)Math.round((double)((SettingColor)this.sideColor.get()).a * progress)), new Color(((SettingColor)this.lineColor.get()).r, ((SettingColor)this.lineColor.get()).g, ((SettingColor)this.lineColor.get()).b, (int)Math.round((double)((SettingColor)this.lineColor.get()).a * progress)), (ShapeMode)this.shapeMode.get(), 0);
         });
      }
   }

   private void update() {
      ++this.tickTimer;
      this.updateMaps();
      this.updateHoles();
      this.updateResult();
      this.updatePlaces();
      this.updatePlacing();
   }

   private void updatePlacing() {
      this.blocksLeft = Math.min(this.placesLeft, this.result.count());
      this.hand = this.getHand();
      this.switched = false;
      this.holes.stream().sorted(Comparator.comparingDouble((pos) -> pos.toCenterPos().distanceTo(this.mc.player.getEyePos()))).forEach(this::place);
      if (this.switched && this.hand == null) {
         switch (((SwitchMode)this.switchMode.get()).ordinal()) {
            case 2 -> InvUtils.swapBack();
            case 3 -> BOInvUtils.pickSwapBack();
            case 4 -> BOInvUtils.swapBack();
         }
      }

   }

   private void updateResult() {
      FindItemResult var10001;
      switch (((SwitchMode)this.switchMode.get()).ordinal()) {
         case 0:
            var10001 = null;
            break;
         case 1:
         case 2:
            var10001 = InvUtils.findInHotbar(this::valid);
            break;
         case 3:
         case 4:
            var10001 = InvUtils.find(this::valid);
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      this.result = var10001;
   }

   private Hand getHand() {
      if (this.valid(Managers.HOLDING.getStack())) {
         return Hand.MAIN_HAND;
      } else {
         return this.valid(this.mc.player.getOffHandStack()) ? Hand.OFF_HAND : null;
      }
   }

   private boolean valid(ItemStack stack) {
      Item var3 = stack.getItem();
      boolean var10000;
      if (var3 instanceof BlockItem block) {
         if (((List)this.blocks.get()).contains(block.getBlock())) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   private void updateMaps() {
      this.updateWalk();
      this.updateLook();
      ExtrapolationUtils.extrapolateMap(this.nearPosition, (player) -> player == this.mc.player ? (Integer)this.selfExt.get() : (Integer)this.nearExt.get(), (player) -> (Integer)this.extSmooth.get());
      ExtrapolationUtils.extrapolateMap(this.boxes, (player) -> player == this.mc.player ? 0 : (Integer)this.boxExt.get(), (player) -> (Integer)this.boxExtSmooth.get());
   }

   private void updateWalk() {
      Map<AbstractClientPlayerEntity, List<Movement>> newMap = new HashMap();

      for(AbstractClientPlayerEntity player : this.mc.world.getPlayers()) {
         Movement m = new Movement(MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(player.getZ() - player.prevZ, player.getX() - player.prevX)) - 90.0F), player.getPos());
         if (!this.walkAngles.containsKey(player)) {
            List<Movement> l = new ArrayList();
            l.add(m);
            newMap.put(player, l);
         } else {
            List<Movement> l = (List<Movement>)(List<?>)this.walkAngles.get(player);
            l.add(0, m);
            if (l.size() > 20) {
               l.subList(20, l.size()).clear();
            }

            newMap.put(player, l);
         }
      }

      this.walkAngles.clear();
      this.walkAngles.putAll(newMap);
      newMap.clear();
   }

   private void updateLook() {
      Map<AbstractClientPlayerEntity, List<Look>> newMap = new HashMap();

      for(AbstractClientPlayerEntity player : this.mc.world.getPlayers()) {
         Look e = new Look(MathHelper.wrapDegrees(player.getYaw()), player.getPitch(), player.getEyePos());
         if (!this.lookAngles.containsKey(player)) {
            List<Look> l = new ArrayList();
            l.add(e);
            newMap.put(player, l);
         } else {
            List<Look> l = (List<Look>)(List<?>)this.lookAngles.get(player);
            l.add(0, e);
            if (l.size() > 20) {
               l.subList(20, l.size()).clear();
            }

            newMap.put(player, l);
         }
      }

      this.lookAngles.clear();
      this.lookAngles.putAll(newMap);
      newMap.clear();
   }

   private void updateHoles() {
      this.holes.clear();
      int range = (int)Math.ceil(Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()) + (double)1.0F);
      BlockPos p = BlockPos.ofFloored(this.mc.player.getEyePos());
      List<Hole> holeList = new ArrayList();

      for(int x = -range; x <= range; ++x) {
         for(int y = -range; y <= range; ++y) {
            for(int z = -range; z <= range; ++z) {
               Hole hole = HoleUtils.getHole(p.add(x, y, z));
               if (hole.type != HoleType.NotHole && ((Boolean)this.single.get() || hole.type != HoleType.Single) && ((Boolean)this.doubleHole.get() || hole.type != HoleType.DoubleX && hole.type != HoleType.DoubleZ) && ((Boolean)this.quad.get() || hole.type != HoleType.Quad)) {
                  holeList.add(hole);
               }
            }
         }
      }

      holeList.forEach((holex) -> {
         if (this.validHole(holex)) {
            Stream var10000 = Arrays.stream(holex.positions).filter(this::validPos);
            List var10001 = this.holes;
            Objects.requireNonNull(var10001);
            var10000.forEach(var10001::add);
         }
      });
   }

   private boolean validPos(BlockPos pos) {
      if (this.timers.contains(pos)) {
         return false;
      } else if (!OLEPOSSUtils.replaceable(pos)) {
         return false;
      } else {
         PlaceData data = SettingUtils.getPlaceData(pos);
         if (!data.valid()) {
            return false;
         } else if (!SettingUtils.inPlaceRange(data.pos())) {
            return false;
         } else {
            return !BOEntityUtils.intersectsWithEntity(Box.from(new BlockBox(pos)), (entity) -> !entity.isSpectator() && !(entity instanceof ItemEntity), this.boxes);
         }
      }
   }

   private boolean validHole(Hole hole) {
      double pDist = (this.nearPosition.containsKey(this.mc.player) ? this.feet((Box)this.nearPosition.get(this.mc.player)) : this.mc.player.getPos()).distanceTo(hole.middle);
      if (this.selfCheck(hole)) {
         return false;
      } else {
         for(AbstractClientPlayerEntity player : this.mc.world.getPlayers()) {
            if (!player.isSpectator() && player != this.mc.player && !(player.getHealth() <= 0.0F) && !Friends.get().isFriend(player)) {
               if (this.nearCheck(player, hole, pDist)) {
                  return true;
               }

               if (this.walkingCheck(player, hole)) {
                  return true;
               }

               if (this.lookCheck(player, hole)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private boolean selfCheck(Hole hole) {
      if (this.selfNearCheck(hole)) {
         return true;
      } else {
         return (Boolean)this.selfWalking.get() && this.walkCheck(this.mc.player, hole, (Integer)this.selfWalkMemory.get(), (Double)this.selfWalkingDist.get());
      }
   }

   private boolean selfNearCheck(Hole hole) {
      BlockPos pos = new BlockPos(this.mc.player.getBlockX(), (int)Math.round(this.mc.player.getY()), this.mc.player.getBlockZ());
      if (!(Boolean)this.iSelfHole.get() || !HoleUtils.inHole(this.mc.player) && !OLEPOSSUtils.collidable(pos)) {
         if ((Boolean)this.selfAbove.get() && this.mc.player.getY() <= hole.middle.y) {
            return false;
         } else {
            return this.mc.player.getPos().distanceTo(hole.middle) <= (Double)this.selfDistance.get();
         }
      } else {
         return false;
      }
   }

   private boolean nearCheck(AbstractClientPlayerEntity player, Hole hole, double pDist) {
      if (!(Boolean)this.near.get()) {
         return false;
      } else {
         BlockPos pos = new BlockPos(player.getBlockX(), (int)Math.round(player.getY()), player.getBlockZ());
         if ((HoleUtils.inHole(player) || OLEPOSSUtils.collidable(pos)) && (Boolean)this.iHole.get()) {
            return false;
         } else if ((Boolean)this.above.get() && player.getY() <= hole.middle.y) {
            return false;
         } else {
            double eDist = (this.nearPosition.containsKey(player) ? this.feet((Box)this.nearPosition.get(player)) : player.getPos()).distanceTo(hole.middle);
            if (eDist > (Double)this.nearDistance.get()) {
               return false;
            } else {
               return !(Boolean)this.efficient.get() || pDist >= eDist;
            }
         }
      }
   }

   private boolean walkingCheck(AbstractClientPlayerEntity player, Hole hole) {
      return !(Boolean)this.walking.get() ? false : this.walkCheck(player, hole, (Integer)this.walkMemory.get(), (Double)this.walkingDist.get());
   }

   private boolean walkCheck(AbstractClientPlayerEntity player, Hole hole, int ticks, double dist) {
      if (this.walkAngles.get(player) == null) {
         return false;
      } else {
         int i = 0;

         for(Movement m : (List<Movement>)(List<?>)this.walkAngles.get(player)) {
            ++i;
            if (i > ticks) {
               break;
            }

            if (m.movementAngle != null && !(m.vec().distanceTo(hole.middle) > dist)) {
               double yawToHole = RotationUtils.getYaw(m.vec(), hole.middle);
               double highestAngle = MathHelper.lerp(Math.min(player.getPos().distanceTo(hole.middle) / (double)8.0F, (double)1.0F), (double)90.0F, (double)0.0F);
               if (Math.abs(RotationUtils.yawAngle(yawToHole, (double)m.movementAngle)) < highestAngle) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private boolean lookCheck(AbstractClientPlayerEntity player, Hole hole) {
      if (!(Boolean)this.look.get()) {
         return false;
      } else if (this.lookAngles.get(player) == null) {
         return false;
      } else {
         int i = 0;

         for(Look l : (List<Look>)(List<?>)this.lookAngles.get(player)) {
            ++i;
            if (i > (Integer)this.lookMemory.get()) {
               break;
            }

            if (!(l.vec().distanceTo(hole.middle) > (Double)this.lookDist.get())) {
               double yawToHole = RotationUtils.getYaw(l.vec(), hole.middle);
               double highestAngle = MathHelper.lerp(Math.min(player.getPos().distanceTo(hole.middle) / (double)20.0F, (double)1.0F), (double)35.0F, (double)5.0F);
               if (Math.abs(RotationUtils.yawAngle(yawToHole, (double)l.yaw)) < highestAngle && Math.abs(RotationUtils.getPitch(l.vec, hole.middle) - (double)l.pitch()) < highestAngle) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private void updatePlaces() {
      switch ((SurroundPlus.PlaceDelayMode)this.placeDelayMode.get()) {
         case Ticks:
            if (this.placesLeft >= (Integer)this.places.get() || this.tickTimer >= (Integer)this.placeDelayT.get()) {
               this.placesLeft = (Integer)this.places.get();
               this.tickTimer = 0;
            }
            break;
         case Seconds:
            if (this.placesLeft >= (Integer)this.places.get() || (double)(System.currentTimeMillis() - this.lastTime) >= (Double)this.placeDelayS.get() * (double)1000.0F) {
               this.placesLeft = (Integer)this.places.get();
               this.lastTime = System.currentTimeMillis();
            }
      }

   }

   private void place(BlockPos pos) {
      if (this.blocksLeft > 0) {
         PlaceData data = SettingUtils.getPlaceData(pos);
         if (data != null && data.valid()) {
            placing = true;
            if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(data.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}))) {
               if (!this.switched && this.hand == null) {
                  switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                     case 1:
                     case 2:
                        InvUtils.swap(this.result.slot(), true);
                        this.switched = true;
                        break;
                     case 3:
                        this.switched = BOInvUtils.pickSwitch(this.result.slot());
                        break;
                     case 4:
                        this.switched = BOInvUtils.invSwitch(this.result.slot());
                  }
               }

               if (this.switched || this.hand != null) {
                  this.render.add(new Render(pos, System.currentTimeMillis()));
                  this.timers.add(pos, (Double)this.delay.get());
                  this.placeBlock(this.hand == null ? Hand.MAIN_HAND : this.hand, data.pos().toCenterPos(), data.dir(), data.pos());
                  if ((Boolean)this.placeSwing.get()) {
                     this.clientSwing((SwingHand)this.placeHand.get(), this.hand == null ? Hand.MAIN_HAND : this.hand);
                  }

                  --this.blocksLeft;
                  --this.placesLeft;
                  if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                     Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
                  }

               }
            }
         }
      }
   }

   private Vec3d feet(Box box) {
      return new Vec3d((box.minX + box.maxX) / (double)2.0F, box.minY, (box.minZ + box.maxZ) / (double)2.0F);
   }

   private static record Movement(Float movementAngle, Vec3d vec) {
   }

   private static record Look(float yaw, float pitch, Vec3d vec) {
   }

   private static record Render(BlockPos pos, Long time) {
   }

   public static enum SwitchMode {
      Disabled,
      Normal,
      Silent,
      PickSilent,
      InvSwitch;

      // $FF: synthetic method
      private static SwitchMode[] $values() {
         return new SwitchMode[]{Disabled, Normal, Silent, PickSilent, InvSwitch};
      }
   }

   public static enum LookCheckMode {
   }
}
