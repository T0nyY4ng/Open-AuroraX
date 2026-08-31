package espada.spacex.aurora.modules.movementplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.meteor.BOEntityUtils;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class MoveUp extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<Boolean> onlyMoveInBurrow;
   private final Setting<Double> rubberbandOffset;
   private final Setting<Double> rubberbandPackets;
   private final Setting<Boolean> pEndChest;

   public MoveUp() {
      super(Aurora.MovementPlus, "AutoBup", "Help you move up from burrow");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.onlyMoveInBurrow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("onlyMoveInBurrow")).description(".")).defaultValue(true)).build());
      this.rubberbandOffset = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("rubberbandOffset")).description("Delay between breaking torches.")).defaultValue((double)9.0F).range((double)-10.0F, (double)10.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.rubberbandPackets = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("rubberbandPackets")).description("Delay between breaking torches.")).defaultValue((double)1.0F).range((double)0.0F, (double)10.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.pEndChest = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("PauseInEndChest")).description("Pause ec player.")).defaultValue(false)).build());
   }

   public void onActivate() {
      if (!(Boolean)this.onlyMoveInBurrow.get() || BOEntityUtils.isBurrowed(this.mc.player, !(Boolean)this.pEndChest.get())) {
         double y = (double)0.0F;
         double velocity = 0.42;

         while(y < 1.1) {
            y += velocity;
            velocity = (velocity - 0.08) * 0.98;
            this.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + y, this.mc.player.getZ(), false));
         }

         for(int i = 0; (double)i < (Double)this.rubberbandPackets.get(); ++i) {
            this.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + y + (Double)this.rubberbandOffset.get(), this.mc.player.getZ(), false));
         }
      }

      this.toggle();
   }
}
