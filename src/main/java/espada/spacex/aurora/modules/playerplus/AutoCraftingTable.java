package espada.spacex.aurora.modules.playerplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.SettingUtils;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class AutoCraftingTable extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgRender;
   private final Setting<SwitchMode> switchMode;
   private final Setting<Double> placeSpeed;
   private final Setting<Double> interactSpeed;
   private final Setting<Boolean> placeSwing;
   private final Setting<SwingHand> placeHand;
   private final Setting<Boolean> interactSwing;
   private final Setting<SwingHand> interactHand;
   public final Setting<SettingColor> color;
   public final Setting<SettingColor> lineColor;
   private BlockPos placePos;
   private PlaceData placeData;
   private BlockPos tablePos;
   private Direction tableDir;
   private double placeTimer;
   private double interactTimer;
   private long lastTime;

   public AutoCraftingTable() {
      super(Aurora.PlayerPlus, "Auto Crafting Table", "Automatically places and opens an Crafting table.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgRender = this.settings.createGroup("Render");
      this.switchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("Switching method. Silent is the most reliable but doesn't work everywhere..")).defaultValue(AutoCraftingTable.SwitchMode.Silent)).build());
      this.placeSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Place Speed")).description("Tries to place this many times every second.")).defaultValue((double)1.0F).min((double)0.0F).sliderMin((double)0.0F).build());
      this.interactSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Interact Speed")).description("Tries to open the crafting table this many times every second.")).defaultValue((double)1.0F).min((double)0.0F).sliderMin((double)0.0F).build());
      this.placeSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Place Swing")).description("Renders swing animation when placing the crafting table.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgRender;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Place Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      Setting<Boolean> var10003 = this.placeSwing;
      Objects.requireNonNull(var10003);
      this.placeHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.interactSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Interact Swing")).description("Renders swing animation when interacting with a block.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Interact Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.interactSwing;
      Objects.requireNonNull(var10003);
      this.interactHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.color = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("Side color of rendered stuff")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("Line color of rendered stuff")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.placeTimer = (double)0.0F;
      this.interactTimer = (double)0.0F;
      this.lastTime = -1L;
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.lastTime < 0L) {
         this.lastTime = System.currentTimeMillis();
      }

      double delta = (double)((float)(System.currentTimeMillis() - this.lastTime) / 1000.0F);
      this.placeTimer += delta;
      this.interactTimer += delta;
      this.placeData = this.findPos();
      if (this.placePos != null) {
         event.renderer.box(this.placePos, (Color)this.color.get(), (Color)this.lineColor.get(), ShapeMode.Both, 0);
      }

      this.update();
      this.lastTime = System.currentTimeMillis();
   }

   private void update() {
      if (!this.screenUpdate()) {
         this.placeUpdate();
         this.interactUpdate();
      }
   }

   private boolean screenUpdate() {
      ScreenHandler screenHandler = this.mc.player.currentScreenHandler;
      if (screenHandler instanceof CraftingScreenHandler) {
         this.toggle();
         this.sendDisableMsg("opened crafting table");
         return true;
      } else {
         return false;
      }
   }

   private void placeUpdate() {
      if (this.placePos != null && this.placeData != null && this.placeData.valid()) {
         if (this.placeTimer < (double)1.0F / (Double)this.placeSpeed.get()) {
            return;
         }

         if (this.place()) {
            this.placeTimer = (double)0.0F;
            this.tablePos = this.placePos;
         }
      }

   }

   private void interactUpdate() {
      if (this.tablePos != null) {
         this.tableDir = SettingUtils.getPlaceOnDirection(this.tablePos);
         if (this.tableDir == null) {
            return;
         }

         if (this.interactTimer < (double)1.0F / (Double)this.interactSpeed.get()) {
            return;
         }

         if (this.interact()) {
            this.interactTimer = (double)0.0F;
         }
      }

   }

   private boolean interact() {
      boolean rotated = !SettingUtils.shouldRotate(RotationType.Interact) || Managers.ROTATION.start(this.tablePos, (double)this.priority - 0.1, RotationType.Interact, (long)Objects.hash(new Object[]{this.name + "interact"}));
      if (!rotated) {
         return false;
      } else {
         this.interactBlock(Hand.MAIN_HAND, this.tablePos.toCenterPos(), this.tableDir, this.tablePos);
         if (SettingUtils.shouldRotate(RotationType.Interact)) {
            Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "interact"}));
         }

         if ((Boolean)this.interactSwing.get()) {
            this.clientSwing((SwingHand)this.interactHand.get(), Hand.MAIN_HAND);
         }

         return true;
      }
   }

   private boolean place() {
      Hand hand = Managers.HOLDING.isHolding(Items.CRAFTING_TABLE) ? Hand.MAIN_HAND : (this.mc.player.getOffHandStack().getItem() == Items.CRAFTING_TABLE ? Hand.OFF_HAND : null);
      boolean var10000;
      switch (((SwitchMode)this.switchMode.get()).ordinal()) {
         case 0:
            var10000 = hand != null;
            break;
         case 1:
         case 2:
            var10000 = InvUtils.findInHotbar(new Item[]{Items.CRAFTING_TABLE}).found();
            break;
         case 3:
         case 4:
            var10000 = InvUtils.find(new Item[]{Items.CRAFTING_TABLE}).found();
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      boolean canSwitch = var10000;
      if (!canSwitch) {
         return false;
      } else {
         boolean rotated = !SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(this.placeData.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}));
         if (!rotated) {
            return false;
         } else {
            boolean switched = false;
            if (hand == null) {
               switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                  case 1:
                  case 2:
                     InvUtils.swap(InvUtils.findInHotbar(new Item[]{Items.CRAFTING_TABLE}).slot(), true);
                     switched = true;
                     break;
                  case 3:
                     switched = BOInvUtils.pickSwitch(InvUtils.findInHotbar(new Item[]{Items.CRAFTING_TABLE}).slot());
                     break;
                  case 4:
                     switched = BOInvUtils.invSwitch(InvUtils.findInHotbar(new Item[]{Items.CRAFTING_TABLE}).slot());
               }
            } else {
               switched = true;
            }

            if (!switched) {
               return false;
            } else {
               Hand rHand = hand != null ? hand : Hand.MAIN_HAND;
               this.placeBlock(rHand, this.placeData.pos().toCenterPos(), this.placeData.dir(), this.placeData.pos());
               if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                  Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
               }

               if ((Boolean)this.placeSwing.get()) {
                  this.clientSwing((SwingHand)this.placeHand.get(), rHand);
               }

               if (hand == null) {
                  switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                     case 2 -> InvUtils.swapBack();
                     case 3 -> BOInvUtils.pickSwapBack();
                     case 4 -> BOInvUtils.swapBack();
                  }
               }

               return true;
            }
         }
      }
   }

   private PlaceData findPos() {
      int i = (int)Math.ceil(Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()));
      PlaceData closestData = null;
      BlockPos closestPos = null;
      double closestDist = (double)1000.0F;
      double closestEnemyDist = (double)0.0F;
      double closestVal = (double)0.0F;

      for(int x = -i; x <= i; ++x) {
         for(int y = -i; y <= i; ++y) {
            for(int z = -i; z <= i; ++z) {
               BlockPos pos = BlockPos.ofFloored(this.mc.player.getEyePos()).add(x, y, z);
               if (OLEPOSSUtils.replaceable(pos)) {
                  if (this.getBlock(pos) == Blocks.CRAFTING_TABLE) {
                     this.tablePos = pos;
                     return null;
                  }

                  PlaceData data = SettingUtils.getPlaceData(pos);
                  if (data.valid() && SettingUtils.getPlaceOnDirection(pos) != null) {
                     double distance = SettingUtils.placeRangeTo(data.pos());
                     if (!(distance > closestDist)) {
                        double val = this.value(pos);
                        if (!(val < closestVal)) {
                           double eDist = this.distToEnemySQ(pos);
                           if ((val != closestVal || !(eDist < closestEnemyDist)) && !EntityUtils.intersectsWithEntity(new Box(pos), (entity) -> !(entity instanceof ItemEntity) && !entity.isSpectator())) {
                              closestData = data;
                              closestPos = pos;
                              closestDist = distance;
                              closestEnemyDist = eDist;
                              closestVal = val;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      this.placePos = closestPos;
      return closestData;
   }

   private double value(BlockPos pos) {
      double val = (double)0.0F;

      for(Direction dir : Direction.values()) {
         val += this.getBlastRes(this.getBlock(pos.offset(dir)));
      }

      return val;
   }

   private double getBlastRes(Block block) {
      return block == Blocks.BEDROCK ? (double)1500.0F : (double)block.getBlastResistance();
   }

   private double distToEnemySQ(BlockPos pos) {
      double closest = Double.MAX_VALUE;

      for(PlayerEntity player : this.mc.world.getPlayers()) {
         if (player != this.mc.player && !Friends.get().isFriend(player)) {
            double dist = player.getEyePos().distanceTo(Vec3d.ofCenter(pos));
            if (dist < closest) {
               closest = dist;
            }
         }
      }

      return closest;
   }

   private Block getBlock(BlockPos pos) {
      return this.mc.world.getBlockState(pos).getBlock();
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
