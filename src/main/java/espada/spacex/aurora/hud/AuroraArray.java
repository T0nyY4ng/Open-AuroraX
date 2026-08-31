package espada.spacex.aurora.hud;

import espada.spacex.aurora.Aurora;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.math.MathHelper;

public class AuroraArray extends HudElement {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgWave;
   private final Setting<SettingColor> color;
   private final Setting<SettingColor> infoColor;
   private final Setting<Side> side;
   private final Setting<From> from;
   private final Setting<Boolean> infoCare;
   private final Setting<Boolean> combatpage;
   private final Setting<Boolean> shadow;
   private final Setting<Double> scale;
   private final Setting<Boolean> wave;
   private final Setting<SettingColor> waveColor;
   private final Setting<SettingColor> infoWaveColor;
   private final Setting<Double> speed;
   private final Setting<Double> length;
   public static final HudElementInfo<AuroraArray> INFO;

   public AuroraArray() {
      super(INFO);
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgWave = this.settings.createGroup("Wave");
      this.color = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Module Color")).description("The color the ArrayList will use for module names.")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.infoColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Info Color")).description("The color the ArrayList will use for info strings.")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
      this.side = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Side")).description("The alignment.")).defaultValue(AuroraArray.Side.Right)).build());
      this.from = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("From")).description("The sorting direction.")).defaultValue(AuroraArray.From.Top)).build());
      this.infoCare = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Info Length")).description("Should the list care about the the info text length when sorting?")).defaultValue(true)).build());
      this.combatpage = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("MainPage")).description("Only shows aurora modules in the hud.")).defaultValue(false)).build());
      this.shadow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Shadow")).description("Renders a shadow behind the chars.")).defaultValue(true)).build());
      this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Scale")).description("The scale the ArrayList will be rendered at.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)10.0F, (double)10.0F).build());
      this.wave = this.sgWave.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Wave")).description("The wave color.")).defaultValue(false)).build());
      this.waveColor = this.sgWave.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Module Wave Color")).description("The color the ArrayList will use for module names.")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
      this.infoWaveColor = this.sgWave.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Info Wave Color")).description("The color the ArrayList will use for info strings.")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
      this.speed = this.sgWave.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Wave Speed")).description("The speed of the color waves.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)1.0F, (double)10.0F).build());
      this.length = this.sgWave.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Wave Length")).description("How long color waves are.")).defaultValue((double)5.0F).min((double)0.0F).sliderRange((double)1.0F, (double)10.0F).build());
   }

   public void render(HudRenderer renderer) {
      double height = this.height(renderer);
      List<Line> lines = (List)this.getModules().stream().sorted(Comparator.comparing((module) -> {
         String var10002 = module.title;
         return this.width(renderer, var10002 + ((Boolean)this.infoCare.get() ? (this.getInfo(module).isEmpty() ? "" : this.getInfo(module) + " ") : ""));
      })).map((module) -> new Line(module.title, this.getInfo(module))).collect(Collectors.toList());
      if (this.from.get() == AuroraArray.From.Top) {
         Collections.reverse(lines);
      }

      this.setSize((double)120.0F * (Double)this.scale.get() * (Double)this.scale.get(), !lines.isEmpty() ? height * (double)lines.size() : (double)30.0F * (Double)this.scale.get() * (Double)this.scale.get());

      for(int i = 0; i < lines.size(); ++i) {
         Line line = (Line)lines.get(i);
         double f = (double)0.0F;
         if ((Boolean)this.wave.get()) {
            f = Math.sin((double)System.currentTimeMillis() / (double)1000.0F * (Double)this.speed.get() - (double)i / (Double)this.length.get()) + (double)1.0F;
         }

         String var10001 = line.name;
         double var10002;
         if (this.side.get() == AuroraArray.Side.Left) {
            var10002 = (double)this.x;
         } else {
            var10002 = (double)(this.x + this.getWidth());
            String var10005 = line.name;
            var10002 -= this.width(renderer, var10005 + (line.info.isEmpty() ? "" : " " + line.info));
         }

         renderer.text(var10001, var10002, (double)this.y + (double)i * height, this.getColor((SettingColor)this.color.get(), (SettingColor)this.waveColor.get(), f), (Boolean)this.shadow.get(), (Double)this.scale.get());
         renderer.text(line.info, this.side.get() == AuroraArray.Side.Left ? (double)this.x + this.width(renderer, line.name + " ") : (double)(this.x + this.getWidth()) - this.width(renderer, line.info), (double)this.y + (double)i * height, this.getColor((SettingColor)this.infoColor.get(), (SettingColor)this.infoWaveColor.get(), f), (Boolean)this.shadow.get(), (Double)this.scale.get());
      }

   }

   private String getInfo(Module module) {
      return module.getInfoString() == null ? "" : module.getInfoString();
   }

   private List<Module> getModules() {
      return (List)Modules.get().getActive().stream().filter((module) -> !(Boolean)this.combatpage.get() || module.category.equals(Aurora.CombatPlus)).collect(Collectors.toList());
   }

   private Color getColor(SettingColor color, SettingColor waveColor, double f) {
      return (Color)((Boolean)this.wave.get() ? new Color(this.colorVal(color.r, waveColor.r, f), this.colorVal(color.g, waveColor.g, f), this.colorVal(color.b, waveColor.b, f), color.a) : color);
   }

   private int colorVal(int original, int wave, double f) {
      return MathHelper.clamp((int)Math.floor((double)wave + (double)(original - wave) * f), 0, 255);
   }

   private double width(HudRenderer renderer, String text) {
      return renderer.textWidth(text) * (Double)this.scale.get() * (Double)this.scale.get();
   }

   private double height(HudRenderer renderer) {
      return renderer.textHeight(true) * (Double)this.scale.get() * (Double)this.scale.get();
   }

   static {
      INFO = new HudElementInfo(Aurora.HUD_EDIT, "AuroraArray", "An ArrayList for aurora features.", AuroraArray::new);
   }

   private static record Line(String name, String info) {
      public String toString() {
         String var10000 = this.name;
         return var10000 + (this.info.isEmpty() ? "" : " " + this.info);
      }
   }

   public static enum Side {
      Right,
      Left;

      // $FF: synthetic method
      private static Side[] $values() {
         return new Side[]{Right, Left};
      }
   }

   public static enum From {
      Top,
      Bottom;

      // $FF: synthetic method
      private static From[] $values() {
         return new From[]{Top, Bottom};
      }
   }
}
