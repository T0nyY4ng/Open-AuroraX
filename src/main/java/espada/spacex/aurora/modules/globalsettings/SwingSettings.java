package espada.spacex.aurora.modules.globalsettings;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import java.util.Objects;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.util.Hand;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;

public class SwingSettings extends Modules {
   private final SettingGroup sgInteract;
   private final SettingGroup sgBlockPlace;
   private final SettingGroup sgMining;
   private final SettingGroup sgAttack;
   private final SettingGroup sgUse;
   public final Setting<Boolean> interact;
   public final Setting<SwingState> interactState;
   public final Setting<Boolean> blockPlace;
   public final Setting<SwingState> blockPlaceState;
   public final Setting<MiningSwingState> mining;
   public final Setting<Boolean> attack;
   public final Setting<SwingState> attackState;
   public final Setting<Boolean> use;
   public final Setting<SwingState> useState;

   public SwingSettings() {
      super(Aurora.Settings, "Swing", "Global swing settings for every aurora module.");
      this.sgInteract = this.settings.createGroup("Interact");
      this.sgBlockPlace = this.settings.createGroup("Block Place");
      this.sgMining = this.settings.createGroup("Mining");
      this.sgAttack = this.settings.createGroup("Attack");
      this.sgUse = this.settings.createGroup("Use");
      this.interact = this.sgInteract.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Interact Swing")).description("Swings your hand when you interact with a block.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgInteract;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Interact State")).description("Should we swing our hand before or after the action.")).defaultValue(SwingState.Post);
      Setting<Boolean> var10003 = this.interact;
      Objects.requireNonNull(var10003);
      this.interactState = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.blockPlace = this.sgBlockPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Block Place Swing")).description("Swings your hand when you interact with a block.")).defaultValue(true)).build());
      var10001 = this.sgBlockPlace;
      var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Block Place State")).description("Should we swing our hand before or after the action.")).defaultValue(SwingState.Post);
      var10003 = this.blockPlace;
      Objects.requireNonNull(var10003);
      this.blockPlaceState = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.mining = this.sgMining.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mining Swing")).description("Swings your hand when you place a crystal.")).defaultValue(SwingSettings.MiningSwingState.Double)).build());
      this.attack = this.sgAttack.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Attack Swing")).description("Swings your hand when you attack any entity.")).defaultValue(true)).build());
      var10001 = this.sgAttack;
      var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Attack State")).description("Should we swing our hand before or after the action.")).defaultValue(SwingState.Post);
      var10003 = this.attack;
      Objects.requireNonNull(var10003);
      this.attackState = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.use = this.sgUse.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Use Swing")).description("Swings your hand when using an item. NCP doesn't check this.")).defaultValue(true)).build());
      var10001 = this.sgUse;
      var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Using State")).description("Should we swing our hand before or after the action.")).defaultValue(SwingState.Post);
      var10003 = this.use;
      Objects.requireNonNull(var10003);
      this.useState = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
   }

   public void swing(SwingState state, SwingType type, Hand hand) {
      if (this.mc.player != null) {
         if (state.equals(this.getState(type))) {
            switch (type) {
               case Interact -> this.swing((Boolean)this.interact.get(), hand);
               case Placing -> this.swing((Boolean)this.blockPlace.get(), hand);
               case Attacking -> this.swing((Boolean)this.attack.get(), hand);
               case Using -> this.swing((Boolean)this.use.get(), hand);
            }

         }
      }
   }

   public void mineSwing(MiningSwingState state) {
      switch (state.ordinal()) {
         case 0:
            return;
         case 1:
            if (this.mining.get() != SwingSettings.MiningSwingState.Start) {
               return;
            }
            break;
         case 2:
            if (this.mining.get() != SwingSettings.MiningSwingState.End) {
               return;
            }
      }

      if (this.mc.player != null) {
         this.swing(true, Hand.MAIN_HAND);
      }
   }

   private SwingState getState(SwingType type) {
      SwingState var10000;
      switch (type) {
         case Interact -> var10000 = (SwingState)this.interactState.get();
         case Placing -> var10000 = (SwingState)this.blockPlaceState.get();
         case Attacking -> var10000 = (SwingState)this.attackState.get();
         case Using -> var10000 = (SwingState)this.useState.get();
         case Mining -> var10000 = SwingState.Post;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   private void swing(boolean shouldSwing, Hand hand) {
      if (this.mc.player != null) {
         if (shouldSwing) {
            this.mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
         }

      }
   }

   public static enum MiningSwingState {
      Disabled,
      Start,
      End,
      Double;

      // $FF: synthetic method
      private static MiningSwingState[] $values() {
         return new MiningSwingState[]{Disabled, Start, End, Double};
      }
   }
}
