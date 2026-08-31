package espada.spacex.aurora.modules.combatplus.autocrystal.crystalinfo;

import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class HyperCalc {
   public static Setting<Double> CoolDown(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("coolDown")).description("cooldown Autocrystal switch")).defaultValue(0.05).min((double)0.0F).sliderRange((double)0.0F, 0.05).build());
   }

   public static Setting<Double> slowDamage(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Slow Damage")).description("dont go higher than minplacedmg")).defaultValue((double)3.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
   }

   public static Setting<Double> slowSpeed(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Slow Speed")).description("How many times should the module place per second when damage is under slow damage.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
   }

   public static Setting<Double> Desyncforce(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("desyncforce")).description("faceplace, if Df set 1 = enemy h 3")).defaultValue((double)0.0F).min((double)0.0F).sliderRange((double)0.0F, (double)12.0F).build());
   }

   public static Setting<Double> selfCheck(SettingGroup group) {
      return group.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("selfCheck")).description("no kill self, if Sc set 1 = self h 1/2")).defaultValue((double)0.0F).min((double)0.0F).sliderRange((double)0.0F, (double)5.0F).build());
   }
}
