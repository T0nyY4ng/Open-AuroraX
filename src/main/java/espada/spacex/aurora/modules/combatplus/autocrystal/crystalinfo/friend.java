package espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo;

import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class friend {
   public static Setting<Double> minFriendPlaceRatio(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("MinFriendPlaceRatio")).description(" ")).defaultValue((double)2.0F).min((double)0.0F).sliderRange((double)0.0F, (double)5.0F).build());
   }

   public static Setting<Double> maxFriendPlace(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("MaxFriendDmg")).description("Max friend damage for exploding a crystal.")).defaultValue((double)12.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
   }

   public static Setting<Double> maxFriendExp(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("MaxFriendDmg")).description("Max friend damage for exploding a crystal.")).defaultValue((double)12.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
   }

   public static Setting<Double> minFriendExpRatio(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("FriendBreakRatio")).description("Min friend damage ratio for exploding a crystal (enemy / friend).")).defaultValue((double)2.0F).min((double)0.0F).sliderRange((double)0.0F, (double)5.0F).build());
   }
}
