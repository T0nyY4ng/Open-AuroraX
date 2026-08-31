package espada.spacex.aurora.modules.movementplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.entity.EntityPose;

public class PacketFlyPlus extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgFly;
   private final SettingGroup sgPhase;
   private final Setting<Boolean> onGroundSpoof;
   private final Setting<Boolean> onGround;
   private final Setting<BoundsMode> boundsMode;
   private final Setting<Boolean> setXZ;
   private final Setting<Integer> xzBound;
   private final Setting<Boolean> setY;
   private final Setting<Integer> yBound;
   private final Setting<Boolean> strictVertical;
   private final Setting<Boolean> syncPacket;
   private final Setting<Boolean> noClip;
   private final Setting<Boolean> resync;
   private final Setting<Double> packets;
   private final Setting<Double> flySpeed;
   private final Setting<Boolean> fastVertical;
   private final Setting<Double> downSpeed;
   private final Setting<Double> upSpeed;
   private final Setting<Boolean> flyEffects;
   private final Setting<Boolean> flyRotate;
   private final Setting<Boolean> stillFlyRotate;
   private final Setting<Boolean> antiKick;
   private final Setting<Double> antiKickAmount;
   private final Setting<Integer> antiKickDelay;
   private final Setting<Double> phasePackets;
   private final Setting<Double> phaseSpeed;
   private final Setting<Boolean> phaseFastVertical;
   private final Setting<Double> phaseDownSpeed;
   private final Setting<Double> phaseUpSpeed;
   private final Setting<Boolean> phaseEffects;
   private final Setting<Boolean> phaseRotate;
   private final Setting<Boolean> stillPhaseRotate;
   private final Setting<Boolean> sneakPhase;
   private int ticks;
   private int sent;
   private int rur;
   private double packetsToSend;
   private final Random random;
   private String info;
   private final List<PlayerMoveC2SPacket> validPackets;
   private Vec3d offset;
   private boolean moving;
   private boolean sneaked;
   private int id;
   private final Map<Integer, Vec3d> validPos;

   public PacketFlyPlus() {
      super(Aurora.MovementPlus, "Packet Fly Plus", "Flies using packets with enhanced features.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgFly = this.settings.createGroup("Fly");
      this.sgPhase = this.settings.createGroup("Phase");
      this.onGroundSpoof = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("On Ground Spoof")).description("Spoofs on ground.")).defaultValue(false)).build());
      SettingGroup var10001 = this.sgGeneral;
      BoolSetting.Builder var10002 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("On Ground")).description("Should we tell the server that you are on ground.")).defaultValue(false);
      Setting<Boolean> var10003 = this.onGroundSpoof;
      Objects.requireNonNull(var10003);
      this.onGround = var10001.add(((BoolSetting.Builder)var10002.visible(var10003::get)).build());
      this.boundsMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Bounds Mode")).description("How to apply bounds offset.")).defaultValue(PacketFlyPlus.BoundsMode.Add)).build());
      this.setXZ = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Set XZ")).description("Sets XZ coordinates instead of adding offset.")).defaultValue(false)).visible(() -> this.boundsMode.get() == PacketFlyPlus.BoundsMode.Set)).build());
      this.xzBound = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("XZ Bound")).description("Bounds offset horizontally.")).defaultValue(0)).sliderRange(-1337, 1337).visible(() -> this.boundsMode.get() == PacketFlyPlus.BoundsMode.Add || (Boolean)this.setXZ.get())).build());
      this.setY = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Set Y")).description("Sets Y coordinate instead of adding offset.")).defaultValue(true)).visible(() -> this.boundsMode.get() == PacketFlyPlus.BoundsMode.Set)).build());
      this.yBound = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Y Bound")).description("Bounds offset vertically.")).defaultValue(-87)).sliderRange(-1337, 1337).visible(() -> this.boundsMode.get() == PacketFlyPlus.BoundsMode.Add || (Boolean)this.setY.get())).build());
      this.strictVertical = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Strict Vertical")).description("Doesn't move horizontally and vertically in the same packet.")).defaultValue(false)).build());
      this.syncPacket = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Sync Packet")).description("Sends a synchronized position packet.")).defaultValue(false)).build());
      this.noClip = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("No Clip")).description("Enables no-clip mode for phasing.")).defaultValue(true)).build());
      this.resync = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Resync")).description("Resynchronizes position on disable.")).defaultValue(true)).build());
      this.packets = this.sgFly.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fly Packets")).description("How many packets to send every movement tick.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.flySpeed = this.sgFly.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fly Speed")).description("Distance to travel each packet.")).defaultValue(0.2873).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).build());
      this.fastVertical = this.sgFly.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Fast Vertical Fly")).description("Sends multiple packets every movement tick while going up.")).defaultValue(false)).build());
      this.downSpeed = this.sgFly.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fly Down Speed")).description("How fast to fly down.")).defaultValue(0.062).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.upSpeed = this.sgFly.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fly Up Speed")).description("How fast to fly up.")).defaultValue(0.062).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.flyEffects = this.sgFly.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Fly Effects")).description("Applies movement effects to fly speed.")).defaultValue(true)).build());
      this.flyRotate = this.sgFly.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Fly Rotate")).description("Allows rotating while flying.")).defaultValue(true)).build());
      var10001 = this.sgFly;
      var10002 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Still Fly Rotate")).description("Rotates even when not moving.")).defaultValue(true);
      var10003 = this.flyRotate;
      Objects.requireNonNull(var10003);
      this.stillFlyRotate = var10001.add(((BoolSetting.Builder)var10002.visible(var10003::get)).build());
      this.antiKick = this.sgFly.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Anti-Kick")).description("Slowly falls down to prevent server kicks.")).defaultValue(false)).build());
      var10001 = this.sgFly;
      DoubleSetting.Builder var6 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Anti-Kick Multiplier")).description("Fall speed multiplier for anti-kick (0.04 blocks * multiplier).")).defaultValue((double)1.0F).sliderRange((double)0.0F, (double)10.0F);
      var10003 = this.antiKick;
      Objects.requireNonNull(var10003);
      this.antiKickAmount = var10001.add(((DoubleSetting.Builder)var6.visible(var10003::get)).build());
      var10001 = this.sgFly;
      IntSetting.Builder var7 = ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Anti-Kick Delay")).description("Tick delay between anti-kick packets.")).defaultValue(10)).min(1).sliderRange(0, 100);
      var10003 = this.antiKick;
      Objects.requireNonNull(var10003);
      this.antiKickDelay = var10001.add(((IntSetting.Builder)var7.visible(var10003::get)).build());
      this.phasePackets = this.sgPhase.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Phase Packets")).description("How many packets to send every movement tick.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.phaseSpeed = this.sgPhase.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Phase Speed")).description("Distance to travel each packet.")).defaultValue(0.062).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).build());
      this.phaseFastVertical = this.sgPhase.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Fast Vertical Phase")).description("Sends multiple packets every movement tick while going up.")).defaultValue(false)).build());
      this.phaseDownSpeed = this.sgPhase.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Phase Down Speed")).description("How fast to phase down.")).defaultValue(0.062).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.phaseUpSpeed = this.sgPhase.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Phase Up Speed")).description("How fast to phase up.")).defaultValue(0.062).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.phaseEffects = this.sgPhase.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Phase Effects")).description("Applies movement effects to phase speed.")).defaultValue(false)).build());
      this.phaseRotate = this.sgPhase.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Phase Rotate")).description("Allows rotating while phasing.")).defaultValue(true)).build());
      var10001 = this.sgPhase;
      BoolSetting.Builder var8 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Still Phase Rotate")).description("Rotates even when not moving.")).defaultValue(true);
      var10003 = this.phaseRotate;
      Objects.requireNonNull(var10003);
      this.stillPhaseRotate = var10001.add(((BoolSetting.Builder)var8.visible(var10003::get)).build());
      this.sneakPhase = this.sgPhase.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Sneak Phase")).description("Automatically sneaks to phase through blocks.")).defaultValue(true)).build());
      this.ticks = 0;
      this.sent = 0;
      this.rur = 0;
      this.packetsToSend = (double)0.0F;
      this.random = new Random();
      this.info = null;
      this.validPackets = new ArrayList();
      this.offset = Vec3d.ZERO;
      this.moving = false;
      this.sneaked = false;
      this.id = -1;
      this.validPos = new HashMap();
   }

   public void onActivate() {
      super.onActivate();
      this.ticks = 0;
      this.validPos.clear();
      this.validPackets.clear();
   }

   public void onDeactivate() {
      if (this.mc.player != null && this.mc.world != null && (Boolean)this.resync.get()) {
         Vec3d pos = this.mc.player.getPos();
         this.sendPacket(new PlayerMoveC2SPacket.Full(pos.x, pos.y + (double)1.0F, pos.z, this.mc.player.getYaw() + 5.0F, this.mc.player.getPitch(), false));
      }

      this.validPos.clear();
      this.validPackets.clear();
   }

   @EventHandler
   private void onTick(TickEvent.Post e) {
      --this.ticks;
      ++this.rur;
      if (this.rur % 20 == 0) {
         this.info = "Packets: " + this.sent;
         this.sent = 0;
      }

   }

   @EventHandler
   private void onMove(PlayerMoveEvent e) {
      if (this.mc.player != null && this.mc.world != null) {
         boolean phasing = this.isPhasing();
         boolean semiPhasing = this.isSemiPhase();
         if ((Boolean)this.noClip.get()) {
            this.mc.player.noClip = true;
         }

         this.packetsToSend += this.packets(semiPhasing);
         boolean shouldAntiKick = (Boolean)this.antiKick.get() && this.ticks <= 0 && !phasing && !this.onGround();
         double yaw = this.getYaw();
         double motion = this.getSpeed(semiPhasing);
         double x = (double)0.0F;
         double y = (double)0.0F;
         double z = (double)0.0F;
         if (this.jumping()) {
            y = semiPhasing ? (Double)this.phaseUpSpeed.get() : (Double)this.upSpeed.get();
         } else if (this.sneaking()) {
            y = semiPhasing ? -(Double)this.phaseDownSpeed.get() : -(Double)this.downSpeed.get();
         }

         if (y != (double)0.0F && (Boolean)this.strictVertical.get()) {
            this.moving = false;
         }

         if (this.moving) {
            x = Math.cos(Math.toRadians(yaw + (double)90.0F)) * motion;
            z = Math.sin(Math.toRadians(yaw + (double)90.0F)) * motion;
         } else {
            if (semiPhasing && !(Boolean)this.phaseFastVertical.get()) {
               this.packetsToSend = Math.min(this.packetsToSend, (double)1.0F);
            }

            if (!semiPhasing && !(Boolean)this.fastVertical.get()) {
               this.packetsToSend = Math.min(this.packetsToSend, (double)1.0F);
            }
         }

         this.offset = new Vec3d((double)0.0F, (double)0.0F, (double)0.0F);
         boolean antiKickSent = false;

         while(this.packetsToSend >= (double)1.0F) {
            double yOffset;
            if (shouldAntiKick && y >= (double)0.0F && !antiKickSent) {
               this.ticks = (Integer)this.antiKickDelay.get();
               yOffset = (Double)this.antiKickAmount.get() * -0.04;
               antiKickSent = true;
            } else {
               yOffset = y;
            }

            this.offset = this.offset.add((Boolean)this.strictVertical.get() && yOffset != (double)0.0F ? (double)0.0F : x, yOffset, (Boolean)this.strictVertical.get() && yOffset != (double)0.0F ? (double)0.0F : z);
            this.send(this.offset.add(this.mc.player.getPos()), this.getBounds(), this.getOnGround(), semiPhasing);
            --this.packetsToSend;
            if (x == (double)0.0F && z == (double)0.0F && y == (double)0.0F) {
               break;
            }
         }

         this.doPhase();
         ((IVec3d)e.movement).set(this.offset.x, this.offset.y, this.offset.z);
         this.packetsToSend = Math.min(this.packetsToSend, (double)1.0F);
      }
   }

   @EventHandler
   private void onSend(PacketEvent.Send event) {
      if (event.packet instanceof PlayerMoveC2SPacket) {
         if (!this.validPackets.contains((PlayerMoveC2SPacket)event.packet)) {
            event.cancel();
         } else {
            ++this.sent;
         }
      } else {
         ++this.sent;
      }

   }

   @EventHandler
   private void onReceive(PacketEvent.Receive e) {
      Packet var3 = e.packet;
      if (var3 instanceof PlayerPositionLookS2CPacket packet) {
         Vec3d vec = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
         if (this.validPos.containsKey(packet.getTeleportId()) && ((Vec3d)this.validPos.get(packet.getTeleportId())).equals(vec)) {
            e.cancel();
            this.sendPacket(new TeleportConfirmC2SPacket(packet.getTeleportId()));
            this.validPos.remove(packet.getTeleportId());
            return;
         }

         this.id = packet.getTeleportId();
      }

   }

   private double getSpeed(boolean phasing) {
      double speed = phasing ? (Double)this.phaseSpeed.get() : (Double)this.flySpeed.get();
      boolean effects = phasing ? (Boolean)this.phaseEffects.get() : (Boolean)this.flyEffects.get();
      return effects ? speed * (double)1.0F : speed;
   }

   private void doPhase() {
      if ((Boolean)this.sneakPhase.get()) {
         if (!this.jumping()) {
            if (this.sneaked) {
               this.sneaked = false;
               this.endSneak();
            }
         } else {
            Box standBox = this.boxFor(EntityPose.STANDING, this.mc.player.getPos()).expand((double)0.0625F, (double)0.0625F, (double)0.0625F).offset((double)0.0F, this.offset.y * (double)2.0F, (double)0.0F);
            Box movedBox = this.boxFor(EntityPose.STANDING, this.mc.player.getPos()).expand((double)0.0625F, (double)0.0F, (double)0.0625F).offset((double)0.0F, this.offset.y * (double)3.0F, (double)0.0F);
            boolean standIn = this.in(standBox);
            boolean movedIn = this.in(movedBox);
            if (this.sneaking()) {
               if (standIn) {
                  this.endSneak();
               }
            } else if (movedIn) {
               this.startSneak();
            }
         }
      }

   }

   private Box boxFor(EntityPose pose, Vec3d vec3d) {
      return this.mc.player.getDimensions(pose).getBoxAt(vec3d);
   }

   private boolean in(Box box) {
      return OLEPOSSUtils.inside(this.mc.player, box);
   }

   private void startSneak() {
      this.sneaked = true;
      this.mc.player.setSneaking(true);
      this.mc.options.sneakKey.setPressed(true);
   }

   private void endSneak() {
      this.mc.player.setSneaking(false);
      this.mc.options.sneakKey.setPressed(false);
   }

   public String getInfoString() {
      return this.info;
   }

   private boolean onGround() {
      return this.mc.player.isOnGround() || (double)this.mc.player.getBlockY() - this.mc.player.getY() == (double)0.0F && OLEPOSSUtils.collidable(this.mc.player.getBlockPos().down());
   }

   private double packets(boolean semiPhasing) {
      return semiPhasing ? (Double)this.phasePackets.get() : (Double)this.packets.get();
   }

   private Vec3d getBounds() {
      double yaw = this.random.nextDouble((double)0.0F, (Math.PI * 2D));
      double x = (double)0.0F;
      double y = (double)0.0F;
      double z = (double)0.0F;
      switch (((BoundsMode)this.boundsMode.get()).ordinal()) {
         case 0:
            x = this.mc.player.getX() + Math.cos(yaw) * (double)(Integer)this.xzBound.get();
            y = this.mc.player.getY() + (double)(Integer)this.yBound.get();
            z = this.mc.player.getZ() + Math.sin(yaw) * (double)(Integer)this.xzBound.get();
            break;
         case 1:
            x = (Boolean)this.setXZ.get() ? Math.cos(yaw) * (double)(Integer)this.xzBound.get() : this.mc.player.getX();
            y = (Boolean)this.setY.get() ? (double)(Integer)this.yBound.get() : this.mc.player.getY();
            z = (Boolean)this.setXZ.get() ? Math.sin(yaw) * (double)(Integer)this.xzBound.get() : this.mc.player.getZ();
      }

      return new Vec3d(x, y, z);
   }

   private boolean getOnGround() {
      return (Boolean)this.onGroundSpoof.get() ? (Boolean)this.onGround.get() : this.mc.player.isOnGround();
   }

   private boolean isPhasing() {
      return OLEPOSSUtils.inside(this.mc.player, this.mc.player.getBoundingBox().expand((double)0.0625F, (double)0.0F, (double)0.0625F));
   }

   private boolean isSemiPhase() {
      return OLEPOSSUtils.inside(this.mc.player, this.mc.player.getBoundingBox().contract(0.01, (double)0.0F, 0.01));
   }

   private boolean jumping() {
      return this.mc.options.jumpKey.isPressed();
   }

   private boolean sneaking() {
      return this.mc.options.sneakKey.isPressed();
   }

   private void send(Vec3d pos, Vec3d bounds, boolean onGround, boolean phasing) {
      PlayerMoveC2SPacket normal = this.getPacket(pos, onGround, phasing);
      PlayerMoveC2SPacket.PositionAndOnGround bound = new PlayerMoveC2SPacket.PositionAndOnGround(bounds.x, bounds.y, bounds.z, onGround);
      this.validPackets.add(normal);
      this.sendPacket(normal);
      this.validPos.put(this.id + 1, pos);
      this.validPackets.add(bound);
      this.sendPacket(bound);
      ++this.id;
      if ((Boolean)this.syncPacket.get()) {
         this.sendPacket(new PlayerMoveC2SPacket.Full(pos.x, pos.y, pos.z, this.mc.player.getYaw(), this.mc.player.getPitch(), onGround));
      }

   }

   private PlayerMoveC2SPacket getPacket(Vec3d pos, boolean onGround, boolean phasing) {
      boolean rotate = phasing ? (Boolean)this.phaseRotate.get() : (Boolean)this.flyRotate.get();
      boolean stillRotate = phasing ? (Boolean)this.stillPhaseRotate.get() : (Boolean)this.stillFlyRotate.get();
      if (!this.shouldRotate(rotate, stillRotate)) {
         return new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y, pos.z, onGround);
      } else {
         float yaw = this.mc.player.getYaw();
         float pitch = this.mc.player.getPitch();
         return new PlayerMoveC2SPacket.Full(pos.x, pos.y, pos.z, yaw, pitch, onGround);
      }
   }

   private boolean shouldRotate(boolean rotate, boolean still) {
      return rotate && (!still || this.offset.length() < 0.01);
   }

   private double getYaw() {
      double f = (double)this.mc.player.input.movementForward;
      double s = (double)this.mc.player.input.movementSideways;
      double yaw = (double)this.mc.player.getYaw();
      if (f > (double)0.0F) {
         this.moving = true;
         yaw += s > (double)0.0F ? (double)-45.0F : (s < (double)0.0F ? (double)45.0F : (double)0.0F);
      } else if (f < (double)0.0F) {
         this.moving = true;
         yaw += s > (double)0.0F ? (double)-135.0F : (s < (double)0.0F ? (double)135.0F : (double)180.0F);
      } else {
         this.moving = s != (double)0.0F;
         yaw += s > (double)0.0F ? (double)-90.0F : (s < (double)0.0F ? (double)90.0F : (double)0.0F);
      }

      return yaw;
   }

   public static enum BoundsMode {
      Add,
      Set;

      // $FF: synthetic method
      private static BoundsMode[] $values() {
         return new BoundsMode[]{Add, Set};
      }
   }
}
