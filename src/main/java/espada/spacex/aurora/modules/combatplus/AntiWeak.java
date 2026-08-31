package espada.spacex.aurora.modules.combatplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.Timer;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class AntiWeak extends Modules {
   private Packet<?> packet = null;
   private final Timer delayTimer = new Timer();
   private final SettingGroup sgGeneral;
   private int lastSlot;
   private final Setting<Integer> delay;
   private final Setting<Boolean> always;
   private final Setting<Boolean> sync;
   private int old;
   private boolean update;

   public AntiWeak() {
      super(Aurora.CombatPlus, "AntiWeak", "test");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.lastSlot = -1;
      this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Delay")).defaultValue(35)).min(0).sliderRange(0, 2000).build());
      this.always = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Always")).description("an.")).defaultValue(true)).build());
      this.sync = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Only Burrow")).description("an.")).defaultValue(true)).build());
      this.old = -1;
      this.update = false;
   }

   @EventHandler(
      priority = -200
   )
   public void onPacketSend(PacketEvent.Send event) {
      if (Utils.canUpdate()) {
         if (!event.isCancelled()) {
            if (this.mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
               if (!(this.mc.player.getMainHandStack().getItem() instanceof SwordItem)) {
                  if (this.delayTimer.passedMs((long)(Integer)this.delay.get())) {
                     if (event.packet instanceof PlayerInteractEntityC2SPacket) {
                        this.packet = event.packet;
                        this.doAnti();
                        event.setCancelled(true);
                     }

                     if (this.update) {
                        this.mc.player.networkHandler.sendPacket(this.packet);
                        BOInvUtils.doSwap(this.old);
                        this.delayTimer.reset();
                     }

                  }
               }
            }
         }
      }
   }

   private void doAnti() {
      if (this.packet != null) {
         int strong = InvUtils.findInHotbar((itemStack) -> itemStack.getItem() instanceof SwordItem).slot();
         if (strong != -1) {
            this.old = this.mc.player.getInventory().selectedSlot;
            BOInvUtils.doSwap(strong);
            this.error(this.packet.toString(), new Object[0]);
            if (this.mc.player.getInventory().selectedSlot == InvUtils.findInHotbar((itemStack) -> itemStack.getItem() instanceof SwordItem).slot() && !this.update) {
               this.update = true;
            }

         }
      }
   }

   @EventHandler(
      priority = 200
   )
   private void onTick(TickEvent.Post event) {
      this.update();
   }

   @EventHandler
   public void onRender2D(Render2DEvent event) {
      this.update();
   }

   private void update() {
      if (this.lastSlot != -1 && !(Boolean)this.always.get() && (Boolean)this.sync.get()) {
         if (!(this.mc.player.getInventory().getStack(this.lastSlot).getItem() instanceof SwordItem)) {
            this.lastSlot = -1;
         }

      }
   }

   public static enum SwapMode {
      Normal,
      Silent,
      Bypass;

      // $FF: synthetic method
      private static SwapMode[] $values() {
         return new SwapMode[]{Normal, Silent, Bypass};
      }
   }
}
