package espada.spacex.aurora.modules.combatplus;

import espada.spacex.aurora.Aurora;
import java.util.Random;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AutoMoan extends Module {
   private static final Logger LOGGER = LogManager.getLogger("AutoMoan");
   private final SettingGroup sgGeneral;
   private final Setting<MoanMode> moanmode;
   private final Setting<Boolean> iFriends;
   private final Setting<Integer> delay;
   private static final String[] Submissive = new String[]{"fuck me harder daddy", "deeper! daddy deeper!", "Fuck yes your so big!", "I love your cock %s!", "Do not stop fucking my ass before i cum!", "Oh your so hard for me", "Want to widen my ass up %s?", "I love you daddy", "Make my bussy pop", "%s loves my bussy so much", "i made %s cum so hard with my tight bussy", "Your cock is so big and juicy daddy!", "Please fuck me as hard as you can", "im %s's personal femboy cumdupster!", "Please shoot your hot load deep inside me daddy!", "I love how %s's dick feels inside of me!", "%s gets so hard when he sees my ass!", "%s really loves fucking my ass really hard!", "why wont u say the last message"};
   private static final String[] Dominant = new String[]{"Be a good boy for daddy", "I love pounding your ass %s!", "Give your bussy to daddy!", "I love how you drip pre-cum while i fuck your ass %s", "Slurp up and down my cock like a good boy", "Come and jump on daddy's cock %s", "I love how you look at me while you suck me off %s", "%s looks so cute when i fuck him", "%s's bussy is so incredibly tight!", "%s takes dick like the good boy he is", "I love how you shake your ass on my dick", "%s moans so cutely when i fuck his ass", "%s is the best cumdupster there is!", "%s is always horny and ready for his daddy's dick", "My dick gets rock hard every time i see %s", "why wont u say the last message"};
   private static final String[] FuckChinaFemboy = new String[]{"%s，兄弟你的手小小的", "%s，兄弟你怎么香香的", "%s，兄弟，我闻闻", "%s，兄弟，别回头，是我", "%s，兄弟，看看腿", "%s，兄弟你好香", "%s，兄弟你的脚怎么香香的", "%s，兄弟你的衣柜怎么那么多女装", "%s，兄弟你好白"};
   private final Random r;
   private int lastNum;
   private int timer;

   public AutoMoan() {
      super(Aurora.CombatPlus, "Auto Moan", "Sends random moan messages to nearby players.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.moanmode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Message Mode")).description("What kind of messages to send.")).defaultValue(AutoMoan.MoanMode.Submissive)).build());
      this.iFriends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Ignore Friends")).description("Doesn't send messages targeted to friends.")).defaultValue(true)).build());
      this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Delay")).description("Tick delay between moans.")).defaultValue(50)).min(0).sliderRange(0, 100).build());
      this.r = new Random();
      this.lastNum = -1;
      this.timer = 0;
      LOGGER.info("AutoMoan module initialized");
   }

   public void onActivate() {
      LOGGER.info("AutoMoan activated");
      this.timer = 0;
      this.lastNum = -1;
   }

   @EventHandler
   private void onTick(TickEvent.Pre event) {
      if (this.mc.player != null && this.mc.world != null) {
         ++this.timer;
         if (this.timer >= (Integer)this.delay.get()) {
            this.sendMoanMessage();
            this.timer = 0;
         }

      } else {
         LOGGER.warn("Player or world is null, skipping tick");
      }
   }

   private void sendMoanMessage() {
      PlayerEntity target = this.getClosest();
      if (target == null) {
         LOGGER.info("No valid target found for moaning");
      } else {
         String name = target.getName().getString();
         String[] messages;
         switch (((MoanMode)this.moanmode.get()).ordinal()) {
            case 0:
               messages = Dominant;
               break;
            case 1:
               messages = Submissive;
               break;
            case 2:
               messages = FuckChinaFemboy;
               break;
            default:
               LOGGER.warn("Invalid moan mode: " + String.valueOf(this.moanmode.get()));
               return;
         }

         int num = this.r.nextInt(messages.length);
         if (num == this.lastNum && messages.length > 1) {
            num = (num + 1) % messages.length;
         }

         this.lastNum = num;
         String message = messages[num].replace("%s", name);
         ChatUtils.sendPlayerMsg(message);
         LOGGER.info("Sent moan message: " + message);
      }
   }

   private PlayerEntity getClosest() {
      if (this.mc.player != null && this.mc.world != null) {
         PlayerEntity closest = null;
         double distance = Double.MAX_VALUE;

         for(PlayerEntity player : this.mc.world.getPlayers()) {
            if (player != this.mc.player && (!(Boolean)this.iFriends.get() || !Friends.get().isFriend(player))) {
               double dist = this.mc.player.getPos().distanceTo(player.getPos());
               if (dist < distance) {
                  closest = player;
                  distance = dist;
               }
            }
         }

         return closest;
      } else {
         LOGGER.warn("Player or world is null in getClosest");
         return null;
      }
   }

   public static enum MoanMode {
      Dominant,
      Submissive,
      FuckChinaFemboy;

      // $FF: synthetic method
      private static MoanMode[] $values() {
         return new MoanMode[]{Dominant, Submissive, FuckChinaFemboy};
      }
   }
}
