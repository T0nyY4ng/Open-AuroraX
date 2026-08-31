package espada.spacex.aurora.modules.combatplus;

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
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockBox;

public class SelfTrapPlus extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgPlacing;
   private final SettingGroup sgToggle;
   private final SettingGroup sgRender;
   private final Setting<Boolean> pauseEat;
   private final Setting<Boolean> onlyConfirmed;
   private final Setting<SwitchMode> switchMode;
   private final Setting<TrapMode> trapMode;
   private final Setting<List<Block>> blocks;
   private final Setting<Double> placeDelay;
   private final Setting<Integer> places;
   private final Setting<Double> delay;
   private final Setting<Boolean> toggleMove;
   private final Setting<ToggleYMode> toggleY;
   private final Setting<Boolean> toggleSneak;
   private final Setting<Boolean> placeSwing;
   private final Setting<SwingHand> placeHand;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<SettingColor> lineColor;
   private final Setting<SettingColor> sideColor;
   private final Setting<SettingColor> supportLineColor;
   private final Setting<SettingColor> supportSideColor;
   private final TimerList<BlockPos> timers;
   private final TimerList<BlockPos> placed;
   private double placeTimer;
   private int placesLeft;
   private BlockPos startPos;
   private boolean lastSneak;
   private final List<Render> render;
   public static boolean placing = false;

   public SelfTrapPlus() {
      super(Aurora.CombatPlus, "Self Trap+", "Traps yourself with blocks.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgPlacing = this.settings.createGroup("Placing");
      this.sgToggle = this.settings.createGroup("Toggle");
      this.sgRender = this.settings.createGroup("Render");
      this.pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause Eat")).description("Pauses when you are eating.")).defaultValue(true)).build());
      this.onlyConfirmed = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Only Confirmed")).description("Only places on blocks the server has confirmed to exist.")).defaultValue(true)).build());
      this.switchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("Method of switching. Silent is the most reliable.")).defaultValue(SelfTrapPlus.SwitchMode.Silent)).build());
      this.trapMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Trap Mode")).description("Where to place blocks.")).defaultValue(SelfTrapPlus.TrapMode.Both)).build());
      this.blocks = this.sgPlacing.add(((BlockListSetting.Builder)((BlockListSetting.Builder)(new BlockListSetting.Builder()).name("Blocks")).description("Blocks to use.")).defaultValue(new Block[]{Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK}).build());
      this.placeDelay = this.sgPlacing.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Place Delay")).description("Delay between places.")).defaultValue((double)0.125F).range((double)0.0F, (double)10.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.places = this.sgPlacing.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Places")).description("Blocks placed per place.")).defaultValue(1)).range(1, 10).sliderRange(1, 10).build());
      this.delay = this.sgPlacing.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Delay")).description("Delay between places at each spot.")).defaultValue(0.3).range((double)0.0F, (double)10.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.toggleMove = this.sgToggle.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Toggle Move")).description("Toggles when you move horizontally.")).defaultValue(true)).build());
      this.toggleY = this.sgToggle.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Toggle Y")).description("Toggles when you move vertically.")).defaultValue(SelfTrapPlus.ToggleYMode.Full)).build());
      this.toggleSneak = this.sgToggle.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Toggle Sneak")).description("Toggles when you sneak.")).defaultValue(false)).build());
      this.placeSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Swing")).description("Renders swing animation when placing a block.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgRender;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      Setting<Boolean> var10003 = this.placeSwing;
      Objects.requireNonNull(var10003);
      this.placeHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description("Which parts of the boxes should be rendered.")).defaultValue(ShapeMode.Both)).build());
      this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("Color of the outlines")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("Color of the sides.")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.supportLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Support Line Color")).description("Color of the outlines for support blocks")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.supportSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Support Side Color")).description("Color of the sides for support blocks.")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.timers = new TimerList<BlockPos>();
      this.placed = new TimerList<BlockPos>();
      this.placeTimer = (double)0.0F;
      this.placesLeft = 0;
      this.startPos = new BlockPos(0, 0, 0);
      this.lastSneak = false;
      this.render = new ArrayList();
   }

   public void onActivate() {
      super.onActivate();
      if (this.mc.player == null || this.mc.world == null) {
         this.toggle();
      }

      this.startPos = this.mc.player.getBlockPos();
   }

   public void onDeactivate() {
      super.onDeactivate();
      this.placesLeft = (Integer)this.places.get();
      this.placeTimer = (double)0.0F;
      ((Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class)).setOverride((double)1.0F);
   }

   @EventHandler(
      priority = 200
   )
   private void onRender(Render3DEvent event) {
      this.timers.update();
      this.placed.update();
      placing = false;
      this.placeTimer = Math.min((Double)this.placeDelay.get(), this.placeTimer + event.frameTime);
      if (this.placeTimer >= (Double)this.placeDelay.get()) {
         this.placesLeft = (Integer)this.places.get();
         this.placeTimer = (double)0.0F;
      }

      if (this.mc.player != null && this.mc.world != null) {
         if ((Boolean)this.toggleMove.get() && (this.mc.player.getBlockPos().getX() != this.startPos.getX() || this.mc.player.getBlockPos().getZ() != this.startPos.getZ())) {
            this.sendDisableMsg("moved");
            this.toggle();
            return;
         }

         switch (((ToggleYMode)this.toggleY.get()).ordinal()) {
            case 1:
               if (this.mc.player.getBlockPos().getY() > this.startPos.getY()) {
                  this.sendDisableMsg("moved up");
                  this.toggle();
                  return;
               }
               break;
            case 2:
               if (this.mc.player.getBlockPos().getY() < this.startPos.getY()) {
                  this.sendDisableMsg("moved down");
                  this.toggle();
                  return;
               }
               break;
            case 3:
               if (this.mc.player.getBlockPos().getY() != this.startPos.getY()) {
                  this.sendDisableMsg("moved vertically");
                  this.toggle();
                  return;
               }
         }

         if ((Boolean)this.toggleSneak.get()) {
            boolean isClicked = this.mc.options.sneakKey.isPressed();
            if (isClicked && !this.lastSneak) {
               this.sendDisableMsg("sneaked");
               this.toggle();
               return;
            }

            this.lastSneak = isClicked;
         }

         List<BlockPos> blocksList = this.getBlocks(this.getSize(this.mc.player.getBlockPos().up()), this.mc.player.getBoundingBox().intersects(Box.from(new BlockBox(this.mc.player.getBlockPos().up(2)))));
         this.render.clear();
         List<BlockPos> placements = this.getValid(blocksList);
         this.render.forEach((item) -> event.renderer.box(Box.from(new BlockBox(item.pos)), item.support ? (Color)this.supportSideColor.get() : (Color)this.sideColor.get(), item.support ? (Color)this.supportLineColor.get() : (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0));
         FindItemResult hotbar = InvUtils.findInHotbar((item) -> item.getItem() instanceof BlockItem && ((List)this.blocks.get()).contains(((BlockItem)item.getItem()).getBlock()));
         FindItemResult inventory = InvUtils.find((item) -> item.getItem() instanceof BlockItem && ((List)this.blocks.get()).contains(((BlockItem)item.getItem()).getBlock()));
         Hand hand = this.isValid(Managers.HOLDING.getStack()) ? Hand.MAIN_HAND : (this.isValid(this.mc.player.getOffHandStack()) ? Hand.OFF_HAND : null);
         if ((!(Boolean)this.pauseEat.get() || !this.mc.player.isUsingItem()) && (hand != null || (this.switchMode.get() == SelfTrapPlus.SwitchMode.Silent || this.switchMode.get() == SelfTrapPlus.SwitchMode.Normal) && hotbar.slot() >= 0 || (this.switchMode.get() == SelfTrapPlus.SwitchMode.PickSilent || this.switchMode.get() == SelfTrapPlus.SwitchMode.InvSwitch) && inventory.slot() >= 0) && this.placesLeft > 0 && !placements.isEmpty()) {
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
                  if (hand == null) {
                     switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                        case 1:
                        case 2:
                           obsidian = hotbar.count();
                           InvUtils.swap(hotbar.slot(), true);
                           break;
                        case 3:
                           obsidian = BOInvUtils.pickSwitch(inventory.slot()) ? inventory.count() : -1;
                           break;
                        case 4:
                           obsidian = BOInvUtils.invSwitch(inventory.slot()) ? inventory.count() : -1;
                     }
                  }

                  if (obsidian <= 0) {
                     return;
                  }

                  placing = true;

                  for(int i = 0; i < Math.min(obsidian, toPlace.size()); ++i) {
                     PlaceData var10000;
                     if ((Boolean)this.onlyConfirmed.get()) {
                        var10000 = SettingUtils.getPlaceData((BlockPos)toPlace.get(i));
                     } else {
                        BlockPos var15 = (BlockPos)toPlace.get(i);
                        TimerList var10001 = this.placed;
                        Objects.requireNonNull(var10001);
                        var10000 = SettingUtils.getPlaceDataOR(var15, var10001::contains);
                     }

                     PlaceData placeData = var10000;
                     if (placeData.valid()) {
                        boolean rotated = !SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(placeData.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}));
                        if (!rotated) {
                           break;
                        }

                        this.place(placeData, (BlockPos)toPlace.get(i), hand == null ? Hand.MAIN_HAND : hand);
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
      }

   }

   private boolean isValid(ItemStack item) {
      return item.getItem() instanceof BlockItem && ((List)this.blocks.get()).contains(((BlockItem)item.getItem()).getBlock());
   }

   private boolean canPlace(BlockPos pos) {
      return SettingUtils.getPlaceData(pos).valid();
   }

   private void place(PlaceData d, BlockPos ogPos, Hand hand) {
      this.timers.add(ogPos, (Double)this.delay.get());
      if ((Boolean)this.onlyConfirmed.get()) {
         this.placed.add(ogPos, (double)1.0F);
      }

      this.placeTimer = (double)0.0F;
      --this.placesLeft;
      this.placeBlock(hand, d.pos().toCenterPos(), d.dir(), d.pos());
      if ((Boolean)this.placeSwing.get()) {
         this.clientSwing((SwingHand)this.placeHand.get(), hand);
      }

      if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
         Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
      }

   }

   private List<BlockPos> getValid(List<BlockPos> blocks) {
      List<BlockPos> list = new ArrayList();
      if (blocks.isEmpty()) {
         return list;
      } else {
         blocks.forEach((block) -> {
            if (OLEPOSSUtils.replaceable(block)) {
               PlaceData var10000;
               if ((Boolean)this.onlyConfirmed.get()) {
                  var10000 = SettingUtils.getPlaceData(block);
               } else {
                  TimerList var10001 = this.placed;
                  Objects.requireNonNull(var10001);
                  var10000 = SettingUtils.getPlaceDataOR(block, var10001::contains);
               }

               PlaceData data = var10000;
               if (data.valid() && SettingUtils.inPlaceRange(data.pos())) {
                  this.render.add(new Render(block, false));
                  if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block)), (entity) -> !entity.isSpectator() && !(entity instanceof ItemEntity)) && !this.timers.contains(block)) {
                     list.add(block);
                  }

               } else {
                  Direction support1 = this.getSupport(block);
                  if (support1 != null) {
                     this.render.add(new Render(block, false));
                     this.render.add(new Render(block.offset(support1), true));
                     if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block.offset(support1))), (entity) -> !entity.isSpectator() && !(entity instanceof ItemEntity)) && !this.timers.contains(block.offset(support1))) {
                        list.add(block.offset(support1));
                     }

                  } else {
                     for(Direction dir : Direction.values()) {
                        if (OLEPOSSUtils.replaceable(block.offset(dir)) && SettingUtils.inPlaceRange(block.offset(dir))) {
                           Direction support2 = this.getSupport(block.offset(dir));
                           if (support2 != null) {
                              this.render.add(new Render(block, false));
                              this.render.add(new Render(block.offset(dir), true));
                              this.render.add(new Render(block.offset(dir).offset(support2), true));
                              if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block.offset(dir).offset(support2))), (entity) -> !entity.isSpectator() && !(entity instanceof ItemEntity)) && !this.timers.contains(block.offset(dir).offset(support2))) {
                                 list.add(block.offset(dir).offset(support2));
                              }

                              return;
                           }
                        }
                     }

                  }
               }
            }
         });
         return list;
      }
   }

   private Direction getSupport(BlockPos position) {
      Direction cDir = null;
      double cDist = (double)1000.0F;
      int value = -1;

      for(Direction dir : Direction.values()) {
         PlaceData var10000;
         if ((Boolean)this.onlyConfirmed.get()) {
            var10000 = SettingUtils.getPlaceData(position.offset(dir));
         } else {
            BlockPos var14 = position.offset(dir);
            TimerList var10001 = this.placed;
            Objects.requireNonNull(var10001);
            var10000 = SettingUtils.getPlaceDataOR(var14, var10001::contains);
         }

         PlaceData data = var10000;
         if (data.valid() && SettingUtils.inPlaceRange(data.pos())) {
            if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(position.offset(dir))), (entity) -> !entity.isSpectator() && entity.getType() != EntityType.ITEM)) {
               double dist = this.mc.player.getEyePos().distanceTo(Vec3d.ofCenter(position.offset(dir)));
               if (dist < cDist || value < 2) {
                  value = 2;
                  cDir = dir;
                  cDist = dist;
               }
            }

            if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(position.offset(dir))), (entity) -> !entity.isSpectator() && entity.getType() != EntityType.ITEM && entity.getType() != EntityType.END_CRYSTAL)) {
               double dist = this.mc.player.getEyePos().distanceTo(Vec3d.ofCenter(position.offset(dir)));
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

   private List<BlockPos> getBlocks(int[] size, boolean higher) {
      List<BlockPos> list = new ArrayList();
      BlockPos pos = this.mc.player.getBlockPos().up(higher ? 2 : 1);
      if (this.mc.player != null && this.mc.world != null) {
         for(int x = size[0] - 1; x <= size[1] + 1; ++x) {
            for(int z = size[2] - 1; z <= size[3] + 1; ++z) {
               boolean isX = x == size[0] - 1 || x == size[1] + 1;
               boolean isZ = z == size[2] - 1 || z == size[3] + 1;
               boolean ignore = isX && !isZ ? !OLEPOSSUtils.replaceable(pos.add(OLEPOSSUtils.closerToZero(x), 0, z)) || this.placed.contains(pos.add(OLEPOSSUtils.closerToZero(x), 0, z)) : !isX && isZ && (!OLEPOSSUtils.replaceable(pos.add(x, 0, OLEPOSSUtils.closerToZero(z))) || this.placed.contains(pos.add(x, 0, OLEPOSSUtils.closerToZero(z))));
               BlockPos bPos = null;
               if (this.eye() && isX != isZ && !ignore) {
                  bPos = (new BlockPos(x, pos.getY(), z)).add(pos.getX(), 0, pos.getZ());
               } else if (this.top() && !isX && !isZ && OLEPOSSUtils.replaceable(pos.add(x, 0, z)) && !this.placed.contains(pos.add(x, 0, z))) {
                  bPos = (new BlockPos(x, pos.getY(), z)).add(pos.getX(), 1, pos.getZ());
               }

               if (bPos != null) {
                  list.add(bPos);
               }
            }
         }
      }

      return list;
   }

   private boolean top() {
      return this.trapMode.get() == SelfTrapPlus.TrapMode.Both || this.trapMode.get() == SelfTrapPlus.TrapMode.Top;
   }

   private boolean eye() {
      return this.trapMode.get() == SelfTrapPlus.TrapMode.Both || this.trapMode.get() == SelfTrapPlus.TrapMode.Eyes;
   }

   private int[] getSize(BlockPos pos) {
      int minX = 0;
      int maxX = 0;
      int minZ = 0;
      int maxZ = 0;
      if (this.mc.player != null && this.mc.world != null) {
         Box box = this.mc.player.getBoundingBox();
         if (box.intersects(Box.from(new BlockBox(pos.north())))) {
            --minZ;
         }

         if (box.intersects(Box.from(new BlockBox(pos.south())))) {
            ++maxZ;
         }

         if (box.intersects(Box.from(new BlockBox(pos.west())))) {
            --minX;
         }

         if (box.intersects(Box.from(new BlockBox(pos.east())))) {
            ++maxX;
         }
      }

      return new int[]{minX, maxX, minZ, maxZ};
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

   public static enum TrapMode {
      Top,
      Eyes,
      Both;

      // $FF: synthetic method
      private static TrapMode[] $values() {
         return new TrapMode[]{Top, Eyes, Both};
      }
   }

   public static enum ToggleYMode {
      Disabled,
      Up,
      Down,
      Full;

      // $FF: synthetic method
      private static ToggleYMode[] $values() {
         return new ToggleYMode[]{Disabled, Up, Down, Full};
      }
   }

   private static record Render(BlockPos pos, boolean support) {
   }
}
