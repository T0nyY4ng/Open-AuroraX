package espada.spacex.aurora.modules.combatplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.utils.BOBlockUtil;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.RenderUtils;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
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
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ButtonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockBox;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;

public class AutoAnvil extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgSwitch;
   private final SettingGroup sgDelay;
   private final SettingGroup sgRender;
   private final Setting<Integer> height;
   private final Setting<Boolean> placeButton;
   private final Setting<Boolean> multiPlace;
   private final Setting<Boolean> toggleOnBreak;
   private final Setting<SwitchMode> switchMode;
   private final Setting<HelperSwitchMode> helperSwitchMode;
   private final Setting<Integer> delay;
   private final Setting<Integer> helperDelay;
   private final Setting<Boolean> placeSwing;
   private final Setting<SwingHand> placeHand;
   private final Setting<Boolean> renderTargetEsp;
   private final Setting<SettingColor> color;
   private final Setting<Boolean> renderAnvil;
   private final Setting<Double> anvilRenderTime;
   private final Setting<Double> anvilFadeTime;
   private final Setting<ShapeMode> anvilShapeMode;
   private final Setting<SettingColor> anvilLineColor;
   private final Setting<SettingColor> anvilSideColor;
   private final Setting<Boolean> renderHelper;
   private final Setting<Double> helperRenderTime;
   private final Setting<Double> helperFadeTime;
   private final Setting<ShapeMode> helperShapeMode;
   private final Setting<SettingColor> helperLineColor;
   private final Setting<SettingColor> helperSideColor;
   private PlayerEntity target;
   private boolean canRenderTarget;
   private int timer;
   private final Timer helperTimer;
   private final List<Render> renderAnvilPlacing;
   private final List<Render> renderHelperPlacing;

   public AutoAnvil() {
      super(Aurora.CombatPlus, "Auto Anvil", "Automatically places anvils above players to destroy helmets.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgSwitch = this.settings.createGroup("Switch");
      this.sgDelay = this.settings.createGroup("Delay");
      this.sgRender = this.settings.createGroup("Render");
      this.height = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Height")).description("The height to place anvils at.")).defaultValue(2)).range(0, 5).sliderMax(5).build());
      this.placeButton = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Place At Feet")).description("Automatically places a button or pressure plate at the targets feet to break the anvils.")).defaultValue(true)).build());
      this.multiPlace = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Multi Place")).description("Places multiple anvils at once.")).defaultValue(true)).build());
      this.toggleOnBreak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Toggle On Break")).description("Toggles when the target's helmet slot is empty.")).defaultValue(false)).build());
      this.switchMode = this.sgSwitch.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("Switching method. Silent is the most reliable but doesn't work everywhere.")).defaultValue(AutoAnvil.SwitchMode.Silent)).build());
      this.helperSwitchMode = this.sgSwitch.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("Switching method. Silent is the most reliable but doesn't work everywhere.")).defaultValue(AutoAnvil.HelperSwitchMode.Silent)).build());
      this.delay = this.sgDelay.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Delay")).description("The delay in between anvil placements.")).defaultValue(10)).min(0).sliderMax(50).build());
      this.helperDelay = this.sgDelay.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Helper Delay")).description("The delay in between helper block placements.")).defaultValue(1)).min(0).sliderMax(50).build());
      this.placeSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Place Swing")).description("Renders swing animation when placing a block.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgRender;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Place Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      Setting<Boolean> var10003 = this.placeSwing;
      Objects.requireNonNull(var10003);
      this.placeHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.renderTargetEsp = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Render Target")).description("Render on target.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      ColorSetting.Builder var5 = ((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Color")).description("COLOR")).defaultValue(new SettingColor(149, 149, 149, 170));
      var10003 = this.renderTargetEsp;
      Objects.requireNonNull(var10003);
      this.color = var10001.add(((ColorSetting.Builder)var5.visible(var10003::get)).build());
      this.renderAnvil = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Render Anvil")).description("Renders the anvil where it is placed.")).defaultValue(true)).visible(() -> !SettingUtils.shouldAirPlace())).build());
      var10001 = this.sgRender;
      DoubleSetting.Builder var6 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Anvil Render Time")).description("How long the box should remain in full alpha.")).defaultValue(0.3).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F);
      var10003 = this.renderAnvil;
      Objects.requireNonNull(var10003);
      this.anvilRenderTime = var10001.add(((DoubleSetting.Builder)var6.visible(var10003::get)).build());
      var10001 = this.sgRender;
      var6 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Helper Fade Time")).description("How long the fading should take.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F);
      var10003 = this.renderAnvil;
      Objects.requireNonNull(var10003);
      this.anvilFadeTime = var10001.add(((DoubleSetting.Builder)var6.visible(var10003::get)).build());
      var10001 = this.sgRender;
      EnumSetting.Builder var8 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Anvil Shape Mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Sides);
      var10003 = this.renderAnvil;
      Objects.requireNonNull(var10003);
      this.anvilShapeMode = var10001.add(((EnumSetting.Builder)var8.visible(var10003::get)).build());
      this.anvilLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Anvil Line Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 255)).visible(() -> (Boolean)this.renderAnvil.get() && ((ShapeMode)this.anvilShapeMode.get()).lines())).build());
      this.anvilSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Anvil Side Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 50)).visible(() -> (Boolean)this.renderAnvil.get() && ((ShapeMode)this.anvilShapeMode.get()).sides())).build());
      this.renderHelper = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Render Helper")).description("Renders the block where it is placed.")).defaultValue(true)).visible(() -> !SettingUtils.shouldAirPlace())).build());
      this.helperRenderTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Helper Render Time")).description("How long the box should remain in full alpha.")).defaultValue(0.3).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).visible(() -> !SettingUtils.shouldAirPlace() && (Boolean)this.renderHelper.get())).build());
      this.helperFadeTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Helper Fade Time")).description("How long the fading should take.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).visible(() -> !SettingUtils.shouldAirPlace() && (Boolean)this.renderHelper.get())).build());
      this.helperShapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Helper Shape Mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Sides)).visible(() -> !SettingUtils.shouldAirPlace() && (Boolean)this.renderHelper.get())).build());
      this.helperLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Helper Line Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 255)).visible(() -> !SettingUtils.shouldAirPlace() && (Boolean)this.renderHelper.get() && ((ShapeMode)this.helperShapeMode.get()).lines())).build());
      this.helperSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Helper Side Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 50)).visible(() -> !SettingUtils.shouldAirPlace() && (Boolean)this.renderHelper.get() && ((ShapeMode)this.helperShapeMode.get()).sides())).build());
      this.helperTimer = new Timer();
      this.renderAnvilPlacing = new ArrayList();
      this.renderHelperPlacing = new ArrayList();
   }

   public void onActivate() {
      this.timer = 0;
      this.target = null;
      this.canRenderTarget = false;
      this.helperTimer.reset();
   }

   @EventHandler
   private void onOpenScreen(OpenScreenEvent event) {
      if (event.screen instanceof AnvilScreen) {
         event.cancel();
      }

   }

   @EventHandler
   private void onTick(TickEvent.Pre event) {
      if ((Boolean)this.toggleOnBreak.get() && this.target != null && this.target.getInventory().getArmorStack(3).isEmpty()) {
         this.error("Target head slot is empty... disabling.", new Object[0]);
         this.toggle();
      } else {
         this.updateTargets();
         if ((Boolean)this.placeButton.get()) {
            this.placeButton();
         }

         if (this.timer >= (Integer)this.delay.get()) {
            this.timer = 0;
            FindItemResult anvil = InvUtils.findInHotbar((itemStack) -> Block.getBlockFromItem(itemStack.getItem()) instanceof AnvilBlock);
            if (!anvil.found()) {
               return;
            }

            for(int i = (Integer)this.height.get(); i > 1; --i) {
               BlockPos blockPos = this.target.getBlockPos().up().add(0, i, 0);
               if (this.canRenderTarget = SettingUtils.inPlaceRange(blockPos)) {
                  for(int j = 0; j < i && this.mc.world.getBlockState(this.target.getBlockPos().up(j + 1)).isReplaceable(); ++j) {
                  }

                  if (this.placeAnvil(blockPos, anvil) && !(Boolean)this.multiPlace.get()) {
                     break;
                  }
               }
            }
         } else {
            ++this.timer;
         }

      }
   }

   @EventHandler
   private void onRender3D(Render3DEvent event) {
      if ((Boolean)this.renderTargetEsp.get() && this.target != null && this.canRenderTarget) {
         RenderUtils.drawJello(event.matrices, this.target, (Color)this.color.get());
      }

      if ((Boolean)this.renderHelper.get()) {
         this.renderHelperPlacing.removeIf((r) -> System.currentTimeMillis() - r.time > 1000L);
         this.renderHelperPlacing.forEach((r) -> {
            double progress = (double)1.0F - Math.min((double)(System.currentTimeMillis() - r.time) + (Double)this.helperRenderTime.get() * (double)1000.0F, (Double)this.helperFadeTime.get() * (double)1000.0F) / ((Double)this.helperFadeTime.get() * (double)1000.0F);
            event.renderer.box(r.pos, RenderUtils.injectAlpha((Color)this.helperSideColor.get(), (int)Math.round((double)((SettingColor)this.helperSideColor.get()).a * progress)), RenderUtils.injectAlpha((Color)this.helperLineColor.get(), (int)Math.round((double)((SettingColor)this.helperLineColor.get()).a * progress)), (ShapeMode)this.helperShapeMode.get(), 0);
         });
      }

      if ((Boolean)this.renderAnvil.get()) {
         this.renderAnvilPlacing.removeIf((r) -> System.currentTimeMillis() - r.time > 1000L);
         this.renderAnvilPlacing.forEach((r) -> {
            double progress = (double)1.0F - Math.min((double)(System.currentTimeMillis() - r.time) + (Double)this.anvilRenderTime.get() * (double)1000.0F, (Double)this.anvilFadeTime.get() * (double)1000.0F) / ((Double)this.anvilFadeTime.get() * (double)1000.0F);
            event.renderer.box(r.pos, RenderUtils.injectAlpha((Color)this.anvilSideColor.get(), (int)Math.round((double)((SettingColor)this.anvilSideColor.get()).a * progress)), RenderUtils.injectAlpha((Color)this.anvilLineColor.get(), (int)Math.round((double)((SettingColor)this.anvilLineColor.get()).a * progress)), (ShapeMode)this.anvilShapeMode.get(), 0);
         });
      }

   }

   private void placeButton() {
      FindItemResult result = !((SwitchMode)this.switchMode.get()).equals(AutoAnvil.SwitchMode.Silent) ? InvUtils.find((itemStack) -> Block.getBlockFromItem(itemStack.getItem()) instanceof AbstractPressurePlateBlock || Block.getBlockFromItem(itemStack.getItem()) instanceof ButtonBlock) : InvUtils.findInHotbar((itemStack) -> Block.getBlockFromItem(itemStack.getItem()) instanceof AbstractPressurePlateBlock || Block.getBlockFromItem(itemStack.getItem()) instanceof ButtonBlock);
      if (result.found()) {
         BlockPos placePos = this.target.getBlockPos();
         if (this.canRenderTarget = SettingUtils.inPlaceRange(placePos)) {
            if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(placePos, (double)0.0F, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}))) {
               switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                  case 0 -> InvUtils.swap(result.slot(), true);
                  case 1 -> BOInvUtils.invSwitch(result.slot());
                  case 2 -> BOInvUtils.pickSwitch(result.slot());
               }

               this.placeBlock(placePos, result, false);
               if ((Boolean)this.placeSwing.get()) {
                  this.clientSwing((SwingHand)this.placeHand.get(), Hand.MAIN_HAND);
               }

               switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                  case 0 -> InvUtils.swapBack();
                  case 1 -> BOInvUtils.invSwapBack();
                  case 2 -> BOInvUtils.pickSwapBack();
               }

               if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                  Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
               }

            }
         }
      }
   }

   private boolean placeAnvil(BlockPos blockPos, FindItemResult result) {
      if (!SettingUtils.shouldAirPlace() && !this.placeHelper(blockPos)) {
         return false;
      } else {
         PlaceData data = SettingUtils.getPlaceData(blockPos);
         if (data.valid()) {
            if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(data.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}))) {
               return false;
            }

            boolean var10000;
            switch (((SwitchMode)this.switchMode.get()).ordinal()) {
               case 0 -> var10000 = InvUtils.swap(result.slot(), true);
               case 1 -> var10000 = BOInvUtils.invSwitch(result.slot());
               case 2 -> var10000 = BOInvUtils.pickSwitch(result.slot());
               default -> throw new MatchException((String)null, (Throwable)null);
            }

            boolean switched = var10000;
            if (!switched) {
               return false;
            }

            this.placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());
            this.renderAnvilPlacing.add(new Render(blockPos, System.currentTimeMillis()));
            if ((Boolean)this.placeSwing.get()) {
               this.clientSwing((SwingHand)this.placeHand.get(), Hand.MAIN_HAND);
            }

            switch (((SwitchMode)this.switchMode.get()).ordinal()) {
               case 0 -> InvUtils.swapBack();
               case 1 -> BOInvUtils.invSwapBack();
               case 2 -> BOInvUtils.pickSwapBack();
            }

            if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
               Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
            }
         }

         return true;
      }
   }

   private boolean placeHelper(BlockPos helpBlockPos) {
      if (!this.helperTimer.passedMs(((Integer)this.helperDelay.get()).longValue())) {
         return false;
      } else {
         for(BlockPos blockPos : this.getHelper(helpBlockPos)) {
            FindItemResult result = !((HelperSwitchMode)this.helperSwitchMode.get()).equals(AutoAnvil.HelperSwitchMode.Silent) ? InvUtils.find(new Item[]{Items.OBSIDIAN}) : InvUtils.findInHotbar(new Item[]{Items.OBSIDIAN});
            if (!result.found()) {
               return true;
            }

            PlaceData data = SettingUtils.getPlaceData(blockPos);
            if (data.valid()) {
               if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(data.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}))) {
                  return false;
               }

               boolean var10000;
               switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                  case 0 -> var10000 = InvUtils.swap(result.slot(), true);
                  case 1 -> var10000 = BOInvUtils.invSwitch(result.slot());
                  case 2 -> var10000 = BOInvUtils.pickSwitch(result.slot());
                  default -> throw new MatchException((String)null, (Throwable)null);
               }

               boolean switched = var10000;
               if (!switched) {
                  return false;
               }

               this.placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());
               if ((Boolean)this.placeSwing.get()) {
                  this.clientSwing((SwingHand)this.placeHand.get(), Hand.MAIN_HAND);
               }

               this.helperDelay.reset();
               switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                  case 0 -> InvUtils.swapBack();
                  case 1 -> BOInvUtils.invSwapBack();
                  case 2 -> BOInvUtils.pickSwapBack();
               }

               if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                  Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
               }
            }
         }

         return true;
      }
   }

   private List<BlockPos> getHelper(BlockPos block) {
      List<BlockPos> list = new ArrayList();
      if (!BOBlockUtil.replaceable(block)) {
         return list;
      } else if (SettingUtils.getPlaceData(block).valid()) {
         return list;
      } else {
         Direction support1 = this.getSupport(block);
         if (support1 != null) {
            this.renderHelperPlacing.add(new Render(block.offset(support1), System.currentTimeMillis()));
            if (block.offset(support1) != block && !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block.offset(support1))), (entity) -> !entity.isSpectator() && !(entity instanceof ItemEntity))) {
               list.add(block.offset(support1));
            }

            return list;
         } else {
            for(Direction dir : Direction.values()) {
               if (BOBlockUtil.replaceable(block.offset(dir)) && SettingUtils.inPlaceRange(block.offset(dir))) {
                  Direction support2 = this.getSupport(block.offset(dir));
                  if (support2 != null) {
                     this.renderHelperPlacing.add(new Render(block.offset(dir), System.currentTimeMillis()));
                     this.renderHelperPlacing.add(new Render(block.offset(dir).offset(support2), System.currentTimeMillis()));
                     if (block.offset(dir).offset(support2) != block && !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block.offset(dir).offset(support2))), (entity) -> !entity.isSpectator() && !(entity instanceof ItemEntity))) {
                        list.add(block.offset(dir).offset(support2));
                     }

                     return list;
                  }
               }
            }

            return list;
         }
      }
   }

   private Direction getSupport(BlockPos position) {
      Direction cDir = null;
      double cDist = (double)1000.0F;
      int value = -1;

      for(Direction dir : Direction.values()) {
         PlaceData data = SettingUtils.getPlaceData(position.offset(dir));
         if (data.valid() && SettingUtils.inPlaceRange(data.pos())) {
            if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(position.offset(dir))), (entity) -> !entity.isSpectator() && entity.getType() != EntityType.ITEM)) {
               double dist = this.mc.player.getEyePos().distanceTo(position.offset(dir).toCenterPos());
               if (dist < cDist || value < 2) {
                  value = 2;
                  cDir = dir;
                  cDist = dist;
               }
            }

            if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(position.offset(dir))), (entity) -> !entity.isSpectator() && entity.getType() != EntityType.ITEM && entity.getType() != EntityType.END_CRYSTAL)) {
               double dist = this.mc.player.getEyePos().distanceTo(position.offset(dir).toCenterPos());
               if (dist < cDist || value < 1) {
                  value = 1;
                  cDir = dir;
                  cDist = dist;
               }
            }
         }
      }

      return cDir;
   }

   private void updateTargets() {
      double closestDist = (double)1000.0F;

      for(int i = 3; i > 0; --i) {
         PlayerEntity closest = null;

         for(PlayerEntity player : this.mc.world.getPlayers()) {
            if (closest != player && !Friends.get().isFriend(player) && player != this.mc.player) {
               double dist = (double)player.distanceTo(this.mc.player);
               if (!(dist > (double)15.0F) && (closest == null || dist < closestDist)) {
                  closestDist = dist;
                  closest = player;
               }
            }
         }

         if (closest != null) {
            this.target = closest;
         }
      }

   }

   public String getInfoString() {
      return EntityUtils.getName(this.target);
   }

   public static enum SwitchMode {
      Silent,
      InvSwitch,
      PickSilent;

      // $FF: synthetic method
      private static SwitchMode[] $values() {
         return new SwitchMode[]{Silent, InvSwitch, PickSilent};
      }
   }

   public static enum HelperSwitchMode {
      Silent,
      InvSwitch,
      PickSilent;

      // $FF: synthetic method
      private static HelperSwitchMode[] $values() {
         return new HelperSwitchMode[]{Silent, InvSwitch, PickSilent};
      }
   }

   public static record Render(BlockPos pos, long time) {
   }
}
