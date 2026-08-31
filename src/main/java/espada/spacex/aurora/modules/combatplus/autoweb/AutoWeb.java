package espada.spacex.aurora.modules.combatplus.autoweb;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.modules.combatplus.MaoJunQingAura;
import espada.spacex.aurora.utils.BOBlockUtil;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.ExtrapolationUtils;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.RenderUtils;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.Timer;
import espada.spacex.aurora.utils.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class AutoWeb extends Modules {
   private final List<Render> renderBlocks = new ArrayList();
   private final SettingGroup sgGeneral;
   private final SettingGroup sgPredict;
   private final SettingGroup sgRender;
   private final Setting<Boolean> down;
   private final Setting<Boolean> feet;
   private final Setting<Boolean> face;
   private final Setting<Integer> surCheck;
   private final Setting<Integer> surCheck2;
   private final Setting<Double> minSpeed;
   private final Setting<Boolean> onlyGround;
   private final Setting<Boolean> pauseEat;
   private final Setting<Boolean> CheckMine;
   private final Setting<Boolean> CheckSelf;
   private final Setting<Boolean> CheckFriend;
   private final Setting<Boolean> OnAnchorPlacePause;
   private final Setting<Double> range;
   private final Setting<Integer> multiPlace;
   private final Setting<Integer> delay;
   private final Setting<Integer> delay2;
   private final Setting<Integer> delay3;
   private List<PlayerEntity> targets;
   private final Setting<Boolean> prediction;
   private final Setting<Boolean> smooth;
   private final Setting<Integer> tick;
   private final Setting<Integer> selfExt;
   private final Setting<Integer> extrapolation;
   private final Setting<Integer> extSmoothness;
   private final Setting<Boolean> placeSwing;
   private final Setting<SwingHand> placeHand;
   private final Setting<Boolean> render;
   private final Setting<Double> renderTime;
   private final Setting<Double> fadeTime;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<SettingColor> lineColor;
   private final Setting<SettingColor> sideColor;
   private final Map<AbstractClientPlayerEntity, Box> extMap;
   private int progress;
   private final Timer timer;
   private final MaoJunQingAura autoAnchor;

   public AutoWeb() {
      super(Aurora.CombatPlus, "AutoWeb", "Automatically places webs on other players.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgPredict = this.settings.createGroup("Predict");
      this.sgRender = this.settings.createGroup("Render");
      this.down = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Down")).description("1")).defaultValue(true)).build());
      this.feet = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Feet")).description("ji ao.")).defaultValue(true)).build());
      this.face = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Face")).description("tou.")).defaultValue(true)).build());
      this.surCheck = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("PeetPlace Surround Check")).defaultValue(5)).min(0).sliderRange(0, 5).build());
      this.surCheck2 = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("FacePlace Surround Check")).defaultValue(5)).min(0).sliderRange(0, 5).build());
      this.minSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("target min speed")).description("ddd.")).defaultValue((double)2.0F).range((double)0.0F, (double)5.0F).sliderMax((double)5.0F).build());
      this.onlyGround = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Only Ground")).description("Pauses when you are fffffff.")).defaultValue(false)).build());
      this.pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause Eat")).description("Pauses when you are eating.")).defaultValue(true)).build());
      this.CheckMine = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("CheckMine")).description("11")).defaultValue(true)).build());
      this.CheckSelf = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("CheckSelf")).description("11")).defaultValue(true)).build());
      this.CheckFriend = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("CheckFriend")).description("11")).defaultValue(true)).build());
      this.OnAnchorPlacePause = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("On AnchorPlace Pause")).description("Pause.")).defaultValue(true)).build());
      this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("target-range")).description("The maximum distance to target players.")).defaultValue((double)3.5F).range((double)0.0F, (double)8.0F).sliderMax((double)8.0F).build());
      this.multiPlace = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("MultiPlace")).defaultValue(5)).min(1).sliderRange(1, 5).build());
      this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("FeetPlace Delay")).defaultValue(35)).min(0).sliderRange(0, 2000).build());
      this.delay2 = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("FacePlace Delay")).defaultValue(35)).min(0).sliderRange(0, 2000).build());
      this.delay3 = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("DownPlace Delay")).defaultValue(35)).min(0).sliderRange(0, 2000).build());
      this.targets = new ArrayList();
      this.prediction = this.sgPredict.add(((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Prediction")).defaultValue(true)).build());
      this.smooth = this.sgPredict.add(((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Smooth")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgPredict;
      IntSetting.Builder var10002 = ((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Prediction Tick")).defaultValue(1)).sliderRange(0, 10);
      Setting<Boolean> var10003 = this.prediction;
      Objects.requireNonNull(var10003);
      this.tick = var10001.add(((IntSetting.Builder)var10002.visible(var10003::get)).build());
      this.selfExt = this.sgPredict.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Self Extrapolation")).description("How many ticks of movement should be predicted for self damage checks.")).defaultValue(0)).range(0, 100).sliderMax(20).build());
      this.extrapolation = this.sgPredict.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Extrapolation")).description("How many ticks of movement should be predicted for enemy damage checks.")).defaultValue(0)).range(0, 100).sliderMax(20).build());
      this.extSmoothness = this.sgPredict.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Extrapolation Smoothening")).description("How many earlier ticks should be used in average calculation for extrapolation motion.")).defaultValue(2)).range(1, 20).sliderRange(1, 20).build());
      this.placeSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Place Swing")).description("Renders swing animation when placing a block.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      EnumSetting.Builder var3 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Place Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.placeSwing;
      Objects.requireNonNull(var10003);
      this.placeHand = var10001.add(((EnumSetting.Builder)var3.visible(var10003::get)).build());
      this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Render")).description("")).defaultValue(true)).build());
      this.renderTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Render Time")).description("How long the box should remain in full alpha.")).defaultValue(0.3).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.fadeTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fade Time")).description("How long the fading should take.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      var10001 = this.sgRender;
      var3 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description("Which parts of the boxes should be rendered.")).defaultValue(ShapeMode.Sides);
      var10003 = this.render;
      Objects.requireNonNull(var10003);
      this.shapeMode = var10001.add(((EnumSetting.Builder)var3.visible(var10003::get)).build());
      this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 255)).visible(() -> (Boolean)this.render.get() && (((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Lines) || ((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Both)))).build());
      this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 20)).visible(() -> (Boolean)this.render.get() && (((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Sides) || ((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Both)))).build());
      this.extMap = new HashMap();
      this.progress = 0;
      this.timer = new Timer();
      this.autoAnchor = (MaoJunQingAura)meteordevelopment.meteorclient.systems.modules.Modules.get().get(MaoJunQingAura.class);
      MeteorClient.EVENT_BUS.subscribe(new Renderer());
   }

   @EventHandler(
      priority = 200
   )
   private void onTickPre(TickEvent.Post event) {
      this.updateTargets();
   }

   public String getInfoString() {
      for(PlayerEntity target : this.targets) {
         if (target != null) {
            return target.getGameProfile().getName();
         }
      }

      return null;
   }

   @EventHandler
   private void onTick(TickEvent.Pre event) {
      if ((!(Boolean)this.onlyGround.get() || this.mc.player.isOnGround()) && this.pauseCheck() && BOInvUtils.findHotbarBlock(Blocks.COBWEB) != -1) {
         this.progress = 0;
         if (this.isAnchor()) {
            return;
         }

         for(PlayerEntity target : this.targets) {
            LinkedHashSet<BlockPos> set = new LinkedHashSet();
            if ((Boolean)this.down.get() && this.timer.passedMs((long)(Integer)this.delay3.get())) {
               set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add((double)0.0F, (double)-1.0F, (double)0.0F))));
               set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, (double)-1.0F, 0.2))));
               set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, (double)-1.0F, 0.2))));
               set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, (double)-1.0F, -0.2))));
               set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, (double)-1.0F, -0.2))));
            }

            if ((Boolean)this.face.get() && this.timer.passedMs((long)(Integer)this.delay2.get())) {
               if (this.surCheck2(target)) {
                  return;
               }

               set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, (double)1.0F, 0.2))));
               set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, (double)1.0F, 0.2))));
               set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, (double)1.0F, -0.2))));
               set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, (double)1.0F, -0.2))));
            }

            if ((Boolean)this.feet.get() && this.timer.passedMs((long)(Integer)this.delay.get())) {
               if ((double)target.speed < (Double)this.minSpeed.get() || this.surCheck(target)) {
                  return;
               }

               set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, (double)0.0F, 0.2))));
               set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, (double)0.0F, 0.2))));
               set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(-0.2, (double)0.0F, -0.2))));
               set.add(new BlockPos(BOBlockUtil.vec3toBlockPos(target.getPos().add(0.2, (double)0.0F, -0.2))));
            }

            List<BlockPos> collect = set.stream().filter(BOBlockUtil::isAir).filter((p) -> !BOBlockUtil.cantBlockPlace(p)).filter((p) -> !Managers.BREAK.isMine(p, false) || !(Boolean)this.CheckMine.get()).filter((p) -> !this.isSelf(p)).filter((p) -> !this.isFriend(p)).limit(1L).toList();
            this.placeWeb(collect.isEmpty() ? null : (BlockPos)collect.get(0));
         }
      }

   }

   private boolean isAnchor() {
      return (Boolean)this.OnAnchorPlacePause.get() && this.autoAnchor.Exploding();
   }

   public boolean surCheck(PlayerEntity player) {
      int n = 0;
      BlockPos pos = player.getBlockPos();
      if (!BOBlockUtil.isAir(pos.add(0, 0, 1))) {
         ++n;
      }

      if (!BOBlockUtil.isAir(pos.add(0, 0, -1))) {
         ++n;
      }

      if (!BOBlockUtil.isAir(pos.add(1, 0, 0))) {
         ++n;
      }

      if (!BOBlockUtil.isAir(pos.add(-1, 0, 0))) {
         ++n;
      }

      return n > (Integer)this.surCheck.get();
   }

   public boolean surCheck2(PlayerEntity player) {
      int n = 0;
      BlockPos pos = player.getBlockPos();
      if (!BOBlockUtil.isAir(pos.add(0, 0, 1))) {
         ++n;
      }

      if (!BOBlockUtil.isAir(pos.add(0, 0, -1))) {
         ++n;
      }

      if (!BOBlockUtil.isAir(pos.add(1, 0, 0))) {
         ++n;
      }

      if (!BOBlockUtil.isAir(pos.add(-1, 0, 0))) {
         ++n;
      }

      return n > (Integer)this.surCheck2.get();
   }

   private void updateTargets() {
      List<PlayerEntity> players = new ArrayList();
      double closestDist = (double)1000.0F;

      for(int i = 3; i > 0; --i) {
         PlayerEntity closest = null;

         for(PlayerEntity player : this.mc.world.getPlayers()) {
            if (!players.contains(player) && !Friends.get().isFriend(player) && player != this.mc.player && !player.isDead()) {
               double dist = (double)player.distanceTo(this.mc.player);
               if (!(dist > (Double)this.range.get()) && !this.surCheck(player) && (closest == null || dist < closestDist)) {
                  closestDist = dist;
                  closest = player;
               }
            }
         }

         if (closest != null) {
            players.add(closest);
         }
      }

      ExtrapolationUtils.extrapolateMap(this.extMap, (playerx) -> playerx == this.mc.player ? (Integer)this.selfExt.get() : (Integer)this.extrapolation.get(), (playerx) -> (Integer)this.extSmoothness.get());
      this.targets = players;
   }

   private void placeWeb(BlockPos pos) {
      PlaceData data = SettingUtils.getPlaceData(pos);
      if (data.valid() && this.progress < (Integer)this.multiPlace.get() && this.mc.world.isAir(pos) && SettingUtils.inPlaceRange(pos) && (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(data.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"})))) {
         int Old = this.mc.player.getInventory().selectedSlot;
         BOInvUtils.doSwap(BOInvUtils.findHotbarBlock(Blocks.COBWEB));
         this.renderBlocks.add(new Render(pos, System.currentTimeMillis()));
         BOBlockUtil.placeBlock(pos, Hand.MAIN_HAND, false, 1);
         if ((Boolean)this.placeSwing.get()) {
            this.clientSwing((SwingHand)this.placeHand.get(), Hand.MAIN_HAND);
         }

         BOInvUtils.doSwap(Old);
         if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
         }

         ++this.progress;
         this.timer.reset();
      }

   }

   private boolean isSelf(BlockPos pos) {
      if (!(Boolean)this.CheckSelf.get()) {
         return false;
      } else {
         for(Entity entity : Util.mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
            if (entity == Util.mc.player) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean isFriend(BlockPos pos) {
      if (!(Boolean)this.CheckFriend.get()) {
         return false;
      } else {
         for(PlayerEntity entity : Util.mc.world.getNonSpectatingEntities(PlayerEntity.class, new Box(pos))) {
            if (Friends.get().isFriend(entity)) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean pauseCheck() {
      return !(Boolean)this.pauseEat.get() || !this.mc.player.isUsingItem();
   }

   public static record Render(BlockPos blockPos, long time) {
   }

   private class Renderer {
      @EventHandler
      private void onRender(Render3DEvent event) {
         if ((Boolean)AutoWeb.this.render.get()) {
            AutoWeb.this.renderBlocks.removeIf((r) -> System.currentTimeMillis() - r.time > 1000L);
            AutoWeb.this.renderBlocks.forEach((r) -> {
               double progress = (double)1.0F - Math.min((double)(System.currentTimeMillis() - r.time) + (Double)AutoWeb.this.renderTime.get() * (double)1000.0F, (Double)AutoWeb.this.fadeTime.get() * (double)1000.0F) / ((Double)AutoWeb.this.fadeTime.get() * (double)1000.0F);
               event.renderer.box(r.blockPos, RenderUtils.injectAlpha((Color)AutoWeb.this.sideColor.get(), (int)Math.round((double)((SettingColor)AutoWeb.this.sideColor.get()).a * progress)), RenderUtils.injectAlpha((Color)AutoWeb.this.lineColor.get(), (int)Math.round((double)((SettingColor)AutoWeb.this.lineColor.get()).a * progress)), (ShapeMode)AutoWeb.this.shapeMode.get(), 0);
            });
         }
      }
   }
}
