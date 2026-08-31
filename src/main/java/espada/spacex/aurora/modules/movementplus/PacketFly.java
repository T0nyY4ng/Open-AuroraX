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
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class PacketFly extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgFly;
   private final SettingGroup sgPhase;
   private final Setting<Boolean> onGroundSpoof;
   private final Setting<Boolean> onGround;
   private final Setting<Integer> xzBound;
   private final Setting<Integer> yBound;
   private final Setting<Boolean> strictVertical;
   private final Setting<Boolean> antiKick;
   private final Setting<Double> antiKickAmount;
   private final Setting<Integer> antiKickDelay;
   private final Setting<Boolean> predictID;
   private final Setting<Boolean> debugID;
   private final Setting<Double> packets;
   private final Setting<Double> speed;
   private final Setting<Boolean> fastVertical;
   private final Setting<Double> downSpeed;
   private final Setting<Double> upSpeed;
   private final Setting<Double> phasePackets;
   private final Setting<Double> phaseSpeed;
   private final Setting<Boolean> phaseFastVertical;
   private final Setting<Double> phaseDownSpeed;
   private final Setting<Double> phaseUpSpeed;
   private int ticks;
   private int id;
   private int sent;
   private int rur;
   private double packetsToSend;
   private final Random random;
   private String info;
   private final Map<Integer, Vec3d> validPos;
   private final List<PlayerMoveC2SPacket> validPackets;
   public boolean moving;

   public PacketFly() {
      super(Aurora.MovementPlus, "Packet Fly", "Flies with packets.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgFly = this.settings.createGroup("Fly");
      this.sgPhase = this.settings.createGroup("Phase");
      this.onGroundSpoof = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("On Ground Spoof")).description("Spoofs on ground.")).defaultValue(false)).build());
      SettingGroup var10001 = this.sgGeneral;
      BoolSetting.Builder var10002 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("On Ground")).description("Should we tell the server that you are on ground.")).defaultValue(false);
      Setting<Boolean> var10003 = this.onGroundSpoof;
      Objects.requireNonNull(var10003);
      this.onGround = var10001.add(((BoolSetting.Builder)var10002.visible(var10003::get)).build());
      this.xzBound = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("XZ Bound")).description("Bounds offset horizontally.")).defaultValue(1337)).sliderRange(-1337, 1337).build());
      this.yBound = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Y Bound")).description("Bounds offset vertically.")).defaultValue(0)).sliderRange(-1337, 1337).build());
      this.strictVertical = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Strict Vertical")).description("Doesn't move horizontally and vertically in the same packet.")).defaultValue(false)).build());
      this.antiKick = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Anti-Kick")).description("Slowly falls down.")).defaultValue(true)).build());
      this.antiKickAmount = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Anti-Kick Multiplier")).description("Fall speed multiplier for antikick (0.04 blocks * multiplier).")).defaultValue((double)1.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.antiKickDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Anti-Kick Delay")).description("Tick delay between moving anti kick packets.")).defaultValue(10)).min(1).sliderRange(0, 100).build());
      this.predictID = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Predict ID")).description("Predicts the id of next rubberband.")).defaultValue(true)).build());
      this.debugID = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Debug ID")).description("Sends rubberband packet id in chat.")).defaultValue(false)).build());
      this.packets = this.sgFly.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fly Packets")).description("How many packets to send every movement tick.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.speed = this.sgFly.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fly Speed")).description("Distance to travel each packet.")).defaultValue(0.2873).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.fastVertical = this.sgFly.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Fast Vertical Fly")).description("Sends multiple packets every movement tick while going up.")).defaultValue(false)).build());
      this.downSpeed = this.sgFly.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fly Down Speed")).description("How fast to fly down.")).defaultValue(0.062).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.upSpeed = this.sgFly.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fly Up Speed")).description("How fast to fly up.")).defaultValue(0.062).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.phasePackets = this.sgPhase.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Phase Packets")).description("How many packets to send every movement tick.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.phaseSpeed = this.sgPhase.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Phase Speed")).description("Distance to travel each packet.")).defaultValue(0.062).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.phaseFastVertical = this.sgPhase.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Fast Vertical Phase")).description("Sends multiple packets every movement tick while going up.")).defaultValue(false)).build());
      this.phaseDownSpeed = this.sgPhase.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Phase Down Speed")).description("How fast to phase down.")).defaultValue(0.062).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.phaseUpSpeed = this.sgPhase.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Phase Up Speed")).description("How fast to phase up.")).defaultValue(0.062).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.ticks = 0;
      this.id = -1;
      this.sent = 0;
      this.rur = 0;
      this.packetsToSend = (double)0.0F;
      this.random = new Random();
      this.info = null;
      this.validPos = new HashMap();
      this.validPackets = new ArrayList();
      this.moving = false;
   }

   public void onActivate() {
      super.onActivate();
      this.ticks = 0;
      this.validPos.clear();
   }

   public void onDeactivate() {
      this.validPos.clear();
   }

   @EventHandler
   private void onTick(TickEvent.Post e) {
      ++this.ticks;
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
         this.mc.player.noClip = semiPhasing;
         this.packetsToSend += this.packets(semiPhasing);
         boolean shouldAntiKick = (Boolean)this.antiKick.get() && this.ticks % (Integer)this.antiKickDelay.get() == 0 && !phasing && !this.onGround();
         double yaw = this.getYaw();
         double motion = semiPhasing ? (Double)this.phaseSpeed.get() : (Double)this.speed.get();
         double x = (double)0.0F;
         double y = (double)0.0F;
         double z = (double)0.0F;
         if (this.jumping()) {
            y = semiPhasing ? (Double)this.phaseUpSpeed.get() : (Double)this.upSpeed.get();
         } else if (this.sneaking()) {
            y = semiPhasing ? -(Double)this.phaseDownSpeed.get() : -(Double)this.downSpeed.get();
         }

         if (y != (double)0.0F) {
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

         Vec3d offset = new Vec3d((double)0.0F, (double)0.0F, (double)0.0F);

         for(boolean antiKickSent = false; this.packetsToSend >= (double)1.0F; --this.packetsToSend) {
            double yOffset;
            if (shouldAntiKick && y >= (double)0.0F && !antiKickSent) {
               yOffset = (Double)this.antiKickAmount.get() * -0.04;
               antiKickSent = true;
            } else {
               yOffset = y;
            }

            offset = offset.add((Boolean)this.strictVertical.get() && yOffset != (double)0.0F ? (double)0.0F : x, yOffset, (Boolean)this.strictVertical.get() && yOffset != (double)0.0F ? (double)0.0F : z);
            this.send(offset.add(this.mc.player.getPos()), this.getBounds(), this.getOnGround());
            if (x == (double)0.0F && z == (double)0.0F && y == (double)0.0F) {
               break;
            }
         }

         ((IVec3d)e.movement).set(offset.x, offset.y, offset.z);
         this.packetsToSend = Math.min(this.packetsToSend, (double)1.0F);
      }
   }

   @EventHandler
   public void onSend(PacketEvent.Send event) {
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
         if ((Boolean)this.debugID.get()) {
            this.debug("id: " + packet.getTeleportId());
         }

         Vec3d vec = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
         if (this.validPos.containsKey(packet.getTeleportId()) && ((Vec3d)this.validPos.get(packet.getTeleportId())).equals(vec)) {
            if ((Boolean)this.debugID.get()) {
               this.debug("true");
            }

            e.cancel();
            if (!(Boolean)this.predictID.get()) {
               this.sendPacket(new TeleportConfirmC2SPacket(packet.getTeleportId()));
            }

            this.validPos.remove(packet.getTeleportId());
            return;
         }

         if ((Boolean)this.debugID.get()) {
            this.debug("false");
         }

         this.id = packet.getTeleportId();
      }

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
      int yaw = this.random.nextInt(0, 360);
      return new Vec3d(Math.cos(Math.toRadians((double)yaw)) * (double)(Integer)this.xzBound.get(), (double)(Integer)this.yBound.get(), Math.sin(Math.toRadians((double)yaw)) * (double)(Integer)this.xzBound.get());
   }

   private boolean getOnGround() {
      return (Boolean)this.onGroundSpoof.get() ? (Boolean)this.onGround.get() : this.mc.player.isOnGround();
   }

   private boolean isPhasing() {
      return OLEPOSSUtils.inside(this.mc.player, this.mc.player.getBoundingBox().shrink((double)0.0625F, (double)0.0F, (double)0.0625F));
   }

   private boolean isSemiPhase() {
      return OLEPOSSUtils.inside(this.mc.player, this.mc.player.getBoundingBox().expand(0.01, (double)0.0F, 0.01));
   }

   private boolean jumping() {
      return this.mc.options.jumpKey.isPressed();
   }

   private boolean sneaking() {
      return this.mc.options.sneakKey.isPressed();
   }

   private void send(Vec3d pos, Vec3d bounds, boolean onGround) {
      PlayerMoveC2SPacket.PositionAndOnGround normal = new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y, pos.z, onGround);
      PlayerMoveC2SPacket.PositionAndOnGround bound = new PlayerMoveC2SPacket.PositionAndOnGround(pos.x + bounds.x, pos.y + bounds.y, pos.z + bounds.z, onGround);
      this.validPackets.add(normal);
      this.sendPacket(normal);
      this.validPos.put(this.id + 1, pos);
      this.validPackets.add(bound);
      this.sendPacket(bound);
      if (this.id >= 0) {
         ++this.id;
         if ((Boolean)this.predictID.get()) {
            this.sendPacket(new TeleportConfirmC2SPacket(this.id));
         }

      }
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
}
