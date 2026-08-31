package espada.spacex.aurora.modules.renderplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;

public class KillEffects extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<Mode> mode;
   private final Setting<Boolean> playSound;
   private final Map<Entity, Long> renderEntities;
   private final Map<Entity, Long> lightingEntities;

   public KillEffects() {
      super(Aurora.RenderPlus, "Kill Effects", "Render some things where enemy died.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mode")).description(".")).defaultValue(KillEffects.Mode.LightningBolt)).build());
      this.playSound = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Play Sound")).description(".")).defaultValue(true)).visible(() -> !((Mode)this.mode.get()).equals(KillEffects.Mode.FallingLava))).build());
      this.renderEntities = new ConcurrentHashMap();
      this.lightingEntities = new ConcurrentHashMap();
   }

   @EventHandler
   public void onRender(Render3DEvent event) {
      switch (((Mode)this.mode.get()).ordinal()) {
         case 0 -> this.renderEntities.keySet().forEach((entity) -> {
   for(int i = 0; (float)i < entity.getHeight() * 10.0F; ++i) {
      for(int j = 0; (float)j < entity.getWidth() * 10.0F; ++j) {
         for(int k = 0; (float)k < entity.getWidth() * 10.0F; ++k) {
            this.mc.world.addParticle(ParticleTypes.FALLING_LAVA, entity.getX() + (double)j * 0.1, entity.getY() + (double)i * 0.1, entity.getZ() + (double)k * 0.1, (double)0.0F, (double)0.0F, (double)0.0F);
         }
      }
   }

   this.renderEntities.remove(entity);
});
         case 1 -> this.renderEntities.forEach((entity, time) -> {
   LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, this.mc.world);
   lightningEntity.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());
   EntitySpawnS2CPacket pac = new EntitySpawnS2CPacket(lightningEntity, 0, lightningEntity.getBlockPos());
   pac.apply(this.mc.getNetworkHandler());
   if ((Boolean)this.playSound.get()) {
      this.mc.world.playSound(this.mc.player, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 10000.0F, 0.16000001F);
      this.mc.world.playSound(this.mc.player, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER, 2.0F, 0.1F);
   }

   this.renderEntities.remove(entity);
   this.lightingEntities.put(entity, System.currentTimeMillis());
});
      }

   }

   @EventHandler
   public void onTick(TickEvent.Pre event) {
      this.mc.world.getEntities().forEach((entity) -> {
         if (entity instanceof PlayerEntity) {
            if (entity != this.mc.player && !this.renderEntities.containsKey(entity) && !this.lightingEntities.containsKey(entity)) {
               if (!entity.isAlive() && ((PlayerEntity)entity).getHealth() == 0.0F) {
                  this.renderEntities.put(entity, System.currentTimeMillis());
               }
            }
         }
      });
      if (!this.lightingEntities.isEmpty()) {
         this.lightingEntities.forEach((entity, time) -> {
            if (System.currentTimeMillis() - time > 0L) {
               this.lightingEntities.remove(entity);
            }

         });
      }

   }

   public static enum Mode {
      FallingLava,
      LightningBolt;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{FallingLava, LightningBolt};
      }
   }
}
