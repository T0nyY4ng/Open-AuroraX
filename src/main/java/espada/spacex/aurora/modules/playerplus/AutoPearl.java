package espada.spacex.aurora.modules.playerplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.RotationUtils;
import espada.spacex.aurora.utils.SettingUtils;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class AutoPearl extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgRender;
   private final Setting<Boolean> ccBypass;
   private final Setting<SwitchMode> ccSwitchMode;
   private final Setting<SwitchMode> switchMode;
   private final Setting<Integer> pitch;
   private final Setting<Boolean> instaRot;
   private final Setting<Boolean> swing;
   private final Setting<SwingHand> swingHand;
   private boolean placed;

   public AutoPearl() {
      super(Aurora.PlayerPlus, "Auto Pearl", "Easily clip inside walls with pearls.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgRender = this.settings.createGroup("Render");
      this.ccBypass = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("CC Bypass")).description("Does funny stuff to bypass cc's anti delay.")).defaultValue(false)).build());
      this.ccSwitchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("CC Switch Mode")).description("Which method of switching should be used for cc items.")).defaultValue(AutoPearl.SwitchMode.Silent)).build());
      this.switchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("Which method of switching should be used.")).defaultValue(AutoPearl.SwitchMode.Silent)).build());
      this.pitch = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Pitch")).description("How deep down to look.")).defaultValue(85)).range(-90, 90).sliderRange(0, 90).build());
      this.instaRot = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Instant Rotation")).description("Instantly rotates.")).defaultValue(false)).build());
      this.swing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Swing")).description("Renders swing animation when throwing an ender pearl.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgRender;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      Setting<Boolean> var10003 = this.swing;
      Objects.requireNonNull(var10003);
      this.swingHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.placed = false;
   }

   public void onActivate() {
      this.placed = false;
   }

   @EventHandler(
      priority = 200
   )
   private void onRender(Render3DEvent event) {
      if (this.mc.player != null && this.mc.world != null) {
         Hand hand = this.getHand();
         switch (((SwitchMode)this.switchMode.get()).ordinal()) {
            case 0:
            case 1:
               if (!InvUtils.findInHotbar(new Item[]{Items.ENDER_PEARL}).found()) {
                  return;
               }
               break;
            case 2:
            case 3:
               if (!InvUtils.find(new Item[]{Items.ENDER_PEARL}).found()) {
                  return;
               }
               break;
            default:
               throw new MatchException((String)null, (Throwable)null);
         }

         if (!(Boolean)this.ccBypass.get() || this.cc() || this.placed) {
            boolean rotated = (Boolean)this.instaRot.get() || Managers.ROTATION.start((double)this.getYaw(), (double)(Integer)this.pitch.get(), (double)this.priority, RotationType.Other, (long)Objects.hash(new Object[]{this.name + "look"})) || RotationUtils.yawAngle((double)Managers.ROTATION.lastDir[0], (double)this.getYaw()) < 0.1 && (double)((float)(Integer)this.pitch.get() - Managers.ROTATION.lastDir[1]) < 0.1;
            if (rotated) {
               if ((Boolean)this.instaRot.get()) {
                  this.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround((float)this.getYaw(), (float)(Integer)this.pitch.get(), Managers.ON_GROUND.isOnGround()));
               }

               boolean switched = hand != null;
               if (!switched) {
                  switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                     case 1:
                        InvUtils.swap(InvUtils.findInHotbar(new Item[]{Items.ENDER_PEARL}).slot(), true);
                        switched = true;
                        break;
                     case 2:
                        switched = BOInvUtils.pickSwitch(InvUtils.find(new Item[]{Items.ENDER_PEARL}).slot());
                        break;
                     case 3:
                        switched = BOInvUtils.invSwitch(InvUtils.find(new Item[]{Items.ENDER_PEARL}).slot());
                  }
               }

               if (switched) {
                  this.useItem(hand == null ? Hand.MAIN_HAND : hand);
                  Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "look"}));
                  if ((Boolean)this.swing.get()) {
                     this.clientSwing((SwingHand)this.swingHand.get(), hand == null ? Hand.MAIN_HAND : hand);
                  }

                  this.toggle();
                  this.sendToggledMsg("success");
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
   }

   private boolean cc() {
      label94: {
         switch (((SwitchMode)this.ccSwitchMode.get()).ordinal()) {
            case 0:
            case 1:
               if (!InvUtils.findInHotbar((item) -> item.getItem() instanceof BlockItem).found()) {
                  break label94;
               }
               break;
            case 2:
            case 3:
               if (!InvUtils.find((item) -> item.getItem() instanceof BlockItem).found()) {
                  break label94;
               }
               break;
            default:
               throw new MatchException((String)null, (Throwable)null);
         }

         BlockPos pos = this.mc.player.getBlockPos();
         boolean rotated = (Boolean)this.instaRot.get() || !SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(pos.down(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}));
         if (!rotated) {
            return false;
         }

         if ((Boolean)this.instaRot.get()) {
            this.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround((float)RotationUtils.getYaw(this.mc.player.getEyePos(), pos.toCenterPos()), (float)RotationUtils.getPitch(this.mc.player.getEyePos(), pos.toCenterPos()), Managers.ON_GROUND.isOnGround()));
         }

         Hand hand = this.mc.player.getOffHandStack().getItem() instanceof BlockItem ? Hand.OFF_HAND : (Managers.HOLDING.getStack().getItem() instanceof BlockItem ? Hand.MAIN_HAND : null);
         boolean switched = false;
         if (hand == null) {
            switch (((SwitchMode)this.ccSwitchMode.get()).ordinal()) {
               case 1:
                  InvUtils.swap(InvUtils.findInHotbar((item) -> item.getItem() instanceof BlockItem).slot(), true);
                  switched = true;
                  break;
               case 2:
                  switched = BOInvUtils.pickSwitch(InvUtils.find((item) -> item.getItem() instanceof BlockItem).slot());
                  break;
               case 3:
                  switched = BOInvUtils.invSwitch(InvUtils.find((item) -> item.getItem() instanceof BlockItem).slot());
            }
         }

         if (hand == null && !switched) {
            return false;
         }

         this.placeBlock(hand == null ? Hand.MAIN_HAND : hand, pos.down().toCenterPos(), Direction.UP, pos.down());
         if (!(Boolean)this.instaRot.get() && SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
         }

         this.placed = true;
         if (hand == null) {
            switch (((SwitchMode)this.ccSwitchMode.get()).ordinal()) {
               case 1 -> InvUtils.swapBack();
               case 2 -> BOInvUtils.pickSwapBack();
               case 3 -> BOInvUtils.swapBack();
            }
         }

         return true;
      }

      this.toggle();
      this.sendToggledMsg("cc blocks not found");
      return false;
   }

   private int getYaw() {
      return (int)Math.round(Rotations.getYaw(new Vec3d(Math.floor(this.mc.player.getX()) + (double)0.5F, (double)0.0F, Math.floor(this.mc.player.getZ()) + (double)0.5F))) + 180;
   }

   private Hand getHand() {
      if (Managers.HOLDING.isHolding(Items.ENDER_PEARL)) {
         return Hand.MAIN_HAND;
      } else {
         return this.mc.player.getOffHandStack().getItem() == Items.ENDER_PEARL ? Hand.OFF_HAND : null;
      }
   }

   public static enum SwitchMode {
      Normal,
      Silent,
      PickSilent,
      InvSwitch;

      // $FF: synthetic method
      private static SwitchMode[] $values() {
         return new SwitchMode[]{Normal, Silent, PickSilent, InvSwitch};
      }
   }
}
