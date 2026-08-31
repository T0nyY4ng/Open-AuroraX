package espada.spacex.aurora.modules.renderplus;

import com.mojang.blaze3d.systems.RenderSystem;
import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.render.FogShape;

public class Fog extends Modules {
   private final SettingGroup sgGeneral;
   public final Setting<FogShape> shape;
   public final Setting<Double> distance;
   public final Setting<Integer> fading;
   public final Setting<Double> thickness;
   public final Setting<SettingColor> color;

   public Fog() {
      super(Aurora.RenderPlus, "Fog", "Customizable fog.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.shape = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape")).description("Fog shape.")).defaultValue(FogShape.SPHERE)).build());
      this.distance = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Distance")).description("How far away should the fog start rendering.")).defaultValue((double)25.0F).min((double)0.0F).sliderRange((double)0.0F, (double)100.0F).build());
      this.fading = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Fading")).description("How smoothly should the fog fade.")).defaultValue(25)).min(0).sliderRange(0, 1000).build());
      this.thickness = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Thickness")).description(".")).defaultValue((double)10.0F).range((double)1.0F, (double)100.0F).sliderRange((double)1.0F, (double)100.0F).build());
      this.color = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Color")).description("Color of the fog.")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
   }

   public void modifyFog() {
      RenderSystem.setShaderFogColor((float)((SettingColor)this.color.get()).r, (float)((SettingColor)this.color.get()).g, (float)((SettingColor)this.color.get()).b, (float)((SettingColor)this.color.get()).a / (float)(((double)100.0F - (Double)this.thickness.get()) * (double)2.55F));
      RenderSystem.setShaderFogStart((float)((Double)this.distance.get() * (double)1.0F));
      RenderSystem.setShaderFogEnd((float)((Double)this.distance.get() + (double)(Integer)this.fading.get()));
      RenderSystem.setShaderFogShape((FogShape)this.shape.get());
   }
}
