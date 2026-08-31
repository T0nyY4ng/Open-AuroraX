package espada.spacex.aurora.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class Coords extends Command {
   public Coords() {
      super("coords", "Copies your coordinates to your clipboard.", new String[0]);
   }

   public void build(LiteralArgumentBuilder<CommandSource> builder) {
      builder.executes((context) -> {
         if (mc.player != null) {
            double var10000 = Math.floor(mc.player.getX());
            String text = "x: " + var10000 + "; y:" + Math.floor(mc.player.getY()) + "; z:" + Math.floor(mc.player.getZ()) + ";";
            this.info("Succesfully copied your coordinates: \n" + text, new Object[0]);
            mc.keyboard.setClipboard(text);
         }

         return 1;
      });
   }
}
