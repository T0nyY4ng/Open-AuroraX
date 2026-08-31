package espada.spacex.aurora.modules.movementplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.utils.meteor.BOEntityUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixin.PlayerPositionLookS2CPacketAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class BRotateBypass extends Module {
   private final SettingGroup sgGeneral;
   private final Setting<Boolean> burrowbypass;

   public BRotateBypass() {
      super(Aurora.MovementPlus, "BRotateBypass", "BRotateBypass");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.burrowbypass = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("burrowbypass")).description("nolag in burrow.")).defaultValue(false)).build());
   }

   @EventHandler
   public void onReceivePacket(PacketEvent.Receive event) {
      if (event.packet instanceof PlayerPositionLookS2CPacket) {
         if ((Boolean)this.burrowbypass.get() && BOEntityUtils.isBlockLag(this.mc.player)) {
            return;
         }

         ((PlayerPositionLookS2CPacketAccessor)event.packet).setPitch(this.mc.player.getPitch());
         ((PlayerPositionLookS2CPacketAccessor)event.packet).setYaw(this.mc.player.getYaw());
      }

   }
}
