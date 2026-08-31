package espada.spacex.aurora.modules.combatplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.RotationUtils;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.meteor.BODamageUtils;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class Aura extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<TargetMode> targetMode;
   private final Setting<Integer> maxHp;
   private final Setting<Double> delay;
   private final Setting<RotationMode> rotationMode;
   private final Setting<SwitchMode> switchMode;
   private final Setting<Boolean> onlyWeapon;
   private final Setting<Boolean> swing;
   private final Setting<SwingHand> swingHand;
   private double timer;
   private PlayerEntity target;

   public Aura() {
      super(Aurora.CombatPlus, "Aura", "Better kill aura. Made for crystal pvp.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.targetMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Target Mode")).description("Which opponent should be targeted.")).defaultValue(Aura.TargetMode.Health)).build());
      this.maxHp = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Max HP")).description("Target's health must be under this value.")).defaultValue(36)).min(0).sliderMax(36).build());
      this.delay = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Delay")).description("Delay that will be used for hits.")).defaultValue((double)0.5F).min((double)0.0F).sliderMax((double)1.0F).build());
      this.rotationMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Rotation mode")).description("When should we rotate. Only active if attack rotations are enabled in rotation settings.")).defaultValue(Aura.RotationMode.OnHit)).build());
      this.switchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch mode")).description("Should be set to disabled.")).defaultValue(Aura.SwitchMode.Disabled)).build());
      this.onlyWeapon = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Only Weapon")).description("Only attacks with a weapon.")).defaultValue(true)).build());
      this.swing = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Swing")).description("Renders swing animation when attacking an entity.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgGeneral;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      Setting<Boolean> var10003 = this.swing;
      Objects.requireNonNull(var10003);
      this.swingHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.timer = (double)0.0F;
      this.target = null;
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      this.timer = Math.min((Double)this.delay.get(), this.timer + event.frameTime);
      this.updateTarget();
      if (this.target != null) {
         boolean switched = false;
         switch (((SwitchMode)this.switchMode.get()).ordinal()) {
            case 0:
               switched = !(Boolean)this.onlyWeapon.get() || this.mc.player.getMainHandStack().getItem() instanceof SwordItem || this.mc.player.getMainHandStack().getItem() instanceof AxeItem;
               break;
            case 1:
               int slot = this.bestSlot(false);
               if (slot >= 0) {
                  InvUtils.swap(slot, true);
                  switched = true;
               }
               break;
            case 2:
            case 3:
            case 4:
               switched = true;
         }

         if (switched) {
            boolean rotated = this.rotationMode.get() != Aura.RotationMode.Constant || !SettingUtils.shouldRotate(RotationType.Attacking) || Managers.ROTATION.start(this.target.getBoundingBox(), (double)this.priority, RotationType.Attacking, (long)Objects.hash(new Object[]{this.name + "attacking"}));
            if (rotated && !(this.timer < (Double)this.delay.get())) {
               rotated = this.rotationMode.get() != Aura.RotationMode.OnHit || !SettingUtils.shouldRotate(RotationType.Attacking) || Managers.ROTATION.start(this.target.getBoundingBox(), (double)this.priority, RotationType.Attacking, (long)Objects.hash(new Object[]{this.name + "attacking"}));
               if (rotated) {
                  switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                     case 2:
                        switched = false;
                        int slotNormal = this.bestSlot(false);
                        if (slotNormal >= 0) {
                           InvUtils.swap(slotNormal, true);
                           switched = true;
                        }
                        break;
                     case 3:
                        switched = false;
                        int slotPick = this.bestSlot(true);
                        if (slotPick >= 0) {
                           switched = BOInvUtils.pickSwitch(slotPick);
                        }
                        break;
                     case 4:
                        switched = false;
                        int slotInventory = this.bestSlot(true);
                        if (slotInventory >= 0) {
                           switched = BOInvUtils.invSwitch(slotInventory);
                        }
                  }

                  if (switched) {
                     this.attackTarget();
                     switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                        case 2 -> InvUtils.swapBack();
                        case 3 -> BOInvUtils.pickSwapBack();
                        case 4 -> BOInvUtils.swapBack();
                     }

                     if (this.rotationMode.get() == Aura.RotationMode.OnHit) {
                        Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "attacking"}));
                     }

                  }
               }
            }
         }
      }
   }

   private void attackTarget() {
      this.timer = (double)0.0F;
      SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.MAIN_HAND);
      this.sendPacket(PlayerInteractEntityC2SPacket.attack(this.target, this.mc.player.isSneaking()));
      SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.MAIN_HAND);
      if ((Boolean)this.swing.get()) {
         this.clientSwing((SwingHand)this.swingHand.get(), Hand.MAIN_HAND);
      }

   }

   private int bestSlot(boolean inventory) {
      int slot = -1;
      double hDmg = (double)-1.0F;

      for(int i = 0; i < (inventory ? this.mc.player.getInventory().size() + 1 : 9); ++i) {
         ItemStack stack = this.mc.player.getInventory().getStack(i);
         if (!(Boolean)this.onlyWeapon.get() || stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem) {
            double dmg = BODamageUtils.getSwordDamage(stack, this.mc.player, this.target, true);
            if (dmg > hDmg) {
               slot = i;
               hDmg = dmg;
            }
         }
      }

      return slot;
   }

   private void updateTarget() {
      double value = (double)0.0F;
      this.target = null;
      this.mc.world.getPlayers().forEach((player) -> {
         if (!(player.getHealth() <= 0.0F) && !player.isSpectator() && !(player.getHealth() + player.getAbsorptionAmount() > (float)(Integer)this.maxHp.get()) && SettingUtils.inAttackRange(player.getBoundingBox()) && player != this.mc.player && !Friends.get().isFriend(player)) {
            double var10000;
            switch (((TargetMode)this.targetMode.get()).ordinal()) {
               case 0 -> var10000 = (double)(10000.0F - player.getHealth() - player.getAbsorptionAmount());
               case 1 -> var10000 = (double)10000.0F - Math.abs(RotationUtils.yawAngle((double)this.mc.player.getYaw(), Rotations.getYaw(player)));
               case 2 -> var10000 = (double)10000.0F - this.mc.player.getPos().distanceTo(player.getPos());
               default -> throw new MatchException((String)null, (Throwable)null);
            }

            double val = var10000;
            if (val > value) {
               this.target = player;
            }

         }
      });
   }

   public static enum TargetMode {
      Health,
      Angle,
      Distance;

      // $FF: synthetic method
      private static TargetMode[] $values() {
         return new TargetMode[]{Health, Angle, Distance};
      }
   }

   public static enum RotationMode {
      OnHit,
      Constant;

      // $FF: synthetic method
      private static RotationMode[] $values() {
         return new RotationMode[]{OnHit, Constant};
      }
   }

   public static enum SwitchMode {
      Disabled,
      Normal,
      Silent,
      PickSwitch,
      InvSwitch;

      // $FF: synthetic method
      private static SwitchMode[] $values() {
         return new SwitchMode[]{Disabled, Normal, Silent, PickSwitch, InvSwitch};
      }
   }
}
