package espada.spacex.aurora.modules.combatplus;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.pathing.goals.GoalRunAway;
import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.HoleType;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.modules.playerplus.Suicide;
import espada.spacex.aurora.utils.Hole;
import espada.spacex.aurora.utils.HoleUtils;
import espada.spacex.aurora.utils.MovementUtils;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.RotationUtils;
import espada.spacex.aurora.utils.RaksuTone.RaksuPath;
import espada.spacex.aurora.utils.RaksuTone.RaksuTone;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.component.DataComponentTypes;

public class Autofix extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgSurround;
   private final SettingGroup sgTarget;
   private final SettingGroup sgSuicide;
   private final SettingGroup sgRotations;
   private final SettingGroup sgEating;
   private final SettingGroup sgBaritone;
   private final SettingGroup sgRaksu;
   private final Setting<Boolean> autoMessage;
   private final Setting<String> onSpawn;
   private final Setting<Integer> spawnRadius;
   private final Setting<Boolean> baritone;
   private final Setting<Boolean> surround;
   private final Setting<Boolean> surroundMove;
   private final Setting<Boolean> antiCamp;
   private final Setting<Integer> antiCampSeconds;
   private final Setting<Boolean> antiBurrow;
   private final Setting<Integer> underY;
   private final Setting<Integer> yDiff;
   private final Setting<Boolean> suicide;
   private final Setting<Integer> totemAmount;
   private final Setting<Integer> crystalAmount;
   private final Setting<Integer> gappleAmount;
   private final Setting<Integer> expAmount;
   private final Setting<Integer> obsidianAmount;
   private final Setting<Boolean> eChests;
   private final Setting<Boolean> rotate;
   private final Setting<Boolean> goldenApple;
   private final Setting<Integer> gappleHealth;
   private final Setting<Boolean> chorus;
   private final Setting<Integer> chorusHealth;
   private final Setting<Integer> stuckTicks;
   private final Setting<Boolean> speedPotion;
   private final Setting<Integer> speedHealth;
   private final Setting<Boolean> assumeStep;
   private final Setting<Boolean> parkour;
   private final Setting<Double> stepCooldown;
   private final Setting<Double> rStepCooldown;
   public static final String desc = "A setting for baritone. Updated on module activation.";
   private PlayerEntity target;
   private boolean inRange;
   private int stuckTimer;
   private int eatingSlot;
   private BlockPos lastPos;
   private final Map<PlayerEntity, Camp> camps;
   private long lastStep;
   private long lastReverse;
   private boolean shouldSuicide;
   private long lastRespawn;
   private RaksuPath path;

   public Autofix() {
      super(Aurora.CombatPlus, "Auto PVP", "Follows people using baritone. Best for crystalpvp.cc");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgSurround = this.settings.createGroup("Surround");
      this.sgTarget = this.settings.createGroup("Target");
      this.sgSuicide = this.settings.createGroup("Suicide");
      this.sgRotations = this.settings.createGroup("Rotations");
      this.sgEating = this.settings.createGroup("Eating");
      this.sgBaritone = this.settings.createGroup("Baritone");
      this.sgRaksu = this.settings.createGroup("Raksutone");
      this.autoMessage = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Auto Message")).description("Sends 'On Spawn' message when you respawn.")).defaultValue(false)).build());
      this.onSpawn = this.sgGeneral.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)(new StringSetting.Builder()).name("On Spawn")).description("What message should be sent on respawn.")).defaultValue("/kit Blizzard")).build());
      this.spawnRadius = this.sgTarget.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Spawn Radius")).description("How far away you have to be to return to spawn if there is no target.")).defaultValue(50)).min(0).sliderRange(0, 500).build());
      this.baritone = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Baritone")).description("Moves using baritone. Should be true.")).defaultValue(true)).build());
      this.surround = this.sgSurround.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Surround")).description("Surrounds near the target.")).defaultValue(true)).build());
      this.surroundMove = this.sgSurround.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Surround Move")).description("Moves inside your surround to.")).defaultValue(false)).build());
      this.antiCamp = this.sgTarget.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Anti Camp")).description("Enables surround when close to target.")).defaultValue(false)).build());
      this.antiCampSeconds = this.sgTarget.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Anti Camp Time (s)")).description("How many seconds a player has to stand still to get ignored.")).defaultValue(30)).min(0).sliderRange(0, 1000).build());
      this.antiBurrow = this.sgTarget.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Anti Burrow")).description("Doesn't fight with players that are inside blocks.")).defaultValue(true)).build());
      this.underY = this.sgTarget.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Under Y")).description("Target has to be under this y.")).defaultValue(500)).min(0).sliderRange(0, 500).build());
      this.yDiff = this.sgTarget.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Y Difference")).description("Doesn't target players.")).defaultValue(500)).min(0).sliderRange(0, 500).build());
      this.suicide = this.sgSuicide.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Suicide")).description("Enables suicide when running out of items.")).defaultValue(false)).build());
      this.totemAmount = this.sgSuicide.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Totem Amount")).description("Suicides if there is under x amount of totems in inventory.")).defaultValue(0)).min(0).sliderRange(0, 16).build());
      this.crystalAmount = this.sgSuicide.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Crystal Amount")).description("Suicides if there is under x amount of crystals in inventory.")).defaultValue(0)).min(0).sliderRange(0, 256).build());
      this.gappleAmount = this.sgSuicide.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Gapple Amount")).description("Suicides if there is under x amount of gapples in inventory.")).defaultValue(0)).min(0).sliderRange(0, 256).build());
      this.expAmount = this.sgSuicide.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Exp Amount")).description("Suicides if there is under x amount of experience bottles in inventory.")).defaultValue(0)).min(0).sliderRange(0, 256).build());
      this.obsidianAmount = this.sgSuicide.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Obsidian Amount")).description("Suicides if there is under x amount of obsidian in inventory.")).defaultValue(0)).min(0).sliderRange(0, 256).build());
      this.eChests = this.sgSuicide.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Count E-Chests")).description("Counts ender chests as 8 obsidian.")).defaultValue(true)).build());
      this.rotate = this.sgRotations.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Rotate")).description("Stares at target enemy.")).defaultValue(true)).build());
      this.goldenApple = this.sgEating.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Golden Apple")).description("Eats golden apples when hp is under 'Gapple Health'.")).defaultValue(true)).build());
      this.gappleHealth = this.sgEating.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Gapple Health")).description("Check 'Golden Apple' description.")).defaultValue(35)).min(0).sliderRange(0, 36).build());
      this.chorus = this.sgEating.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Chorus")).description("Eats a chorus fruit when stuck.")).defaultValue(true)).build());
      this.chorusHealth = this.sgEating.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Chorus Health")).description("Only eats chorus fruit if above x hp.")).defaultValue(14)).min(0).sliderRange(0, 36).build());
      this.stuckTicks = this.sgEating.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Stuck Ticks")).description("Eats a chorus fruit after being stuck for x ticks.")).defaultValue(100)).min(0).sliderRange(0, 1000).build());
      this.speedPotion = this.sgEating.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Speed Potion")).description("Drinks a speed potion.")).defaultValue(true)).build());
      this.speedHealth = this.sgEating.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Speed Health")).description("Only allows drinking potions when above x hp.")).defaultValue(20)).min(0).sliderRange(0, 36).build());
      this.assumeStep = this.sgBaritone.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Baritone Step")).description("A setting for baritone. Updated on module activation.")).defaultValue(true)).build());
      this.parkour = this.sgBaritone.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Baritone Parkour")).description("A setting for baritone. Updated on module activation.")).defaultValue(true)).build());
      this.stepCooldown = this.sgRaksu.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Step Cooldown")).description("How many seconds to wait between steps.")).defaultValue(0.1).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).build());
      this.rStepCooldown = this.sgRaksu.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Reverse Step Cooldown")).description("How many seconds to wait between reverse steps.")).defaultValue(0.1).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).build());
      this.target = null;
      this.inRange = false;
      this.stuckTimer = 0;
      this.eatingSlot = -1;
      this.lastPos = null;
      this.camps = new HashMap();
      this.lastStep = 0L;
      this.lastReverse = 0L;
      this.shouldSuicide = false;
      this.lastRespawn = 0L;
      this.path = null;
   }

   public void onActivate() {
      this.camps.clear();
      this.settings();
   }

   public void onDeactivate() {
      this.command("stop");
   }

   @EventHandler(
      priority = 200
   )
   private void onPacket(PacketEvent.Receive event) {
      if ((Boolean)this.autoMessage.get() && event.packet instanceof PlayerRespawnS2CPacket) {
         ChatUtils.sendPlayerMsg((String)this.onSpawn.get());
      }

   }

   @EventHandler(
      priority = 200
   )
   private void onMove(PlayerMoveEvent event) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (this.inRange && !this.shouldSuicide) {
         if ((Boolean)this.surroundMove.get()) {
            BlockPos walkPos = this.getSurroundWalk();
            if (walkPos != null) {
               this.move(event.movement, walkPos.toCenterPos());
            }
         }

      } else {
         if (this.lastPos == null) {
            this.lastPos = mc.player.getBlockPos();
         }

         if (mc.player.getBlockPos().equals(this.lastPos)) {
            ++this.stuckTimer;
         } else {
            this.stuckTimer = 0;
         }

         this.lastPos = mc.player.getBlockPos();
         if (this.path != null && !this.path.path.isEmpty()) {
            this.move(event.movement, ((RaksuPath.Movement)this.path.path.get(0)).pos().toCenterPos());
         }
      }
   }

   @EventHandler(
      priority = 200
   )
   private void onRender(Render3DEvent event) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player != null && mc.world != null) {
         mc.world.getPlayers().forEach((player) -> {
            if (this.camps.containsKey(player)) {
               Camp camp = (Camp)this.camps.get(player);
               if (player.getBlockPos().equals(camp.pos)) {
                  return;
               }

               this.camps.remove(player);
            }

            this.camps.put(player, new Camp(player.getBlockPos(), System.currentTimeMillis()));
         });
         if (mc.currentScreen instanceof DeathScreen && System.currentTimeMillis() - this.lastRespawn > 1000L) {
            mc.player.requestRespawn();
            this.lastRespawn = System.currentTimeMillis();
         }

         this.updateTarget();
         if (this.target == null) {
            if ((Math.abs(mc.player.getBlockX()) > (Integer)this.spawnRadius.get() || Math.abs(mc.player.getBlockZ()) > (Integer)this.spawnRadius.get()) && (Boolean)this.baritone.get()) {
               BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalNear(new BlockPos(0, 2, 0), 5));
            }

            this.stuckTimer = 0;
         } else {
            this.shouldSuicide = this.updateSuicide();
            if (this.shouldSuicide) {
               if (!meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(Suicide.class)) {
                  ((Suicide)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Suicide.class)).toggle();
               }
            } else if (meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(Suicide.class)) {
               ((Suicide)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Suicide.class)).toggle();
            }

            this.eatUpdate();
            if ((Boolean)this.rotate.get() && this.target != null) {
               Managers.ROTATION.start(this.target.getBlockPos(), RotationUtils.getYaw(mc.player.getEyePos(), this.target.getEyePos()), RotationType.Other, 200L);
            }

            if (this.inRange && (Boolean)this.surround.get() && !this.shouldSuicide) {
               if (!meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(SurroundPlus.class)) {
                  ((SurroundPlus)meteordevelopment.meteorclient.systems.modules.Modules.get().get(SurroundPlus.class)).toggle();
               }
            } else if (meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(SurroundPlus.class)) {
               ((SurroundPlus)meteordevelopment.meteorclient.systems.modules.Modules.get().get(SurroundPlus.class)).toggle();
            }

            if (this.shouldSuicide) {
               if ((Boolean)this.baritone.get()) {
                  BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalRunAway((double)50.0F, new BlockPos[]{this.target.getBlockPos()}));
               } else {
                  this.path = RaksuTone.runAway(3, this.target.getBlockPos());
               }

            } else {
               if (this.inRange || !(mc.player.getY() > (double)100.0F) && !(Boolean)this.baritone.get()) {
                  BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath((Goal)null);
                  if (!(Boolean)this.baritone.get()) {
                     this.path = RaksuTone.getPath(3, this.target.getBlockPos());
                  }
               } else {
                  BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalNear(this.target.getBlockPos(), 3));
                  this.path = null;
               }

            }
         }
      }
   }

   private void move(Vec3d movement, Vec3d vec) {
      MinecraftClient mc = MinecraftClient.getInstance();
      MovementUtils.moveTowards(movement, 0.2873, vec, (double)(System.currentTimeMillis() - this.lastStep) > (Double)this.stepCooldown.get() * (double)1000.0F ? 2 : 0, (double)(System.currentTimeMillis() - this.lastReverse) > (Double)this.rStepCooldown.get() * (double)1000.0F ? 3 : 0);
      if (movement.y >= 0.6) {
         this.lastStep = System.currentTimeMillis();
      }

      if (movement.y <= -0.6) {
         this.lastReverse = System.currentTimeMillis();
      }

   }

   private BlockPos getSurroundWalk() {
      MinecraftClient mc = MinecraftClient.getInstance();
      Hole hole = this.getHole(mc.player.getBlockPos());
      if (hole == null) {
         return null;
      } else {
         BlockPos closest = null;

         for(BlockPos pos : hole.positions) {
            if (closest == null || this.target != null && pos.toCenterPos().distanceTo(this.target.getPos()) < closest.toCenterPos().distanceTo(this.target.getPos())) {
               closest = pos;
            }
         }

         return closest;
      }
   }

   private boolean isCamper(PlayerEntity player) {
      return (Boolean)this.antiCamp.get() && this.camps.containsKey(player) && System.currentTimeMillis() - ((Camp)this.camps.get(player)).time > (long)((Integer)this.antiCampSeconds.get() * 1000);
   }

   private void eatUpdate() {
      MinecraftClient mc = MinecraftClient.getInstance();
      Predicate<ItemStack> food = this.getFood();
      if (food == null) {
         if (this.eatingSlot > -1) {
            mc.options.useKey.setPressed(false);
         }

      } else {
         int slot = InvUtils.findInHotbar(food).slot();
         if (Managers.HOLDING.slot != slot) {
            InvUtils.swap(slot, false);
         }

         if (this.eatingSlot != slot || !mc.player.isUsingItem()) {
            this.eatingSlot = slot;
            mc.options.useKey.setPressed(true);
            Utils.rightClick();
         }

      }
   }

   private Predicate<ItemStack> getFood() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (this.shouldSuicide) {
         return null;
      } else {
         float hp = mc.player.getHealth() + mc.player.getAbsorptionAmount();
         if ((Boolean)this.speedPotion.get() && hp >= (float)(Integer)this.speedHealth.get() && this.available(this::isSpeed) && !mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            return this::isSpeed;
         } else if ((Boolean)this.chorus.get() && this.stuckTimer > (Integer)this.stuckTicks.get() && hp >= (float)(Integer)this.chorusHealth.get() && this.available((i) -> i.getItem() == Items.CHORUS_FRUIT)) {
            return (i) -> i.getItem() == Items.CHORUS_FRUIT;
         } else {
            return (Boolean)this.goldenApple.get() && hp <= (float)(Integer)this.gappleHealth.get() && this.available(OLEPOSSUtils::isGapple) ? OLEPOSSUtils::isGapple : null;
         }
      }
   }

   private boolean available(Predicate<ItemStack> predicate) {
      return InvUtils.findInHotbar(predicate).found();
   }

   private boolean isSpeed(ItemStack stack) {
      PotionContentsComponent potionContents = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
      if (potionContents != null) {
         for(StatusEffectInstance i : potionContents.getEffects()) {
            if (i.getEffectType() == StatusEffects.SPEED) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean inRange() {
      MinecraftClient mc = MinecraftClient.getInstance();
      Goal goal = BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().getGoal();
      return goal == null ? Math.abs(mc.player.getBlockX() - this.target.getBlockX()) < 5 && Math.abs(mc.player.getBlockZ() - this.target.getBlockZ()) < 5 && Math.abs(mc.player.getBlockY() - this.target.getBlockY()) < 5 : goal.isInGoal(mc.player.getBlockPos());
   }

   private void command(String command) {
      BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute(command);
   }

   private boolean updateSuicide() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (!(Boolean)this.suicide.get()) {
         return false;
      } else if (this.amountOf((i) -> i.getItem() == Items.END_CRYSTAL) <= 0) {
         return false;
      } else if (this.amountOf(OLEPOSSUtils::isGapple) <= (Integer)this.gappleAmount.get()) {
         return true;
      } else if (this.amountOf((i) -> i.getItem() == Items.OBSIDIAN) + ((Boolean)this.eChests.get() ? this.amountOf((i) -> i.getItem() == Items.ENDER_CHEST) * 8 : 0) <= (Integer)this.obsidianAmount.get()) {
         return true;
      } else if (this.amountOf((i) -> i.getItem() == Items.END_CRYSTAL) <= (Integer)this.crystalAmount.get()) {
         return true;
      } else if (this.amountOf((i) -> i.getItem() == Items.EXPERIENCE_BOTTLE) <= (Integer)this.expAmount.get()) {
         return true;
      } else {
         return this.amountOf((i) -> i.getItem() == Items.TOTEM_OF_UNDYING) <= (Integer)this.totemAmount.get();
      }
   }

   private int amountOf(Predicate<ItemStack> predicate) {
      MinecraftClient mc = MinecraftClient.getInstance();
      int a = 0;

      for(int i = 0; i < mc.player.getInventory().size(); ++i) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (predicate.test(stack)) {
            a += stack.getCount();
         }
      }

      return a;
   }

   private void updateTarget() {
      MinecraftClient mc = MinecraftClient.getInstance();
      PlayerEntity closest = null;

      for(PlayerEntity pl : mc.world.getPlayers()) {
         if (pl != mc.player && !pl.isSpectator() && !Friends.get().isFriend(pl) && !(pl.getHealth() <= 0.0F) && (!(Boolean)this.antiBurrow.get() || !OLEPOSSUtils.collidable(pl.getBlockPos())) && !this.isCamper(pl) && pl.getBlockY() <= (Integer)this.underY.get() && pl.getBlockY() - mc.player.getBlockY() <= (Integer)this.yDiff.get() && (closest == null || mc.player.distanceTo(closest) > mc.player.distanceTo(pl))) {
            closest = pl;
         }
      }

      this.target = closest;
      this.inRange = this.target != null && this.inRange();
   }

   private void settings() {
      BaritoneAPI.getSettings().assumeStep.value = this.assumeStep.get();
      BaritoneAPI.getSettings().allowParkour.value = this.parkour.get();
      BaritoneAPI.getSettings().allowBreak.value = false;
      BaritoneAPI.getSettings().maxFallHeightNoWater.value = 1000;
      BaritoneAPI.getSettings().allowPlace.value = false;
      BaritoneAPI.getSettings().allowParkourPlace.value = false;
      BaritoneAPI.getSettings().logger.value = (text) -> {
      };
   }

   private Hole getHole(BlockPos pos) {
      if (HoleUtils.getHole(pos, 1).type == HoleType.Single) {
         return null;
      } else if (HoleUtils.getHole(pos, 1).type == HoleType.DoubleX) {
         return HoleUtils.getHole(pos, 1);
      } else if (HoleUtils.getHole(pos.add(-1, 0, 0), 1).type == HoleType.DoubleX) {
         return HoleUtils.getHole(pos.add(-1, 0, 0), 1);
      } else if (HoleUtils.getHole(pos, 1).type == HoleType.DoubleZ) {
         return HoleUtils.getHole(pos, 1);
      } else if (HoleUtils.getHole(pos.add(0, 0, -1), 1).type == HoleType.DoubleZ) {
         return HoleUtils.getHole(pos.add(0, 0, -1), 1);
      } else if (HoleUtils.getHole(pos, 1).type == HoleType.Quad) {
         return HoleUtils.getHole(pos, 1);
      } else if (HoleUtils.getHole(pos.add(-1, 0, -1), 1).type == HoleType.Quad) {
         return HoleUtils.getHole(pos.add(-1, 0, -1), 1);
      } else if (HoleUtils.getHole(pos.add(-1, 0, 0), 1).type == HoleType.Quad) {
         return HoleUtils.getHole(pos.add(-1, 0, 0), 1);
      } else {
         return HoleUtils.getHole(pos.add(0, 0, -1), 1).type == HoleType.Quad ? HoleUtils.getHole(pos.add(0, 0, -1), 1) : null;
      }
   }

   private static record Camp(BlockPos pos, long time) {
   }
}
