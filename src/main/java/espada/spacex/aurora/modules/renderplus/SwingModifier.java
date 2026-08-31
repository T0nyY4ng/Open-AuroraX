package espada.spacex.aurora.modules.renderplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;

public class SwingModifier extends Modules {
   private final SettingGroup sgMainHand;
   private final SettingGroup sgOffHand;
   private final Setting<Double> mSpeed;
   private final Setting<Double> mStart;
   private final Setting<Double> mEnd;
   private final Setting<Double> myStart;
   private final Setting<Double> myEnd;
   private final Setting<Boolean> mReset;
   private final Setting<Double> oSpeed;
   private final Setting<Double> oStart;
   private final Setting<Double> oEnd;
   private final Setting<Double> oyStart;
   private final Setting<Double> oyEnd;
   private final Setting<Boolean> oReset;
   private static boolean mainSwinging = false;
   private float mainProgress;
   private boolean offSwinging;
   private float offProgress;

   public SwingModifier() {
      super(Aurora.RenderPlus, "Swing Modifier", "Modifies swing rendering.");
      this.sgMainHand = this.settings.createGroup("Main Hand");
      this.sgOffHand = this.settings.createGroup("Off Hand");
      this.mSpeed = this.sgMainHand.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Main Speed")).description("Speed of swinging.")).defaultValue((double)1.0F).min((double)0.0F).sliderMax((double)10.0F).build());
      this.mStart = this.sgMainHand.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Main Start Progress")).description("Starts swing at this progress.")).defaultValue((double)0.0F).sliderMax((double)10.0F).build());
      this.mEnd = this.sgMainHand.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Main End Progress")).description("Swings until reaching this progress.")).defaultValue((double)1.0F).sliderMax((double)10.0F).build());
      this.myStart = this.sgMainHand.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Main Start Y")).description("Hand Y value in the beginning.")).defaultValue((double)0.0F).sliderRange((double)-10.0F, (double)10.0F).build());
      this.myEnd = this.sgMainHand.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Main End Y")).description("Hand Y value in the end.")).defaultValue((double)0.0F).sliderRange((double)-10.0F, (double)10.0F).build());
      this.mReset = this.sgMainHand.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Reset")).description("Resets swing when swinging again.")).defaultValue(false)).build());
      this.oSpeed = this.sgOffHand.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Off Speed")).description("Speed of swinging for offhand")).defaultValue((double)1.0F).min((double)0.0F).sliderMax((double)10.0F).build());
      this.oStart = this.sgOffHand.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Off Start Progress")).description("Starts swing at this progress.")).defaultValue((double)0.0F).sliderMax((double)10.0F).build());
      this.oEnd = this.sgOffHand.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Off End Progress")).description("Swings until reaching this progress.")).defaultValue((double)1.0F).sliderMax((double)10.0F).build());
      this.oyStart = this.sgOffHand.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Off Start Y")).description("Start Y value for offhand.")).defaultValue((double)0.0F).sliderRange((double)-10.0F, (double)10.0F).build());
      this.oyEnd = this.sgOffHand.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Off End Y")).description("End Y value for offhand.")).defaultValue((double)0.0F).sliderRange((double)-10.0F, (double)10.0F).build());
      this.oReset = this.sgOffHand.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Reset")).description("Resets swing when swinging again.")).defaultValue(false)).build());
      this.mainProgress = 0.0F;
      this.offSwinging = false;
      this.offProgress = 0.0F;
   }

   public void startSwing(Hand hand) {
      if (hand == Hand.MAIN_HAND) {
         if ((Boolean)this.mReset.get() || !mainSwinging) {
            this.mainProgress = 0.0F;
            mainSwinging = true;
         }
      } else if ((Boolean)this.oReset.get() || !this.offSwinging) {
         this.offProgress = 0.0F;
         this.offSwinging = true;
      }

   }

   @EventHandler
   public void onRender(Render3DEvent event) {
      if (mainSwinging) {
         if (this.mainProgress >= 1.0F) {
            mainSwinging = false;
            this.mainProgress = 0.0F;
         } else {
            this.mainProgress = (float)((double)this.mainProgress + event.frameTime * (Double)this.mSpeed.get());
         }
      }

      if (this.offSwinging) {
         if (this.offProgress >= 1.0F) {
            this.offSwinging = false;
            this.offProgress = 0.0F;
         } else {
            this.offProgress = (float)((double)this.offProgress + event.frameTime * (Double)this.oSpeed.get());
         }
      }

   }

   public float getSwing(Hand hand) {
      return hand == Hand.MAIN_HAND ? (float)((Double)this.mStart.get() + ((Double)this.mEnd.get() - (Double)this.mStart.get()) * (double)this.mainProgress) : (float)((Double)this.oStart.get() + ((Double)this.oEnd.get() - (Double)this.oStart.get()) * (double)this.offProgress);
   }

   public float getY(Hand hand) {
      return hand == Hand.MAIN_HAND ? (float)((Double)this.myStart.get() + ((Double)this.myEnd.get() - (Double)this.myStart.get()) * (double)this.mainProgress) / -10.0F : (float)((Double)this.oyStart.get() + ((Double)this.oyEnd.get() - (Double)this.oyStart.get()) * (double)this.offProgress) / -10.0F;
   }
}
