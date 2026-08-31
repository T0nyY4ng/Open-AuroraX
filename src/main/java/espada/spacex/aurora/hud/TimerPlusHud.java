package espada.spacex.aurora.hud;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.modules.movementplus.timer.TimerPlus;
import java.util.Objects;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class TimerPlusHud extends HudElement {
   public static final HudElementInfo<TimerPlusHud> INFO;
   private final SettingGroup sgGeneral;
   private final SettingGroup sgScale;
   private final SettingGroup sgBackground;
   private final Setting<Boolean> shadow;
   private final Setting<Integer> border;
   private final Setting<Boolean> customScale;
   private final Setting<Double> scale;
   private final Setting<Boolean> background;
   private final Setting<SettingColor> backgroundColor;
   private final Setting<SettingColor> textColor;

   public TimerPlusHud() {
      super(INFO);
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgScale = this.settings.createGroup("Scale");
      this.sgBackground = this.settings.createGroup("Background");
      this.shadow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("shadow")).description("Text shadow.")).defaultValue(true)).build());
      this.border = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("border")).description("How much space to add around the element.")).defaultValue(0)).build());
      this.customScale = this.sgScale.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("custom-scale")).description("Applies custom text scale rather than the global one.")).defaultValue(false)).build());
      SettingGroup var10001 = this.sgScale;
      DoubleSetting.Builder var10002 = (DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("scale")).description("Custom scale.");
      Setting<Boolean> var10003 = this.customScale;
      Objects.requireNonNull(var10003);
      this.scale = var10001.add(((DoubleSetting.Builder)var10002.visible(var10003::get)).defaultValue((double)1.0F).min((double)0.5F).sliderRange((double)0.5F, (double)3.0F).build());
      this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("background")).description("Displays background.")).defaultValue(false)).build());
      var10001 = this.sgBackground;
      ColorSetting.Builder var2 = (ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("background-color")).description("Color used for the background.");
      var10003 = this.background;
      Objects.requireNonNull(var10003);
      this.backgroundColor = var10001.add(((ColorSetting.Builder)var2.visible(var10003::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
      this.textColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("text-color")).description("A.")).defaultValue(new SettingColor()).build());
   }

   public void setSize(double width, double height) {
      super.setSize(width + (double)((Integer)this.border.get() * 2), height + (double)((Integer)this.border.get() * 2));
   }

   public static double find_percent(double start, double end, double val) {
      end -= start;
      val -= start;
      start = (double)0.0F;
      return ((double)1.0F - val / end) * (double)100.0F;
   }

   public void render(HudRenderer renderer) {
      if ((Boolean)this.background.get()) {
         renderer.quad((double)this.x, (double)this.y, (double)this.getWidth(), (double)this.getHeight(), (Color)this.backgroundColor.get());
      }

      if (this.isInEditor()) {
         this.render(renderer, "4.3", (Color)this.textColor.get());
      } else {
         double percentage = find_percent((double)0.0F, (double)TimerPlus.rechargeDelay, (double)TimerPlus.rechargeTimer);
         this.render(renderer, String.format("%.1f", percentage), (Color)this.textColor.get());
      }
   }

   private void render(HudRenderer renderer, String right, Color rightColor) {
      double x = (double)(this.x + (Integer)this.border.get());
      double y = (double)(this.y + (Integer)this.border.get());
      double x2 = renderer.text("TimerChange: ", x, y, (Color)this.textColor.get(), (Boolean)this.shadow.get(), this.getScale());
      x2 = renderer.text(right, x2, y, rightColor, (Boolean)this.shadow.get(), this.getScale());
      x2 = renderer.text("%", x2, y, rightColor, (Boolean)this.shadow.get(), this.getScale());
      this.setSize(x2 - x, renderer.textHeight((Boolean)this.shadow.get(), this.getScale()));
   }

   private double getScale() {
      return (Boolean)this.customScale.get() ? (Double)this.scale.get() : (double)-1.0F;
   }

   static {
      INFO = new HudElementInfo(Aurora.HUD_EDIT, "timer-plus", "Displays timer plus charge.", TimerPlusHud::new);
   }
}
