package espada.spacex.aurora.hud;

import espada.spacex.aurora.Aurora;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class PacketHud extends HudElement {
   private final SettingGroup sgGeneral;
   private final Setting<Boolean> swap;
   public static final HudElementInfo<PacketHud> INFO;

   public PacketHud() {
      super(INFO);
      this.sgGeneral = this.settings.getDefaultGroup();
      this.swap = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("swap")).description("Swaps the order of the text.")).defaultValue(false)).build());
   }

   public void tick(HudRenderer renderer) {
      int send = MeteorClient.mc.getNetworkHandler() == null ? 23 : (int)MeteorClient.mc.getNetworkHandler().getConnection().getAveragePacketsSent();
      int received = MeteorClient.mc.getNetworkHandler() == null ? 86 : (int)MeteorClient.mc.getNetworkHandler().getConnection().getAveragePacketsReceived();
      double width = (double)0.0F;
      double height = (double)0.0F;
      width += renderer.textWidth("Send: " + send + "10");
      height += renderer.textHeight() * (double)2.0F;
      if (renderer.textWidth("Received: " + received + "10") > width) {
         width = renderer.textWidth("Received: " + received + "10");
      }

      this.box.setSize(width, height);
   }

   public void render(HudRenderer renderer) {
      int send = MeteorClient.mc.getNetworkHandler() == null ? 23 : (int)MeteorClient.mc.getNetworkHandler().getConnection().getAveragePacketsSent();
      int received = MeteorClient.mc.getNetworkHandler() == null ? 86 : (int)MeteorClient.mc.getNetworkHandler().getConnection().getAveragePacketsReceived();
      double x = (double)this.x;
      double y = (double)this.y;
      Color primaryColor = TextHud.getSectionColor(0);
      Color secondaryColor = TextHud.getSectionColor(1);
      if ((Boolean)this.swap.get()) {
         renderer.text("Received: ", x, y, primaryColor, true);
         x += renderer.textWidth("Received: ");
         renderer.text("" + received, x, y, secondaryColor, true);
      } else {
         renderer.text("Send: ", x, y, primaryColor, true);
         x += renderer.textWidth("Send: ");
         renderer.text("" + send, x, y, secondaryColor, true);
      }

      y += renderer.textHeight();
      x = (double)this.x;
      if ((Boolean)this.swap.get()) {
         renderer.text("Send: ", x, y, primaryColor, true);
         x += renderer.textWidth("Send: ");
         renderer.text("" + send, x, y, secondaryColor, true);
      } else {
         renderer.text("Received: ", x, y, primaryColor, true);
         x += renderer.textWidth("Received: ");
         renderer.text("" + received, x, y, secondaryColor, true);
      }

   }

   static {
      INFO = new HudElementInfo(Aurora.HUD_EDIT, "Packet Hud", "Displays your average send packets.", PacketHud::new);
   }
}
