package espada.spacex.aurora.modules.playerplus;

import com.mojang.authlib.GameProfile;
import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.mixins.ILivingEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.combat.Criticals;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.Difficulty;
import net.minecraft.util.Hand;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.world.explosion.Explosion.DestructionType;
import org.jetbrains.annotations.NotNull;

public class NewFakePlayer extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<String> name;
   private final Setting<Boolean> copyInventory;
   private final Setting<Boolean> record;
   private final Setting<Boolean> play;
   private final Setting<Boolean> autoTotem;
   private int movementTick;
   private int deathTime;
   private long TICK_TIMER;
   public static OtherClientPlayerEntity fakePlayer;
   private final List<PlayerState> positions;

   public NewFakePlayer() {
      super(Aurora.PlayerPlus, "New FakePlayer", "Spawns a client-side fake player for testing usages. No need to be active.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.name = this.sgGeneral.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)(new StringSetting.Builder()).name("Name")).description("")).defaultValue("MaoJunQing")).build());
      this.copyInventory = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Copy Inventory")).description("")).defaultValue(false)).build());
      this.record = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Record")).description("")).defaultValue(false)).build());
      this.play = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Play")).description("")).defaultValue(false)).build());
      this.autoTotem = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Auto Totem")).description("")).defaultValue(false)).build());
      this.positions = new ArrayList();
   }

   public void onActivate() {
      this.TICK_TIMER = System.currentTimeMillis();
      fakePlayer = new OtherClientPlayerEntity(this.mc.world, new GameProfile(UUID.fromString("66123666-6666-6666-6666-666666666600"), (String)this.name.get()));
      fakePlayer.copyPositionAndRotation(this.mc.player);
      if ((Boolean)this.copyInventory.get()) {
         fakePlayer.setStackInHand(Hand.MAIN_HAND, this.mc.player.getMainHandStack().copy());
         fakePlayer.setStackInHand(Hand.OFF_HAND, this.mc.player.getOffHandStack().copy());
         fakePlayer.getInventory().setStack(36, this.mc.player.getInventory().getStack(36).copy());
         fakePlayer.getInventory().setStack(37, this.mc.player.getInventory().getStack(37).copy());
         fakePlayer.getInventory().setStack(38, this.mc.player.getInventory().getStack(38).copy());
         fakePlayer.getInventory().setStack(39, this.mc.player.getInventory().getStack(39).copy());
      }

      this.mc.world.addEntity(fakePlayer);
      fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 9999, 2));
      fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 9999, 4));
      fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 9999, 1));
   }

   @EventHandler
   public void onPacketReceive(PacketEvent.Receive event) {
      Packet var3 = event.packet;
      if (var3 instanceof ExplosionS2CPacket explosion) {
         if (fakePlayer != null && fakePlayer.hurtTime == 0) {
            fakePlayer.onDamaged(this.mc.world.getDamageSources().generic());
            fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - this.getExplosionDamage2(new Vec3d(explosion.getX(), explosion.getY(), explosion.getZ()), fakePlayer));
            if (fakePlayer.isDead()) {
               fakePlayer.setHealth(10.0F);
               (new EntityStatusS2CPacket(fakePlayer, (byte)35)).apply(this.mc.player.networkHandler);
            }
         }
      }

   }

   private PlayerEntity equipAndReturn(PlayerEntity original, Vec3d posVec) {
      PlayerEntity copyEntity = new PlayerEntity(this.mc.world, original.getBlockPos(), original.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {
         public boolean isSpectator() {
            return false;
         }

         public boolean isCreative() {
            return false;
         }
      };
      copyEntity.setPosition(posVec);
      copyEntity.setHealth(original.getHealth());
      copyEntity.prevX = original.prevX;
      copyEntity.prevZ = original.prevZ;
      copyEntity.prevY = original.prevY;
      copyEntity.getInventory().clone(original.getInventory());

      for(StatusEffectInstance se : original.getStatusEffects()) {
         copyEntity.addStatusEffect(se);
      }

      return copyEntity;
   }

   private PlayerEntity predictPlayer(PlayerEntity entity, int ticks) {
      Vec3d posVec = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
      double motionX = entity.getX() - entity.prevX;
      double motionY = entity.getY() - entity.prevY;
      double motionZ = entity.getZ() - entity.prevZ;

      for(int i = 0; i < ticks; ++i) {
         if (!this.mc.world.isAir(BlockPos.ofFloored(posVec.add((double)0.0F, motionY, (double)0.0F)))) {
            motionY = (double)0.0F;
         }

         if (!this.mc.world.isAir(BlockPos.ofFloored(posVec.add(motionX, (double)0.0F, (double)0.0F))) || !this.mc.world.isAir(BlockPos.ofFloored(posVec.add(motionX, (double)1.0F, (double)0.0F)))) {
            motionX = (double)0.0F;
         }

         if (!this.mc.world.isAir(BlockPos.ofFloored(posVec.add((double)0.0F, (double)0.0F, motionZ))) || !this.mc.world.isAir(BlockPos.ofFloored(posVec.add((double)0.0F, (double)1.0F, motionZ)))) {
            motionZ = (double)0.0F;
         }

         posVec = posVec.add(motionX, motionY, motionZ);
      }

      return this.equipAndReturn(entity, posVec);
   }

   private float getExplosionDamage2(Vec3d crysPos, PlayerEntity target) {
      try {
         return this.getExplosionDamageWPredict(crysPos, target, this.predictPlayer(target, 3));
      } catch (Exception var4) {
         return 0.0F;
      }
   }

   private float getExplosionDamageWPredict(Vec3d explosionPos, PlayerEntity target, PlayerEntity predict) {
      if (this.mc.world.getDifficulty() == Difficulty.PEACEFUL) {
         return 0.0F;
      } else {
         Explosion explosion = new Explosion(this.mc.world, (Entity)null, explosionPos.x, explosionPos.y, explosionPos.z, 6.0F, false, DestructionType.DESTROY);
         if (!(new Box((double)MathHelper.floor(explosionPos.x - (double)11.0F), (double)MathHelper.floor(explosionPos.y - (double)11.0F), (double)MathHelper.floor(explosionPos.z - (double)11.0F), (double)MathHelper.floor(explosionPos.x + (double)13.0F), (double)MathHelper.floor(explosionPos.y + (double)13.0F), (double)MathHelper.floor(explosionPos.z + (double)13.0F))).intersects(predict.getBoundingBox())) {
            return 0.0F;
         } else {
            if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
               double distExposure = (double)MathHelper.sqrt((float)predict.squaredDistanceTo(explosionPos)) / (double)12.0F;
               if (distExposure <= (double)1.0F) {
                  double xDiff = predict.getX() - explosionPos.x;
                  double yDiff = predict.getY() - explosionPos.y;
                  double zDiff = predict.getX() - explosionPos.z;
                  double diff = (double)MathHelper.sqrt((float)(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff));
                  if (diff != (double)0.0F) {
                     double exposure = (double)Explosion.getExposure(explosionPos, predict);
                     double finalExposure = ((double)1.0F - distExposure) * exposure;
                     float toDamage = (float)Math.floor((finalExposure * finalExposure + finalExposure) / (double)2.0F * (double)7.0F * (double)12.0F + (double)1.0F);
                     if (this.mc.world.getDifficulty() == Difficulty.EASY) {
                        toDamage = Math.min(toDamage / 2.0F + 1.0F, toDamage);
                     } else if (this.mc.world.getDifficulty() == Difficulty.HARD) {
                        toDamage = toDamage * 3.0F / 2.0F;
                     }

                     toDamage = DamageUtil.getDamageLeft(target, toDamage, this.mc.world.getDamageSources().explosion(explosion), (float)target.getArmor(), (float)((EntityAttributeInstance)Objects.requireNonNull(target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS))).getValue());
                     if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                        int resistance = 25 - (((StatusEffectInstance)Objects.requireNonNull(target.getStatusEffect(StatusEffects.RESISTANCE))).getAmplifier() + 1) * 5;
                        float resistance_1 = toDamage * (float)resistance;
                        toDamage = Math.max(resistance_1 / 25.0F, 0.0F);
                     }

                     if (toDamage <= 0.0F) {
                        toDamage = 0.0F;
                     } else {
                        int protAmount = 0;
                        if (protAmount > 0) {
                           toDamage = DamageUtil.getInflictedDamage(toDamage, (float)protAmount);
                        }
                     }

                     return toDamage;
                  }
               }
            }

            return 0.0F;
         }
      }
   }

   @EventHandler
   public void onTick(TickEvent.Post event) {
      if (Utils.canUpdate()) {
         if ((Boolean)this.record.get()) {
            this.positions.add(new PlayerState(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), this.mc.player.getYaw(), this.mc.player.getPitch()));
         } else {
            if (fakePlayer != null) {
               if ((Boolean)this.play.get() && !this.positions.isEmpty()) {
                  ++this.movementTick;
                  if (this.movementTick >= this.positions.size()) {
                     this.movementTick = 0;
                     return;
                  }

                  PlayerState p = (PlayerState)this.positions.get(this.movementTick);
                  fakePlayer.setYaw(p.yaw);
                  fakePlayer.setPitch(p.pitch);
                  fakePlayer.setHeadYaw(p.yaw);
                  fakePlayer.updateTrackedPosition(p.x, p.y, p.z);
                  fakePlayer.updateTrackedPositionAndAngles(p.x, p.y, p.z, p.yaw, p.pitch, 3);
               } else {
                  this.movementTick = 0;
               }

               if ((Boolean)this.autoTotem.get() && fakePlayer.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                  fakePlayer.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
               }

               if (fakePlayer.isDead()) {
                  ++this.deathTime;
                  if (this.deathTime > 10) {
                     this.toggle();
                  }
               }
            }

         }
      }
   }

   private float getAttackCooldownProgressPerTick() {
      return (float)((double)1.0F / this.mc.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * (double)20.0F * (double)this.TICK_TIMER);
   }

   private float getAttackCooldown() {
      return MathHelper.clamp(((float)((ILivingEntity)this.mc.player).getLastAttackedTicks() + 0.5F) / this.getAttackCooldownProgressPerTick(), 0.0F, 1.0F);
   }

   @EventHandler
   public void onAttack(AttackEntityEvent event) {
      if (fakePlayer != null && event.entity == fakePlayer && fakePlayer.hurtTime == 0) {
         this.mc.world.playSound(this.mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1.0F, 1.0F);
         if (this.mc.player.fallDistance > 0.0F || meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(Criticals.class)) {
            this.mc.world.playSound(this.mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.0F, 1.0F);
         }

         fakePlayer.onDamaged(this.mc.world.getDamageSources().generic());
         if ((double)this.getAttackCooldown() >= 0.85) {
            fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - this.getHitDamage(this.mc.player.getMainHandStack(), fakePlayer));
         } else {
            fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - 1.0F);
         }

         if (fakePlayer.isDead()) {
            fakePlayer.setHealth(10.0F);
            (new EntityStatusS2CPacket(fakePlayer, (byte)35)).apply(this.mc.player.networkHandler);
         }
      }

   }

   private float getHitDamage(@NotNull ItemStack weapon, PlayerEntity ent) {
      if (this.mc.player == null) {
         return 0.0F;
      } else {
         float baseDamage = 1.0F;
         Item var5 = weapon.getItem();
         if (var5 instanceof SwordItem) {
            SwordItem swordItem = (SwordItem)var5;
            baseDamage = 3.0F + swordItem.getMaterial().getAttackDamage();
         }

         var5 = weapon.getItem();
         if (var5 instanceof AxeItem) {
            AxeItem axeItem = (AxeItem)var5;
            baseDamage = 5.0F + axeItem.getMaterial().getAttackDamage();
         }

         if (this.mc.player.fallDistance > 0.0F || meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(Criticals.class)) {
            baseDamage += baseDamage / 2.0F;
         }

         baseDamage += (float)EnchantmentHelper.getLevel((RegistryEntry)this.mc.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.SHARPNESS).get(), weapon);
         if (this.mc.player.hasStatusEffect(StatusEffects.STRENGTH)) {
            int strength = ((StatusEffectInstance)Objects.requireNonNull(this.mc.player.getStatusEffect(StatusEffects.STRENGTH))).getAmplifier() + 1;
            baseDamage += (float)(3 * strength);
         }

         baseDamage = DamageUtil.getDamageLeft(ent, baseDamage, this.mc.world.getDamageSources().generic(), (float)ent.getArmor(), (float)ent.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
         return baseDamage;
      }
   }

   public void onDeactivate() {
      if (fakePlayer != null) {
         fakePlayer.kill();
         fakePlayer.setRemoved(RemovalReason.KILLED);
         fakePlayer.onRemoved();
         fakePlayer = null;
         this.positions.clear();
         this.deathTime = 0;
      }
   }

   private static record PlayerState(double x, double y, double z, float yaw, float pitch) {
   }
}
