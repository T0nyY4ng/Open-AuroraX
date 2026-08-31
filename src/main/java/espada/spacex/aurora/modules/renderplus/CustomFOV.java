package espada.spacex.aurora.modules.renderplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.events.render.GetFovEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

public class CustomFOV extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<Integer> FOV;

   public CustomFOV() {
      super(Aurora.RenderPlus, "Custom FOV", "Allows more customisation to the FOV.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.FOV = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("FOV")).description("What the FOV should be.")).defaultValue(120)).range(0, 358).sliderRange(0, 358).build());
   }

   @EventHandler
   private void onFov(GetFovEvent event) {
      event.fov = (double)(Integer)this.FOV.get();
   }
}
