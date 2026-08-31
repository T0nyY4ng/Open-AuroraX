package espada.spacex.aurora.modules.miscplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

public class Suffix extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<Mode> mode;

   public Suffix() {
      super(Aurora.MiscPlus, "Suffix", "Suffix.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mode")).description("4")).defaultValue(Suffix.Mode.Aurora)).build());
   }

   private String getSuffix() {
      String var10000;
      switch (((Mode)this.mode.get()).ordinal()) {
         case 0 -> var10000 = " A̷u̷r̷o̷r̷a̷";
         case 1 -> var10000 = " ʜʏᴘᴇʀɪᴏɴ";
         case 2 -> var10000 = " 上にカミブルー ";
         case 3 -> var10000 = " ʍօօռ օf Sաօʀɖ†";
         case 4 -> var10000 = " \ud835\ude4f\ud835\ude5d\ud835\ude5a \ud835\ude45\ud835\ude56\ud835\ude58\ud835\ude60";
         case 5 -> var10000 = " 3ᵃʳᵗʰʰ4ᶜᵏ";
         case 6 -> var10000 = " ᶻᵒʳⁱᴴᵃᶜᵏ";
         case 7 -> var10000 = " ＴＲＯＬＬ ＨＡＣＫ";
         case 8 -> var10000 = " ✷ℜ\ud835\udd22\ud835\udd1f\ud835\udd26\ud835\udd2f\ud835\udd31\ud835\udd25";
         case 9 -> var10000 = " \ud835\udde0\ud835\uddf6\ud835\uddfc";
         case 10 -> var10000 = " \ud835\udd10\ud835\udd22\ud835\udd29\ud835\udd2c\ud835\udd2b\ud835\udd05\ud835\udd22\ud835\udd31\ud835\udd1e";
         default -> throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   @EventHandler
   private void onMessageSend(SendMessageEvent event) {
      Object message = event.message;
      if (!((String)message).startsWith(".") && !((String)message).startsWith("/") && !((String)message).startsWith("+")) {
         event.message = (String)message + this.getSuffix();
      }
   }

   public static enum Mode {
      Aurora,
      Hyperion,
      kamiblue,
      Espada,
      Jack,
      earthhack,
      Zori,
      Trollhack,
      Rebirth,
      mio,
      shit;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Aurora, Hyperion, kamiblue, Espada, Jack, earthhack, Zori, Trollhack, Rebirth, mio, shit};
      }
   }
}
