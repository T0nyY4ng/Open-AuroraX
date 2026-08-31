package espada.spacex.aurora.modules.movementplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.registry.tag.FluidTags;

public class JesusPlus extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<Double> bob;
   private final Setting<Boolean> toggle;
   private final Setting<Double> water_speed;
   private boolean move;
   private boolean inWater;
   private boolean isSlowed;

   public JesusPlus() {
      super(Aurora.MovementPlus, "Jesus+", "Better jesus");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.bob = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Bob force")).description("Use 0.005 or 0.1")).defaultValue(0.005).min((double)0.0F).sliderMax((double)1.0F).build());
      this.toggle = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Change speed")).description("")).build());
      this.water_speed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Water speed")).description("0.265 is generally better")).defaultValue(1.175).min((double)0.0F).sliderMax((double)2.0F).build());
      this.move = false;
      this.inWater = false;
      this.isSlowed = false;
   }

   @EventHandler
   public void onMove(PlayerMoveEvent event) {
      if (this.mc.player != null && this.mc.world != null) {
         if (this.mc.world.getBlockState(this.mc.player.getBlockPos().down()).getBlock() != Blocks.WATER && this.mc.world.getBlockState(this.mc.player.getBlockPos()).getBlock() != Blocks.WATER) {
            this.inWater = false;
         } else {
            if (!this.inWater) {
               this.isSlowed = false;
            }

            this.inWater = true;
         }

         if (this.mc.player.isTouchingWater() && !this.mc.player.isSubmergedInWater() || this.mc.player.isInLava() && !this.mc.player.isSubmergedIn(FluidTags.LAVA)) {
            ((IVec3d)this.mc.player.getVelocity()).setY((Double)this.bob.get());
            if ((Boolean)this.toggle.get() && (!this.mc.player.isInLava() || this.mc.player.isSubmergedIn(FluidTags.LAVA)) && !this.isSlowed) {
               double motion = (Double)this.water_speed.get();
               if (this.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                  motion *= 1.2 + (double)this.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() * 0.2;
               }

               if (this.mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                  motion /= 1.2 + (double)this.mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() * 0.2;
               }

               double forward = (double)this.mc.player.input.movementForward;
               double sideways = (double)this.mc.player.input.movementSideways;
               double yaw = this.getYaw(forward, sideways);
               double x = Math.cos(Math.toRadians(yaw + (double)90.0F));
               double z = Math.sin(Math.toRadians(yaw + (double)90.0F));
               if (this.move) {
                  ((IVec3d)event.movement).setXZ(motion * x, motion * z);
               } else {
                  ((IVec3d)event.movement).setXZ((double)0.0F, (double)0.0F);
               }
            }
         }
      }

   }

   @EventHandler
   private void OnRecieve(PacketEvent.Receive event) {
      if (event.packet instanceof PlayerPositionLookS2CPacket) {
         this.isSlowed = true;
      }

   }

   public void onActivate() {
      this.inWater = false;
   }

   private double getYaw(double f, double s) {
      double yaw = (double)this.mc.player.getYaw();
      if (f > (double)0.0F) {
         this.move = true;
         yaw += s > (double)0.0F ? (double)-45.0F : (s < (double)0.0F ? (double)45.0F : (double)0.0F);
      } else if (f < (double)0.0F) {
         this.move = true;
         yaw += s > (double)0.0F ? (double)-135.0F : (s < (double)0.0F ? (double)135.0F : (double)180.0F);
      } else {
         this.move = s != (double)0.0F;
         yaw += s > (double)0.0F ? (double)-90.0F : (s < (double)0.0F ? (double)90.0F : (double)0.0F);
      }

      return yaw;
   }
}
