package espada.spacex.aurora.managers;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.util.math.BlockPos;

public class RenderManager {
   public static BlockPos lastPos = null;
   public static BlockPos lastPos2 = null;

   public void subscribe() {
      MeteorClient.EVENT_BUS.subscribe(this);
   }
}
