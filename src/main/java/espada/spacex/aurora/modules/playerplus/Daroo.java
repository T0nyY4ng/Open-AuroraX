package espada.spacex.aurora.modules.playerplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.timers.TimerList;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.SettingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.movement.SafeWalk;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockBox;

public class Daroo extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgPlacing;
   private final SettingGroup sgRender;
   private final Setting<ScaffoldMode> scaffoldMode;
   private final Setting<Boolean> smart;
   private final Setting<Boolean> tower;
   private final Setting<Boolean> sSprint;
   private final Setting<Boolean> safeWalk;
   private final Setting<Boolean> useTimer;
   private final Setting<Double> timer;
   private final Setting<SwitchMode> switchMode;
   private final Setting<List<Block>> blocks;
   private final Setting<Double> placeDelay;
   private final Setting<Integer> places;
   private final Setting<Double> cooldown;
   private final Setting<Integer> extrapolation;
   private final Setting<Boolean> placeSwing;
   private final Setting<SwingHand> placeHand;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<SettingColor> lineColor;
   private final Setting<SettingColor> sideColor;
   private final TimerList<BlockPos> timers;
   private Vec3d motion;
   private double placeTimer;
   private int placesLeft;
   public static boolean shouldStopSprinting = false;
   private final List<Render> render;
   private int jumpProgress;
   private final double[] velocities;

   public Daroo() {
      super(Aurora.PlayerPlus, "Daroo", "KasumsSoft blockwalk.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgPlacing = this.settings.createGroup("Placing");
      this.sgRender = this.settings.createGroup("Render");
      this.scaffoldMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Scaffold Mode")).description("Mode for scaffold.")).defaultValue(Daroo.ScaffoldMode.Normal)).build());
      this.smart = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Smart")).description("Only places on blocks that you can reach.")).defaultValue(true)).visible(() -> this.scaffoldMode.get() == Daroo.ScaffoldMode.Normal)).build());
      this.tower = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Tower")).description("Flies up with blocks.")).defaultValue(true)).visible(() -> this.scaffoldMode.get() == Daroo.ScaffoldMode.Normal)).build());
      this.sSprint = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Stop Sprint")).description("Stops you from sprinting.")).defaultValue(true)).visible(() -> this.scaffoldMode.get() == Daroo.ScaffoldMode.Normal)).build());
      this.safeWalk = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("SafeWalk")).description("Should SafeWalk be used.")).defaultValue(true)).visible(() -> this.scaffoldMode.get() == Daroo.ScaffoldMode.Normal)).build());
      this.useTimer = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Use timer")).description("Should we use timer.")).defaultValue(false)).visible(() -> this.scaffoldMode.get() == Daroo.ScaffoldMode.Normal)).build());
      SettingGroup var10001 = this.sgGeneral;
      DoubleSetting.Builder var10002 = new DoubleSetting.Builder();
      Setting<Boolean> var10003 = this.useTimer;
      Objects.requireNonNull(var10003);
      this.timer = var10001.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)var10002.visible(var10003::get)).name("Timer")).description("Sends more packets.")).defaultValue(1.088).min((double)0.0F).sliderMax((double)10.0F).visible(() -> this.scaffoldMode.get() == Daroo.ScaffoldMode.Normal && (Boolean)this.useTimer.get())).build());
      this.switchMode = this.sgPlacing.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("Method of switching. Silent is the most reliable.")).defaultValue(Daroo.SwitchMode.Silent)).visible(() -> this.scaffoldMode.get() == Daroo.ScaffoldMode.Normal)).build());
      this.blocks = this.sgPlacing.add(((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)(new BlockListSetting.Builder()).name("Blocks")).description("Blocks to use.")).defaultValue(new Block[]{Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK}).visible(() -> this.scaffoldMode.get() == Daroo.ScaffoldMode.Normal)).build());
      this.placeDelay = this.sgPlacing.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Place Delay")).description("Delay between places.")).defaultValue((double)0.125F).range((double)0.0F, (double)10.0F).sliderRange((double)0.0F, (double)10.0F).visible(() -> this.scaffoldMode.get() == Daroo.ScaffoldMode.Normal)).build());
      this.places = this.sgPlacing.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Places")).description("Blocks placed per place.")).defaultValue(1)).range(1, 10).sliderRange(1, 10).visible(() -> this.scaffoldMode.get() == Daroo.ScaffoldMode.Normal)).build());
      this.cooldown = this.sgPlacing.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Cooldown")).description("Delay between places at each spot.")).defaultValue(0.3).range((double)0.0F, (double)5.0F).sliderRange((double)0.0F, (double)5.0F).visible(() -> this.scaffoldMode.get() == Daroo.ScaffoldMode.Normal)).build());
      this.extrapolation = this.sgPlacing.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Extrapolation")).description("Predicts movement.")).defaultValue(3)).range(1, 20).sliderRange(0, 20).visible(() -> this.scaffoldMode.get() == Daroo.ScaffoldMode.Normal)).build());
      this.placeSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Swing")).description("Renders swing animation when placing a block.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      EnumSetting.Builder var2 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.placeSwing;
      Objects.requireNonNull(var10003);
      this.placeHand = var10001.add(((EnumSetting.Builder)var2.visible(var10003::get)).build());
      this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description("Which parts of boxes should be rendered.")).defaultValue(ShapeMode.Both)).build());
      this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.timers = new TimerList<BlockPos>();
      this.motion = null;
      this.placesLeft = 0;
      this.render = new ArrayList();
      this.jumpProgress = -1;
      this.velocities = new double[]{0.42, 0.33319999999999994, 0.2468};
   }

   public void onDeactivate() {
      switch (((ScaffoldMode)this.scaffoldMode.get()).ordinal()) {
         case 0:
            this.placeTimer = (double)0.0F;
            this.placesLeft = (Integer)this.places.get();
            ((Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class)).setOverride((double)1.0F);
            if (((SafeWalk)meteordevelopment.meteorclient.systems.modules.Modules.get().get(SafeWalk.class)).isActive()) {
               ((SafeWalk)meteordevelopment.meteorclient.systems.modules.Modules.get().get(SafeWalk.class)).toggle();
            }
            break;
         case 1:
            this.mc.options.sneakKey.setPressed(false);
      }

   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      this.timers.update();
      if (this.scaffoldMode.get() != Daroo.ScaffoldMode.Legit) {
         this.placeTimer = Math.min((Double)this.placeDelay.get(), this.placeTimer + event.frameTime);
         if (this.placeTimer >= (Double)this.placeDelay.get()) {
            this.placesLeft = (Integer)this.places.get();
            this.placeTimer = (double)0.0F;
         }

         this.render.removeIf((r) -> System.currentTimeMillis() - r.time > 1000L);
         this.render.forEach((r) -> {
            double progress = (double)1.0F - (double)Math.min(System.currentTimeMillis() - r.time, 500L) / (double)500.0F;
            event.renderer.box(r.pos, new Color(((SettingColor)this.sideColor.get()).r, ((SettingColor)this.sideColor.get()).g, ((SettingColor)this.sideColor.get()).b, (int)Math.round((double)((SettingColor)this.sideColor.get()).a * progress)), new Color(((SettingColor)this.lineColor.get()).r, ((SettingColor)this.lineColor.get()).g, ((SettingColor)this.lineColor.get()).b, (int)Math.round((double)((SettingColor)this.lineColor.get()).a * progress)), (ShapeMode)this.shapeMode.get(), 0);
         });
      }
   }

   @EventHandler(
      priority = 10000
   )
   private void onTick(TickEvent.Pre event) {
      if (this.scaffoldMode.get() == Daroo.ScaffoldMode.Legit) {
         this.mc.options.sneakKey.setPressed(this.mc.world.getBlockState(this.mc.player.getBlockPos().down()).getBlock() instanceof AirBlock);
      }

   }

   @EventHandler(
      priority = 10000
   )
   private void onMove(PlayerMoveEvent event) {
      shouldStopSprinting = false;
      if (this.scaffoldMode.get() != Daroo.ScaffoldMode.Legit) {
         if (this.mc.player != null && this.mc.world != null) {
            FindItemResult hotbar = InvUtils.findInHotbar((item) -> item.getItem() instanceof BlockItem && ((List)this.blocks.get()).contains(((BlockItem)item.getItem()).getBlock()));
            FindItemResult inventory = InvUtils.find((item) -> item.getItem() instanceof BlockItem && ((List)this.blocks.get()).contains(((BlockItem)item.getItem()).getBlock()));
            Hand hand = this.isValid(Managers.HOLDING.getStack()) ? Hand.MAIN_HAND : (this.isValid(this.mc.player.getOffHandStack()) ? Hand.OFF_HAND : null);
            if (hand != null || (this.switchMode.get() == Daroo.SwitchMode.PickSilent || this.switchMode.get() == Daroo.SwitchMode.InvSwitch) && inventory.slot() >= 0 || (this.switchMode.get() == Daroo.SwitchMode.Silent || this.switchMode.get() == Daroo.SwitchMode.Normal) && hotbar.slot() >= 0) {
               if ((Boolean)this.safeWalk.get() && !((SafeWalk)meteordevelopment.meteorclient.systems.modules.Modules.get().get(SafeWalk.class)).isActive()) {
                  ((SafeWalk)meteordevelopment.meteorclient.systems.modules.Modules.get().get(SafeWalk.class)).toggle();
               }

               this.motion = event.movement;
               this.yVel();
               if ((Boolean)this.sSprint.get()) {
                  shouldStopSprinting = true;
                  this.mc.player.setSprinting(false);
               }

               if ((Boolean)this.useTimer.get()) {
                  ((Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class)).setOverride((Double)this.timer.get());
               }

               List<BlockPos> placements = this.getBlocks();
               if (!placements.isEmpty() && this.placesLeft > 0) {
                  List<BlockPos> toPlace = new ArrayList();

                  for(BlockPos placement : placements) {
                     if (toPlace.size() < this.placesLeft && this.canPlace(placement)) {
                        toPlace.add(placement);
                     }
                  }

                  if (!toPlace.isEmpty()) {
                     int obsidian = hand == Hand.MAIN_HAND ? Managers.HOLDING.getStack().getCount() : (hand == Hand.OFF_HAND ? this.mc.player.getOffHandStack().getCount() : -1);
                     if (hand == null) {
                        switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                           case 1:
                           case 2:
                              obsidian = hotbar.count();
                              break;
                           case 3:
                           case 4:
                              obsidian = inventory.slot() >= 0 ? inventory.count() : -1;
                        }
                     }

                     if (obsidian >= 0) {
                        Block block = null;
                        if (hand == Hand.MAIN_HAND) {
                           block = ((BlockItem)Managers.HOLDING.getStack().getItem()).getBlock();
                        }

                        if (hand == Hand.OFF_HAND) {
                           block = ((BlockItem)this.mc.player.getOffHandStack().getItem()).getBlock();
                        } else {
                           switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                              case 1:
                              case 2:
                                 obsidian = hotbar.count();
                                 InvUtils.swap(hotbar.slot(), true);
                                 block = ((BlockItem)this.mc.player.getInventory().getStack(hotbar.slot()).getItem()).getBlock();
                                 break;
                              case 3:
                                 obsidian = BOInvUtils.pickSwitch(inventory.slot()) ? inventory.count() : -1;
                                 block = ((BlockItem)this.mc.player.getInventory().getStack(inventory.slot()).getItem()).getBlock();
                                 break;
                              case 4:
                                 obsidian = BOInvUtils.invSwitch(inventory.slot()) ? inventory.count() : -1;
                                 block = ((BlockItem)this.mc.player.getInventory().getStack(inventory.slot()).getItem()).getBlock();
                           }
                        }

                        for(int i = 0; i < Math.min(obsidian, toPlace.size()); ++i) {
                           PlaceData placeData = SettingUtils.getPlaceData((BlockPos)toPlace.get(i));
                           if (placeData.valid()) {
                              boolean rotated = !SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(placeData.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}));
                              if (!rotated) {
                                 break;
                              }

                              this.place(placeData, (BlockPos)toPlace.get(i), hand == null ? Hand.MAIN_HAND : hand, block);
                           }
                        }

                        if (hand == null) {
                           switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                              case 2 -> InvUtils.swapBack();
                              case 3 -> BOInvUtils.pickSwapBack();
                              case 4 -> BOInvUtils.swapBack();
                           }
                        }
                     }
                  }
               }
            } else if ((Boolean)this.safeWalk.get() && ((SafeWalk)meteordevelopment.meteorclient.systems.modules.Modules.get().get(SafeWalk.class)).isActive()) {
               ((SafeWalk)meteordevelopment.meteorclient.systems.modules.Modules.get().get(SafeWalk.class)).toggle();
            }

         }
      }
   }

   void yVel() {
      if ((Boolean)this.tower.get()) {
         if (this.mc.options.jumpKey.isPressed() && this.mc.player.input.movementForward == 0.0F && this.mc.player.input.movementSideways == 0.0F) {
            if (this.mc.player.isOnGround() || this.jumpProgress == 3) {
               this.jumpProgress = 0;
            }

            if (this.jumpProgress > -1 && this.jumpProgress < 3) {
               ((IVec3d)this.motion).setXZ((double)0.0F, (double)0.0F);
               ((IVec3d)this.motion).setY(this.velocities[this.jumpProgress]);
               ((IVec3d)this.mc.player.getVelocity()).setY(this.velocities[this.jumpProgress]);
               ++this.jumpProgress;
            }
         } else {
            this.jumpProgress = -1;
         }

      }
   }

   private boolean isValid(ItemStack item) {
      return item.getItem() instanceof BlockItem && ((List)this.blocks.get()).contains(((BlockItem)item.getItem()).getBlock());
   }

   private boolean canPlace(BlockPos pos) {
      return SettingUtils.getPlaceData(pos).valid();
   }

   private List<BlockPos> getBlocks() {
      List<BlockPos> list = new ArrayList();
      Vec3d vec = this.mc.player.getPos();

      for(int i = 0; i < (Integer)this.extrapolation.get() * 10; ++i) {
         vec = vec.add(this.motion.x / (double)10.0F, (double)0.0F, this.motion.z / (double)10.0F);
         if ((Boolean)this.smart.get() && this.inside(this.getBox(vec))) {
            break;
         }

         BlockPos pos = BlockPos.ofFloored(vec).down();
         if (!this.timers.contains(pos) && OLEPOSSUtils.replaceable(pos) && !list.contains(pos) && !this.mc.player.getBoundingBox().intersects(Box.from(new BlockBox(pos)))) {
            list.add(pos);
         }
      }

      return list;
   }

   private Box getBox(Vec3d vec) {
      Box box = this.mc.player.getBoundingBox();
      return new Box(vec.x - 0.3, vec.y, vec.z - 0.3, vec.x + 0.3, vec.y + (box.maxY - box.minY), vec.z + 0.3);
   }

   private boolean inside(Box bb) {
      return this.mc.world.getBlockCollisions(this.mc.player, bb).iterator().hasNext();
   }

   private void place(PlaceData d, BlockPos ogPos, Hand hand, Block block) {
      this.timers.add(ogPos, (Double)this.cooldown.get());
      this.render.add(new Render(ogPos, System.currentTimeMillis()));
      --this.placesLeft;
      this.placeBlock(hand, d.pos().toCenterPos(), d.dir(), d.pos());
      if ((Boolean)this.placeSwing.get()) {
         this.clientSwing((SwingHand)this.placeHand.get(), hand);
      }

      this.mc.world.setBlockState(ogPos, block.getDefaultState());
      if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
         Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
      }

   }

   public static record Render(BlockPos pos, long time) {
   }

   public static enum ScaffoldMode {
      Normal,
      Legit;

      // $FF: synthetic method
      private static ScaffoldMode[] $values() {
         return new ScaffoldMode[]{Normal, Legit};
      }
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
}
