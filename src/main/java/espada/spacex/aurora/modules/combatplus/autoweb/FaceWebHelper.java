package espada.spacex.aurora.modules.combatplus.autoweb;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.utils.BOBlockUtil;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.RenderUtils;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.Timer;
import espada.spacex.aurora.utils.Util;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class FaceWebHelper extends Modules {
   private final List<Render> renderBlocks = new ArrayList();
   private final SettingGroup sgGeneral;
   private final SettingGroup sgRender;
   private final Setting<Boolean> face;
   private final Setting<Integer> surCheck;
   private final Setting<Double> minSpeed;
   private final Setting<Boolean> onlyGround;
   private final Setting<Boolean> pauseEat;
   private final Setting<Boolean> CheckMine;
   private final Setting<Boolean> CheckSelf;
   private final Setting<Boolean> CheckFriend;
   private final Setting<Double> range;
   private final Setting<Integer> multiPlace;
   private final Setting<Integer> delay;
   private List<PlayerEntity> targets;
   private final Setting<Boolean> render;
   private final Setting<Double> renderTime;
   private final Setting<Double> fadeTime;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<SettingColor> lineColor;
   private final Setting<SettingColor> sideColor;
   private int progress;
   private final Timer timer;

   public FaceWebHelper() {
      super(Aurora.CombatPlus, "FaceWeb", "FaceWeb with autoweb.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgRender = this.settings.createGroup("Render");
      this.face = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Face")).description("tou.")).defaultValue(true)).build());
      this.surCheck = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Surround Check")).defaultValue(5)).min(0).sliderRange(0, 5).build());
      this.minSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("target min speed")).description("ddd.")).defaultValue((double)2.0F).range((double)0.0F, (double)5.0F).sliderMax((double)5.0F).build());
      this.onlyGround = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Only Ground")).description("Pauses when you are fffffff.")).defaultValue(false)).build());
      this.pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause Eat")).description("Pauses when you are eating.")).defaultValue(true)).build());
      this.CheckMine = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("CheckMine")).description("11")).defaultValue(true)).build());
      this.CheckSelf = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("CheckSelf")).description("11")).defaultValue(true)).build());
      this.CheckFriend = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("CheckFriend")).description("11")).defaultValue(true)).build());
      this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("target-range")).description("The maximum distance to target players.")).defaultValue((double)3.5F).range((double)0.0F, (double)8.0F).sliderMax((double)8.0F).build());
      this.multiPlace = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("MultiPlace")).defaultValue(5)).min(1).sliderRange(1, 5).build());
      this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Delay")).defaultValue(35)).min(0).sliderRange(0, 2000).build());
      this.targets = new ArrayList();
      this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Render")).description("")).defaultValue(true)).build());
      this.renderTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Render Time")).description("How long the box should remain in full alpha.")).defaultValue(0.3).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.fadeTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fade Time")).description("How long the fading should take.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      SettingGroup var10001 = this.sgRender;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description("Which parts of the boxes should be rendered.")).defaultValue(ShapeMode.Sides);
      Setting<Boolean> var10003 = this.render;
      Objects.requireNonNull(var10003);
      this.shapeMode = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 255)).visible(() -> (Boolean)this.render.get() && (((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Lines) || ((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Both)))).build());
      this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 20)).visible(() -> (Boolean)this.render.get() && (((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Sides) || ((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Both)))).build());
      this.progress = 0;
      this.timer = new Timer();
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
      if (this.timer.passedMs((long)(Integer)this.delay.get()) && (!(Boolean)this.onlyGround.get() || this.mc.player.isOnGround()) && this.pauseCheck() && InvUtils.find(new Item[]{Items.COBWEB}).found()) {
         this.progress = 0;

         for(PlayerEntity target : this.targets) {
            if ((Boolean)this.face.get()) {
               this.placeWeb(this.getPlaceBlock(target, (double)1.0F));
            }

            if ((Boolean)this.face.get() && ((double)target.speed < (Double)this.minSpeed.get() || this.surCheck(target))) {
               return;
            }
         }
      }

   }

   private static boolean isWeb(BlockPos pos) {
      if (Util.mc.world != null && Util.mc.player != null && pos != null) {
         return Util.mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB || Util.mc.player.getBlockPos().equals(pos);
      } else {
         return false;
      }
   }

   public boolean isInWeb(PlayerEntity player) {
      if (isWeb(this.getPlaceBlock(player, (double)-1.0F))) {
         return true;
      } else {
         return isWeb(this.getPlaceBlock(player, (double)0.0F)) ? true : isWeb(this.getPlaceBlock(player, (double)1.0F));
      }
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

   protected BlockPos getPlaceBlock(PlayerEntity player, double y) {
      LinkedHashSet<BlockPos> feetBlock = this.getAllPos(player, y);
      List<BlockPos> collect = feetBlock.stream().filter(BOBlockUtil::isAir).filter((p) -> !BOBlockUtil.cantBlockPlace(p)).limit(1L).toList();
      return collect.size() == 0 ? null : (BlockPos)collect.get(0);
   }

   public LinkedHashSet<BlockPos> getAllPos(PlayerEntity player, double yOff) {
      LinkedHashSet<BlockPos> set = new LinkedHashSet();
      if (player != null) {
         set.add(BOBlockUtil.vec3toBlockPos(player.getPos().add((double)0.0F, yOff, (double)0.0F)));
         set.add(BOBlockUtil.vec3toBlockPos(player.getPos().add(0.2, yOff, 0.2)));
         set.add(BOBlockUtil.vec3toBlockPos(player.getPos().add(-0.2, yOff, 0.2)));
         set.add(BOBlockUtil.vec3toBlockPos(player.getPos().add(0.2, yOff, -0.2)));
         set.add(BOBlockUtil.vec3toBlockPos(player.getPos().add(-0.2, yOff, -0.2)));
      }

      return set;
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

      this.targets = players;
   }

   private void placeWeb(BlockPos pos) {
      PlaceData data = SettingUtils.getPlaceData(pos);
      if (data.valid() && this.progress < (Integer)this.multiPlace.get() && this.mc.world.isAir(pos) && this.mc.world.isAir(pos.up()) && (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(data.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"})))) {
         if (Managers.BREAK.isMine(pos, true) && (Boolean)this.CheckMine.get() || this.isSelf(pos) || this.isFriend(pos)) {
            return;
         }

         InvUtils.swap(InvUtils.findInHotbar(new Item[]{Items.COBWEB}).slot(), true);
         this.renderBlocks.add(new Render(pos, System.currentTimeMillis()));
         this.placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());
         if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
         }

         InvUtils.swapBack();
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
         if ((Boolean)FaceWebHelper.this.render.get()) {
            FaceWebHelper.this.renderBlocks.removeIf((r) -> System.currentTimeMillis() - r.time > 1000L);
            FaceWebHelper.this.renderBlocks.forEach((r) -> {
               double progress = (double)1.0F - Math.min((double)(System.currentTimeMillis() - r.time) + (Double)FaceWebHelper.this.renderTime.get() * (double)1000.0F, (Double)FaceWebHelper.this.fadeTime.get() * (double)1000.0F) / ((Double)FaceWebHelper.this.fadeTime.get() * (double)1000.0F);
               event.renderer.box(r.blockPos, RenderUtils.injectAlpha((Color)FaceWebHelper.this.sideColor.get(), (int)Math.round((double)((SettingColor)FaceWebHelper.this.sideColor.get()).a * progress)), RenderUtils.injectAlpha((Color)FaceWebHelper.this.lineColor.get(), (int)Math.round((double)((SettingColor)FaceWebHelper.this.lineColor.get()).a * progress)), (ShapeMode)FaceWebHelper.this.shapeMode.get(), 0);
            });
         }
      }
   }
}
