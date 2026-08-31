package espada.spacex.aurora.modules.playerplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.utils.BOInvUtils;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.potion.Potions;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.component.DataComponentTypes;

public class AutoPot extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgRender;
   private final Setting<Double> healthThreshold;
   private final Setting<Boolean> autoDisable;
   private final Setting<Integer> throwDelay;
   private final Setting<Boolean> ccBypass;
   private final Setting<SwitchMode> ccSwitchMode;
   private final Setting<SwitchMode> switchMode;
   private final Setting<Boolean> swing;
   private final Setting<SwingHand> swingHand;
   private boolean placed;
   private long lastThrowTick;

   public AutoPot() {
      super(Aurora.PlayerPlus, "Auto Pot", "Automatically throws Splash Potions of Healing II when health is low.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgRender = this.settings.createGroup("Render");
      this.healthThreshold = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Health Threshold")).description("Throw potion when health is below this value.")).defaultValue((double)20.0F).min((double)1.0F).max((double)40.0F).build());
      this.autoDisable = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Auto Disable")).description("Disables the module after throwing a potion.")).defaultValue(true)).build());
      this.throwDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Throw Delay")).description("Delay between consecutive potion throws (in ticks).")).defaultValue(4)).min(0).max(20).build());
      this.ccBypass = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("CC Bypass")).description("Places a block to bypass anti-cheat restrictions.")).defaultValue(false)).build());
      this.ccSwitchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("CC Switch Mode")).description("Switch method for CC bypass block.")).defaultValue(AutoPot.SwitchMode.Silent)).build());
      this.switchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("Switch method for the potion.")).defaultValue(AutoPot.SwitchMode.Silent)).build());
      this.swing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Swing")).description("Renders swing animation when throwing the potion.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgRender;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      Setting<Boolean> var10003 = this.swing;
      Objects.requireNonNull(var10003);
      this.swingHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.placed = false;
      this.lastThrowTick = 0L;
   }

   public void onActivate() {
      this.placed = false;
      this.lastThrowTick = 0L;
   }

   @EventHandler(
      priority = 200
   )
   private void onRender(Render3DEvent event) {
      if (this.mc.player != null && this.mc.world != null) {
         if (!((double)this.mc.player.getHealth() > (Double)this.healthThreshold.get())) {
            long currentTick = this.mc.world.getTime();
            if (currentTick - this.lastThrowTick >= (long)(Integer)this.throwDelay.get()) {
               label97: {
                  Hand hand = this.getHand();
                  switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                     case 0:
                     case 1:
                        if (!InvUtils.findInHotbar(this::isSplashHealthPotion).found()) {
                           break label97;
                        }
                        break;
                     case 2:
                     case 3:
                        if (!InvUtils.find(this::isSplashHealthPotion).found()) {
                           break label97;
                        }
                        break;
                     default:
                        throw new MatchException((String)null, (Throwable)null);
                  }

                  if ((Boolean)this.ccBypass.get() && !this.cc() && !this.placed) {
                     return;
                  }

                  boolean switched = hand != null;
                  if (!switched) {
                     switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                        case 1:
                           InvUtils.swap(InvUtils.findInHotbar(this::isSplashHealthPotion).slot(), true);
                           switched = true;
                           break;
                        case 2:
                           switched = BOInvUtils.pickSwitch(InvUtils.find(this::isSplashHealthPotion).slot());
                           break;
                        case 3:
                           switched = BOInvUtils.invSwitch(InvUtils.find(this::isSplashHealthPotion).slot());
                     }
                  }

                  if (!switched) {
                     return;
                  }

                  this.throwSplashPotion(hand == null ? Hand.MAIN_HAND : hand);
                  this.lastThrowTick = currentTick;
                  if ((Boolean)this.swing.get()) {
                     this.clientSwing((SwingHand)this.swingHand.get(), hand == null ? Hand.MAIN_HAND : hand);
                  }

                  if ((Boolean)this.autoDisable.get()) {
                     this.toggle();
                     this.sendToggledMsg("success");
                  }

                  if (hand == null) {
                     switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                        case 1 -> InvUtils.swapBack();
                        case 2 -> BOInvUtils.pickSwapBack();
                        case 3 -> BOInvUtils.swapBack();
                     }
                  }

                  return;
               }

               if ((Boolean)this.autoDisable.get()) {
                  this.toggle();
                  this.sendToggledMsg("no splash health potions found");
               }

            }
         }
      }
   }

   private boolean cc() {
      label65: {
         switch (((SwitchMode)this.ccSwitchMode.get()).ordinal()) {
            case 0:
            case 1:
               if (!InvUtils.findInHotbar((item) -> item.getItem() instanceof BlockItem).found()) {
                  break label65;
               }
               break;
            case 2:
            case 3:
               if (!InvUtils.find((item) -> item.getItem() instanceof BlockItem).found()) {
                  break label65;
               }
               break;
            default:
               throw new MatchException((String)null, (Throwable)null);
         }

         BlockPos pos = this.mc.player.getBlockPos();
         Hand hand = this.mc.player.getOffHandStack().getItem() instanceof BlockItem ? Hand.OFF_HAND : (this.mc.player.getMainHandStack().getItem() instanceof BlockItem ? Hand.MAIN_HAND : null);
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

      if ((Boolean)this.autoDisable.get()) {
         this.toggle();
         this.sendToggledMsg("cc blocks not found");
      }

      return false;
   }

   private Hand getHand() {
      if (this.isSplashHealthPotion(this.mc.player.getMainHandStack())) {
         return Hand.MAIN_HAND;
      } else {
         return this.isSplashHealthPotion(this.mc.player.getOffHandStack()) ? Hand.OFF_HAND : null;
      }
   }

   private boolean isSplashHealthPotion(ItemStack stack) {
      if (stack.getItem() != Items.SPLASH_POTION) {
         return false;
      } else {
         PotionContentsComponent potionContents = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
         return potionContents != null && potionContents.potion().isPresent() && ((RegistryEntry)potionContents.potion().get()).value() == Potions.STRONG_HEALING;
      }
   }

   private void throwSplashPotion(Hand hand) {
      if (this.mc.player != null && this.mc.world != null) {
         this.mc.interactionManager.interactItem(this.mc.player, hand);
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
