package espada.spacex.aurora.modules.combatplus.autocrystal;

public class AutoCrystalType {
   public static enum SwitchMode {
      Disabled,
      Simple,
      Gapple,
      Silent,
      InvSilent,
      PickSilent;

      // $FF: synthetic method
      private static SwitchMode[] $values() {
         return new SwitchMode[]{Disabled, Simple, Gapple, Silent, InvSilent, PickSilent};
      }
   }

   public static enum calcMode {
      HyperCard,
      earthhack,
      Normal;

      // $FF: synthetic method
      private static calcMode[] $values() {
         return new calcMode[]{HyperCard, earthhack, Normal};
      }
   }

   public static enum ExplodeMode {
      Crystal,
      Calc,
      Always;

      // $FF: synthetic method
      private static ExplodeMode[] $values() {
         return new ExplodeMode[]{Crystal, Calc, Always};
      }
   }

   public static enum SequentialMode {
      Disabled(0),
      Strict(2),
      Strong(1);

      public final int ticks;

      private SequentialMode(int ticks) {
         this.ticks = ticks;
      }

      // $FF: synthetic method
      private static SequentialMode[] $values() {
         return new SequentialMode[]{Disabled, Strict, Strong};
      }
   }

   public static enum DelayMode {
      Seconds,
      Ticks;

      // $FF: synthetic method
      private static DelayMode[] $values() {
         return new DelayMode[]{Seconds, Ticks};
      }
   }

   public static enum RenderMode {
      MotionOut,
      Smooth,
      Future,
      Earthhack,
      Romb;

      // $FF: synthetic method
      private static RenderMode[] $values() {
         return new RenderMode[]{MotionOut, Smooth, Future, Earthhack, Romb};
      }
   }

   public static enum EarthFadeMode {
      Normal,
      Up,
      Down,
      Shrink;

      // $FF: synthetic method
      private static EarthFadeMode[] $values() {
         return new EarthFadeMode[]{Normal, Up, Down, Shrink};
      }
   }

   public static enum MotionOutMode {
      blockbox,
      None;

      // $FF: synthetic method
      private static MotionOutMode[] $values() {
         return new MotionOutMode[]{blockbox, None};
      }
   }

   public static enum FadeMode {
      Up,
      Down,
      Normal;

      // $FF: synthetic method
      private static FadeMode[] $values() {
         return new FadeMode[]{Up, Down, Normal};
      }
   }

   public static enum AutoMineBrokenMode {
      Near(true, false, false),
      Broken(true, true, false),
      Never(false, false, false),
      Always(true, true, true);

      public final boolean normal;
      public final boolean near;
      public final boolean broken;

      private AutoMineBrokenMode(boolean normal, boolean near, boolean broken) {
         this.normal = normal;
         this.near = near;
         this.broken = broken;
      }

      // $FF: synthetic method
      private static AutoMineBrokenMode[] $values() {
         return new AutoMineBrokenMode[]{Near, Broken, Never, Always};
      }
   }
}
