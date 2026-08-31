package espada.spacex.aurora.modules.miscplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import java.util.Objects;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class SoundModifier extends Modules {
   private final SettingGroup sgCrystal;
   public final Setting<Boolean> crystalHits;
   public final Setting<Double> crystalHitVolume;
   public final Setting<Double> crystalHitPitch;
   public final Setting<Boolean> expSound;
   public final Setting<Double> explosionVolume;
   public final Setting<Double> explosionPitch;

   public SoundModifier() {
      super(Aurora.MiscPlus, "Sound Modifier", "Modifies sounds to make crystal pvp less horrible for ears.");
      this.sgCrystal = this.settings.createGroup("Crystal");
      this.crystalHits = this.sgCrystal.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Crystal Hit Sound")).description("Allows hit sounds when attacking end crystal.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgCrystal;
      DoubleSetting.Builder var10002 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Crystal Hit Volume")).description("Multiplies crystal hit volumes.")).defaultValue((double)1.0F).sliderRange((double)0.0F, (double)10.0F);
      Setting<Boolean> var10003 = this.crystalHits;
      Objects.requireNonNull(var10003);
      this.crystalHitVolume = var10001.add(((DoubleSetting.Builder)var10002.visible(var10003::get)).build());
      var10001 = this.sgCrystal;
      var10002 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Crystal Hit Pitch")).description("Multiplies pitch of crystal hit sounds.")).defaultValue((double)1.0F).sliderRange((double)0.0F, (double)10.0F);
      var10003 = this.crystalHits;
      Objects.requireNonNull(var10003);
      this.crystalHitPitch = var10001.add(((DoubleSetting.Builder)var10002.visible(var10003::get)).build());
      this.expSound = this.sgCrystal.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Explosion Sound")).description("Allows explosion sounds")).defaultValue(true)).build());
      var10001 = this.sgCrystal;
      var10002 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Explosion Volume")).description("Multiplies explosion volumes.")).defaultValue((double)1.0F).sliderRange((double)0.0F, (double)10.0F);
      var10003 = this.expSound;
      Objects.requireNonNull(var10003);
      this.explosionVolume = var10001.add(((DoubleSetting.Builder)var10002.visible(var10003::get)).build());
      var10001 = this.sgCrystal;
      var10002 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Explosion Pitch")).description("Multiplies pitch of explosions sounds.")).defaultValue((double)1.0F).sliderRange((double)0.0F, (double)10.0F);
      var10003 = this.expSound;
      Objects.requireNonNull(var10003);
      this.explosionPitch = var10001.add(((DoubleSetting.Builder)var10002.visible(var10003::get)).build());
   }
}
