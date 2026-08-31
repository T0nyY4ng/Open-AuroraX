package espada.spacex.aurora.modules.renderplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.modules.globalsettings.SwingSettings;
import espada.spacex.aurora.utils.SettingUtils;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.TorchBlock;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;

public class LightsOut extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<Double> delay;
   private final Setting<Boolean> swing;
   private final Setting<SwingHand> swingHand;
   private double timer;

   public LightsOut() {
      super(Aurora.RenderPlus, "Lights Out", "A tribute to Reliant.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.delay = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Tick Delay")).description("Delay between breaking torches.")).defaultValue((double)2.0F).range((double)0.0F, (double)10.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.swing = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Swing")).description("Renders swing animation when breaking a torch.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgGeneral;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      Setting<Boolean> var10003 = this.swing;
      Objects.requireNonNull(var10003);
      this.swingHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.timer = (double)0.0F;
   }

   @EventHandler
   private void onTick(TickEvent.Post event) {
      BlockPos block = this.getLightSource(this.mc.player.getEyePos(), SettingUtils.getMineRange());
      if (block != null && this.timer >= (Double)this.delay.get()) {
         this.timer = (double)0.0F;
         SettingUtils.mineSwing(SwingSettings.MiningSwingState.Start);
         this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, block, Direction.UP));
         this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, block, Direction.UP));
         SettingUtils.mineSwing(SwingSettings.MiningSwingState.End);
         if ((Boolean)this.swing.get()) {
            this.clientSwing((SwingHand)this.swingHand.get(), Hand.MAIN_HAND);
         }
      }

   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      this.timer = Math.min((Double)this.delay.get(), this.timer + event.frameTime);
   }

   private BlockPos getLightSource(Vec3d vec, double r) {
      int c = (int)(Math.ceil(r) + (double)1.0F);
      BlockPos closest = null;
      float closestDist = -1.0F;

      for(int x = -c; x <= c; ++x) {
         for(int y = -c; y <= c; ++y) {
            for(int z = -c; z <= c; ++z) {
               BlockPos pos = this.mc.player.getBlockPos().add(x, y, z);
               if (this.mc.world.getBlockState(pos).getBlock() instanceof TorchBlock) {
                  float dist = (float)vec.distanceTo(pos.toCenterPos());
                  if ((double)dist <= r && (closest == null || dist < closestDist)) {
                     closest = pos;
                     closestDist = dist;
                  }
               }
            }
         }
      }

      return closest;
   }
}
