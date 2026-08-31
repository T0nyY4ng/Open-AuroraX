package espada.spacex.aurora.mixins;

import com.mojang.authlib.GameProfile;
import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.modules.miscplus.CapesModule;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.NameProtect;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Util;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlayerListEntry.class})
public abstract class PlayerListEntryMixin {
   @Unique
   private boolean loadedCapeTexture;
   @Unique
   private String name;
   @Unique
   private String cape;
   @Unique
   private Identifier customCapeTexture;

   @Inject(
      method = {"<init>(Lcom/mojang/authlib/GameProfile;Z)V"},
      at = {@At("TAIL")}
   )
   private void initHook(GameProfile profile, boolean secureChatEnforced, CallbackInfo ci) {
      this.getTexture(profile);
   }

   @Inject(
      method = {"getSkinTextures"},
      at = {@At("TAIL")},
      cancellable = true
   )
   private void getCapeTexture(CallbackInfoReturnable<SkinTextures> cir) {
      if (this.customCapeTexture != null) {
         SkinTextures prev = (SkinTextures)cir.getReturnValue();
         SkinTextures newTextures = new SkinTextures(prev.texture(), prev.textureUrl(), this.customCapeTexture, this.customCapeTexture, prev.model(), prev.secure());
         cir.setReturnValue(newTextures);
      }

   }

   @Unique
   private void getTexture(GameProfile profile) {
      if (!this.loadedCapeTexture) {
         this.loadedCapeTexture = true;
         Util.getMainWorkerExecutor().execute(() -> {
            try {
               if (CapesModule.capeed != null) {
                  this.name = MeteorClient.mc.getSession().getUsername();
                  this.cape = CapesModule.capeed;
                  if (Objects.equals(profile.getName(), this.name)) {
                     this.customCapeTexture = Identifier.of("meteorcapes", "textures/capes/" + this.cape + ".png");
                     return;
                  }
               }

               URL capesList = new URL("https://raw.githubusercontent.com/Gllody/nhcapes/refs/heads/main/capeBase.txt");
               BufferedReader in = new BufferedReader(new InputStreamReader(capesList.openStream()));

               String inputLine;
               while((inputLine = in.readLine()) != null) {
                  String colune = inputLine.trim();
                  this.name = colune.split(":")[0];
                  this.cape = colune.split(":")[1];
                  if (Objects.equals(profile.getName(), this.name)) {
                     this.customCapeTexture = Identifier.of("meteorcapes", "textures/capes/" + this.cape + ".png");
                     return;
                  }
               }
            } catch (Exception e) {
               Aurora.LOG.error("Failed to load cape texture for " + profile.getName(), e);
            }

         });
      }
   }

   @Shadow
   public abstract GameProfile getProfile();

   @Inject(
      method = {"getSkinTextures"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetTexture(CallbackInfoReturnable<SkinTextures> info) {
      if (this.getProfile().getName().equals(MinecraftClient.getInstance().getSession().getUsername()) && ((NameProtect)Modules.get().get(NameProtect.class)).skinProtect()) {
         info.setReturnValue(DefaultSkinHelper.getSkinTextures(this.getProfile()));
      }

   }
}
