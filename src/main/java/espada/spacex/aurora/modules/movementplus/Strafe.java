package espada.spacex.aurora.modules.movementplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.meteor.BOEntityUtils;
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
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.movement.LongJump;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.MovementType;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.joml.Vector2d;

public class Strafe extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgVanilla;
   private final SettingGroup sgNCP;
   private final SettingGroup sgPotion;
   private final SettingGroup sgPause;
   private final SettingGroup sgAC;
   private final Setting<Mode> mode;
   private final Setting<Boolean> earthh4ckMode;
   private final Setting<Double> groundTimer;
   private final Setting<Double> airTimer;
   private final Setting<Boolean> autoSprint;
   private final Setting<Double> vanillaSneakSpeed;
   private final Setting<Double> vanillaGroundSpeed;
   private final Setting<Double> vanillaAirSpeed;
   private final Setting<Boolean> rubberbandPause;
   private final Setting<Integer> rubberbandTime;
   private final Setting<Double> ncpSpeed;
   private final Setting<Boolean> ncpSpeedLimit;
   private final Setting<Double> startingSpeed;
   private final Setting<HopMode> hopMode;
   private final Setting<Double> hopHeight;
   private final Setting<Integer> jumpTime;
   private final Setting<Double> jumpedSlowDown;
   private final Setting<Double> resetDivisor;
   private final Setting<Boolean> applyJumpBoost;
   private final Setting<Boolean> applySpeed;
   private final Setting<Boolean> applySlowness;
   private final Setting<Boolean> longJumpPause;
   private final Setting<Boolean> flightPause;
   private final Setting<Boolean> eFlyPause;
   private final Setting<Boolean> inWater;
   private final Setting<Boolean> inLava;
   private final Setting<Boolean> whenSneaking;
   private final Setting<Boolean> hungerCheck;
   private final Setting<WebbedPause> webbedPause;
   private final Setting<Boolean> pBur;
   private int stage;
   private double distance;
   private double speed;
   private long timer;
   private int rubberbandTicks;
   private boolean rubberbanded;
   private boolean sentMessage;
   private int jumpTicks;
   private boolean jumped;
   final Timer timerClass;
   final LongJump longJump;
   final Flight flight;
   final ElytraFly efly;

   public Strafe() {
      super(Aurora.MovementPlus, "strafe", "Increase speed and control.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgVanilla = this.settings.createGroup("Vanilla");
      this.sgNCP = this.settings.createGroup("NCP");
      this.sgPotion = this.settings.createGroup("Potions");
      this.sgPause = this.settings.createGroup("Pause");
      this.sgAC = this.settings.createGroup("Anti Cheat");
      this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("mode")).description("Behaviour of your movements.")).defaultValue(Strafe.Mode.Smart)).build());
      this.earthh4ckMode = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("3arthh4ck-Mode")).description("only air to boost")).defaultValue(true)).build());
      this.groundTimer = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("ground-timer")).description("Ground timer override.")).defaultValue((double)1.0F).sliderRange(0.001, (double)10.0F).build());
      this.airTimer = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("air-timer")).description("Air timer override.")).defaultValue(1.088).sliderRange(0.001, (double)10.0F).build());
      this.autoSprint = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("auto-sprint")).description("Makes you sprint if you are moving forward.")).defaultValue(false)).build());
      this.vanillaSneakSpeed = this.sgVanilla.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("vanilla-sneak-speed")).description("The speed in blocks per second (on ground and sneaking).")).defaultValue(2.6).min((double)0.0F).sliderMax((double)20.0F).visible(() -> this.mode.get() == Strafe.Mode.Vanilla)).build());
      this.vanillaGroundSpeed = this.sgVanilla.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("vanilla-ground-speed")).description("The speed in blocks per second (on ground).")).defaultValue(5.6).min((double)0.0F).sliderMax((double)20.0F).visible(() -> this.mode.get() == Strafe.Mode.Vanilla)).build());
      this.vanillaAirSpeed = this.sgVanilla.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("vanilla-air-speed")).description("The speed in blocks per second (on air).")).defaultValue((double)6.0F).min((double)0.0F).sliderMax((double)20.0F).visible(() -> this.mode.get() == Strafe.Mode.Vanilla)).build());
      this.rubberbandPause = this.sgVanilla.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("pause-on-rubberband")).description("Will pause Vanilla mode when you rubberband.")).defaultValue(false)).visible(() -> this.mode.get() == Strafe.Mode.Vanilla)).build());
      this.rubberbandTime = this.sgVanilla.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("pause-time")).description("Pauses vanilla mode for x ticks when a rubberband is detected.")).defaultValue(30)).min(0).sliderMax(100).visible(() -> this.mode.get() == Strafe.Mode.Vanilla && (Boolean)this.rubberbandPause.get())).build());
      this.ncpSpeed = this.sgNCP.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("NCP-speed")).description("The speed.")).defaultValue((double)2.0F).min((double)0.0F).sliderMax((double)3.0F).visible(() -> this.mode.get() != Strafe.Mode.Vanilla)).build());
      this.ncpSpeedLimit = this.sgNCP.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("speed-limit")).description("Limits your speed on servers with very strict anticheats.")).defaultValue(false)).visible(() -> this.mode.get() != Strafe.Mode.Vanilla)).build());
      this.startingSpeed = this.sgNCP.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("starting-speed")).description("Initial speed when starting (recommended 1.18 on NCP, 1.080 on Smart).")).defaultValue(1.08).min((double)0.0F).sliderMax((double)2.0F).visible(() -> this.mode.get() != Strafe.Mode.Vanilla)).build());
      this.hopMode = this.sgNCP.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("hop-mode")).description("Mode to use for the hop height.")).defaultValue(Strafe.HopMode.Auto)).visible(() -> this.mode.get() != Strafe.Mode.Vanilla)).build());
      this.hopHeight = this.sgNCP.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("hop-height")).description("The hop intensity.")).defaultValue(0.401).min((double)0.0F).sliderMax((double)1.0F).visible(() -> this.hopMode.get() == Strafe.HopMode.Custom && this.mode.get() != Strafe.Mode.Vanilla)).build());
      this.jumpTime = this.sgNCP.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("jump-time")).description("How many ticks to recognise that you have jumped for smart mode.")).defaultValue(20)).min(0).sliderMax(30).visible(() -> this.mode.get() == Strafe.Mode.Smart)).build());
      this.jumpedSlowDown = this.sgNCP.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("jumped-slow-down")).description("How much to slow down by after jumping.")).defaultValue(0.76).min((double)0.0F).sliderMax((double)1.0F).visible(() -> this.mode.get() != Strafe.Mode.Vanilla)).build());
      this.resetDivisor = this.sgNCP.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("reset-divisor")).description("Speed value get divided by this amount on rubberband or collision.")).defaultValue((double)159.0F).min((double)0.0F).sliderMax((double)200.0F).visible(() -> this.mode.get() != Strafe.Mode.Vanilla)).build());
      this.applyJumpBoost = this.sgPotion.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("jump-boost")).description("Apply jump boost effect if the player has it.")).defaultValue(true)).visible(() -> this.mode.get() != Strafe.Mode.Vanilla)).build());
      this.applySpeed = this.sgPotion.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("speed-effect")).description("Apply speed effect if the player has it.")).defaultValue(true)).build());
      this.applySlowness = this.sgPotion.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("slowness-effect")).description("Apply slowness effect if the player has it.")).defaultValue(true)).build());
      this.longJumpPause = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("pause-on-long-jump")).description("Pauses the module if long jump is active.")).defaultValue(false)).build());
      this.flightPause = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("pause-on-flight")).description("Pauses the module if flight is active.")).defaultValue(false)).build());
      this.eFlyPause = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("pause-on-elytra-fly")).description("Pauses the module if elytra fly is active.")).defaultValue(false)).build());
      this.inWater = this.sgAC.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("in-water")).description("Uses speed when in water.")).defaultValue(false)).build());
      this.inLava = this.sgAC.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("in-lava")).description("Uses speed when in lava.")).defaultValue(false)).build());
      this.whenSneaking = this.sgAC.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("when-sneaking")).description("Uses speed when sneaking.")).defaultValue(false)).build());
      this.hungerCheck = this.sgAC.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("hunger-check")).description("Pauses when hunger reaches 3 or less drumsticks.")).defaultValue(true)).build());
      this.webbedPause = this.sgAC.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("pause-on-webbed")).description("Pauses when you are webbed.")).defaultValue(Strafe.WebbedPause.OnAir)).build());
      this.pBur = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("burrowbypass")).description("nolag in burrow.")).defaultValue(true)).build());
      this.timer = 0L;
      this.timerClass = (Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class);
      this.longJump = (LongJump)meteordevelopment.meteorclient.systems.modules.Modules.get().get(LongJump.class);
      this.flight = (Flight)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Flight.class);
      this.efly = (ElytraFly)meteordevelopment.meteorclient.systems.modules.Modules.get().get(ElytraFly.class);
   }

   public void onDeactivate() {
      this.timerClass.setOverride((double)1.0F);
   }

   @EventHandler
   private void onPlayerMove(PlayerMoveEvent event) {
      if (event.type == MovementType.SELF && !this.mc.player.isFallFlying() && !this.mc.player.isClimbing() && this.mc.player.getVehicle() == null) {
         if ((Boolean)this.whenSneaking.get() || !this.mc.player.isSneaking()) {
            if ((Boolean)this.inWater.get() || !this.mc.player.isTouchingWater()) {
               if ((Boolean)this.inLava.get() || !this.mc.player.isInLava()) {
                  if (!(Boolean)this.hungerCheck.get() || this.mc.player.getHungerManager().getFoodLevel() > 6) {
                     if (!(Boolean)this.earthh4ckMode.get() || !this.mc.player.isOnGround()) {
                        if (!BOEntityUtils.isBurrowed(this.mc.player, !(Boolean)this.pBur.get())) {
                           if (!(Boolean)this.longJumpPause.get() || !this.longJump.isActive()) {
                              if (!(Boolean)this.flightPause.get() || !this.flight.isActive()) {
                                 if (!(Boolean)this.eFlyPause.get() || !this.efly.isActive()) {
                                    if (!BOEntityUtils.isWebbed(this.mc.player) || this.webbedPause.get() != Strafe.WebbedPause.Always) {
                                       if (!BOEntityUtils.isWebbed(this.mc.player) || this.mc.player.isOnGround() || this.webbedPause.get() != Strafe.WebbedPause.OnAir) {
                                          if (this.mc.player.isOnGround()) {
                                             this.timerClass.setOverride(PlayerUtils.isMoving() ? (Double)this.groundTimer.get() : (double)1.0F);
                                          } else {
                                             this.timerClass.setOverride(PlayerUtils.isMoving() ? (Double)this.airTimer.get() : (double)1.0F);
                                          }

                                          if (this.mode.get() == Strafe.Mode.Vanilla && !this.rubberbanded) {
                                             if (this.mc.player.isOnGround()) {
                                                if (this.mc.player.isSneaking()) {
                                                   Vec3d vel = PlayerUtils.getHorizontalVelocity((Double)this.vanillaSneakSpeed.get());
                                                   double velX = vel.getX();
                                                   double velZ = vel.getZ();
                                                   if (this.mc.player.hasStatusEffect(StatusEffects.SPEED) && (Boolean)this.applySpeed.get()) {
                                                      double value = (double)(this.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1) * 0.205;
                                                      velX += velX * value;
                                                      velZ += velZ * value;
                                                   }

                                                   if (this.mc.player.hasStatusEffect(StatusEffects.SLOWNESS) && (Boolean)this.applySlowness.get()) {
                                                      double value = (double)(this.mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() + 1) * 0.205;
                                                      velX -= velX * value;
                                                      velZ -= velZ * value;
                                                   }

                                                   ((IVec3d)event.movement).set(velX, event.movement.y, velZ);
                                                } else {
                                                   Vec3d vel = PlayerUtils.getHorizontalVelocity((Double)this.vanillaGroundSpeed.get());
                                                   double velX = vel.getX();
                                                   double velZ = vel.getZ();
                                                   if (this.mc.player.hasStatusEffect(StatusEffects.SPEED) && (Boolean)this.applySpeed.get()) {
                                                      double value = (double)(this.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1) * 0.205;
                                                      velX += velX * value;
                                                      velZ += velZ * value;
                                                   }

                                                   if (this.mc.player.hasStatusEffect(StatusEffects.SLOWNESS) && (Boolean)this.applySlowness.get()) {
                                                      double value = (double)(this.mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() + 1) * 0.205;
                                                      velX -= velX * value;
                                                      velZ -= velZ * value;
                                                   }

                                                   ((IVec3d)event.movement).set(velX, event.movement.y, velZ);
                                                }
                                             } else {
                                                Vec3d vel = PlayerUtils.getHorizontalVelocity((Double)this.vanillaAirSpeed.get());
                                                double velX = vel.getX();
                                                double velZ = vel.getZ();
                                                if (this.mc.player.hasStatusEffect(StatusEffects.SPEED) && (Boolean)this.applySpeed.get()) {
                                                   double value = (double)(this.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1) * 0.205;
                                                   velX += velX * value;
                                                   velZ += velZ * value;
                                                }

                                                if (this.mc.player.hasStatusEffect(StatusEffects.SLOWNESS) && (Boolean)this.applySlowness.get()) {
                                                   double value = (double)(this.mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() + 1) * 0.205;
                                                   velX -= velX * value;
                                                   velZ -= velZ * value;
                                                }

                                                ((IVec3d)event.movement).set(velX, event.movement.y, velZ);
                                             }
                                          }

                                          if (this.mode.get() == Strafe.Mode.NCP) {
                                             switch (this.stage) {
                                                case 0:
                                                   if (PlayerUtils.isMoving()) {
                                                      ++this.stage;
                                                      this.speed = (Double)this.startingSpeed.get() * this.getDefaultSpeed() - 0.01;
                                                   }
                                                case 1:
                                                   if (PlayerUtils.isMoving() && this.mc.player.isOnGround()) {
                                                      if (this.hopMode.get() == Strafe.HopMode.Auto) {
                                                         ((IVec3d)event.movement).setY(this.getHop(0.40123128));
                                                      } else {
                                                         ((IVec3d)event.movement).setY(this.getHop((Double)this.hopHeight.get()));
                                                      }

                                                      this.speed *= (Double)this.ncpSpeed.get();
                                                      ++this.stage;
                                                   }
                                                   break;
                                                case 2:
                                                   this.speed = this.distance - (Double)this.jumpedSlowDown.get() * (this.distance - this.getDefaultSpeed());
                                                   ++this.stage;
                                                   break;
                                                case 3:
                                                   if (!this.mc.world.isSpaceEmpty(this.mc.player.getBoundingBox().offset((double)0.0F, this.mc.player.getVelocity().y, (double)0.0F)) || this.mc.player.verticalCollision && this.stage > 0) {
                                                      this.stage = 0;
                                                   }

                                                   this.speed = this.distance - this.distance / (Double)this.resetDivisor.get();
                                             }

                                             this.speed = Math.max(this.speed, this.getDefaultSpeed());
                                             if ((Boolean)this.ncpSpeedLimit.get()) {
                                                if (System.currentTimeMillis() - this.timer > 2500L) {
                                                   this.timer = System.currentTimeMillis();
                                                }

                                                this.speed = Math.min(this.speed, System.currentTimeMillis() - this.timer > 1250L ? 0.44 : 0.43);
                                             }

                                             Vector2d change = this.transformStrafe(this.speed);
                                             double velX = change.x;
                                             double velZ = change.y;
                                             ((IVec3d)event.movement).setXZ(velX, velZ);
                                          }

                                          if (this.mode.get() == Strafe.Mode.Smart) {
                                             switch (this.stage) {
                                                case 0:
                                                   if (PlayerUtils.isMoving()) {
                                                      ++this.stage;
                                                      this.speed = (Double)this.startingSpeed.get() * this.getDefaultSpeed() - 0.01;
                                                   }
                                                case 1:
                                                   if (PlayerUtils.isMoving() && this.mc.player.isOnGround() && this.jumped) {
                                                      if (this.hopMode.get() == Strafe.HopMode.Auto) {
                                                         ((IVec3d)event.movement).setY(this.getHop(0.40123128));
                                                      } else {
                                                         ((IVec3d)event.movement).setY(this.getHop((Double)this.hopHeight.get()));
                                                      }

                                                      this.speed *= (Double)this.ncpSpeed.get();
                                                      ++this.stage;
                                                   }
                                                   break;
                                                case 2:
                                                   this.speed = this.distance - (Double)this.jumpedSlowDown.get() * (this.distance - this.getDefaultSpeed());
                                                   ++this.stage;
                                                   break;
                                                case 3:
                                                   if (!this.mc.world.isSpaceEmpty(this.mc.player.getBoundingBox().offset((double)0.0F, this.mc.player.getVelocity().y, (double)0.0F)) || this.mc.player.verticalCollision && this.stage > 0) {
                                                      this.stage = 0;
                                                   }

                                                   this.speed = this.distance - this.distance / (Double)this.resetDivisor.get();
                                             }

                                             this.speed = Math.max(this.speed, this.getDefaultSpeed());
                                             if ((Boolean)this.ncpSpeedLimit.get()) {
                                                if (System.currentTimeMillis() - this.timer > 2500L) {
                                                   this.timer = System.currentTimeMillis();
                                                }

                                                this.speed = Math.min(this.speed, System.currentTimeMillis() - this.timer > 1250L ? 0.44 : 0.43);
                                             }

                                             Vector2d change = this.transformStrafe(this.speed);
                                             double velX = change.x;
                                             double velZ = change.y;
                                             ((IVec3d)event.movement).setXZ(velX, velZ);
                                          }

                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   private void onPreTick(TickEvent.Pre event) {
      if (!this.mc.player.isFallFlying() && !this.mc.player.isClimbing() && this.mc.player.getVehicle() == null) {
         if ((Boolean)this.whenSneaking.get() || !this.mc.player.isSneaking()) {
            if ((Boolean)this.inWater.get() || !this.mc.player.isTouchingWater()) {
               if ((Boolean)this.inLava.get() || !this.mc.player.isInLava()) {
                  if (!(Boolean)this.hungerCheck.get() || this.mc.player.getHungerManager().getFoodLevel() > 6) {
                     if (!(Boolean)this.longJumpPause.get() || !this.longJump.isActive()) {
                        if (!(Boolean)this.flightPause.get() || !this.flight.isActive()) {
                           if (!(Boolean)this.eFlyPause.get() || !this.efly.isActive()) {
                              if (!BOEntityUtils.doesBoxTouchBlock(this.mc.player.getBoundingBox(), Blocks.COBWEB) || this.webbedPause.get() != Strafe.WebbedPause.Always) {
                                 if (!BOEntityUtils.doesBoxTouchBlock(this.mc.player.getBoundingBox(), Blocks.COBWEB) || this.mc.player.isOnGround() || this.webbedPause.get() != Strafe.WebbedPause.OnAir) {
                                    if (this.mc.player.forwardSpeed > 0.0F && (Boolean)this.autoSprint.get()) {
                                       this.mc.player.setSprinting(true);
                                    }

                                    if ((Boolean)this.rubberbandPause.get() && this.mode.get() == Strafe.Mode.Vanilla) {
                                       if (this.rubberbandTicks > 0) {
                                          --this.rubberbandTicks;
                                          this.rubberbanded = true;
                                          this.info("Rubberband detected... pausing", new Object[0]);
                                          this.sentMessage = false;
                                       } else {
                                          this.rubberbanded = false;
                                          if (!this.sentMessage) {
                                             this.info("Continued", new Object[0]);
                                          }

                                          this.sentMessage = true;
                                       }
                                    }

                                    if (this.mode.get() == Strafe.Mode.Smart) {
                                       if (this.mc.options.jumpKey.isPressed() && this.mc.player.isOnGround()) {
                                          this.jumpTicks = (Integer)this.jumpTime.get();
                                       }

                                       if (this.jumpTicks > 0) {
                                          this.jumped = true;
                                          --this.jumpTicks;
                                       } else {
                                          this.jumped = false;
                                       }

                                       if (this.mc.player.isOnGround()) {
                                          this.jumpTicks = 0;
                                       }
                                    }

                                    if (this.mode.get() != Strafe.Mode.Vanilla) {
                                       this.distance = Math.sqrt((this.mc.player.getX() - this.mc.player.prevX) * (this.mc.player.getX() - this.mc.player.prevX) + (this.mc.player.getZ() - this.mc.player.prevZ) * (this.mc.player.getZ() - this.mc.player.prevZ));
                                    }

                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   private void onPacketReceive(PacketEvent.Receive event) {
      if (event.packet instanceof PlayerPositionLookS2CPacket) {
         this.rubberbandTicks = (Integer)this.rubberbandTime.get();
         this.reset();
      }

   }

   private double getDefaultSpeed() {
      double defaultSpeed = 0.2873;
      if (this.mc.player.hasStatusEffect(StatusEffects.SPEED) && (Boolean)this.applySpeed.get()) {
         int amplifier = this.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
         defaultSpeed *= (double)1.0F + 0.2 * (double)(amplifier + 1);
      }

      if (this.mc.player.hasStatusEffect(StatusEffects.SLOWNESS) && (Boolean)this.applySlowness.get()) {
         int amplifier = this.mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
         defaultSpeed /= (double)1.0F + 0.2 * (double)(amplifier + 1);
      }

      return defaultSpeed;
   }

   private void reset() {
      this.stage = 0;
      this.distance = (double)0.0F;
      this.speed = 0.2873;
   }

   private double getHop(double height) {
      StatusEffectInstance jumpBoost = this.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) ? this.mc.player.getStatusEffect(StatusEffects.JUMP_BOOST) : null;
      if (jumpBoost != null && (Boolean)this.applyJumpBoost.get()) {
         height += (double)((float)(jumpBoost.getAmplifier() + 1) * 0.1F);
      }

      return height;
   }

   private Vector2d transformStrafe(double speed) {
      float forward = this.mc.player.input.movementForward;
      float side = this.mc.player.input.movementSideways;
      float yaw = this.mc.player.prevYaw + (this.mc.player.getYaw() - this.mc.player.prevYaw) * this.mc.getRenderTickCounter().getTickDelta(true);
      if (forward == 0.0F && side == 0.0F) {
         return new Vector2d((double)0.0F, (double)0.0F);
      } else {
         if (forward != 0.0F) {
            if (side >= 1.0F) {
               yaw += (float)(forward > 0.0F ? -45 : 45);
               side = 0.0F;
            } else if (side <= -1.0F) {
               yaw += (float)(forward > 0.0F ? 45 : -45);
               side = 0.0F;
            }

            if (forward > 0.0F) {
               forward = 1.0F;
            } else if (forward < 0.0F) {
               forward = -1.0F;
            }
         }

         double mx = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
         double mz = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
         double velX = (double)forward * speed * mx + (double)side * speed * mz;
         double velZ = (double)forward * speed * mz - (double)side * speed * mx;
         return new Vector2d(velX, velZ);
      }
   }

   public static enum Mode {
      Vanilla,
      NCP,
      Smart;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Vanilla, NCP, Smart};
      }
   }

   public static enum HopMode {
      Auto,
      Custom;

      // $FF: synthetic method
      private static HopMode[] $values() {
         return new HopMode[]{Auto, Custom};
      }
   }

   public static enum WebbedPause {
      Always,
      OnAir,
      None;

      // $FF: synthetic method
      private static WebbedPause[] $values() {
         return new WebbedPause[]{Always, OnAir, None};
      }
   }
}
