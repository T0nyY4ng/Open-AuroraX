package espada.spacex.aurora.hud;

import espada.spacex.aurora.Aurora;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class OnTope extends HudElement {
   private final SettingGroup sgGeneral;
   private final Setting<SettingColor> color;
   private final Setting<Double> scale;
   private final Setting<Boolean> shadow;
   public static final HudElementInfo<OnTope> INFO;

   public OnTope() {
      super(INFO);
      this.sgGeneral = this.settings.getDefaultGroup();
      this.color = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
      this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Scale")).description("Modify the size of the text.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.shadow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Text Shadow")).description("Should the text have a shadow.")).defaultValue(true)).build());
   }

   public void render(HudRenderer renderer) {
      if (MeteorClient.mc.player != null) {
         String text = MeteorClient.mc.player.getName().getString() + " on top!";
         this.setSize(renderer.textWidth(text, (Boolean)this.shadow.get(), (Double)this.scale.get()), renderer.textHeight(true, (Double)this.scale.get()));
         renderer.text(text, (double)this.x, (double)this.y, (Color)this.color.get(), (Boolean)this.shadow.get(), (Double)this.scale.get());
      }
   }

   static {
      INFO = new HudElementInfo(Aurora.HUD_EDIT, "OnTope", "I don't even know what this is.", OnTope::new);
   }
}
