package espada.spacex.aurora.modules.movementplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.managers.Managers;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;

public class StrictNoSlow extends Modules {
   private final SettingGroup sgGeneral;
   public final Setting<Boolean> onlyGap;
   public final Setting<Boolean> single;
   public final Setting<Integer> delay;
   private int timer;

   public StrictNoSlow() {
      super(Aurora.MovementPlus, "Strict No Slow", "Should only be used on very strict servers. Requires any other noslow to work.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.onlyGap = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Only Gapples")).description("Only sends packets when eating gapples.")).defaultValue(true)).build());
      this.single = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Single Packet")).description("Only sends 1 switch packet after starting to eat. Works on most servers that require this module.")).defaultValue(true)).build());
      this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Delay")).description("Tick delay between switch packets.")).defaultValue(0)).min(0).sliderRange(0, 20).visible(() -> !(Boolean)this.single.get())).build());
      this.timer = 0;
   }

   @EventHandler
   private void onSend(PacketEvent.Sent event) {
      if (this.mc.player != null) {
         Packet var3 = event.packet;
         if (var3 instanceof PlayerInteractItemC2SPacket) {
            PlayerInteractItemC2SPacket packet = (PlayerInteractItemC2SPacket)var3;
            if (this.shouldSend(packet.getHand() == Hand.MAIN_HAND ? Managers.HOLDING.getStack() : this.mc.player.getOffHandStack())) {
               this.send();
               this.timer = 0;
            }
         }
      }

   }

   @EventHandler
   private void onMove(PlayerMoveEvent event) {
      ++this.timer;
      if (this.timer > (Integer)this.delay.get() && !(Boolean)this.single.get()) {
         this.send();
         this.timer = 0;
      }

   }

   private void send() {
      this.sendPacket(new UpdateSelectedSlotC2SPacket(Managers.HOLDING.slot));
   }

   private boolean shouldSend(ItemStack stack) {
      return this.mc.player != null && ((Boolean)this.onlyGap.get() || stack != null && !stack.isEmpty() && stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE || stack.getItem() == Items.GOLDEN_APPLE);
   }
}
