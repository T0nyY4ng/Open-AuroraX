package espada.spacex.aurora.modules.miscplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class AutoEz extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgKill;
   private final SettingGroup sgPop;
   private final Setting<Double> range;
   private final Setting<Integer> tickDelay;
   private final Setting<Boolean> kill;
   private final Setting<MessageMode> killMsgMode;
   private final Setting<List<String>> killMessages;
   private final Setting<Boolean> pop;
   private final Setting<List<String>> popMessages;
   private final Random r;
   private int lastNum;
   private int lastPop;
   private boolean lastState;
   private String name;
   private final List<Message> messageQueue;
   private int timer;
   private final String[] exhibobo;
   private final String[] Aurora;
   private final String[] QuickMacro;
   private final String[] ACE_joni;
   private final String[] Xin;
   private final String[] Dyyibang;
   private final String[] Test;
   private final String[] KRY4TAL;
   private final String[] AlexJonnyMine;
   private final String[] Xsclub;
   private final String[] noclue;

   public AutoEz() {
      super(espada.spacex.aurora.Aurora.MiscPlus, "Auto EZ", "Sends message after enemy dies (too EZ nn's).");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgKill = this.settings.createGroup("Kill");
      this.sgPop = this.settings.createGroup("Pop");
      this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Enemy Range")).description("Only send message if enemy died inside this range.")).defaultValue((double)25.0F).min((double)0.0F).sliderRange((double)0.0F, (double)50.0F).build());
      this.tickDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Delay")).description("How many ticks to wait between sending messages.")).defaultValue(50)).min(0).sliderRange(0, 100).build());
      this.kill = this.sgKill.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Kill")).description("Should we send a message when enemy dies")).defaultValue(true)).build());
      this.killMsgMode = this.sgKill.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Kill Message Mode")).description("What kind of messages to send.")).defaultValue(AutoEz.MessageMode.Blackout)).build());
      this.killMessages = this.sgKill.add(((StringListSetting.Builder)((StringListSetting.Builder)((StringListSetting.Builder)((StringListSetting.Builder)(new StringListSetting.Builder()).name("Kill Messages")).description("Messages to send when killing an enemy with aurora message mode on")).defaultValue(List.of("Fucked by BlackOut!", "BlackOut on top", "BlackOut strong", "BlackOut gayming"))).visible(() -> this.killMsgMode.get() == AutoEz.MessageMode.Blackout)).build());
      this.pop = this.sgPop.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pop")).description("Should we send a message when enemy pops a totem")).defaultValue(true)).build());
      this.popMessages = this.sgPop.add(((StringListSetting.Builder)((StringListSetting.Builder)((StringListSetting.Builder)(new StringListSetting.Builder()).name("Pop Messages")).description("Messages to send when popping an enemy")).defaultValue(List.of("I love it when you pop <NAME>", "Music to my ears <NAME>", "Pop pop pop wont stop till you drop <NAME>"))).build());
      this.r = new Random();
      this.name = null;
      this.messageQueue = new LinkedList();
      this.timer = 0;
      this.exhibobo = new String[]{"Wow, you just died in a block game %s", "%s died in a block game lmfao.", "%s died for using an android device. LOL", "%s, your mother is of the homophobic type", "That's a #VictoryRoyale!, better luck next time, %s!", "%s, used Flux then got backhanded by the face of hypixel", "even loolitsalex has more wins then you %s", "my grandma plays minecraft better than you %s", "%s, you should look into purchasing vape", "Omg %s I'm so sorry", "%s, What's worse your skin or the fact your a casual f3ckin normie", "you know what %s, blind gamers deserve a chance too. I support you.", "that was a pretty bad move %s", "how does it feel to get stomped on %s", "%s, do you really like dying this much?", "if i had a choice between %s and jake paul, id choose jake paul", "hey %s, what does your IQ and kills have in common? They are both low af", "Hey %s, want some PvP advice?", "wow, you just died in a game about legos", "i'm surprised that you were able hit the 'Install' button %s", "%s I speak English not your gibberish.", "%s Take the L, kid", "%s got memed", "%s is a default skin!!!1!1!1!1!!1!1", "%s You died in a fucking block game", "%s likes anime", "%s Trash dawg, you barely even hit me.", "%s I just fucked him so hard he left the game", "%s get bent over and fucked kid", "%s couldn't even beat 4 block", "Someone get this kid a tissue, %s is about to cry!", "%s's dad is bald", "%s Your family tree must be a cactus because everybody on it is a prick.", "%s You're so fucking trash that the binman mistook you for garbage and collected you in the morning", "%s some kids were dropped at birth but you were clearly thrown at a wall", "%s go back to your mother's womb you retarded piece of shit", "Thanks for the free kill %s !", "Benjamin's forehead is bigger than your future Minecraft PvP career %s", "%s are you even trying?", "%s You. Are. Terrible.", "%s my mom is better at this game then you", "%s lololololol mad? lololololol", "%s /friend me so we can talk about how useless you are", "%s: \"Staff! Staff! Help me! I am dogcrap at this game and i am getting rekt!\"", "%s Is it really that hard to trace me while i'm hopping around you?", "%s, Vape is a cool thing you should look into!", "%s I'm not using reach, you just need to click faster.", "%s I hope you recorded that, so that you can watch how trash you really are.", "%s You have to use the left and right mouse button in this game, in case you forgot.", "%s I think that the amount of ping you have equates to your braincells dumbfuck asshat", "%s ALT+F4 to remove the problem", "%s alt+f4 for hidden perk window", "%s You'll eventually switch back to Fortnite again, so why not do it now?", "%s go back to fortnite where you belong, you degenerate 5 year old", "%s I'll be sure to Orange Justice the fucck out of your corpse", "%s Exhibob better than you!1", "%s I'm a real gamer, and you just got owned!!", "%s Take a taste of your own medicine you clapped closet cheater", "%s go drown in your own salt", "%s go and suck off prestonplayz, you 7 yr old fanboy", "%s how are you so bad. I'm losing brain cells just watching you play", "%s Jump down from your school building with a rope around your neck.", "%s dominated, monkey :dab:", "%s Please add me as a friend so that you can shout at me. I live for it.", "%s i fvcked your dad", "%s Yeah, I dare you, rage quit. Come on, make us both happy.", "%s No, you are not blind! I DID own you!", "%s easy 10 hearted L", "%s It's almost as if i can hear you squeal from the other side!", "%s If you read this, you are confirmed homosexual", "%s have you taken a dump lately? Because I just beat the shit of out you.", "%s 6 block woman beater", "%s feminist demolisher", "%s chromosome count doubles the size of this game", "a million years of evolution and we get %s", "if the body is 70 percent water how is %s 100 percent salt???", "%s L", "%s got rekt", "%s you're so fat that when you had a fire in your house you dialled 999 on the microwave", "LMAO %s is a Fluxuser", "LMAO %s is a Sigmauser", "%s I suffer from these fukking kicks, grow brain lol", "LMAO %s a crack user", "%s Hypixel thought could stop us from cheating, huh, you are just as delusional as him", "%s GET FUCKED IM ON BADLION CLIENT WHORE", "%s should ask tene if i was hacking or not", "%s check out ARITHMOS CHANNEL", "%s gay", "%s, please stop", "%s, I play fortnite duos with your mom", "%s acts hard but %s's dad beats him harder", "Lol commit not alive %s", "How'd you hit the DOWNLOAD button with that aim? %s", "I'd say your aim is cancer, but at least cancer kills people. %s", "%s is about as useful as pedals on a wheelchair", "%s's aim is now sponsored by Parkinson's!", "%s, I'd say uninstall but you'd probably miss that too.", "%s, I bet you edate.", "%s, you probably watch tenebrous videos and are intruiged", "%s Please could you not commit not die kind sir thanks", "%s gay", "%s you probably suck on door knobs", "%s go commit stop breathing u dumb idot", "%s go commit to sucking on door knobs", "the only way you can improve at pvp %s is by taking a long walk off a short pier", "L %s", "%s Does not have a good client", "%s's client refused to work", "%s Stop hacking idiot", "%s :potato:", "%s go hunt kangaroos fucking aussie ping", "%s Super Mario Bros. deathsound", "Hey everyone, do /friend add %s , and tell them how trash they are", "%s Just do a France 1940, thank you", "Hey %s , would you like to hear a joke? Yeah, you ain't getting any", "%s got OOFed", "You mum your dad the ones you never had %s", "%s please be toxic to me, I enjoy it", "oof %s", "%s knock knock, FBI open up, we saw you searched for cracked vape.", "%s plez commit jump out of window for free rank", "%s you didn't even stand a chance!", "%s keep trying!", "%s, you're the type of player to get 3rd place in a 1v1", "%s, I'm not saying you're worthless, but I would unplug your life support to charge my phone", "I didn't know dying was a special ability %s", "%s, Stephen Hawking had better hand-eye coordination than you", "%s, kids like you were the inspiration for birth control", "%s you're the definition of bane", "%s lol GG!!!", "%s lol bad client what is it exhibition?", "%s L what are you lolitsalex?", "%s gg e z kid", "%s tene is my favorite youtuber and i bought his badlion client clock so i'm legit", "Don't forget to report me %s", "Your IQ is that of a Steve %s", "%s have you taken a dump lately? Because I just beat the shit of out you.", "%s dont ever put bean in my donut again.", "%s 2 plus 2 is 4, minus 1 that's your IQ", "I think you need vape %s !", "%s You just got oneTapped LUL", "%s You're the inspiration for birth control", "%s I don't understand why condoms weren't named by you.", "%s, My blind grandpa has better aim than you.", "%s, Exhibob better then you!", "%s, u r So E.Z", "Exhibition > %s", "%s, NMSL", "%s, your parents abondoned you, then the orphanage did the same", "%s,stop using trash client like sigma.", "%s, your client is worse than sigma, and that's an achievement", "%s, ur fatter than Napoleon", "%s please consider not alive", "%s, probably bought sigma premium", "%s, probably asks for sigma premium keys", "%s the type of person to murder someone and apologize saying it was a accident", "%s you're the type of person who would quickdrop irl", "%s, got an F on the iq test.", "Don't forget to report me %s", "%s even viv is better than you LMAO", "%s your mom gaye", "%s I Just Sneezed On Your Forehead", "%s your teeth are like stars - golden, and apart.", "%s Rose are blue, stars are red, you just got hacked on and now you're dead", "%s i don't hack because watchdog is watching so it would ban me anyway.", "%s, chill out on the paint bro", "%s You got died from the best client in the game, now with Infinite Sprint bypass", "%s you're so fat, that your bellybutton reaches your house 20 minutes before you do", "%s your dick is so small, that you bang cheerios"};
      this.Aurora = new String[]{"%s Aurora User Version Kill You"};
      this.QuickMacro = new String[]{"Aurora用户击杀了 %s 其他主播小心点 "};
      this.ACE_joni = new String[]{"%s 这不行那不行 我乔尼哥要你有何用? "};
      this.Xin = new String[]{"%s 我正在使用Aurora 游玩中国2B2T "};
      this.Dyyibang = new String[]{"%s 可以详细说一下你买Future被骗200r的过程吗? "};
      this.Test = new String[]{"/kill"};
      this.KRY4TAL = new String[]{"LJM语音所有人里面都在说 %s 牛逼 %s 牛逼"};
      this.AlexJonnyMine = new String[]{"%s 挖掘失败 "};
      this.Xsclub = new String[]{"首先Dyyibang不是我在玩 其次EZ %s"};
      this.noclue = new String[]{"Yeah, these niggas say, I’ll catch a foul, what do they know?", "Just tryna score a point in the end zone", "Didn’t ask your opinion, nigga who the fuck are you?", "Hand on my choppa, I'll turn you to pasta linguini, Yeah okay", "Niggas be hostile, I know that they just wanna be me, in my lane", "Vibin' with the gang, smoking gas in the coupe", "Had to drop her and she had no clue", "Run up, you done up, your ass gon' get toast", "All you niggas on lame shit", "Your bitch come back to my place, I do the most", "Break their net, with the rolex, that two-tone", "Mobbin' with a thick bitch, might be a redbone"};
   }

   public void onActivate() {
      super.onActivate();
      this.lastState = false;
      this.lastNum = -1;
   }

   public String getInfoString() {
      return ((MessageMode)this.killMsgMode.get()).name();
   }

   @EventHandler(
      priority = 200
   )
   private void onTick(TickEvent.Pre event) {
      ++this.timer;
      if (this.mc.player != null && this.mc.world != null) {
         if (this.anyDead((Double)this.range.get()) && (Boolean)this.kill.get()) {
            if (!this.lastState) {
               this.lastState = true;
               this.sendKillMessage();
            }
         } else {
            this.lastState = false;
         }

         if (this.timer >= (Integer)this.tickDelay.get() && !this.messageQueue.isEmpty()) {
            Message msg = (Message)this.messageQueue.get(0);
            ChatUtils.sendPlayerMsg(msg.message);
            this.timer = 0;
            if (msg.kill) {
               this.messageQueue.clear();
            } else {
               this.messageQueue.remove(0);
            }
         }
      }

   }

   @EventHandler
   private void onReceive(PacketEvent.Receive event) {
      Packet var3 = event.packet;
      if (var3 instanceof EntityStatusS2CPacket packet) {
         if (packet.getStatus() == 35) {
            Entity entity = packet.getEntity(this.mc.world);
            if ((Boolean)this.pop.get() && this.mc.player != null && this.mc.world != null && entity instanceof PlayerEntity && entity != this.mc.player && !Friends.get().isFriend((PlayerEntity)entity) && this.mc.player.getPos().distanceTo(entity.getPos()) <= (Double)this.range.get()) {
               this.sendPopMessage(entity.getName().getString());
            }
         }
      }

   }

   private boolean anyDead(double range) {
      for(PlayerEntity pl : this.mc.world.getPlayers()) {
         if (pl != this.mc.player && !Friends.get().isFriend(pl) && pl.getPos().distanceTo(this.mc.player.getPos()) <= range && pl.getHealth() <= 0.0F) {
            this.name = pl.getName().getString();
            return true;
         }
      }

      return false;
   }

   private void sendKillMessage() {
      switch (((MessageMode)this.killMsgMode.get()).ordinal()) {
         case 0:
            if (!((List)this.killMessages.get()).isEmpty()) {
               int num1 = this.r.nextInt(0, ((List)this.killMessages.get()).size());
               if (num1 == this.lastNum) {
                  num1 = num1 < ((List)this.killMessages.get()).size() - 1 ? num1 + 1 : 0;
               }

               this.lastNum = num1;
               this.messageQueue.add(0, new Message(((String)((List)this.killMessages.get()).get(num1)).replace("%s", this.name == null ? "You" : this.name), true));
            }
            break;
         case 1:
            int num2 = this.r.nextInt(0, this.exhibobo.length);
            if (num2 == this.lastNum) {
               num2 = num2 < this.exhibobo.length - 1 ? num2 + 1 : 0;
            }

            this.lastNum = num2;
            this.messageQueue.add(0, new Message(this.exhibobo[num2].replace("%s", this.name == null ? "You" : this.name), true));
            break;
         case 2:
            int num3 = this.r.nextInt(0, this.noclue.length);
            if (num3 == this.lastNum) {
               num3 = num3 < this.noclue.length - 1 ? num3 + 1 : 0;
            }

            this.lastNum = num3;
            this.messageQueue.add(0, new Message(this.noclue[num3].replace("%s", this.name == null ? "You" : this.name), true));
            break;
         case 3:
            int num4 = this.r.nextInt(0, this.Aurora.length);
            if (num4 == this.lastNum) {
               num4 = num4 < this.Aurora.length - 1 ? num4 + 1 : 0;
            }

            this.lastNum = num4;
            this.messageQueue.add(0, new Message(this.Aurora[num4].replace("%s", this.name == null ? "You" : this.name), true));
            break;
         case 4:
            int num5 = this.r.nextInt(0, this.QuickMacro.length);
            if (num5 == this.lastNum) {
               num5 = num5 < this.QuickMacro.length - 1 ? num5 + 1 : 0;
            }

            this.lastNum = num5;
            this.messageQueue.add(0, new Message(this.QuickMacro[num5].replace("%s", this.name == null ? "You" : this.name), true));
            break;
         case 5:
            int num6 = this.r.nextInt(0, this.ACE_joni.length);
            if (num6 == this.lastNum) {
               num6 = num6 < this.ACE_joni.length - 1 ? num6 + 1 : 0;
            }

            this.lastNum = num6;
            this.messageQueue.add(0, new Message(this.ACE_joni[num6].replace("%s", this.name == null ? "You" : this.name), true));
            break;
         case 6:
            int num7 = this.r.nextInt(0, this.Xin.length);
            if (num7 == this.lastNum) {
               num7 = num7 < this.Xin.length - 1 ? num7 + 1 : 0;
            }

            this.lastNum = num7;
            this.messageQueue.add(0, new Message(this.Xin[num7].replace("%s", this.name == null ? "You" : this.name), true));
            break;
         case 7:
            int num8 = this.r.nextInt(0, this.Test.length);
            if (num8 == this.lastNum) {
               num8 = num8 < this.Test.length - 1 ? num8 + 1 : 0;
            }

            this.lastNum = num8;
            this.messageQueue.add(0, new Message(this.Test[num8].replace("%s", this.name == null ? "You" : this.name), true));
            break;
         case 8:
            int num9 = this.r.nextInt(0, this.KRY4TAL.length);
            if (num9 == this.lastNum) {
               num9 = num9 < this.KRY4TAL.length - 1 ? num9 + 1 : 0;
            }

            this.lastNum = num9;
            this.messageQueue.add(0, new Message(this.KRY4TAL[num9].replace("%s", this.name == null ? "You" : this.name), true));
            break;
         case 9:
            int num10 = this.r.nextInt(0, this.Xsclub.length);
            if (num10 == this.lastNum) {
               num10 = num10 < this.Xsclub.length - 1 ? num10 + 1 : 0;
            }

            this.lastNum = num10;
            this.messageQueue.add(0, new Message(this.Xsclub[num10].replace("%s", this.name == null ? "You" : this.name), true));
            break;
         case 10:
            int num11 = this.r.nextInt(0, this.Dyyibang.length);
            if (num11 == this.lastNum) {
               num11 = num11 < this.Dyyibang.length - 1 ? num11 + 1 : 0;
            }

            this.lastNum = num11;
            this.messageQueue.add(0, new Message(this.Dyyibang[num11].replace("%s", this.name == null ? "You" : this.name), true));
            break;
         case 11:
            int num12 = this.r.nextInt(0, this.AlexJonnyMine.length);
            if (num12 == this.lastNum) {
               num12 = num12 < this.AlexJonnyMine.length - 1 ? num12 + 1 : 0;
            }

            this.lastNum = num12;
            this.messageQueue.add(0, new Message(this.AlexJonnyMine[num12].replace("%s", this.name == null ? "You" : this.name), true));
      }

   }

   private void sendPopMessage(String name) {
      if (!((List)this.popMessages.get()).isEmpty()) {
         int num = this.r.nextInt(0, ((List)this.popMessages.get()).size() - 1);
         if (num == this.lastPop) {
            num = num < ((List)this.popMessages.get()).size() - 1 ? num + 1 : 0;
         }

         this.lastPop = num;
         this.messageQueue.add(new Message(((String)((List)this.popMessages.get()).get(num)).replace("<NAME>", name), false));
      }

   }

   private static record Message(String message, boolean kill) {
   }

   public static enum MessageMode {
      Blackout,
      Exhibition,
      NoClue,
      Aurora,
      QuickMacro,
      ACE_joni,
      Xin,
      Test,
      Kry4tal,
      Xsclub,
      Dyyibang,
      AlexJonnyMine;

      // $FF: synthetic method
      private static MessageMode[] $values() {
         return new MessageMode[]{Blackout, Exhibition, NoClue, Aurora, QuickMacro, ACE_joni, Xin, Test, Kry4tal, Xsclub, Dyyibang, AlexJonnyMine};
      }
   }
}
