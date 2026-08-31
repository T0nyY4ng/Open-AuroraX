package espada.spacex.aurora.modules.playerplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.BOInvUtils;
import meteordevelopment.meteorclient.events.entity.player.FinishUsingItemEvent;
import meteordevelopment.meteorclient.events.entity.player.StoppedUsingItemEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class MCP extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<Mode> mode;
   private final Setting<Boolean> autoToggle;
   private final Setting<SwitchMode> switchMode;
   private final Setting<Boolean> noInventory;
   private final Setting<Boolean> notify;
   private boolean isUsing;

   public MCP() {
      super(Aurora.PlayerPlus, "MiddleClickExtra", "Lets you use items when you middle click.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mode")).description("Which item to use when you middle click.")).defaultValue(MCP.Mode.Pearl)).build());
      this.autoToggle = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Auto Toggle")).description("Auto toggle when item not found.")).defaultValue(false)).build());
      this.switchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description(".")).defaultValue(MCP.SwitchMode.InvSwitch)).build());
      this.noInventory = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Anti Inventory")).description("Not work in inventory.")).defaultValue(true)).build());
      this.notify = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("notify")).description("Notifies you when you do not have the specified item in your hotbar.")).defaultValue(true)).build());
   }

   public void onDeactivate() {
      this.stopIfUsing();
   }

   @EventHandler
   private void onMouseButton(MouseButtonEvent event) {
      if (event.action == KeyAction.Press && event.button == 2) {
         if (!(Boolean)this.noInventory.get() || this.mc.currentScreen == null) {
            FindItemResult result = !((SwitchMode)this.switchMode.get()).equals(MCP.SwitchMode.Silent) ? InvUtils.find(new Item[]{((Mode)this.mode.get()).item}) : InvUtils.findInHotbar(new Item[]{((Mode)this.mode.get()).item});
            if (!result.found()) {
               if ((Boolean)this.autoToggle.get()) {
                  this.toggle();
               }

               if ((Boolean)this.notify.get()) {
                  this.sendDisableMsg("unable to find specified item");
               }

            } else {
               switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                  case 0 -> InvUtils.swap(result.slot(), true);
                  case 1 -> BOInvUtils.pickSwitch(result.slot());
                  case 2 -> BOInvUtils.invSwitch(result.slot());
               }

               switch (((Mode)this.mode.get()).type.ordinal()) {
                  case 0:
                     if (this.mc.interactionManager != null) {
                        this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
                        switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                           case 0 -> InvUtils.swapBack();
                           case 1 -> BOInvUtils.pickSwapBack();
                           case 2 -> BOInvUtils.invSwapBack();
                        }
                     }
                     break;
                  case 1:
                     if (this.mc.interactionManager != null) {
                        this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
                     }
                     break;
                  case 2:
                     this.mc.options.useKey.setPressed(true);
                     this.isUsing = true;
               }

            }
         }
      }
   }

   @EventHandler
   private void onTick(TickEvent.Pre event) {
      if (Utils.canUpdate()) {
         if (this.isUsing) {
            boolean pressed = true;
            if (this.mc.player != null && this.mc.player.getMainHandStack().getItem() instanceof BowItem) {
               pressed = BowItem.getPullProgress(this.mc.player.getItemUseTime()) < 1.0F;
            }

            this.mc.options.useKey.setPressed(pressed);
         }

      }
   }

   @EventHandler
   private void onFinishUsingItem(FinishUsingItemEvent event) {
      this.stopIfUsing();
   }

   @EventHandler
   private void onStoppedUsingItem(StoppedUsingItemEvent event) {
      this.stopIfUsing();
   }

   private void stopIfUsing() {
      if (this.isUsing) {
         this.mc.options.useKey.setPressed(false);
         switch (((SwitchMode)this.switchMode.get()).ordinal()) {
            case 0 -> InvUtils.swapBack();
            case 1 -> BOInvUtils.pickSwapBack();
            case 2 -> BOInvUtils.invSwapBack();
         }

         this.isUsing = false;
      }

   }

   private static enum Type {
      Immediate,
      LongerSingleClick,
      Longer;

      // $FF: synthetic method
      private static Type[] $values() {
         return new Type[]{Immediate, LongerSingleClick, Longer};
      }
   }

   public static enum Mode {
      Pearl(Items.ENDER_PEARL, MCP.Type.Immediate),
      Rocket(Items.FIREWORK_ROCKET, MCP.Type.Immediate),
      Rod(Items.FISHING_ROD, MCP.Type.LongerSingleClick),
      Bow(Items.BOW, MCP.Type.Longer),
      Gap(Items.GOLDEN_APPLE, MCP.Type.Longer),
      EGap(Items.ENCHANTED_GOLDEN_APPLE, MCP.Type.Longer),
      Chorus(Items.CHORUS_FRUIT, MCP.Type.Longer);

      private final Item item;
      private final Type type;

      private Mode(Item item, Type type) {
         this.item = item;
         this.type = type;
      }

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Pearl, Rocket, Rod, Bow, Gap, EGap, Chorus};
      }
   }

   public static enum SwitchMode {
      Silent,
      PickSilent,
      InvSwitch;

      // $FF: synthetic method
      private static SwitchMode[] $values() {
         return new SwitchMode[]{Silent, PickSilent, InvSwitch};
      }
   }
}
