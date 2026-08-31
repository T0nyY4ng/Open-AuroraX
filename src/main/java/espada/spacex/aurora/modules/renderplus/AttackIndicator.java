package espada.spacex.aurora.modules.renderplus;

import com.mojang.blaze3d.systems.RenderSystem;
import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.modules.combatplus.autocrystal.AutoCrystal;
import espada.spacex.aurora.utils.RenderUtils;
import java.util.Set;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.GameRenderer;

public class AttackIndicator extends Modules {
   private final SettingGroup sgRender;
   private final SettingGroup sgWhitelist;
   public final Setting<Double> lifetime;
   public final Setting<Boolean> renderOnCA;
   private final Setting<ListMode> listMode;
   private final Setting<Set<EntityType<?>>> whitelist;
   private final Setting<Set<EntityType<?>>> blacklist;
   public boolean shouldRender;
   private long lastAttackTime;

   public AttackIndicator() {
      super(Aurora.RenderPlus, "Attack Indicator", "attack animation.");
      this.sgRender = this.settings.createGroup("Render");
      this.sgWhitelist = this.settings.createGroup("Render Whitelist");
      this.lifetime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Live Time")).description("The lifetime of indicator in seconds.")).defaultValue((double)1.0F).min((double)0.0F).range((double)0.0F, (double)10.0F).build());
      this.renderOnCA = this.sgWhitelist.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Render On CA")).description(".")).defaultValue(true)).build());
      this.listMode = this.sgWhitelist.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("List Mode")).description("Selection mode.")).defaultValue(AttackIndicator.ListMode.Whitelist)).build());
      this.whitelist = this.sgWhitelist.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)(new EntityTypeListSetting.Builder()).name("Whitelist")).description("The entities you want to render.")).defaultValue(new EntityType[]{EntityType.END_CRYSTAL}).visible(() -> this.listMode.get() == AttackIndicator.ListMode.Whitelist)).build());
      this.blacklist = this.sgWhitelist.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)(new EntityTypeListSetting.Builder()).name("Blacklist")).description("The entities you don't want to render.")).visible(() -> this.listMode.get() == AttackIndicator.ListMode.Blacklist)).build());
      this.shouldRender = false;
      this.lastAttackTime = 0L;
   }

   @EventHandler
   private void onAttack(AttackEntityEvent event) {
      this.shouldRender = this.isRenderEntity(event.entity.getType());
      if (this.shouldRender) {
         this.lastAttackTime = System.currentTimeMillis();
      }

   }

   private boolean isRenderEntity(EntityType<?> type) {
      boolean var10000;
      switch (((ListMode)this.listMode.get()).ordinal()) {
         case 0 -> var10000 = ((Set)this.whitelist.get()).contains(type);
         case 1 -> var10000 = !((Set)this.blacklist.get()).contains(type);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public void render(DrawContext context, int width, int height) {
      AutoCrystal autoCrystal = (AutoCrystal)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AutoCrystal.class);
      if (this.shouldRender || autoCrystal.isActive() && (Boolean)this.renderOnCA.get() && autoCrystal.placePos != null && this.isRenderEntity(EntityType.END_CRYSTAL)) {
         long currentTime = System.currentTimeMillis();
         long startTime = 0L;
         if (this.shouldRender) {
            startTime = this.lastAttackTime;
         }

         if (autoCrystal.isActive() && autoCrystal.placePos != null) {
            startTime = autoCrystal.lastAttack;
         }

         long timeElapsed = currentTime - startTime;
         int alpha;
         if (timeElapsed < 1000L) {
            alpha = (int)((double)255.0F - (double)timeElapsed / ((Double)this.lifetime.get() * (double)1000.0F) * (double)255.0F);
         } else {
            alpha = 0;
         }

         alpha = MathHelper.clamp(alpha, 0, 255);
         if (alpha > 0) {
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float)alpha / 255.0F);
            RenderUtils.drawTexture(context, Identifier.of("spacex", "hitmarker.png"), (width - 15) / 2, (height - 15) / 2, 15, 15);
         }
      }

   }

   public static enum ListMode {
      Whitelist,
      Blacklist;

      // $FF: synthetic method
      private static ListMode[] $values() {
         return new ListMode[]{Whitelist, Blacklist};
      }
   }
}
