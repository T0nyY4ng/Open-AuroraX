package espada.spacex.aurora.utils;

import espada.spacex.aurora.modules.combatplus.AutoTrapPlus;
import espada.spacex.aurora.modules.combatplus.HoleFillRewrite;
import espada.spacex.aurora.modules.combatplus.PistonCrystal;
import espada.spacex.aurora.modules.combatplus.SelfTrapPlus;
import espada.spacex.aurora.modules.combatplus.SurroundPlus;
import espada.spacex.aurora.modules.combatplus.automine.AuroraMine;
import espada.spacex.aurora.modules.globalsettings.RotationPrioritySettings;
import espada.spacex.aurora.modules.playerplus.AntiAim;
import espada.spacex.aurora.modules.playerplus.AutoPearl;
import espada.spacex.aurora.modules.playerplus.ScaffoldPlus;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.movement.AntiAFK;

public class PriorityUtils {
   public static int get(Object module) {
      RotationPrioritySettings priority = (RotationPrioritySettings)Modules.get().get(RotationPrioritySettings.class);
      if (priority != null) {
         if (module instanceof AntiAim) {
            return (Integer)priority.antiAim.get();
         }

         if (module instanceof AntiAFK) {
            return (Integer)priority.antiAFK.get();
         }

         if (module instanceof HoleFillRewrite) {
            return (Integer)priority.autoHoleFillPlus.get();
         }

         if (module instanceof AutoPearl) {
            return (Integer)priority.autoPearlClip.get();
         }

         if (module instanceof AutoTrapPlus) {
            return (Integer)priority.autoTrap.get();
         }

         if (module instanceof AuroraMine) {
            return (Integer)priority.autoMine.get();
         }

         if (module instanceof KillAura) {
            return (Integer)priority.killAura.get();
         }

         if (module instanceof PistonCrystal) {
            return (Integer)priority.pistonCrystal.get();
         }

         if (module instanceof ScaffoldPlus) {
            return (Integer)priority.scaffold.get();
         }

         if (module instanceof SelfTrapPlus) {
            return (Integer)priority.selfTrap.get();
         }

         if (module instanceof SurroundPlus) {
            return (Integer)priority.surroundPlus.get();
         }
      }

      return 1000;
   }
}
