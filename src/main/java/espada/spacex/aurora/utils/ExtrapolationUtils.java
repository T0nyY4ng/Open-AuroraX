package espada.spacex.aurora.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class ExtrapolationUtils {
   private static Map<AbstractClientPlayerEntity, List<Vec3d>> motions = new HashMap();

   @PreInit
   public static void preInit() {
      MeteorClient.EVENT_BUS.subscribe(ExtrapolationUtils.class);
   }

   @EventHandler(
      priority = 1000000
   )
   private static void onTick(TickEvent.Post event) {
      if (MeteorClient.mc.player != null && MeteorClient.mc.world != null && !MeteorClient.mc.world.getPlayers().isEmpty()) {
         Map<AbstractClientPlayerEntity, List<Vec3d>> newMotions = new HashMap();

         for(AbstractClientPlayerEntity player : MeteorClient.mc.world.getPlayers()) {
            Vec3d vec = player.getPos().subtract(player.prevX, player.prevY, player.prevZ);
            if (!motions.containsKey(player)) {
               List<Vec3d> v = new ArrayList();
               v.add(vec);
               newMotions.put(player, v);
            } else {
               List<Vec3d> v = (List)motions.get(player);
               v.add(0, vec);
               if (v.size() > 20) {
                  v.subList(20, v.size()).clear();
               }

               newMotions.put(player, v);
            }
         }

         motions = newMotions;
      }
   }

   public static void extrapolateMap(Map<AbstractClientPlayerEntity, Box> old, EpicInterface<AbstractClientPlayerEntity, Integer> extrapolation, EpicInterface<AbstractClientPlayerEntity, Integer> smoothening) {
      old.clear();
      motions.forEach((player, m) -> {
         if (m != null) {
            old.put(player, extrapolate(player, m, (Integer)extrapolation.get(player), (Integer)smoothening.get(player)));
         }
      });
   }

   public static Box extrapolate(AbstractClientPlayerEntity player, int extrapolation, int smoothening) {
      List<Vec3d> m = (List)motions.get(player);
      return m == null ? null : extrapolate(player, m, extrapolation, smoothening);
   }

   public static Box extrapolate(AbstractClientPlayerEntity player, List<Vec3d> m, int extrapolation, int smoothening) {
      Vec3d motion = getMotion(m, smoothening);
      double x = motion.x;
      double y = motion.y;
      double z = motion.z;
      double stepHeight = 0.6;
      Box box = new Box(player.getX() - 0.3, player.getY(), player.getZ() - 0.3, player.getX() + 0.3, player.getY() + (player.getBoundingBox().maxY - player.getBoundingBox().minY), player.getZ() + 0.3);
      boolean onGround = inside(player, box.offset((double)0.0F, -0.04, (double)0.0F));

      for(int i = 0; i < extrapolation; ++i) {
         List<VoxelShape> list = MeteorClient.mc.world.getEntityCollisions(player, box.stretch(x, y, z));
         Vec3d movement = new Vec3d(x, y, z);
         Vec3d vec3d = movement.lengthSquared() == (double)0.0F ? movement : Entity.adjustMovementForCollisions(player, movement, box, MeteorClient.mc.world, list);
         boolean canStep = (onGround || y < (double)0.0F && vec3d.y != y) && (vec3d.x != x || vec3d.z != z);
         if (canStep) {
            Vec3d vec3d2 = Entity.adjustMovementForCollisions(player, new Vec3d(x, stepHeight, z), box, MeteorClient.mc.world, list);
            Vec3d vec3d3 = Entity.adjustMovementForCollisions(player, new Vec3d((double)0.0F, stepHeight, (double)0.0F), box.stretch(x, (double)0.0F, z), MeteorClient.mc.world, list);
            if (vec3d3.y < stepHeight) {
               Vec3d vec3d4 = Entity.adjustMovementForCollisions(player, new Vec3d(movement.x, (double)0.0F, movement.z), box.offset(vec3d3), MeteorClient.mc.world, list).add(vec3d3);
               if (vec3d4.horizontalLengthSquared() > vec3d2.horizontalLengthSquared()) {
                  vec3d2 = vec3d4;
               }
            }

            if (vec3d2.horizontalLengthSquared() > vec3d.horizontalLengthSquared()) {
               Vec3d vec = vec3d2.add(Entity.adjustMovementForCollisions(player, new Vec3d((double)0.0F, -vec3d2.y + movement.y, (double)0.0F), box.offset(vec3d2), MeteorClient.mc.world, list));
               box = box.offset(vec);
               onGround = true;
               continue;
            }
         }

         box = box.offset(vec3d);
         onGround = inside(player, box.offset((double)0.0F, -0.04, (double)0.0F));
         if (onGround) {
            y = (double)0.0F;
         }

         y = (y - 0.08) * 0.98;
      }

      return box;
   }

   private static boolean inside(PlayerEntity player, Box box) {
      return OLEPOSSUtils.inside(player, box);
   }

   private static Vec3d getMotion(List<Vec3d> vecs, int max) {
      Vec3d avg = new Vec3d((double)0.0F, (((Vec3d)vecs.get(0)).y - 0.08) * 0.98, (double)0.0F);
      int s = Math.min(vecs.size(), max);

      for(int i = 0; i < s; ++i) {
         avg = avg.add(((Vec3d)vecs.get(i)).x, (double)0.0F, ((Vec3d)vecs.get(i)).z);
      }

      return avg.multiply((double)(1.0F / (float)s), (double)1.0F, (double)(1.0F / (float)s));
   }
}
