package espada.spacex.aurora.modules.playerplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.managers.Managers;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class AntiAim extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgIgnore;
   private final Setting<Modes> mode;
   private final Setting<Double> enemyRange;
   private final Setting<Double> spinSpeed;
   private final Setting<Boolean> rYaw;
   private final Setting<Boolean> rPitch;
   private final Setting<Integer> csgoPitch;
   private final Setting<Double> csDelay;
   private final Setting<Integer> yaw;
   private final Setting<Integer> pitch;
   private final Setting<Boolean> bowMode;
   private final Setting<Boolean> encMode;
   private final Setting<Boolean> iYaw;
   private final Setting<List<Item>> yItems;
   private final Setting<Boolean> iPitch;
   private final Setting<List<Item>> pItems;
   private final Random r;
   private double spinYaw;
   private double csTick;
   private double csYaw;
   private double csPitch;

   public AntiAim() {
      super(Aurora.PlayerPlus, "Anti Aim", "Funi conter stik module.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgIgnore = this.settings.createGroup("Ignore");
      this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mode")).description(".")).defaultValue(AntiAim.Modes.Custom)).build());
      this.enemyRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Enemy Range")).description("Looks at players in the range.")).defaultValue((double)20.0F).range((double)0.0F, (double)1000.0F).sliderMin((double)0.0F).visible(() -> ((Modes)this.mode.get()).equals(AntiAim.Modes.Enemy))).sliderMax((double)1000.0F).build());
      this.spinSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Spin Speed")).description("How many degrees should be turned every tick.")).defaultValue((double)5.0F).min((double)0.0F).sliderMin((double)0.0F).visible(() -> ((Modes)this.mode.get()).equals(AntiAim.Modes.Spin))).sliderMax((double)100.0F).build());
      this.rYaw = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Random Yaw")).description("Sets yaw to random value.")).defaultValue(true)).visible(() -> ((Modes)this.mode.get()).equals(AntiAim.Modes.CSGO))).build());
      this.rPitch = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Random Pitch")).description("Sets pitch to random value.")).defaultValue(false)).visible(() -> ((Modes)this.mode.get()).equals(AntiAim.Modes.CSGO))).build());
      this.csgoPitch = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("CS Pitch")).description("Sets pitch to this")).defaultValue(90)).range(-90, 90).sliderMin(-90).visible(() -> ((Modes)this.mode.get()).equals(AntiAim.Modes.CSGO) && !(Boolean)this.rPitch.get())).sliderMax(90).build());
      this.csDelay = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("CSGO Delay")).description("Tick delay between csgo rotation update.")).defaultValue((double)5.0F).range((double)0.0F, (double)100.0F).sliderMin((double)0.0F).visible(() -> ((Modes)this.mode.get()).equals(AntiAim.Modes.CSGO))).sliderMax((double)100.0F).build());
      this.yaw = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Yaw")).description("Sets yaw to this")).defaultValue(0)).range(-180, 180).sliderMin(-180).visible(() -> ((Modes)this.mode.get()).equals(AntiAim.Modes.Custom))).sliderMax(180).build());
      this.pitch = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Pitch")).description("Sets pitch to this")).defaultValue(90)).range(-90, 90).sliderMin(-90).sliderMax(90).visible(() -> ((Modes)this.mode.get()).equals(AntiAim.Modes.Custom))).build());
      this.bowMode = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Look Up With Bow")).description("Looks up while holding a bow.")).defaultValue(true)).build());
      this.encMode = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Look Down With Exp")).description("Looks down while holding experience bottles.")).defaultValue(true)).build());
      this.iYaw = this.sgIgnore.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Ignore Yaw")).description("Doesn't change yaw when holding specific items.")).defaultValue(true)).build());
      this.yItems = this.sgIgnore.add(((ItemListSetting.Builder)((ItemListSetting.Builder)(new ItemListSetting.Builder()).name("Ignore Yaw Items")).description("Ignores yaw rotations when holding these items.")).defaultValue(new Item[]{Items.ENDER_PEARL, Items.BOW, Items.EXPERIENCE_BOTTLE}).build());
      this.iPitch = this.sgIgnore.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Ignore Pitch")).description("Doesn't change pitch when holding specific items.")).defaultValue(true)).build());
      this.pItems = this.sgIgnore.add(((ItemListSetting.Builder)((ItemListSetting.Builder)(new ItemListSetting.Builder()).name("Ignore Pitch items")).description("Ignores pitch rotations when holding these items.")).defaultValue(new Item[]{Items.ENDER_PEARL, Items.BOW, Items.EXPERIENCE_BOTTLE}).build());
      this.r = new Random();
      this.csTick = (double)0.0F;
   }

   public void onActivate() {
      super.onActivate();
      this.spinYaw = (double)0.0F;
   }

   @EventHandler(
      priority = 200
   )
   private void onTick(TickEvent.Pre event) {
      if (this.mc.player != null && this.mc.world != null) {
         if (this.mode.get() == AntiAim.Modes.CSGO) {
            if (this.csTick <= (double)0.0F) {
               this.csTick += (Double)this.csDelay.get();
               this.csYaw = (Boolean)this.rYaw.get() ? (double)this.r.nextInt(-180, 180) : (double)this.mc.player.getYaw();
               this.csPitch = (Boolean)this.rPitch.get() ? (double)this.r.nextInt(-90, 90) : (double)(Integer)this.csgoPitch.get();
            } else {
               --this.csTick;
            }
         }

         Item item = this.mc.player.getMainHandStack().getItem();
         boolean ignoreYaw = ((List)this.yItems.get()).contains(item) && (Boolean)this.iYaw.get();
         boolean ignorePitch = ((List)this.pItems.get()).contains(item) && (Boolean)this.iPitch.get();
         double var10000;
         if (ignoreYaw) {
            var10000 = (double)this.mc.player.getYaw();
         } else {
            switch (((Modes)this.mode.get()).ordinal()) {
               case 0 -> var10000 = this.closestYaw();
               case 1 -> var10000 = this.getSpinYaw();
               case 2 -> var10000 = this.csYaw;
               case 3 -> var10000 = (double)(Integer)this.yaw.get();
               default -> throw new MatchException((String)null, (Throwable)null);
            }
         }

         double y = var10000;
         if (item == Items.EXPERIENCE_BOTTLE && (Boolean)this.encMode.get()) {
            var10000 = (double)90.0F;
         } else if (item == Items.BOW && (Boolean)this.bowMode.get()) {
            var10000 = (double)-90.0F;
         } else if (ignorePitch) {
            var10000 = (double)this.mc.player.getPitch();
         } else {
            switch (((Modes)this.mode.get()).ordinal()) {
               case 0 -> var10000 = this.closestPitch();
               case 1 -> var10000 = (double)0.0F;
               case 2 -> var10000 = this.csPitch;
               case 3 -> var10000 = (double)(Integer)this.pitch.get();
               default -> throw new MatchException((String)null, (Throwable)null);
            }
         }

         double p = var10000;
         Managers.ROTATION.start(y, p, (double)this.priority, RotationType.Other, (long)Objects.hash(new Object[]{this.name + "look"}));
      }

   }

   public String getInfoString() {
      return ((Modes)this.mode.get()).name();
   }

   private double closestYaw() {
      PlayerEntity closest = this.getClosest();
      return closest != null ? Rotations.getYaw(closest) : (double)this.mc.player.getYaw();
   }

   private double closestPitch() {
      PlayerEntity closest = this.getClosest();
      return closest != null ? Rotations.getPitch(closest) : (double)this.mc.player.getPitch();
   }

   private double getSpinYaw() {
      this.spinYaw += (Double)this.spinSpeed.get();
      return this.spinYaw;
   }

   private PlayerEntity getClosest() {
      PlayerEntity closest = null;

      for(PlayerEntity pl : this.mc.world.getPlayers()) {
         if (pl != this.mc.player && !Friends.get().isFriend(pl)) {
            if (closest == null) {
               closest = pl;
            }

            double distance = this.mc.player.getPos().distanceTo(pl.getPos());
            if (!(distance > (Double)this.enemyRange.get()) && distance < closest.getPos().distanceTo(this.mc.player.getPos())) {
               closest = pl;
            }
         }
      }

      return closest;
   }

   public static enum Modes {
      Enemy,
      Spin,
      CSGO,
      Custom;

      // $FF: synthetic method
      private static Modes[] $values() {
         return new Modes[]{Enemy, Spin, CSGO, Custom};
      }
   }
}
