package espada.spacex.aurora.modules.combatplus.autocrystal;

public class ObsidianHelper {
   protected final AutoCrystal obi;

   public ObsidianHelper(AutoCrystal obsidian) {
      this.obi = obsidian;
   }

   public static enum Mode {
      fast,
      smart,
      none;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{fast, smart, none};
      }
   }
}
