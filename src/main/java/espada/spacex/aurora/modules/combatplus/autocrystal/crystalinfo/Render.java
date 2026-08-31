package espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class Render {
   public static Setting<Boolean> placeSwing(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Place-Swing")).description("Renders swing animation when placing a crystal.")).defaultValue(true)).build());
   }

   public static Setting<Boolean> attackSwing(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Attack-Swing")).description("Renders swing animation when placing a crystal.")).defaultValue(true)).build());
   }

   public static Setting<SettingColor> lineColor(SettingGroup group) {
      return group.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("Line color of rendered boxes")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
   }

   public static Setting<SettingColor> color(SettingGroup group) {
      return group.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("Side color of rendered boxes")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
   }

   public static Setting<Boolean> Render(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Rendere")).description("Render RenderRenderRenderRenderRender")).defaultValue(true)).build());
   }

   public static Setting<Boolean> renderTargetEsp(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Render Target")).description("Render on target.")).defaultValue(true)).build());
   }

   public static Setting<SettingColor> color2(SettingGroup group) {
      return group.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("RenderTargetEsp Color")).description("Color")).defaultValue(new SettingColor(149, 149, 149, 170)).build());
   }

   public static Setting<Boolean> renderDmg(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Render Text Damage")).description("2D rendering of player and enemy damage.")).defaultValue(true)).build());
   }

   public static Setting<Double> scale(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Scale")).defaultValue((double)1.0F).sliderRange(0.1, (double)2.0F).build());
   }

   public static Setting<Integer> decimal(SettingGroup group) {
      return group.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Decimal")).defaultValue(1)).min(1).sliderRange(1, 10).build());
   }

   public static Setting<SettingColor> damageColor(SettingGroup group) {
      return group.add(((ColorSetting.Builder)(new ColorSetting.Builder()).name("damageColor")).defaultValue(new SettingColor(255, 255, 255)).build());
   }
}
