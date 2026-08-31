package espada.spacex.aurora.utils.meteor;

import espada.spacex.aurora.utils.SettingUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.mixininterface.IExplosion;
import meteordevelopment.meteorclient.mixininterface.IRaycastContext;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.GameMode;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.world.explosion.Explosion.DestructionType;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class BODamageUtils {
   private static final Vec3d vec3d = new Vec3d((double)0.0F, (double)0.0F, (double)0.0F);
   private static Explosion explosion;
   public static RaycastContext raycastContext;
   public static RaycastContext bedRaycast;

   @PreInit
   public static void init() {
      MeteorClient.EVENT_BUS.subscribe(BODamageUtils.class);
   }

   @EventHandler
   private static void onGameJoined(GameJoinedEvent event) {
      explosion = new Explosion(MeteorClient.mc.world, (Entity)null, (double)0.0F, (double)0.0F, (double)0.0F, 6.0F, false, DestructionType.DESTROY);
      raycastContext = new RaycastContext((Vec3d)null, (Vec3d)null, ShapeType.COLLIDER, FluidHandling.ANY, MeteorClient.mc.player);
      bedRaycast = new RaycastContext((Vec3d)null, (Vec3d)null, ShapeType.COLLIDER, FluidHandling.ANY, MeteorClient.mc.player);
   }

   public static double crystal(PlayerEntity player, Box bb, Vec3d crystal, BlockPos ignore, boolean ignoreTerrain) {
      return SettingUtils.oldDamage() ? (double)oldVerCrystal(player, bb, crystal, ignore, ignoreTerrain) : crystalDamage(player, bb, crystal, ignore, ignoreTerrain);
   }

   public static double crystalDamage(PlayerEntity player, Box bb, Vec3d crystal, BlockPos obsidianPos, boolean ignoreTerrain) {
      if (player == null) {
         return (double)0.0F;
      } else if (EntityUtils.getGameMode(player) == GameMode.CREATIVE && !(player instanceof FakePlayerEntity)) {
         return (double)0.0F;
      } else {
         ((IVec3d)vec3d).set((bb.minX + bb.maxX) / (double)2.0F, bb.minY, (bb.minZ + bb.maxZ) / (double)2.0F);
         double modDistance = Math.sqrt(vec3d.squaredDistanceTo(crystal));
         if (modDistance > (double)12.0F) {
            return (double)0.0F;
         } else {
            double exposure = getExposure(crystal, player, bb, raycastContext, obsidianPos, ignoreTerrain);
            double impact = ((double)1.0F - modDistance / (double)12.0F) * exposure;
            double damage = (impact * impact + impact) / (double)2.0F * (double)7.0F * (double)12.0F + (double)1.0F;
            damage = getDamageForDifficulty(damage);
            damage = (double)DamageUtil.getDamageLeft(player, (float)damage, MeteorClient.mc.world.getDamageSources().explosion(explosion), (float)player.getArmor(), (float)player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
            damage = resistanceReduction(player, damage);
            ((IExplosion)explosion).set(crystal, 6.0F, false);
            damage = blastProtReduction(player, damage, explosion);
            return damage < (double)0.0F ? (double)0.0F : damage;
         }
      }
   }

   public static float oldVerCrystal(PlayerEntity player, Box bb, Vec3d crystal, BlockPos ignore, boolean ignoreTerrain) {
      ((IVec3d)vec3d).set((bb.minX + bb.maxX) / (double)2.0F, bb.minY, (bb.minZ + bb.maxZ) / (double)2.0F);
      double dist = vec3d.distanceTo(crystal) / (double)12.0F;
      if (dist > (double)1.0F) {
         return 0.0F;
      } else {
         double exposure = getExposure(crystal, player, bb, raycastContext, ignore, ignoreTerrain);
         double d10 = ((double)1.0F - dist) * exposure;
         float damage = (float)((int)((d10 * d10 + d10) / (double)2.0F * (double)7.0F * (double)12.0F + (double)1.0F));
         damage = (float)getDamageForDifficulty((double)damage);
         damage = getDamageAfterAbsorb(damage, (float)player.getArmor(), (float)player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
         damage = oldVerPotionReduce(player, damage);
         return damage;
      }
   }

   public static float getDamageAfterAbsorb(float damage, float totalArmor, float toughnessAttribute) {
      float f = 2.0F + toughnessAttribute / 4.0F;
      float f1 = MathHelper.clamp(totalArmor - damage / f, totalArmor * 0.2F, 20.0F);
      return damage * (1.0F - f1 / 25.0F);
   }

   private static float oldVerPotionReduce(LivingEntity livingEntity, float damage) {
      if (livingEntity.hasStatusEffect(StatusEffects.RESISTANCE)) {
         int i = (livingEntity.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
         int j = 25 - i;
         float f = damage * (float)j;
         damage = f / 25.0F;
      }

      int k = getEnchantmentModifierDamage(livingEntity.getArmorItems());
      if (k > 0) {
         damage = getDamageAfterMagicAbsorb(damage, (float)k);
      }

      return damage;
   }

   private static int getEnchantmentModifierDamage(Iterable<ItemStack> stacks) {
      int i = 0;

      for(ItemStack stack : stacks) {
         i += sus(stack);
      }

      return i;
   }

   private static int sus(ItemStack stack) {
      int r = 0;
      if (!stack.isEmpty()) {
         ItemEnchantmentsComponent enchants = stack.getEnchantments();

         for(Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchants.getEnchantmentEntries()) {
            RegistryEntry<Enchantment> enchantment = (RegistryEntry)entry.getKey();
            int level = entry.getIntValue();
            int k = level + 1;
            if (enchantment.matchesKey(Enchantments.BLAST_PROTECTION)) {
               r += k * 2;
            } else if (enchantment.matchesKey(Enchantments.PROTECTION)) {
               r += k;
            }
         }
      }

      return r;
   }

   private static float getDamageAfterMagicAbsorb(float damage, float enchantModifiers) {
      float f = MathHelper.clamp(enchantModifiers, 0.0F, 20.0F);
      return damage * (1.0F - f / 25.0F);
   }

   public static double getSwordDamage(ItemStack stack, PlayerEntity player, PlayerEntity target, boolean charged) {
      double damage = (double)0.0F;
      if (charged) {
         if (stack.getItem() == Items.NETHERITE_SWORD) {
            damage += (double)8.0F;
         } else if (stack.getItem() == Items.DIAMOND_SWORD) {
            damage += (double)7.0F;
         } else if (stack.getItem() == Items.GOLDEN_SWORD) {
            damage += (double)4.0F;
         } else if (stack.getItem() == Items.IRON_SWORD) {
            damage += (double)6.0F;
         } else if (stack.getItem() == Items.STONE_SWORD) {
            damage += (double)5.0F;
         } else if (stack.getItem() == Items.WOODEN_SWORD) {
            damage += (double)4.0F;
         }

         damage *= (double)1.5F;
      }

      ItemEnchantmentsComponent enchantments = stack.getEnchantments();
      int level = enchantments.getLevel((RegistryEntry)MeteorClient.mc.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.SHARPNESS).get());
      if (level > 0) {
         damage += (double)0.5F * (double)level + (double)0.5F;
      }

      if (player.getActiveStatusEffects().containsKey(StatusEffects.STRENGTH)) {
         int strength = ((StatusEffectInstance)Objects.requireNonNull(player.getStatusEffect(StatusEffects.STRENGTH))).getAmplifier() + 1;
         damage += (double)(3 * strength);
      }

      damage = resistanceReduction(target, damage);
      damage = (double)DamageUtil.getDamageLeft(target, (float)damage, MeteorClient.mc.world.getDamageSources().generic(), (float)target.getArmor(), (float)target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
      damage = normalProtReduction(target, damage);
      return damage < (double)0.0F ? (double)0.0F : damage;
   }

   public static double bedDamage(LivingEntity player, Box box, Vec3d bed, BlockPos ignore) {
      if (player instanceof PlayerEntity && ((PlayerEntity)player).getAbilities().creativeMode) {
         return (double)0.0F;
      } else {
         double modDistance = Math.sqrt(player.squaredDistanceTo(bed));
         if (modDistance > (double)10.0F) {
            return (double)0.0F;
         } else {
            double exposure = getExposure(bed, player, box, raycastContext, ignore, true);
            double impact = ((double)1.0F - modDistance / (double)10.0F) * exposure;
            double damage = (impact * impact + impact) / (double)2.0F * (double)7.0F * (double)10.0F + (double)1.0F;
            damage = getDamageForDifficulty(damage);
            damage = resistanceReduction(player, damage);
            damage = (double)DamageUtil.getDamageLeft(player, (float)damage, MeteorClient.mc.world.getDamageSources().explosion(explosion), (float)player.getArmor(), (float)player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
            ((IExplosion)explosion).set(bed, 5.0F, true);
            damage = blastProtReduction(player, damage, explosion);
            if (damage < (double)0.0F) {
               damage = (double)0.0F;
            }

            return damage;
         }
      }
   }

   private Vec3d calcPredict(Entity e, int ticks) {
      return ticks == 0 ? e.getPos() : new Vec3d(e.getX() + (e.getX() - e.lastRenderX) * (double)ticks, e.getY() + (e.getY() - e.lastRenderY) * (double)ticks, e.getZ() + (e.getZ() - e.lastRenderZ) * (double)ticks);
   }

   public static double anchorDamage(LivingEntity player, Box box, BlockPos anchor, Vec3d centerPos) {
      return bedDamage(player, box, anchor.toCenterPos(), anchor);
   }

   private static double getDamageForDifficulty(double damage) {
      double var10000;
      switch (MeteorClient.mc.world.getDifficulty()) {
         case EASY:
            var10000 = Math.min(damage / (double)2.0F + (double)1.0F, damage);
            break;
         case HARD:
         case PEACEFUL:
            var10000 = damage * (double)3.0F / (double)2.0F;
            break;
         default:
            var10000 = damage;
      }

      return var10000;
   }

   private static double normalProtReduction(Entity player, double damage) {
      int protLevel = 0;
      if (protLevel > 20) {
         protLevel = 20;
      }

      damage *= (double)1.0F - (double)protLevel / (double)25.0F;
      return damage < (double)0.0F ? (double)0.0F : damage;
   }

   private static double blastProtReduction(Entity player, double damage, Explosion explosion) {
      int protLevel = 0;
      if (protLevel > 20) {
         protLevel = 20;
      }

      damage *= (double)1.0F - (double)protLevel / (double)25.0F;
      return damage < (double)0.0F ? (double)0.0F : damage;
   }

   private static double resistanceReduction(LivingEntity player, double damage) {
      if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
         int lvl = player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1;
         damage *= (double)1.0F - (double)lvl * 0.2;
      }

      return damage < (double)0.0F ? (double)0.0F : damage;
   }

   public static double getExposure(Vec3d source, Entity entity, Box box, RaycastContext raycastContext, BlockPos ignore, boolean ignoreTerrain) {
      double d = (double)1.0F / ((box.maxX - box.minX) * (double)2.0F + (double)1.0F);
      double e = (double)1.0F / ((box.maxY - box.minY) * (double)2.0F + (double)1.0F);
      double f = (double)1.0F / ((box.maxZ - box.minZ) * (double)2.0F + (double)1.0F);
      double g = ((double)1.0F - Math.floor((double)1.0F / d) * d) / (double)2.0F;
      double h = ((double)1.0F - Math.floor((double)1.0F / f) * f) / (double)2.0F;
      if (!(d < (double)0.0F) && !(e < (double)0.0F) && !(f < (double)0.0F)) {
         int i = 0;
         int j = 0;

         for(double k = (double)0.0F; k <= (double)1.0F; k += d) {
            for(double l = (double)0.0F; l <= (double)1.0F; l += e) {
               for(double m = (double)0.0F; m <= (double)1.0F; m += f) {
                  double n = MathHelper.lerp(k, box.minX, box.maxX);
                  double o = MathHelper.lerp(l, box.minY, box.maxY);
                  double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                  ((IVec3d)vec3d).set(n + g, o, p + h);
                  ((IRaycastContext)raycastContext).set(vec3d, source, ShapeType.COLLIDER, FluidHandling.NONE, entity);
                  if (raycast(raycastContext, ignore, ignoreTerrain).getType() == Type.MISS) {
                     ++i;
                  }

                  ++j;
               }
            }
         }

         return (double)i / (double)j;
      } else {
         return (double)0.0F;
      }
   }

   public static BlockHitResult raycast(RaycastContext context) {
      return (BlockHitResult)BlockView.raycast(context.getStart(), context.getEnd(), context, (raycastContext, blockPos) -> {
         BlockState blockState = MeteorClient.mc.world.getBlockState(blockPos);
         Vec3d vec3d = raycastContext.getStart();
         Vec3d vec3d2 = raycastContext.getEnd();
         VoxelShape voxelShape = raycastContext.getBlockShape(blockState, MeteorClient.mc.world, blockPos);
         BlockHitResult blockHitResult = MeteorClient.mc.world.raycastBlock(vec3d, vec3d2, blockPos, voxelShape, blockState);
         VoxelShape voxelShape2 = VoxelShapes.empty();
         BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, blockPos);
         double d = blockHitResult == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult.getPos());
         double e = blockHitResult2 == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult2.getPos());
         return d <= e ? blockHitResult : blockHitResult2;
      }, (raycastContext) -> {
         Vec3d vec3d = raycastContext.getStart().subtract(raycastContext.getEnd());
         return BlockHitResult.createMissed(raycastContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(raycastContext.getEnd()));
      });
   }

   private static BlockHitResult raycast(RaycastContext context, BlockPos ignore, boolean ignoreTerrain) {
      return (BlockHitResult)BlockView.raycast(context.getStart(), context.getEnd(), context, (raycastContext, blockPos) -> {
         BlockState blockState;
         if (blockPos.equals(ignore)) {
            blockState = Blocks.AIR.getDefaultState();
         } else {
            blockState = MeteorClient.mc.world.getBlockState(blockPos);
            if (blockState.getBlock().getBlastResistance() < 600.0F && ignoreTerrain) {
               blockState = Blocks.AIR.getDefaultState();
            }
         }

         Vec3d vec3d = raycastContext.getStart();
         Vec3d vec3d2 = raycastContext.getEnd();
         VoxelShape voxelShape = raycastContext.getBlockShape(blockState, MeteorClient.mc.world, blockPos);
         BlockHitResult blockHitResult = MeteorClient.mc.world.raycastBlock(vec3d, vec3d2, blockPos, voxelShape, blockState);
         VoxelShape voxelShape2 = VoxelShapes.empty();
         BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, blockPos);
         double d = blockHitResult == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult.getPos());
         double e = blockHitResult2 == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult2.getPos());
         return d <= e ? blockHitResult : blockHitResult2;
      }, (raycastContext) -> {
         Vec3d vec3d = raycastContext.getStart().subtract(raycastContext.getEnd());
         return BlockHitResult.createMissed(raycastContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(raycastContext.getEnd()));
      });
   }
}
