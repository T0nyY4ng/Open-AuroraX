package espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class Break {
   public static Setting<Boolean> Break(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Break")).description("")).defaultValue(true)).build());
   }

   public static Setting<Boolean> onlyOwn(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("onlyOwn")).description("")).defaultValue(false)).build());
   }

   public static Setting<Integer> existed(SettingGroup group) {
      return group.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Existed")).description("How many seconds should the crystal exist before attacking.")).defaultValue(0)).min(0).sliderRange(0, 1).build());
   }

   public static Setting<Double> existedTicks(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("existedTicks")).description("")).defaultValue((double)0.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
   }

   public static Setting<Boolean> instantAttack(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("instantAttack")).description("")).defaultValue(true)).build());
   }

   public static Setting<Double> expSpeedLimit(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Explode Speed Limit")).description("How many times to hit any crystal each second. 0 = no limit")).defaultValue((double)0.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
   }

   public static Setting<Double> expSpeed(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Explode Speed")).description("How many times to hit crystal each second.")).defaultValue((double)4.0F).range(0.01, (double)20.0F).sliderRange(0.01, (double)20.0F).build());
   }

   public static Setting<Double> minExplode(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("MinBreakDmg")).description("Minimum enemy damage for exploding a crystal.")).defaultValue((double)2.5F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
   }

   public static Setting<Double> maxExp(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("MaxSelfBreak")).description("Max self damage for exploding a crystal.")).defaultValue((double)9.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
   }

   public static Setting<Double> minExpRatio(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("minBreakRatio")).description("Max self damage ratio for exploding a crystal (enemy / self).")).defaultValue(1.1).min((double)0.0F).sliderRange((double)0.0F, (double)5.0F).build());
   }
}
