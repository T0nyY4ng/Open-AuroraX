package espada.spacex.aurora.utils.meteor;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.EntityTrackingSectionAccessor;
import meteordevelopment.meteorclient.mixin.SectionedEntityCacheAccessor;
import meteordevelopment.meteorclient.mixin.SimpleEntityLookupAccessor;
import meteordevelopment.meteorclient.mixin.WorldAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.entity.SimpleEntityLookup;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class BOEntityUtils {
   public static boolean intersectsWithEntity(Box box, Predicate<Entity> predicate, Map<AbstractClientPlayerEntity, Box> customBoxes) {
      EntityLookup<Entity> entityLookup = ((WorldAccessor)MeteorClient.mc.world).getEntityLookup();
      if (!(entityLookup instanceof SimpleEntityLookup<Entity> simpleEntityLookup)) {
         AtomicBoolean found = new AtomicBoolean(false);
         entityLookup.forEachIntersects(box, (entityx) -> {
            if (!found.get() && predicate.test(entityx)) {
               found.set(true);
            }

         });
         return found.get();
      } else {
         SectionedEntityCache<Entity> cache = ((SimpleEntityLookupAccessor)simpleEntityLookup).getCache();
         LongSortedSet trackedPositions = ((SectionedEntityCacheAccessor)cache).getTrackedPositions();
         Long2ObjectMap<EntityTrackingSection<Entity>> trackingSections = ((SectionedEntityCacheAccessor)cache).getTrackingSections();
         int i = ChunkSectionPos.getSectionCoord(box.minX - (double)2.0F);
         int j = ChunkSectionPos.getSectionCoord(box.minY - (double)2.0F);
         int k = ChunkSectionPos.getSectionCoord(box.minZ - (double)2.0F);
         int l = ChunkSectionPos.getSectionCoord(box.maxX + (double)2.0F);
         int m = ChunkSectionPos.getSectionCoord(box.maxY + (double)2.0F);
         int n = ChunkSectionPos.getSectionCoord(box.maxZ + (double)2.0F);

         for(int o = i; o <= l; ++o) {
            long p = ChunkSectionPos.asLong(o, 0, 0);
            long q = ChunkSectionPos.asLong(o, -1, -1);
            LongBidirectionalIterator longIterator = trackedPositions.subSet(p, q + 1L).iterator();

            while(longIterator.hasNext()) {
               long r = longIterator.nextLong();
               int s = ChunkSectionPos.unpackY(r);
               int t = ChunkSectionPos.unpackZ(r);
               if (s >= j && s <= m && t >= k && t <= n) {
                  EntityTrackingSection<Entity> entityTrackingSection = (EntityTrackingSection)trackingSections.get(r);
                  if (entityTrackingSection != null && entityTrackingSection.getStatus().shouldTrack()) {
                     for(Object rawEntity : ((EntityTrackingSectionAccessor)entityTrackingSection).getCollection()) {
                        Entity entity = (Entity)rawEntity;
                        if ((entity instanceof PlayerEntity && customBoxes.containsKey(entity) ? (Box)customBoxes.get(entity) : entity.getBoundingBox()).intersects(box) && predicate.test(entity)) {
                           return true;
                        }
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   public static boolean isBurrowed(PlayerEntity entity, boolean Echest) {
      return doesBoxTouchBlock(entity.getBoundingBox(), Echest);
   }

   public static boolean isBlockLag(PlayerEntity entity) {
      return doesBoxTouchBlock(entity.getBoundingBox(), Blocks.OBSIDIAN);
   }

   public static boolean doesBoxTouchBlock(Box box, boolean Echest) {
      for(int x = (int)Math.floor(box.minX); (double)x < Math.ceil(box.maxX); ++x) {
         for(int y = (int)Math.floor(box.minY); (double)y < Math.ceil(box.maxY); ++y) {
            for(int z = (int)Math.floor(box.minZ); (double)z < Math.ceil(box.maxZ); ++z) {
               if (MeteorClient.mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.OBSIDIAN || MeteorClient.mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.BEDROCK || MeteorClient.mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.ENDER_CHEST && Echest || MeteorClient.mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.RESPAWN_ANCHOR) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static boolean doesBoxTouchBlock(Box box, Block block) {
      for(int x = (int)Math.floor(box.minX); (double)x < Math.ceil(box.maxX); ++x) {
         for(int y = (int)Math.floor(box.minY); (double)y < Math.ceil(box.maxY); ++y) {
            for(int z = (int)Math.floor(box.minZ); (double)z < Math.ceil(box.maxZ); ++z) {
               if (MeteorClient.mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == block) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static boolean isWebbed(PlayerEntity entity) {
      return doesBoxTouchBlock(entity.getBoundingBox(), Blocks.COBWEB);
   }

   public static boolean isAnchor(PlayerEntity entity) {
      return doesBoxTouchBlock(entity.getBoundingBox(), Blocks.RESPAWN_ANCHOR);
   }

   public static Direction rayTraceCheck(BlockPos pos, boolean forceReturn) {
      Vec3d eyesPos = new Vec3d(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY() + (double)MeteorClient.mc.player.getEyeHeight(MeteorClient.mc.player.getPose()), MeteorClient.mc.player.getZ());

      for(Direction direction : Direction.values()) {
         RaycastContext raycastContext = new RaycastContext(eyesPos, new Vec3d((double)pos.getX() + (double)0.5F + (double)direction.getVector().getX() * (double)0.5F, (double)pos.getY() + (double)0.5F + (double)direction.getVector().getY() * (double)0.5F, (double)pos.getZ() + (double)0.5F + (double)direction.getVector().getZ() * (double)0.5F), ShapeType.COLLIDER, FluidHandling.NONE, MeteorClient.mc.player);
         BlockHitResult result = MeteorClient.mc.world.raycast(raycastContext);
         if (result != null && result.getType() == Type.BLOCK && result.getBlockPos().equals(pos)) {
            return direction;
         }
      }

      if (forceReturn) {
         if ((double)pos.getY() > eyesPos.y) {
            return Direction.DOWN;
         } else {
            return Direction.UP;
         }
      } else {
         return null;
      }
   }
}
