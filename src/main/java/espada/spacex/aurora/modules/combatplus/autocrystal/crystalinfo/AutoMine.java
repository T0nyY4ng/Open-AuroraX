package espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo;

import espada.spacex.aurora.modules.combatplus.autocrystal.AutoCrystalType;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class AutoMine {
   public static Setting<Double> autoMineDamage(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Auto Mine Damage")).description("Prioritizes placing on automine target block.")).defaultValue(1.1).min((double)1.0F).sliderRange((double)1.0F, (double)5.0F).build());
   }

   public static Setting<Boolean> amPlace(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Auto Mine Place")).description("Ignores automine block before if actually breaks.")).defaultValue(true)).build());
   }

   public static Setting<Double> amProgress(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Auto Mine Progress")).description("Ignores the block after it has reached this progress.")).defaultValue(0.95).range((double)0.0F, (double)1.0F).sliderRange((double)0.0F, (double)1.0F).build());
   }

   public static Setting<Boolean> amSpam(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Auto Mine Spam")).description("Spams crystals before the block breaks.")).defaultValue(false)).build());
   }

   public static Setting<AutoCrystalType.AutoMineBrokenMode> amBroken(SettingGroup group) {
      return group.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Auto Mine Broken")).description("Doesn't place on automine block.")).defaultValue(AutoCrystalType.AutoMineBrokenMode.Near)).build());
   }

   public static Setting<Boolean> paAttack(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Piston Crystal Attack")).description("Doesn't attack the crystal placed by piston crystal.")).defaultValue(true)).build());
   }

   public static Setting<Boolean> paPlace(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Piston Crystal Placing")).description("Doesn't place crystals when piston crystal is enabled.")).defaultValue(true)).build());
   }
}
