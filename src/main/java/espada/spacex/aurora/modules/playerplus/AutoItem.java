package espada.spacex.aurora.modules.playerplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.utils.BOInvUtils;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoItem extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgRender;
   private final Setting<UseItem> item;
   private final Setting<Boolean> ccBypass;
   private final Setting<SwitchMode> ccSwitchMode;
   private final Setting<SwitchMode> switchMode;
   private final Setting<Boolean> swing;
   private final Setting<SwingHand> swingHand;
   private boolean placed;

   public AutoItem() {
      super(Aurora.PlayerPlus, "Auto Item", "Automatically uses ender pearl or firework rocket without rotating.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgRender = this.settings.createGroup("Render");
      this.item = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Item")).description("The item to use (Ender Pearl or Firework Rocket).")).defaultValue(AutoItem.UseItem.EnderPearl)).build());
      this.ccBypass = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("CC Bypass")).description("Places a block to bypass anti-cheat restrictions.")).defaultValue(false)).build());
      this.ccSwitchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("CC Switch Mode")).description("Switch method for CC bypass block.")).defaultValue(AutoItem.SwitchMode.Silent)).build());
      this.switchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("Switch method for the item.")).defaultValue(AutoItem.SwitchMode.Silent)).build());
      this.swing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Swing")).description("Renders swing animation when using the item.")).defaultValue(true)).build());
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
               if (!InvUtils.findInHotbar(new Item[]{((UseItem)this.item.get()).item}).found()) {
                  return;
               }
               break;
            case 2:
            case 3:
               if (!InvUtils.find(new Item[]{((UseItem)this.item.get()).item}).found()) {
                  return;
               }
               break;
            default:
               throw new MatchException((String)null, (Throwable)null);
         }

         if (!(Boolean)this.ccBypass.get() || this.cc() || this.placed) {
            boolean switched = hand != null;
            if (!switched) {
               switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                  case 1:
                     InvUtils.swap(InvUtils.findInHotbar(new Item[]{((UseItem)this.item.get()).item}).slot(), true);
                     switched = true;
                     break;
                  case 2:
                     switched = BOInvUtils.pickSwitch(InvUtils.find(new Item[]{((UseItem)this.item.get()).item}).slot());
                     break;
                  case 3:
                     switched = BOInvUtils.invSwitch(InvUtils.find(new Item[]{((UseItem)this.item.get()).item}).slot());
               }
            }

            if (switched) {
               this.useItem(hand == null ? Hand.MAIN_HAND : hand);
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

   private boolean cc() {
      label60: {
         switch (((SwitchMode)this.ccSwitchMode.get()).ordinal()) {
            case 0:
            case 1:
               if (!InvUtils.findInHotbar((item) -> item.getItem() instanceof BlockItem).found()) {
                  break label60;
               }
               break;
            case 2:
            case 3:
               if (!InvUtils.find((item) -> item.getItem() instanceof BlockItem).found()) {
                  break label60;
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

      this.toggle();
      this.sendToggledMsg("cc blocks not found");
      return false;
   }

   private Hand getHand() {
      if (this.mc.player.getMainHandStack().getItem() == ((UseItem)this.item.get()).item) {
         return Hand.MAIN_HAND;
      } else {
         return this.mc.player.getOffHandStack().getItem() == ((UseItem)this.item.get()).item ? Hand.OFF_HAND : null;
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

   public static enum UseItem {
      EnderPearl(Items.ENDER_PEARL),
      FireworkRocket(Items.FIREWORK_ROCKET);

      public final Item item;

      private UseItem(Item item) {
         this.item = item;
      }

      // $FF: synthetic method
      private static UseItem[] $values() {
         return new UseItem[]{EnderPearl, FireworkRocket};
      }
   }
}
