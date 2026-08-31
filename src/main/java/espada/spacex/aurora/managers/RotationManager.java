package espada.spacex.aurora.managers;

import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.events.PreRotationEvent;
import espada.spacex.aurora.modules.globalsettings.RotationSettings;
import espada.spacex.aurora.modules.movementplus.MoveFix;
import espada.spacex.aurora.utils.NCPRaytracer;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.RotationUtils;
import espada.spacex.aurora.utils.SettingUtils;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

public class RotationManager {
   private Target target = null;
   private double timer = (double)0.0F;
   public final float[] prevDir = new float[2];
   public final float[] currentDir = new float[2];
   public final float[] lastDir = new float[2];
   private double priority = (double)1000.0F;
   private RotationSettings settings = null;
   private boolean unsent = false;
   public Target lastTarget = null;
   public static final List<Rotation> history = new ArrayList();
   boolean shouldRotate = false;
   private float[] next;
   private boolean rotated = false;
   private long key = 0L;
   private Vec3d eyePos = new Vec3d((double)0.0F, (double)0.0F, (double)0.0F);

   public RotationManager() {
      MeteorClient.EVENT_BUS.subscribe(this);
   }

   @EventHandler(
      priority = 200
   )
   private void onTick(TickEvent.Pre event) {
      this.prevDir[0] = this.currentDir[0];
      this.prevDir[1] = this.currentDir[1];
      this.currentDir[0] = this.lastDir[0];
      this.currentDir[1] = this.lastDir[1];
   }

   @EventHandler(
      priority = 200
   )
   private void onMovePre(SendMovementPacketsEvent.Pre event) {
      this.unsent = true;
   }

   @EventHandler(
      priority = 200
   )
   private void onMovePost(SendMovementPacketsEvent.Post event) {
      if (this.unsent) {
         this.onPreRotate();
         if (this.updateShouldRotate()) {
            this.setEyePos(MeteorClient.mc.player.getPos());
            this.updateNextRotation();
            if (this.rotated) {
               MeteorClient.mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(this.next[0], this.next[1], Managers.ON_GROUND.isOnGround()));
            }
         }
      }

   }

   @EventHandler(
      priority = 200
   )
   private void onRender(Render3DEvent event) {
      if (MeteorClient.mc.player != null) {
         if (this.settings == null) {
            this.settings = (RotationSettings)Modules.get().get(RotationSettings.class);
         }

         this.timer -= event.frameTime;
         if (this.timer > (double)0.0F && this.target != null && this.lastDir != null) {
            if (SettingUtils.shouldVanillaRotate()) {
               MeteorClient.mc.player.setYaw(MathHelper.lerpAngleDegrees(MeteorClient.mc.getRenderTickCounter().getTickDelta(true), this.prevDir[0], this.currentDir[0]));
               MeteorClient.mc.player.setPitch(MathHelper.lerp(MeteorClient.mc.getRenderTickCounter().getTickDelta(true), this.prevDir[1], this.currentDir[1]));
            }
         } else if (this.target != null) {
            this.target = null;
            this.priority = (double)1000.0F;
         } else {
            this.priority = (double)1000.0F;
         }

      }
   }

   public PlayerMoveC2SPacket onFull(PlayerMoveC2SPacket.Full packet) {
      this.unsent = false;
      this.onPreRotate();
      if (!this.updateShouldRotate()) {
         return packet;
      } else {
         this.setEyePos(new Vec3d(packet.getX((double)0.0F), packet.getY((double)0.0F), packet.getZ((double)0.0F)));
         this.updateNextRotation();
         return (PlayerMoveC2SPacket)(this.rotated ? new PlayerMoveC2SPacket.Full(packet.getX((double)0.0F), packet.getY((double)0.0F), packet.getZ((double)0.0F), this.next[0], this.next[1], packet.isOnGround()) : new PlayerMoveC2SPacket.PositionAndOnGround(packet.getX((double)0.0F), packet.getY((double)0.0F), packet.getZ((double)0.0F), packet.isOnGround()));
      }
   }

   public PlayerMoveC2SPacket onPositionOnGround(PlayerMoveC2SPacket.PositionAndOnGround packet) {
      this.unsent = false;
      this.onPreRotate();
      if (!this.updateShouldRotate()) {
         return packet;
      } else {
         this.setEyePos(new Vec3d(packet.getX((double)0.0F), packet.getY((double)0.0F), packet.getZ((double)0.0F)));
         this.updateNextRotation();
         return (PlayerMoveC2SPacket)(this.rotated ? new PlayerMoveC2SPacket.Full(packet.getX((double)0.0F), packet.getY((double)0.0F), packet.getZ((double)0.0F), this.next[0], this.next[1], packet.isOnGround()) : packet);
      }
   }

   public PlayerMoveC2SPacket onLookAndOnGround(PlayerMoveC2SPacket.LookAndOnGround packet) {
      this.unsent = false;
      this.onPreRotate();
      if (!this.updateShouldRotate()) {
         return packet;
      } else {
         this.setEyePos(MeteorClient.mc.player.getPos());
         this.updateNextRotation();
         if (this.rotated) {
            return new PlayerMoveC2SPacket.LookAndOnGround(this.next[0], this.next[1], packet.isOnGround());
         } else {
            return packet.isOnGround() != Managers.ON_GROUND.isOnGround() ? new PlayerMoveC2SPacket.OnGroundOnly(packet.isOnGround()) : null;
         }
      }
   }

   public PlayerMoveC2SPacket onOnlyOnground(PlayerMoveC2SPacket.OnGroundOnly packet) {
      this.unsent = false;
      this.onPreRotate();
      if (!this.updateShouldRotate()) {
         return packet;
      } else {
         this.setEyePos(MeteorClient.mc.player.getPos());
         this.updateNextRotation();
         return (PlayerMoveC2SPacket)(this.rotated ? new PlayerMoveC2SPacket.LookAndOnGround(this.next[0], this.next[1], packet.isOnGround()) : packet);
      }
   }

   private void onPreRotate() {
      MeteorClient.EVENT_BUS.post(PreRotationEvent.INSTANCE);
   }

   private boolean updateShouldRotate() {
      this.shouldRotate = this.target != null && this.timer > (double)0.0F;
      return this.shouldRotate;
   }

   private void updateNextRotation() {
      if (this.shouldRotate) {
         if (this.target instanceof BoxTarget) {
            ((BoxTarget)this.target).vec = this.getTargetPos();
            this.next = new float[]{RotationUtils.nextYaw((double)this.lastDir[0], RotationUtils.getYaw(this.eyePos, ((BoxTarget)this.target).vec), this.settings.yawStep(((BoxTarget)this.target).type)), RotationUtils.nextPitch((double)this.lastDir[1], RotationUtils.getPitch(this.eyePos, ((BoxTarget)this.target).vec), this.settings.pitchStep(((BoxTarget)this.target).type))};
         } else {
            this.next = new float[]{RotationUtils.nextYaw((double)this.lastDir[0], ((AngleTarget)this.target).yaw, this.settings.yawStep(((AngleTarget)this.target).type)), RotationUtils.nextPitch((double)this.lastDir[1], ((AngleTarget)this.target).pitch, this.settings.pitchStep(((AngleTarget)this.target).type))};
         }

         this.rotated = Math.abs(RotationUtils.yawAngle((double)this.next[0], (double)this.lastDir[0])) > (double)0.0F || Math.abs(this.next[1] - this.lastDir[1]) > 0.0F;
      }

   }

   @EventHandler(
      priority = 300
   )
   private void onSend(PacketEvent.Sent event) {
      Packet var3 = event.packet;
      if (var3 instanceof PlayerMoveC2SPacket packet) {
         if (packet.changesLook()) {
            this.lastDir[0] = packet.getYaw(0.0F);
            this.lastDir[1] = packet.getPitch(0.0F);
            this.addHistory((double)this.lastDir[0], (double)this.lastDir[1]);
         }
      }

   }

   public void end(long k) {
      if (k == this.key) {
         this.priority = (double)1000.0F;
      }

   }

   public void endYaw(double yaw, boolean reset) {
      if (this.target instanceof AngleTarget) {
         if (yaw == ((AngleTarget)this.target).yaw) {
            this.priority = (double)1000.0F;
            if (reset) {
               this.target = null;
            }
         }

      }
   }

   public void endPitch(double pitch, boolean reset) {
      if (this.target instanceof AngleTarget) {
         if (pitch == ((AngleTarget)this.target).pitch) {
            this.priority = (double)1000.0F;
            if (reset) {
               this.target = null;
            }
         }

      }
   }

   public boolean startYaw(double yaw, double p, RotationType type, long key) {
      return this.start(yaw, (double)this.lastDir[1], p, type, key);
   }

   public boolean startPitch(double pitch, double p, RotationType type, long key) {
      return this.start((double)this.lastDir[0], pitch, p, type, key);
   }

   public boolean start(double yaw, double pitch, double p, RotationType type, long key) {
      if (this.settings == null) {
         return false;
      } else {
         if (p <= this.priority) {
            this.key = key;
            this.priority = p;
            this.lastTarget = this.target;
            this.target = new AngleTarget(yaw, pitch, type);
            this.timer = this.settings.time(type);
         }

         return (double)this.lastDir[0] == yaw && (double)this.lastDir[1] == pitch;
      }
   }

   public boolean start(BlockPos pos, Box box, Vec3d vec, double p, RotationType type, long key) {
      if (this.settings == null) {
         return false;
      } else {
         boolean alreadyRotated = SettingUtils.rotationCheck(box, type);
         if (p < this.priority || p == this.priority && (!(this.target instanceof BoxTarget) || SettingUtils.rotationCheck(((BoxTarget)this.target).box, type))) {
            if (!alreadyRotated) {
               this.priority = p;
            }

            this.lastTarget = this.target;
            this.key = key;
            this.target = pos != null ? new BoxTarget(pos, vec != null ? vec : OLEPOSSUtils.getMiddle(box), p, type) : new BoxTarget(box, vec != null ? vec : OLEPOSSUtils.getMiddle(box), p, type);
            this.timer = this.settings.time(type);
         }

         return alreadyRotated;
      }
   }

   public boolean start(Box box, Vec3d vec, double p, RotationType type, long key) {
      return this.start((BlockPos)null, box, vec, p, type, key);
   }

   public boolean start(Box box, double p, RotationType type, long key) {
      return this.start(box, OLEPOSSUtils.getMiddle(box), p, type, key);
   }

   public boolean start(BlockPos pos, double p, RotationType type, long key) {
      return this.start(pos, Box.from(new BlockBox(pos)), pos.toCenterPos(), p, type, key);
   }

   public boolean start(BlockPos pos, Vec3d vec, double p, RotationType type, long key) {
      return this.start(pos, new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 1), (double)(pos.getZ() + 1)), vec, p, type, key);
   }

   private void setEyePos(Vec3d vec3d) {
      this.eyePos = vec3d.add((double)0.0F, (double)MeteorClient.mc.player.getEyeHeight(MeteorClient.mc.player.getPose()), (double)0.0F);
   }

   public void addHistory(double yaw, double pitch) {
      history.add(0, new Rotation(yaw, pitch, MeteorClient.mc.player.getEyePos()));

      for(int i = history.size(); i > 20; --i) {
         if (history.size() > i) {
            history.remove(i);
         }
      }

   }

   public void updateNext() {
      if (this.target == null) {
         MoveFix.shouldRotate = false;
         MoveFix.fixRotation = MeteorClient.mc.player.getYaw();
         MoveFix.fixPitch = MeteorClient.mc.player.getPitch();
      } else {
         MoveFix.shouldRotate = true;
         if (this.shouldRotate) {
            Target next = this.target;
            if (next instanceof BoxTarget) {
               BoxTarget target = (BoxTarget)next;
               target.vec = this.getTargetPos();
               float[] nextAngles = new float[]{RotationUtils.nextYaw((double)this.lastDir[0], RotationUtils.getYaw(this.eyePos, target.vec), this.settings.yawStep(target.type)), RotationUtils.nextPitch((double)this.lastDir[1], RotationUtils.getPitch(this.eyePos, target.vec), this.settings.pitchStep(target.type))};
               MoveFix.fixRotation = nextAngles[0];
               MoveFix.fixPitch = nextAngles[1];
            } else {
               next = this.target;
               if (next instanceof AngleTarget) {
                  AngleTarget target = (AngleTarget)next;
                  float[] nextAngles = new float[]{RotationUtils.nextYaw((double)this.lastDir[0], target.yaw, this.settings.yawStep(target.type)), RotationUtils.nextPitch((double)this.lastDir[1], target.pitch, this.settings.pitchStep(target.type))};
                  MoveFix.fixRotation = nextAngles[0];
                  MoveFix.fixPitch = nextAngles[1];
               }
            }
         } else if (this.next != null) {
            MoveFix.fixRotation = this.next[0];
            MoveFix.fixPitch = this.next[1];
         } else {
            MoveFix.fixRotation = MeteorClient.mc.player.getYaw();
            MoveFix.fixPitch = MeteorClient.mc.player.getPitch();
         }

      }
   }

   public Vec3d getTargetPos() {
      BoxTarget t = (BoxTarget)this.target;
      if (this.settings.mode(t.type) == RotationSettings.RotationCheckMode.StrictRaytrace && !NCPRaytracer.raytrace(MeteorClient.mc.player.getEyePos(), t.targetVec, t.box)) {
         Vec3d eye = MeteorClient.mc.player.getEyePos();
         double cd = (double)1000000.0F;
         Vec3d closest = null;

         for(double x = (double)0.0F; x <= (double)1.0F; x += 0.1) {
            for(double y = (double)0.0F; y <= (double)1.0F; y += 0.1) {
               for(double z = (double)0.0F; z <= (double)1.0F; z += 0.1) {
                  Vec3d vec = new Vec3d(this.lerp(t.box.minX, t.box.maxX, x), this.lerp(t.box.minY, t.box.maxY, y), this.lerp(t.box.minZ, t.box.maxZ, z));
                  double d = t.targetVec.distanceTo(vec);
                  if (!(d > cd) && NCPRaytracer.raytrace(eye, vec, ((BoxTarget)this.target).box)) {
                     cd = d;
                     closest = vec;
                  }
               }
            }
         }

         return closest == null ? t.targetVec : closest;
      } else {
         return new Vec3d(MathHelper.clamp(t.targetVec.x + (Math.random() - (double)0.5F) * 0.05, t.box.minX, t.box.maxX), MathHelper.clamp(t.targetVec.y + (Math.random() - (double)0.5F) * 0.05, t.box.minY, t.box.maxY), MathHelper.clamp(t.targetVec.z + (Math.random() - (double)0.5F) * 0.05, t.box.minZ, t.box.maxZ));
      }
   }

   private double lerp(double from, double to, double delta) {
      return from + (to - from) * delta;
   }

   public void setHeadYaw(Args args) {
      if (this.shouldRotate) {
         args.set(1, this.prevDir[0]);
         args.set(2, this.currentDir[0]);
      }
   }

   public void setBodyYaw(Args args) {
      if (this.shouldRotate) {
         args.set(1, this.prevDir[0]);
         args.set(2, this.currentDir[0]);
      }
   }

   public void setPitch(Args args) {
      if (this.shouldRotate) {
         args.set(1, this.prevDir[1]);
         args.set(2, this.currentDir[1]);
      }
   }

   public static record Rotation(double yaw, double pitch, Vec3d vec) {
   }

   private static class Target {
   }

   private static class BoxTarget extends Target {
      public final BlockPos pos;
      public final Box box;
      public final Vec3d targetVec;
      public Vec3d vec;
      public final double priority;
      public final RotationType type;

      public BoxTarget(BlockPos pos, Vec3d vec, double priority, RotationType type) {
         this.pos = pos;
         this.box = new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 1), (double)(pos.getZ() + 1));
         this.vec = vec;
         this.targetVec = vec;
         this.priority = priority;
         this.type = type;
      }

      public BoxTarget(Box box, Vec3d vec, double priority, RotationType type) {
         this.pos = null;
         this.box = box;
         this.vec = vec;
         this.targetVec = vec;
         this.priority = priority;
         this.type = type;
      }
   }

   private static class AngleTarget extends Target {
      public final double yaw;
      public final double pitch;
      public final boolean ended;
      public final RotationType type;

      public AngleTarget(double yaw, double pitch, RotationType type) {
         this.yaw = yaw;
         this.pitch = pitch;
         this.ended = false;
         this.type = type;
      }
   }
}
