package espada.spacex.aurora.modules.renderplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class AspectRatio extends Modules {
   private final SettingGroup sgGeneral;
   public final Setting<Double> ratio;

   public AspectRatio() {
      super(Aurora.RenderPlus, "AspectRatio", "");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.ratio = this.sgGeneral.add(((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Ratio")).defaultValue((double)1.78F).sliderRange((double)0.1F, (double)5.0F).range((double)0.1F, (double)5.0F).build());
   }
}
