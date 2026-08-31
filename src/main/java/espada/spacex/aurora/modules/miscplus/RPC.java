package espada.spacex.aurora.modules.miscplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.starscript.Script;

public class RPC extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<List<String>> l1;
   private final Setting<List<String>> l2;
   private final Setting<Integer> refreshDelay;
   private int ticks;
   private int index1;
   private int index2;
   private static final RichPresence presence = new RichPresence();

   public RPC() {
      super(Aurora.MiscPlus, "RPC", "Epic rpc.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.l1 = this.sgGeneral.add(((StringListSetting.Builder)((StringListSetting.Builder)(new StringListSetting.Builder()).name("Line 1")).description(".")).defaultValue(new String[]{"Playing on {server}", "{player}"}).renderer(StarscriptTextBoxRenderer.class).build());
      this.l2 = this.sgGeneral.add(((StringListSetting.Builder)((StringListSetting.Builder)(new StringListSetting.Builder()).name("Line 2")).description(".")).defaultValue(new String[]{"{server.player_count} Players online", "{round(player.health, 1)}hp"}).renderer(StarscriptTextBoxRenderer.class).build());
      this.refreshDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Refresh Delay")).description("Ticks between refreshing.")).defaultValue(100)).range(0, 1000).sliderRange(0, 1000).build());
      this.ticks = 0;
      this.index1 = 0;
      this.index2 = 0;
   }

   public void onActivate() {
      DiscordIPC.start(1038168991258136576L, (Runnable)null);
      presence.setStart(System.currentTimeMillis() / 1000L);
      this.updatePresence();
   }

   public void onDeactivate() {
      DiscordIPC.stop();
   }

   @EventHandler(
      priority = 200
   )
   public void onTick(TickEvent.Pre event) {
      if (this.ticks > 0) {
         --this.ticks;
      } else {
         this.updatePresence();
      }

   }

   public void updatePresence() {
      this.ticks = (Integer)this.refreshDelay.get();
      List<String> messages1 = this.getMessages((List)this.l1.get());
      List<String> messages2 = this.getMessages((List)this.l2.get());
      this.index1 = this.index1 < messages1.size() - 1 ? this.index1 + 1 : 0;
      this.index2 = this.index2 < messages2.size() - 1 ? this.index2 + 1 : 0;
      presence.setDetails(this.mc.player == null ? "In Main Menu" : (String)messages1.get(this.index1));
      presence.setState(this.mc.player == null ? "In Main Menu" : (String)messages2.get(this.index2));
      presence.setLargeImage("logo1", "v.X");
      DiscordIPC.setActivity(presence);
   }

   private List<String> getMessages(List<String> stateList) {
      List<String> messages = new ArrayList();

      for(String msg : stateList) {
         Script script = MeteorStarscript.compile(msg);
         if (script != null) {
            messages.add(MeteorStarscript.run(script));
         }
      }

      return messages;
   }
}
