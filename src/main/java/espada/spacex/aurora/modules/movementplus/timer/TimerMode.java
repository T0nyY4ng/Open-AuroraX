package espada.spacex.aurora.modules.movementplus.timer;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;

public class TimerMode {
   protected final MinecraftClient mc = MinecraftClient.getInstance();
   private final TimerPlus.TimerModes type;

   public TimerMode(TimerPlus.TimerModes type) {
      this.type = type;
   }

   // Resolved lazily: TimerPlus is only registered in the Modules registry *after* its
   // constructor runs, so an eager field initializer here would capture null forever
   // (this class is instantiated from TimerPlus's constructor). By the time tick events
   // fire, TimerPlus is always registered.
   protected TimerPlus settings() {
      return (TimerPlus)Modules.get().get(TimerPlus.class);
   }

   public void onSendPacket(PacketEvent.Send event) {
   }

   public void onSentPacket(PacketEvent.Sent event) {
   }

   public void onTickEventPre(TickEvent.Pre event) {
   }

   public void onTickEventPost(TickEvent.Post event) {
   }

   public void onActivate() {
   }

   public void onDeactivate() {
   }
}
