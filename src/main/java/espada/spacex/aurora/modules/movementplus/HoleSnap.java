package espada.spacex.aurora.modules.movementplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.HoleType;
import espada.spacex.aurora.utils.Hole;
import espada.spacex.aurora.utils.HoleUtils;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import java.util.Objects;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class HoleSnap extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgSpeed;
   private final SettingGroup sgHole;
   private final Setting<Boolean> jump;
   private final Setting<Integer> jumpCoolDown;
   private final Setting<Integer> range;
   private final Setting<Integer> downRange;
   private final Setting<Integer> coll;
   private final Setting<Integer> rDisable;
   private final Setting<Double> speed;
   private final Setting<Boolean> boost;
   private final Setting<Double> boostedSpeed;
   private final Setting<Integer> boostTicks;
   private final Setting<Double> timer;
   private final Setting<Boolean> singleTarget;
   private final Setting<Integer> depth;
   private final Setting<Boolean> singleHoles;
   private final Setting<Boolean> doubleHoles;
   private final Setting<Boolean> quadHoles;
   private Hole singleHole;
   private int collisions;
   private int rubberbands;
   private int ticks;
   private int boostLeft;

   public HoleSnap() {
      super(Aurora.MovementPlus, "Hole Snap", "For the times when you cant even press W.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgSpeed = this.settings.createGroup("Speed");
      this.sgHole = this.settings.createGroup("Hole");
      this.jump = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Jump")).description("Jumps to the hole (very useful).")).defaultValue(false)).build());
      SettingGroup var10001 = this.sgGeneral;
      IntSetting.Builder var10002 = ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Jump Cooldown")).description("Ticks between jumps.")).defaultValue(5)).min(0).sliderMax(100);
      Setting<Boolean> var10003 = this.jump;
      Objects.requireNonNull(var10003);
      this.jumpCoolDown = var10001.add(((IntSetting.Builder)var10002.visible(var10003::get)).build());
      this.range = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Range")).description("Horizontal range for finding holes.")).defaultValue(3)).range(0, 5).sliderMax(5).build());
      this.downRange = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Down Range")).description("Vertical range for finding holes.")).defaultValue(3)).range(0, 5).sliderMax(5).build());
      this.coll = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Collisions to disable")).description("0 = doesn't disable.")).defaultValue(15)).min(0).sliderRange(0, 100).build());
      this.rDisable = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Rubberbands to disable")).description("0 = doesn't disable.")).defaultValue(1)).min(0).sliderRange(0, 100).build());
      this.speed = this.sgSpeed.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Speed")).description("Movement Speed.")).defaultValue(0.2873).min((double)0.0F).sliderMax((double)1.0F).build());
      this.boost = this.sgSpeed.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Speed Boost")).description("Jumps to the hole (very useful).")).defaultValue(false)).build());
      var10001 = this.sgSpeed;
      DoubleSetting.Builder var3 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Boosted Speed")).description("Movement Speed.")).defaultValue((double)0.5F).min((double)0.0F).sliderMax((double)1.0F);
      var10003 = this.boost;
      Objects.requireNonNull(var10003);
      this.boostedSpeed = var10001.add(((DoubleSetting.Builder)var3.visible(var10003::get)).build());
      var10001 = this.sgSpeed;
      IntSetting.Builder var4 = ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Boost Ticks")).description("How many boosted speed packets should be sent before returning to normal speed.")).defaultValue(3)).min(1).sliderMax(10);
      var10003 = this.boost;
      Objects.requireNonNull(var10003);
      this.boostTicks = var10001.add(((IntSetting.Builder)var4.visible(var10003::get)).build());
      this.timer = this.sgSpeed.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Timer")).description("Sends packets faster.")).defaultValue((double)10.0F).min((double)0.0F).sliderMax((double)100.0F).build());
      this.singleTarget = this.sgHole.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Single Target")).description("Only chooses target hole once.")).defaultValue(true)).build());
      this.depth = this.sgHole.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Hole Depth")).description("How deep a hole has to be.")).defaultValue(3)).range(1, 5).sliderRange(1, 5).build());
      this.singleHoles = this.sgHole.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Single Holes")).description("Targets single block holes.")).defaultValue(true)).build());
      this.doubleHoles = this.sgHole.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Double Holes")).description("Targets double holes.")).defaultValue(true)).build());
      this.quadHoles = this.sgHole.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Quad Holes")).description("Targets quad holes.")).defaultValue(true)).build());
      this.boostLeft = 0;
   }

   public void onActivate() {
      super.onActivate();
      this.singleHole = this.findHole();
      this.rubberbands = 0;
      this.ticks = 0;
      this.boostLeft = (Boolean)this.boost.get() ? (Integer)this.boostTicks.get() : 0;
   }

   public void onDeactivate() {
      super.onDeactivate();
      ((Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class)).setOverride((double)1.0F);
   }

   @EventHandler
   private void onPacket(PacketEvent.Receive event) {
      if (event.packet instanceof PlayerPositionLookS2CPacket && (Integer)this.rDisable.get() > 0) {
         ++this.rubberbands;
         if (this.rubberbands >= (Integer)this.rDisable.get() && (Integer)this.rDisable.get() > 0) {
            this.toggle();
            this.sendDisableMsg("rubberbanding");
         }
      }

   }

   @EventHandler(
      priority = 200
   )
   private void onMove(PlayerMoveEvent event) {
      if (this.mc.player != null && this.mc.world != null) {
         Hole hole = (Boolean)this.singleTarget.get() ? this.singleHole : this.findHole();
         if (hole != null && !this.singleBlocked()) {
            ((Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class)).setOverride((Double)this.timer.get());
            double yaw = Math.cos(Math.toRadians((double)(this.getAngle(hole.middle) + 90.0F)));
            double pit = Math.sin(Math.toRadians((double)(this.getAngle(hole.middle) + 90.0F)));
            if (this.mc.player.getX() == hole.middle.x && this.mc.player.getZ() == hole.middle.z) {
               if (this.mc.player.getY() == hole.middle.y) {
                  this.toggle();
                  this.sendDisableMsg("in hole");
                  ((IVec3d)event.movement).setXZ((double)0.0F, (double)0.0F);
               } else if (OLEPOSSUtils.inside(this.mc.player, this.mc.player.getBoundingBox().offset((double)0.0F, -0.05, (double)0.0F))) {
                  this.toggle();
                  this.sendDisableMsg("hole unreachable");
               } else {
                  ((IVec3d)event.movement).setXZ((double)0.0F, (double)0.0F);
               }
            } else {
               double x = this.getSpeed() * yaw;
               double dX = hole.middle.x - this.mc.player.getX();
               double z = this.getSpeed() * pit;
               double dZ = hole.middle.z - this.mc.player.getZ();
               if (OLEPOSSUtils.inside(this.mc.player, this.mc.player.getBoundingBox().offset(x, (double)0.0F, z))) {
                  ++this.collisions;
                  if (this.collisions >= (Integer)this.coll.get() && (Integer)this.coll.get() > 0) {
                     this.toggle();
                     this.sendDisableMsg("collided");
                  }
               } else {
                  this.collisions = 0;
               }

               if (this.ticks > 0) {
                  --this.ticks;
               } else if (OLEPOSSUtils.inside(this.mc.player, this.mc.player.getBoundingBox().offset((double)0.0F, -0.05, (double)0.0F)) && (Boolean)this.jump.get()) {
                  this.ticks = (Integer)this.jumpCoolDown.get();
                  ((IVec3d)event.movement).setY(0.42);
               }

               --this.boostLeft;
               ((IVec3d)event.movement).setXZ(Math.abs(x) < Math.abs(dX) ? x : dX, Math.abs(z) < Math.abs(dZ) ? z : dZ);
            }
         } else {
            this.toggle();
            this.sendDisableMsg("no hole found");
         }
      }

   }

   private boolean singleBlocked() {
      if (!(Boolean)this.singleTarget.get()) {
         return false;
      } else {
         for(BlockPos pos : this.singleHole.positions) {
            if (OLEPOSSUtils.collidable(pos)) {
               return true;
            }
         }

         return false;
      }
   }

   private Hole findHole() {
      Hole closest = null;

      for(int x = -(Integer)this.range.get(); x <= (Integer)this.range.get(); ++x) {
         for(int y = -(Integer)this.downRange.get(); y < 1; ++y) {
            for(int z = -(Integer)this.range.get(); z < (Integer)this.range.get(); ++z) {
               BlockPos pos = this.mc.player.getBlockPos().add(x, y, z);
               Hole hole = HoleUtils.getHole(pos, (Boolean)this.singleHoles.get(), (Boolean)this.doubleHoles.get(), (Boolean)this.quadHoles.get(), (Integer)this.depth.get(), true);
               if (hole.type != HoleType.NotHole) {
                  if (y == 0 && this.inHole(hole)) {
                     return hole;
                  }

                  if (closest == null || hole.middle.distanceTo(this.mc.player.getPos()) < closest.middle.distanceTo(this.mc.player.getPos())) {
                     closest = hole;
                  }
               }
            }
         }
      }

      return closest;
   }

   private boolean inHole(Hole hole) {
      for(BlockPos pos : hole.positions) {
         if (this.mc.player.getBlockPos().equals(pos)) {
            return true;
         }
      }

      return false;
   }

   private float getAngle(Vec3d pos) {
      return (float)Rotations.getYaw(pos);
   }

   private double getSpeed() {
      return this.boostLeft > 0 ? (Double)this.boostedSpeed.get() : (Double)this.speed.get();
   }
}
