package espada.spacex.aurora.modules.movementplus.timer.modes;

import espada.spacex.aurora.modules.movementplus.timer.TimerMode;
import espada.spacex.aurora.modules.movementplus.timer.TimerPlus;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;

public class NCP extends TimerMode {
   private final Timer timer = (Timer)Modules.get().get(Timer.class);

   public NCP() {
      super(TimerPlus.TimerModes.NCP);
   }

   public void onDeactivate() {
      this.timer.setOverride((double)1.0F);
   }

   public void onTickEventPre(TickEvent.Pre event) {
      if (this.mc.player != null) {
         if (TimerPlus.rechargeTimer == 0) {
            if (TimerPlus.workingTimer > TimerPlus.workingDelay) {
               TimerPlus.rechargeTimer = TimerPlus.rechargeDelay;
               TimerPlus.workingTimer = 0;
               this.timer.setOverride((double)1.0F);
            } else if (this.settings().isActive()) {
               if ((Boolean)this.settings().onlyInMove.get() && PlayerUtils.isMoving()) {
                  ++TimerPlus.workingTimer;
                  this.timer.setOverride(TimerPlus.timerMultiplier);
               } else if (!(Boolean)this.settings().onlyInMove.get()) {
                  ++TimerPlus.workingTimer;
                  this.timer.setOverride(TimerPlus.timerMultiplier);
               } else {
                  this.timer.setOverride(TimerPlus.timerMultiplierOnRecharge);
               }
            }
         } else {
            --TimerPlus.rechargeTimer;
            if (this.settings().isActive()) {
               this.timer.setOverride(TimerPlus.timerMultiplierOnRecharge);
            } else {
               this.timer.setOverride((double)1.0F);
            }
         }

      }
   }
}
