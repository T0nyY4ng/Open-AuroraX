package espada.spacex.aurora.mixins;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.InputSupplier;
import net.minecraft.client.util.Icons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({Window.class})
public class MixinWindow {
   @Redirect(
      method = {"setIcon"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/util/Icons;getIcons(Lnet/minecraft/resource/ResourcePack;)Ljava/util/List;"
)
   )
   private List<InputSupplier<InputStream>> setupIcon(Icons instance, ResourcePack resourcePack) throws IOException {
      InputStream stream16 = MixinWindow.class.getResourceAsStream("/assets/spacex/icon_16x16.png");
      InputStream stream32 = MixinWindow.class.getResourceAsStream("/assets/spacex/icon_32x32.png");
      if (stream16 != null && stream32 != null) {
         return List.of((InputSupplier)() -> stream16, (InputSupplier)() -> stream32);
      } else {
         MeteorClient.LOG.error("Unable to find client icons.");
         return instance.getIcons(resourcePack);
      }
   }
}
