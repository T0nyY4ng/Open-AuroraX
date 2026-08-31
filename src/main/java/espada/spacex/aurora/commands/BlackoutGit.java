package espada.spacex.aurora.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class BlackoutGit extends Command {
   public BlackoutGit() {
      super("blackoutinfo", "Gives the Blackout GitHub", new String[0]);
   }

   public void build(LiteralArgumentBuilder<CommandSource> builder) {
      builder.executes((context) -> {
         this.info("https://github.com/KassuK1/BlackOut", new Object[0]);
         return 1;
      });
   }
}
