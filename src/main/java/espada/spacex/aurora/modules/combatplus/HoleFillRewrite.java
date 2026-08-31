package espada.spacex.aurora.modules.combatplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.HoleType;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.timers.TimerList;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.Hole;
import espada.spacex.aurora.utils.HoleUtils;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.SettingUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Direction.Type;

public class HoleFillRewrite extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgPlacing;
   private final SettingGroup sgRender;
   private final SettingGroup sgHole;
   private final Setting<Boolean> pauseEat;
   private final Setting<Boolean> efficient;
   private final Setting<Boolean> above;
   private final Setting<Boolean> iHole;
   private final Setting<Double> holeRange;
   private final Setting<SwitchMode> switchMode;
   private final Setting<List<Block>> blocks;
   private final Setting<Double> placeDelay;
   private final Setting<Integer> places;
   private final Setting<Double> delay;
   private final Setting<Boolean> single;
   private final Setting<Boolean> doubleHole;
   private final Setting<Boolean> quad;
   private final Setting<Boolean> placeSwing;
   private final Setting<SwingHand> placeHand;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<Double> renderTime;
   private final Setting<Double> fadeTime;
   public final Setting<SettingColor> lineColor;
   public final Setting<SettingColor> color;
   private List<BlockPos> holes;
   private final TimerList<BlockPos> timers;
   private double placeTimer;
   private final Map<BlockPos, Double[]> toRender;

   public HoleFillRewrite() {
      super(Aurora.CombatPlus, "AutoHoleFill", "Automatically is a cunt to your enemies.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgPlacing = this.settings.createGroup("Placing");
      this.sgRender = this.settings.createGroup("Render");
      this.sgHole = this.settings.createGroup("Hole");
      this.pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause Eat")).description("Pauses when you are eating")).defaultValue(false)).build());
      this.efficient = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Efficient")).description("Only places if the hole is closer to target")).defaultValue(true)).build());
      this.above = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Above")).description("Only places if target is above the hole")).defaultValue(true)).build());
      this.iHole = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Ignore Hole")).description("Doesn't place if enemy is in a hole")).defaultValue(true)).build());
      this.holeRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Hole Range")).description("Places when enemy is close enough to target hole")).defaultValue((double)3.0F).min((double)0.0F).sliderMax((double)10.0F).build());
      this.switchMode = this.sgPlacing.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("Method of switching. Silent is the most reliable but delays crystals on some servers.")).defaultValue(HoleFillRewrite.SwitchMode.Silent)).build());
      this.blocks = this.sgPlacing.add(((BlockListSetting.Builder)((BlockListSetting.Builder)(new BlockListSetting.Builder()).name("Blocks")).description("Which blocks to use.")).defaultValue(new Block[]{Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK}).build());
      this.placeDelay = this.sgPlacing.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Place Delay")).description("Delay between places.")).defaultValue((double)0.125F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.places = this.sgPlacing.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Places")).description("Blocks placed per place")).defaultValue(1)).min(1).sliderRange(1, 10).build());
      this.delay = this.sgPlacing.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Delay")).description("Delay between places at single spot.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.single = this.sgHole.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Single")).description("Fills 1x1 holes")).defaultValue(true)).build());
      this.doubleHole = this.sgHole.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Double")).description("Fills 2x1 block holes")).defaultValue(true)).build());
      this.quad = this.sgHole.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Quad")).description("Fills 2x2 block holes")).defaultValue(true)).build());
      this.placeSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Swing")).description("Renders swing animation when placing a block.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgRender;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      Setting<Boolean> var10003 = this.placeSwing;
      Objects.requireNonNull(var10003);
      this.placeHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description(".")).defaultValue(ShapeMode.Both)).build());
      this.renderTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Render Time")).description("How long the box should remain in full alpha.")).defaultValue(0.3).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.fadeTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fade Time")).description("How long the fading should take.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("Color of the outline.")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.color = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Color")).description("Color of the sides.")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.holes = new ArrayList();
      this.timers = new TimerList<BlockPos>();
      this.placeTimer = (double)0.0F;
      this.toRender = new HashMap();
   }

   @EventHandler(
      priority = 200
   )
   private void onRender(Render3DEvent event) {
      this.timers.update();
      double d = event.frameTime;
      if (this.mc.player != null && this.mc.world != null) {
         this.placeTimer = Math.min(this.placeTimer + event.frameTime, (Double)this.placeDelay.get());
         this.update();
         List<BlockPos> toRemove = new ArrayList();

         for(Map.Entry<BlockPos, Double[]> entry : this.toRender.entrySet()) {
            BlockPos pos = (BlockPos)entry.getKey();
            Double[] alpha = (Double[])entry.getValue();
            if (alpha[0] <= d) {
               toRemove.add(pos);
            } else {
               event.renderer.box(Box.from(new BlockBox(pos)), new Color(((SettingColor)this.color.get()).r, ((SettingColor)this.color.get()).g, ((SettingColor)this.color.get()).b, (int)Math.round((double)((SettingColor)this.color.get()).a * Math.min((double)1.0F, alpha[0] / alpha[1]))), new Color(((SettingColor)this.lineColor.get()).r, ((SettingColor)this.lineColor.get()).g, ((SettingColor)this.lineColor.get()).b, (int)Math.round((double)((SettingColor)this.lineColor.get()).a * Math.min((double)1.0F, alpha[0] / alpha[1]))), (ShapeMode)this.shapeMode.get(), 0);
               entry.setValue(new Double[]{alpha[0] - d, alpha[1]});
            }
         }

         Map var10001 = this.toRender;
         Objects.requireNonNull(var10001);
         toRemove.forEach(var10001::remove);
      }

   }

   private void update() {
      this.updateHoles(Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()) + (double)1.0F);
      List<BlockPos> placements = this.getValid(this.holes);
      FindItemResult result = InvUtils.findInHotbar((itemStack) -> itemStack.getItem() instanceof BlockItem && ((List)this.blocks.get()).contains(((BlockItem)itemStack.getItem()).getBlock()));
      FindItemResult invResult = InvUtils.find((itemStack) -> itemStack.getItem() instanceof BlockItem && ((List)this.blocks.get()).contains(((BlockItem)itemStack.getItem()).getBlock()));
      Hand hand = this.isValid(Managers.HOLDING.getStack()) ? Hand.MAIN_HAND : (this.isValid(this.mc.player.getOffHandStack()) ? Hand.OFF_HAND : null);
      if (!placements.isEmpty() && (!(Boolean)this.pauseEat.get() || !this.mc.player.isUsingItem()) && this.placeTimer >= (Double)this.placeDelay.get() && (hand != null || this.switchMode.get() == HoleFillRewrite.SwitchMode.Silent && result.slot() >= 0 || (this.switchMode.get() == HoleFillRewrite.SwitchMode.PickSilent || this.switchMode.get() == HoleFillRewrite.SwitchMode.InvSwitch) && invResult.slot() >= 0)) {
         List<BlockPos> toPlace = new ArrayList();

         for(BlockPos pos : placements) {
            if (toPlace.size() < (Integer)this.places.get() && this.canPlace(pos)) {
               toPlace.add(pos);
            }
         }

         if (!toPlace.isEmpty()) {
            int obsidian = hand == Hand.MAIN_HAND ? Managers.HOLDING.getStack().getCount() : (hand == Hand.OFF_HAND ? this.mc.player.getOffHandStack().getCount() : -1);
            if (hand == null) {
               switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                  case 1:
                     obsidian = result.count();
                     break;
                  case 2:
                  case 3:
                     obsidian = invResult.slot() >= 0 ? invResult.count() : -1;
               }
            }

            if (obsidian >= 0) {
               if (hand == null) {
                  switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                     case 1:
                        obsidian = result.count();
                        InvUtils.swap(result.slot(), true);
                        break;
                     case 2:
                        obsidian = BOInvUtils.pickSwitch(invResult.slot()) ? invResult.count() : -1;
                        break;
                     case 3:
                        obsidian = BOInvUtils.invSwitch(invResult.slot()) ? invResult.count() : -1;
                  }
               }

               this.placeTimer = (double)0.0F;

               for(int i = 0; i < Math.min(obsidian, toPlace.size()); ++i) {
                  PlaceData placeData = SettingUtils.getPlaceData((BlockPos)toPlace.get(i));
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
                     case 1 -> InvUtils.swapBack();
                     case 2 -> BOInvUtils.pickSwapBack();
                     case 3 -> BOInvUtils.swapBack();
                  }
               }
            }
         }
      }

   }

   private boolean isValid(ItemStack itemStack) {
      return itemStack.getItem() instanceof BlockItem && ((List)this.blocks.get()).contains(((BlockItem)itemStack.getItem()).getBlock());
   }

   private List<BlockPos> getValid(List<BlockPos> positions) {
      List<BlockPos> list = new ArrayList();

      for(BlockPos pos : positions) {
         if (!this.timers.contains(pos)) {
            list.add(pos);
         }
      }

      return list;
   }

   private void updateHoles(double range) {
      this.holes = new ArrayList();

      for(int x = (int)(-Math.ceil(range)); (double)x <= Math.ceil(range); ++x) {
         for(int y = (int)(-Math.ceil(range)); (double)y <= Math.ceil(range); ++y) {
            for(int z = (int)(-Math.ceil(range)); (double)z <= Math.ceil(range); ++z) {
               BlockPos pos = this.mc.player.getBlockPos().add(x, y, z);
               Hole h = HoleUtils.getHole(pos, (Boolean)this.single.get(), (Boolean)this.doubleHole.get(), (Boolean)this.quad.get(), 3, true);
               if (h.type != HoleType.NotHole) {
                  for(BlockPos p : h.positions()) {
                     if (OLEPOSSUtils.replaceable(p) && !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(p)), (entity) -> !entity.isSpectator() && !(entity instanceof ItemEntity))) {
                        double closest = this.closestDist(p);
                        PlaceData d = SettingUtils.getPlaceData(p);
                        if (d.valid() && closest >= (double)0.0F && closest <= (Double)this.holeRange.get() && (!(Boolean)this.efficient.get() || this.mc.player.getPos().distanceTo(Vec3d.ofCenter(p)) > closest) && SettingUtils.inPlaceRange(d.pos())) {
                           this.holes.add(p);
                        }
                     }
                  }
               }
            }
         }
      }

   }

   private double closestDist(BlockPos pos) {
      double closest = (double)-1.0F;

      for(PlayerEntity pl : this.mc.world.getPlayers()) {
         double dist = pl.getPos().distanceTo(Vec3d.ofCenter(pos));
         if ((!(Boolean)this.iHole.get() || !this.inHole(pl)) && (!(Boolean)this.above.get() || pl.getY() > (double)pos.getY()) && pl != this.mc.player && !Friends.get().isFriend(pl) && (closest < (double)0.0F || dist < closest)) {
            closest = dist;
         }
      }

      return closest;
   }

   private boolean inHole(PlayerEntity pl) {
      for(Direction dir : Type.HORIZONTAL) {
         if (this.mc.world.getBlockState(pl.getBlockPos().offset(dir)).getBlock() == Blocks.AIR) {
            return false;
         }
      }

      return true;
   }

   private boolean canPlace(BlockPos pos) {
      return SettingUtils.getPlaceData(pos).valid();
   }

   private void place(PlaceData d, BlockPos ogPos, Hand hand) {
      this.timers.add(ogPos, (Double)this.delay.get());
      this.placeBlock(hand, d.pos().toCenterPos(), d.dir(), d.pos());
      if ((Boolean)this.placeSwing.get()) {
         this.clientSwing((SwingHand)this.placeHand.get(), hand);
      }

      if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
         Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
      }

      if (!this.toRender.containsKey(ogPos)) {
         this.toRender.put(ogPos, new Double[]{(Double)this.fadeTime.get() + (Double)this.renderTime.get(), (Double)this.fadeTime.get()});
      } else {
         this.toRender.replace(ogPos, new Double[]{(Double)this.fadeTime.get() + (Double)this.renderTime.get(), (Double)this.fadeTime.get()});
      }

   }

   public static enum SwitchMode {
      Disabled,
      Silent,
      PickSilent,
      InvSwitch;

      // $FF: synthetic method
      private static SwitchMode[] $values() {
         return new SwitchMode[]{Disabled, Silent, PickSilent, InvSwitch};
      }
   }
}
