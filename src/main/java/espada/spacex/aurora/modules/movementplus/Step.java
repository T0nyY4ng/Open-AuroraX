package espada.spacex.aurora.modules.movementplus;

import com.google.common.collect.Streams;
import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.pathing.PathManagers;
import espada.spacex.aurora.utils.meteor.BOEntityUtils;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.attribute.EntityAttributes;

public class Step extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<Boolean> pauseWeb;
   private final Setting<Boolean> earth;
   public final Setting<Double> height;
   private final Setting<ActiveWhen> activeWhen;
   private final Setting<Boolean> safeStep;
   private final Setting<Integer> stepHealth;
   public float prevStepHeight;
   public boolean prevPathManagerStep;

   public Step() {
      super(Aurora.MovementPlus, "AutoStep", "Allows you to walk up full blocks instantly.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.pauseWeb = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause On Web")).description("Pause when player is stuck by cobweb.")).defaultValue(false)).build());
      this.earth = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("3arthhck-Mode")).description("only on ground to step")).defaultValue(false)).build());
      this.height = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("height")).description("Step height.")).defaultValue((double)1.0F).min((double)0.0F).build());
      this.activeWhen = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("active-when")).description("Step is active when you meet these requirements.")).defaultValue(Step.ActiveWhen.Always)).build());
      this.safeStep = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("safe-step")).description("Doesn't let you step out of a hole if you are low on health or there is a crystal nearby.")).defaultValue(false)).build());
      SettingGroup var10001 = this.sgGeneral;
      IntSetting.Builder var10002 = ((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("step-health")).description("The health you stop being able to step at.")).defaultValue(5)).range(1, 36).sliderRange(1, 36);
      Setting<Boolean> var10003 = this.safeStep;
      Objects.requireNonNull(var10003);
      this.stepHealth = var10001.add(((IntSetting.Builder)var10002.visible(var10003::get)).build());
   }

   public void onActivate() {
      this.prevStepHeight = (float)this.mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT).getBaseValue();
      this.prevPathManagerStep = (Boolean)PathManagers.get().getSettings().getStep().get();
      PathManagers.get().getSettings().getStep().set(true);
   }

   @EventHandler
   private void onTick(TickEvent.Post event) {
      if (!(Boolean)this.pauseWeb.get() || !BOEntityUtils.isWebbed(this.mc.player)) {
         boolean work = this.activeWhen.get() == Step.ActiveWhen.Always || this.activeWhen.get() == Step.ActiveWhen.Sneaking && this.mc.player.isSneaking() || this.activeWhen.get() == Step.ActiveWhen.NotSneaking && !this.mc.player.isSneaking();
         this.mc.player.setBoundingBox(this.mc.player.getBoundingBox().offset((double)0.0F, (double)1.0F, (double)0.0F));
         if (!work || (Boolean)this.safeStep.get() && (!(this.getHealth() > (float)(Integer)this.stepHealth.get()) || !((double)this.getHealth() - this.getExplosionDamage() > (double)(Integer)this.stepHealth.get()))) {
            this.mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue((double)this.prevStepHeight);
         } else {
            this.mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue((Double)this.height.get());
         }

         if (!(Boolean)this.earth.get() || !this.mc.player.checkFallFlying()) {
            this.mc.player.setBoundingBox(this.mc.player.getBoundingBox().offset((double)0.0F, (double)-1.0F, (double)0.0F));
         }
      }
   }

   public void onDeactivate() {
      this.mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue((double)this.prevStepHeight);
      PathManagers.get().getSettings().getStep().set(this.prevPathManagerStep);
   }

   private float getHealth() {
      return this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount();
   }

   private double getExplosionDamage() {
      Optional<EndCrystalEntity> crystal = Streams.stream(this.mc.world.getEntities()).filter((entity) -> entity instanceof EndCrystalEntity).filter(Entity::isAlive).max(Comparator.comparingDouble((o) -> (double)DamageUtils.crystalDamage(this.mc.player, o.getPos()))).map((entity) -> (EndCrystalEntity)entity);
      return (double)(Float)crystal.map((endCrystalEntity) -> DamageUtils.crystalDamage(this.mc.player, endCrystalEntity.getPos())).orElse(0.0F);
   }

   public static enum ActiveWhen {
      Always,
      Sneaking,
      NotSneaking;

      // $FF: synthetic method
      private static ActiveWhen[] $values() {
         return new ActiveWhen[]{Always, Sneaking, NotSneaking};
      }
   }
}
