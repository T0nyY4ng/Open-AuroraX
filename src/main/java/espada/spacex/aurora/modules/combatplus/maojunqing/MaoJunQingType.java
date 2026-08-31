package espada.spacex.aurora.modules.combatplus.maojunqing;

public class MaoJunQingType {
   public static enum LogicMode {
      PlaceBreak,
      BreakPlace;

      // $FF: synthetic method
      private static LogicMode[] $values() {
         return new LogicMode[]{PlaceBreak, BreakPlace};
      }
   }

   public static enum SwitchMode {
      Silent,
      Normal,
      PickSilent,
      InvSwitch,
      Disabled;

      // $FF: synthetic method
      private static SwitchMode[] $values() {
         return new SwitchMode[]{Silent, Normal, PickSilent, InvSwitch, Disabled};
      }
   }

   public static enum AnchorState {
      Air,
      Anchor,
      Loaded;

      // $FF: synthetic method
      private static AnchorState[] $values() {
         return new AnchorState[]{Air, Anchor, Loaded};
      }
   }

   public static enum FadeMode {
      Up,
      Down,
      Normal,
      Test,
      Test2;

      // $FF: synthetic method
      private static FadeMode[] $values() {
         return new FadeMode[]{Up, Down, Normal, Test, Test2};
      }
   }
}
