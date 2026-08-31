package espada.spacex.aurora.modules.miscplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;

public class WeakAlert extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<Boolean> single;
   private final Setting<Integer> delay;
   private int timer;
   private boolean last;

   public WeakAlert() {
      super(Aurora.MiscPlus, "Weak Alert", "Alerts you if you get weakness.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.single = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Single")).description("Only sends the message once.")).defaultValue(false)).build());
      this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Delay")).description("Tick delay between sending the message.")).defaultValue(5)).range(0, 60).sliderMax(60).visible(() -> !(Boolean)this.single.get())).build());
      this.timer = 0;
      this.last = false;
   }

   @EventHandler(
      priority = 100
   )
   private void onTick(TickEvent.Pre event) {
      if (this.mc.player != null && this.mc.world != null) {
         if (this.mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
            if ((Boolean)this.single.get()) {
               if (!this.last) {
                  this.last = true;
                  this.sendBOInfo("you have weakness!!!");
               }
            } else if (this.timer > 0) {
               --this.timer;
            } else {
               this.timer = (Integer)this.delay.get();
               this.last = true;
               this.sendBOInfo("you have weakness!!!");
            }
         } else if (this.last) {
            this.last = false;
            this.sendBOInfo("weakness has ended");
         }
      }

   }
}
