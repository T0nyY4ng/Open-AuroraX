package espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo;

import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;

public class Misc extends Modules {
   public Misc(Category category, String name, String description) {
      super(category, name, description);
   }

   public static Setting<Boolean> Pause(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause Eat")).description("Pauses when eating")).defaultValue(false)).build());
   }

   public static Setting<Boolean> smartRot(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Smart Rotations")).description("")).defaultValue(true)).build());
   }

   public static Setting<Boolean> ignoreTerrain(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("ignoreTerrain")).description("")).defaultValue(true)).build());
   }

   public static Setting<Boolean> OnAnchorPlacePause(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("OnAnchorPlacePause")).description("")).defaultValue(false)).build());
   }

   public static Setting<Integer> maxtarget(SettingGroup group) {
      return group.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("MaxTarget")).description("")).defaultValue(3)).min(0).sliderRange(0, 6).build());
   }
}
