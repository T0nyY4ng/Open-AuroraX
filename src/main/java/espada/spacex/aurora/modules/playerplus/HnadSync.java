package espada.spacex.aurora.modules.playerplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.Timer;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class HnadSync extends Modules {
   private final Timer timer = new Timer();
   private final SettingGroup sgGeneral;
   private final Setting<Boolean> delaySync;
   private final Setting<Integer> delay;

   public HnadSync() {
      super(Aurora.PlayerPlus, "Hand Sync", "Sync.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.delaySync = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("DelaySync")).defaultValue(false)).build());
      this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Delay")).defaultValue(35)).min(0).sliderRange(0, 2000).build());
   }

   @EventHandler
   private void onTick(TickEvent.Pre event) {
      if (this.mc.player.isUsingItem()) {
         this.sync();
      }

      if (this.timer.passedMs((long)(Integer)this.delay.get()) && (Boolean)this.delaySync.get()) {
         this.sync();
      }

   }

   private void sync() {
      this.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
      this.mc.player.getInventory().updateItems();
      this.timer.reset();
   }
}
