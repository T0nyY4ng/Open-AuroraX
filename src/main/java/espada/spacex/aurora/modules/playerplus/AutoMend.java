package espada.spacex.aurora.modules.playerplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.modules.combatplus.SelfTrapPlus;
import espada.spacex.aurora.modules.combatplus.SurroundPlus;
import espada.spacex.aurora.modules.combatplus.autocrystal.AutoCrystal;
import espada.spacex.aurora.utils.BOInvUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class AutoMend extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgPause;
   private final SettingGroup sgRender;
   private final Setting<Boolean> antiCharity;
   private final Setting<Double> speed;
   private final Setting<Integer> bottles;
   private final Setting<SwitchMode> switchMode;
   private final Setting<Integer> minDur;
   private final Setting<Integer> antiWaste;
   private final Setting<Integer> forceMend;
   private final Setting<Boolean> autoCrystal;
   private final Setting<Integer> autoCrystalTicks;
   private final Setting<Boolean> surroundPause;
   private final Setting<Integer> surroundTicks;
   private final Setting<Boolean> selfTrapPause;
   private final Setting<Integer> selfTrapTicks;
   private final Setting<Boolean> movePause;
   private final Setting<Integer> moveTicks;
   private final Setting<Boolean> offGroundPause;
   private final Setting<Integer> offGroundTicks;
   private final Setting<Boolean> swing;
   private final Setting<SwingHand> swingHand;
   private double timer;
   private BlockPos lastPos;
   private boolean started;
   private boolean shouldRot;
   private int acTimer;
   private int surroundTimer;
   private int selfTrapTimer;
   private int moveTimer;
   private int offGroundTimer;

   public AutoMend() {
      super(Aurora.PlayerPlus, "Auto Mend", "Automatically mends your armor with experience bottles.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgPause = this.settings.createGroup("Pause");
      this.sgRender = this.settings.createGroup("Render");
      this.antiCharity = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Anti Charity")).description("Doesn't mend if any enemy is at same position.")).defaultValue(true)).build());
      this.speed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Throw Speed")).description("How many bottles to throw every second. 20 is recommended.")).defaultValue((double)20.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.bottles = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Bottles")).description("Amount of bottles to throw every time.")).defaultValue(1)).min(0).sliderRange(0, 10).build());
      this.switchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("Method of switching. Silent is the most reliable.")).defaultValue(AutoMend.SwitchMode.Silent)).build());
      this.minDur = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Min Durability")).description("Uses experience if any armor piece is under this durability.")).defaultValue(60)).range(0, 100).sliderRange(0, 100).build());
      this.antiWaste = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Anti Waste")).description("Doesn't use experience if any armor piece is above this durability.")).defaultValue(90)).range(0, 100).sliderRange(0, 100).build());
      this.forceMend = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Force Mend")).description("Ignores anti waste if any armor piece if under this durability.")).defaultValue(30)).range(0, 100).sliderRange(0, 100).build());
      this.autoCrystal = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Auto Crystal Pause")).description("Only throws bottles if auto crystal isn't placing.")).defaultValue(false)).build());
      SettingGroup var10001 = this.sgPause;
      IntSetting.Builder var10002 = ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Auto Crystal Ticks")).description("How many ticks to wait after auto crystal places.")).defaultValue(20)).min(0).sliderRange(0, 100);
      Setting<Boolean> var10003 = this.autoCrystal;
      Objects.requireNonNull(var10003);
      this.autoCrystalTicks = var10001.add(((IntSetting.Builder)var10002.visible(var10003::get)).build());
      this.surroundPause = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Surround Pause")).description("Only throws bottles if surround is not placing.")).defaultValue(false)).build());
      var10001 = this.sgPause;
      var10002 = ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Surround Ticks")).description("How many ticks to wait after surround places.")).defaultValue(20)).min(0).sliderRange(0, 100);
      var10003 = this.surroundPause;
      Objects.requireNonNull(var10003);
      this.surroundTicks = var10001.add(((IntSetting.Builder)var10002.visible(var10003::get)).build());
      this.selfTrapPause = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Self Trap Pause")).description("Only throws bottles if self trap is not placing.")).defaultValue(false)).build());
      var10001 = this.sgPause;
      var10002 = ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Self Trap Ticks")).description("How many ticks to wait after self trap places.")).defaultValue(20)).min(0).sliderRange(0, 100);
      var10003 = this.selfTrapPause;
      Objects.requireNonNull(var10003);
      this.selfTrapTicks = var10001.add(((IntSetting.Builder)var10002.visible(var10003::get)).build());
      this.movePause = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Move Pause")).description("Only throws bottles if you aren't moving")).defaultValue(true)).build());
      var10001 = this.sgPause;
      var10002 = ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Move Ticks")).description("How many ticks to wait after moving.")).defaultValue(20)).min(0).sliderRange(0, 100);
      var10003 = this.movePause;
      Objects.requireNonNull(var10003);
      this.moveTicks = var10001.add(((IntSetting.Builder)var10002.visible(var10003::get)).build());
      this.offGroundPause = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Off Ground Pause")).description("Only throws bottles if not on ground.")).defaultValue(false)).build());
      var10001 = this.sgPause;
      var10002 = ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Off Ground Ticks")).description("How many ticks to wait after being off ground.")).defaultValue(20)).min(0).sliderRange(0, 100);
      var10003 = this.offGroundPause;
      Objects.requireNonNull(var10003);
      this.offGroundTicks = var10001.add(((IntSetting.Builder)var10002.visible(var10003::get)).build());
      this.swing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Swing")).description("Renders swing animation when throwing an exp bottle.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      EnumSetting.Builder var10 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.swing;
      Objects.requireNonNull(var10003);
      this.swingHand = var10001.add(((EnumSetting.Builder)var10.visible(var10003::get)).build());
      this.timer = (double)0.0F;
      this.lastPos = null;
      this.started = false;
      this.shouldRot = false;
      this.acTimer = 0;
      this.surroundTimer = 0;
      this.selfTrapTimer = 0;
      this.moveTimer = 0;
      this.offGroundTimer = 0;
   }

   @EventHandler(
      priority = 200
   )
   private void onRender(Render3DEvent event) {
      if (this.mc.player != null && this.mc.world != null) {
         if (AutoCrystal.placing) {
            this.acTimer = (Integer)this.autoCrystalTicks.get();
         }

         if (SurroundPlus.placing) {
            this.surroundTimer = (Integer)this.surroundTicks.get();
         }

         if (SelfTrapPlus.placing) {
            this.selfTrapTimer = (Integer)this.selfTrapTicks.get();
         }

         if (!this.mc.player.getBlockPos().equals(this.lastPos)) {
            this.lastPos = this.mc.player.getBlockPos();
            this.moveTimer = (Integer)this.moveTicks.get();
         }

         if (!this.mc.player.isOnGround()) {
            this.offGroundTimer = (Integer)this.offGroundTicks.get();
         }

      }
   }

   @EventHandler(
      priority = 200
   )
   private void onTick(TickEvent.Pre event) {
      if (this.mc.player != null && this.mc.world != null && !meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(Suicide.class)) {
         this.timer += (Double)this.speed.get() / (double)20.0F;
         this.updateTimers();
         if (this.timer >= (double)1.0F) {
            Hand hand = Managers.HOLDING.isHolding(Items.EXPERIENCE_BOTTLE) ? Hand.MAIN_HAND : (this.mc.player.getOffHandStack().getItem() == Items.EXPERIENCE_BOTTLE ? Hand.OFF_HAND : null);
            int bottleSlot;
            int bottleAmount;
            if (hand != null) {
               bottleSlot = hand == Hand.MAIN_HAND ? Managers.HOLDING.slot : -1;
               bottleAmount = hand == Hand.MAIN_HAND ? Managers.HOLDING.getStack().getCount() : this.mc.player.getOffHandStack().getCount();
            } else {
               FindItemResult result = this.switchMode.get() != AutoMend.SwitchMode.PickSilent && this.switchMode.get() != AutoMend.SwitchMode.InvSwitch ? InvUtils.findInHotbar((item) -> item.getItem() == Items.EXPERIENCE_BOTTLE) : InvUtils.find((item) -> item.getItem() == Items.EXPERIENCE_BOTTLE);
               bottleSlot = result.slot();
               bottleAmount = result.count();
            }

            if (bottleSlot >= 0 && this.shouldThrow()) {
               this.shouldRot = true;
               boolean rotated = (this.switchMode.get() != AutoMend.SwitchMode.Disabled || hand != null) && Managers.ROTATION.startPitch((double)90.0F, (double)this.priority, RotationType.Use, (long)Objects.hash(new Object[]{this.name + "look"}));
               if (rotated) {
                  boolean switched = hand != null;
                  if (!switched) {
                     switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                        case 1:
                        case 2:
                           InvUtils.swap(bottleSlot, true);
                           switched = true;
                           break;
                        case 3:
                           switched = BOInvUtils.pickSwitch(bottleSlot);
                           break;
                        case 4:
                           switched = BOInvUtils.invSwitch(bottleSlot);
                     }
                  }

                  if (switched) {
                     this.started = true;

                     for(int i = Math.min(bottleAmount, (Integer)this.bottles.get()); i > 0; --i) {
                        this.throwBottle(hand == null ? Hand.MAIN_HAND : hand);
                        --bottleAmount;
                     }

                     --this.timer;
                     if (hand == null) {
                        switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                           case 2 -> InvUtils.swapBack();
                           case 3 -> BOInvUtils.pickSwapBack();
                           case 4 -> BOInvUtils.swapBack();
                        }
                     }
                  }
               }
            } else {
               if (this.shouldRot) {
                  Managers.ROTATION.endPitch((double)90.0F, true);
                  this.shouldRot = false;
               }

               this.started = false;
            }
         }

         this.timer = Math.min((double)1.0F, this.timer);
      }
   }

   private boolean shouldThrow() {
      return this.shouldMend() && (!(Boolean)this.autoCrystal.get() || this.acTimer <= 0) && (!(Boolean)this.surroundPause.get() || this.surroundTimer <= 0) && (!(Boolean)this.selfTrapPause.get() || this.selfTrapTimer <= 0) && (!(Boolean)this.movePause.get() || this.moveTimer <= 0) && (!(Boolean)this.offGroundPause.get() || this.offGroundTimer <= 0);
   }

   private void updateTimers() {
      --this.acTimer;
      --this.surroundTimer;
      --this.selfTrapTimer;
      --this.moveTimer;
      --this.offGroundTimer;
   }

   private boolean shouldMend() {
      List<ItemStack> armors = new ArrayList();

      for(int i = 0; i < 4; ++i) {
         armors.add(this.mc.player.getInventory().getArmorStack(i));
      }

      float max = -1.0F;
      float lowest = 500.0F;

      for(ItemStack stack : armors) {
         float dur = (float)(stack.getMaxDamage() - stack.getDamage()) / (float)stack.getMaxDamage() * 100.0F;
         if (dur > max) {
            max = dur;
         }

         if (dur < lowest) {
            lowest = dur;
         }
      }

      if (lowest <= (float)(Integer)this.forceMend.get()) {
         return true;
      } else if ((Boolean)this.antiCharity.get() && this.playerAtPos()) {
         return false;
      } else if (max >= (float)(Integer)this.antiWaste.get()) {
         return false;
      } else {
         return lowest <= (float)(Integer)this.minDur.get() || this.started;
      }
   }

   private boolean playerAtPos() {
      for(AbstractClientPlayerEntity player : this.mc.world.getPlayers()) {
         if (player != this.mc.player && !Friends.get().isFriend(player) && player.getBlockPos().equals(this.mc.player.getBlockPos())) {
            return true;
         }
      }

      return false;
   }

   private void throwBottle(Hand hand) {
      this.useItem(hand);
      if ((Boolean)this.swing.get()) {
         this.clientSwing((SwingHand)this.swingHand.get(), hand);
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
