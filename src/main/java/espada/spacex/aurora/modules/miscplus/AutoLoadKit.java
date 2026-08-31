package espada.spacex.aurora.modules.miscplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DeathScreen;

public class AutoLoadKit extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<String> kCommand;
   private final Setting<String> kName;
   private boolean lock;
   private int i;

   public AutoLoadKit() {
      super(Aurora.MiscPlus, "Auto Load Kit", "Automatically takes specified kit after joining server/respawn.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.kCommand = this.sgGeneral.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)(new StringSetting.Builder()).name("Kit Command")).description("Command to activate kit commands.")).defaultValue("/kit")).build());
      this.kName = this.sgGeneral.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)(new StringSetting.Builder()).name("Name Of Kit")).description("Name of kit that should be taken.")).defaultValue("2b2t")).build());
      this.lock = false;
      this.i = 40;
   }

   @EventHandler
   private void onOpenScreenEvent(OpenScreenEvent event) {
      if (event.screen instanceof DeathScreen) {
         this.lock = true;
         this.i = 40;
      }
   }

   @EventHandler
   private void onGameJoin(GameJoinedEvent event) {
      this.lock = true;
      this.i = 40;
   }

   @EventHandler
   private void onTick(TickEvent.Post event) {
      if (Utils.canUpdate()) {
         if (!(this.mc.currentScreen instanceof DeathScreen)) {
            if (this.lock) {
               --this.i;
            }

            if (this.lock && this.i <= 0) {
            }

            String var10000 = (String)this.kCommand.get();
            ChatUtils.sendPlayerMsg(var10000 + " " + (String)this.kName.get());
            this.lock = false;
            this.i = 40;
         }
      }
   }
}
