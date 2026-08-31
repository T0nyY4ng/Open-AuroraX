package espada.spacex.aurora.modules.miscplus;

import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class CapesModule extends Module {
   public static String capeed;
   private final SettingGroup sgGeneral1;
   public final Setting<Mode> modee;

   public CapesModule() {
      super(Categories.Misc, "capes", "Just Capes");
      this.sgGeneral1 = this.settings.getDefaultGroup();
      this.modee = this.sgGeneral1.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("mode")).description("Decide from packet or client sided rotation.")).defaultValue(CapesModule.Mode.Aetheric)).build());
   }

   @EventHandler
   public void onActivate() {
      Mode selectedMode = (Mode)this.modee.get();
      switch (selectedMode.ordinal()) {
         case 0 -> capeed = "aetheric";
         case 1 -> capeed = "avo";
         case 2 -> capeed = "anime1";
         case 3 -> capeed = "anime2";
         case 4 -> capeed = "anime3";
         case 5 -> capeed = "anime4";
         case 6 -> capeed = "clow";
         case 7 -> capeed = "dev";
         case 8 -> capeed = "feather";
         case 9 -> capeed = "vapev";
         case 10 -> capeed = "hacker";
         case 11 -> capeed = "anarchy";
         case 12 -> capeed = "2011";
         case 13 -> capeed = "2012";
         case 14 -> capeed = "2013";
         case 15 -> capeed = "2015";
         case 16 -> capeed = "2016";
         default -> capeed = "anime2";
      }

   }

   @EventHandler
   public void onDeactivate() {
      capeed = null;
   }

   public static enum Mode {
      Aetheric("Aetheric"),
      Avo("AVO"),
      Anime1("Anime1"),
      Anime2("Anime2"),
      Anime3("Anime3"),
      Anime4("Anime4"),
      Clown("Clown"),
      Developer("DEV"),
      Feather("Feather"),
      VapeV4("VapeV4"),
      hacker("Hacker"),
      Anarchy("Anarchy"),
      Minecon2011("2011"),
      Minecon2012("2012"),
      Minecon2013("2013"),
      Minecon2015("2015"),
      Minecon2016("2016");

      private final String title;

      private Mode(String title) {
         this.title = title;
      }

      public String toString() {
         return this.title;
      }

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Aetheric, Avo, Anime1, Anime2, Anime3, Anime4, Clown, Developer, Feather, VapeV4, hacker, Anarchy, Minecon2011, Minecon2012, Minecon2013, Minecon2015, Minecon2016};
      }
   }
}
