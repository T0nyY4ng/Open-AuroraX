package espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo;

import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;

public class Place extends Modules {
   public Place(Category category, String name, String description) {
      super(category, name, description);
   }

   public static Setting<Boolean> Place(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Place")).description("")).defaultValue(true)).build());
   }

   public static Setting<Boolean> instantPlace(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("instantPlace")).description("")).defaultValue(true)).build());
   }

   public static Setting<Double> speedLimit(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("speedLimit")).description("")).defaultValue((double)0.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
   }

   public static Setting<Double> placeSpeed(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("placeSpeed")).description("")).defaultValue((double)0.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
   }

   public static Setting<Double> placeDelay(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("placeDelay")).description("")).defaultValue((double)0.0F).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).build());
   }

   public static Setting<Double> placeDelayTicks(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("placeDelayTicks")).description("")).defaultValue((double)0.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
   }

   public static Setting<Double> MinDmg(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Mindamage")).description("Minimum damage to place.")).defaultValue((double)6.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
   }

   public static Setting<Double> maxPlace(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("MaxSelfPlace")).description("w")).defaultValue((double)9.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
   }

   public static Setting<Double> minPlaceRatio(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("minPlaceRatio")).description("w")).defaultValue(1.4).min((double)0.0F).sliderRange((double)0.0F, (double)5.0F).build());
   }
}
