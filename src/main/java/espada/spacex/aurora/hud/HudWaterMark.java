package espada.spacex.aurora.hud;

import espada.spacex.aurora.Aurora;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;
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
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;

public class HudWaterMark extends HudElement {
   private final SettingGroup sgGeneral;
   private final Setting<SettingColor> color;
   private final Setting<Double> scale;
   private final Setting<Boolean> logo;
   private final Setting<Double> logoScale;
   private final Identifier LOGO;
   public static final HudElementInfo<HudWaterMark> INFO;

   public HudWaterMark() {
      super(INFO);
      this.sgGeneral = this.settings.getDefaultGroup();
      this.color = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
      this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Scale")).description("Modify the size of the text.")).defaultValue((double)1.5F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.logo = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Logo")).description("Renders BlackOut logo.")).defaultValue(true)).build());
      this.logoScale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Logo Scale")).description("Modify the size of the logo.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.LOGO = Identifier.of("spacex", "logo.png");
   }

   public void render(HudRenderer renderer) {
      this.setSize(renderer.textWidth("Aurora X", true) * (Double)this.scale.get() * (Double)this.scale.get(), renderer.textHeight(true) * (Double)this.scale.get() * (Double)this.scale.get());
      String text = "Aurora X";
      renderer.text(text, (double)this.x, (double)this.y, (Color)this.color.get(), true, (Double)this.scale.get());
      if ((Boolean)this.logo.get()) {
         MatrixStack matrixStack = new MatrixStack();
         GL.bindTexture(this.LOGO);
         Renderer2D.TEXTURE.begin();
         Renderer2D.TEXTURE.texQuad((double)this.x + renderer.textWidth("Aurora X") * (Double)this.scale.get() * (Double)this.scale.get(), (double)this.y + renderer.textHeight(true) * (Double)this.scale.get() * (Double)this.scale.get() / (double)2.0F - (Double)this.logoScale.get() * (double)128.0F / (double)2.0F, (Double)this.logoScale.get() * (double)128.0F, (Double)this.logoScale.get() * (double)128.0F, new Color(255, 255, 255, 255));
         Renderer2D.TEXTURE.render(matrixStack);
      }
   }

   static {
      INFO = new HudElementInfo(Aurora.HUD_EDIT, "WaterMARK", "The watermark.", HudWaterMark::new);
   }
}
