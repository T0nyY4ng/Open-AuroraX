package espada.spacex.aurora.modules.globalsettings;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class RotationPrioritySettings extends Modules {
   private final SettingGroup sgMain;
   private final SettingGroup sgMisc;
   private final SettingGroup sgPlayer;
   public final Setting<Integer> autoAnchor;
   public final Setting<Integer> autoCrystal;
   public final Setting<Integer> autoWeb;
   public final Setting<Integer> autoMine;
   public final Setting<Integer> autoHoleFillPlus;
   public final Setting<Integer> autoPearlClip;
   public final Setting<Integer> autoTrap;
   public final Setting<Integer> killAura;
   public final Setting<Integer> pistonCrystal;
   public final Setting<Integer> scaffold;
   public final Setting<Integer> selfTrap;
   public final Setting<Integer> surroundPlus;
   public final Setting<Integer> antiAim;
   public final Setting<Integer> antiAFK;

   public RotationPrioritySettings() {
      super(Aurora.Settings, "Priority", "The highest value is prioritized if you want cyrstal > web ,so crystal= 9,web = 10 (high+1 or more)");
      this.sgMain = this.settings.createGroup("Main");
      this.sgMisc = this.settings.createGroup("Misc");
      this.sgPlayer = this.settings.createGroup("Player");
      this.autoAnchor = this.sgMain.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("MaoAura")).description(".9")).defaultValue(0)).range(0, 1000).sliderMax(1000).build());
      this.autoCrystal = this.sgMain.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("AutoCrystal")).description(",9")).defaultValue(0)).range(0, 1000).sliderMax(1000).build());
      this.autoWeb = this.sgMain.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("AutoWeb")).description(".8")).defaultValue(0)).range(0, 1000).sliderMax(1000).build());
      this.autoMine = this.sgMain.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("AutoMine")).description(".0-6")).defaultValue(0)).range(0, 1000).sliderMax(1000).build());
      this.autoHoleFillPlus = this.sgMain.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("AutoHoleFill")).description("10")).defaultValue(0)).range(0, 1000).sliderMax(1000).build());
      this.autoPearlClip = this.sgMisc.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("AutoPearlClip")).description("9-15")).defaultValue(0)).range(0, 1000).sliderMax(1000).build());
      this.autoTrap = this.sgMisc.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("AutoTrapPlus")).description("5-12")).defaultValue(0)).range(0, 1000).sliderMax(1000).build());
      this.killAura = this.sgMisc.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("KillAura")).description("1")).defaultValue(0)).range(0, 1000).sliderMax(1000).build());
      this.pistonCrystal = this.sgMisc.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("PistonCrystal")).description("10")).defaultValue(0)).range(0, 1000).sliderMax(1000).build());
      this.scaffold = this.sgMisc.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("ScaffoldPlus")).description("12")).defaultValue(0)).range(0, 1000).sliderMax(1000).build());
      this.selfTrap = this.sgMisc.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("SelfTrapPlus")).description("9")).defaultValue(0)).range(0, 1000).sliderMax(1000).build());
      this.surroundPlus = this.sgMisc.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("SurroundPlus")).description("0-100")).defaultValue(0)).range(0, 1000).sliderMax(1000).build());
      this.antiAim = this.sgPlayer.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("AntiAim")).description("12")).defaultValue(0)).range(0, 1000).sliderMax(1000).build());
      this.antiAFK = this.sgPlayer.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("AntiAFK")).description("15")).defaultValue(0)).range(0, 1000).sliderMax(1000).build());
   }
}
