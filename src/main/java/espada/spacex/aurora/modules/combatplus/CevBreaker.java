package espada.spacex.aurora.modules.combatplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.mixins.IClientPlayerInteractionManager;
import espada.spacex.aurora.utils.BOBlockUtil;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.meteor.BOEntityUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import org.joml.Vector3d;

public class CevBreaker extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgBreaking;
   private final SettingGroup sgPause;
   private final SettingGroup sgSwing;
   private final SettingGroup sgRenderPlace;
   private final SettingGroup sgRenderBreak;
   private final SettingGroup sgNone;
   private final Setting<Boolean> toggleModules;
   private final Setting<Boolean> toggleBack;
   private final Setting<List<Module>> modules;
   private final Setting<SwitchMode> switchMode;
   private final Setting<Mode> mode;
   private final Setting<Boolean> smartDelay;
   private final Setting<Integer> switchDelay;
   private final Setting<Double> pauseAtHealth;
   private final Setting<Boolean> eatPause;
   private final Setting<Boolean> swing;
   private final Setting<SwingHand> placeHand;
   private final Setting<Boolean> renderPlace;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<SettingColor> lineColor;
   private final Setting<SettingColor> sideColor;
   private final Setting<Boolean> renderBreak;
   private final Setting<ShapeMode> breakShapeMode;
   private final Setting<SettingColor> breakLineColor;
   private final Setting<SettingColor> breakSideColor;
   private final Setting<SettingColor> endBreakLineColor;
   private final Setting<SettingColor> endBreakSideColor;
   private final Setting<Boolean> renderProgress;
   private final Setting<Double> scale;
   private final Setting<SettingColor> miningColor;
   private final Setting<SettingColor> endColor;
   private BlockPos blockPos;
   private PlayerEntity target;
   private boolean startedYet;
   boolean pause;
   private double progress;
   private int switchDelayLeft;
   private int timer;
   private int breakDelayLeft;
   private final List<PlayerEntity> blacklisted;
   private final List<EndCrystalEntity> crystals;
   private final List<Render> renderPlacing;
   public ArrayList<Module> toActivate;

   public CevBreaker() {
      super(Aurora.CombatPlus, "Cev Breaker", "Break crystals over a ppl's head to deal massive damage!");
      this.sgGeneral = this.settings.createGroup("General");
      this.sgBreaking = this.settings.createGroup("Breaking");
      this.sgPause = this.settings.createGroup("Pause");
      this.sgSwing = this.settings.createGroup("Swing");
      this.sgRenderPlace = this.settings.createGroup("Render Place");
      this.sgRenderBreak = this.settings.createGroup("Render Break");
      this.sgNone = this.settings.createGroup("");
      this.toggleModules = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("toggle-modules")).description("Turn off other modules when Cev Breaker is activated.")).defaultValue(false)).build());
      SettingGroup var10001 = this.sgGeneral;
      BoolSetting.Builder var10002 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("toggle-back-on")).description("Turn the modules back on when Cev Breaker is deactivated.")).defaultValue(false);
      Setting<Boolean> var10003 = this.toggleModules;
      Objects.requireNonNull(var10003);
      this.toggleBack = var10001.add(((BoolSetting.Builder)var10002.visible(var10003::get)).build());
      var10001 = this.sgGeneral;
      ModuleListSetting.Builder var2 = (ModuleListSetting.Builder)((ModuleListSetting.Builder)(new ModuleListSetting.Builder()).name("modules")).description("Which modules to toggle.");
      var10003 = this.toggleModules;
      Objects.requireNonNull(var10003);
      this.modules = var10001.add(((ModuleListSetting.Builder)var2.visible(var10003::get)).build());
      this.switchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("How to switch to obsidian.")).defaultValue(CevBreaker.SwitchMode.Silent)).build());
      this.mode = this.sgBreaking.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mode")).description("Which mode to use for breaking the obsidian.")).defaultValue(CevBreaker.Mode.Packet)).build());
      this.smartDelay = this.sgBreaking.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Smart Delay")).description("Waits until the target can get damaged again with breaking the block.")).defaultValue(false)).visible(() -> this.mode.get() == CevBreaker.Mode.Instant)).build());
      this.switchDelay = this.sgBreaking.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Switch Delay")).description("How many ticks to wait before hitting an entity after switching hotbar slots.")).defaultValue(1)).range(0, 20).sliderRange(0, 20).visible(() -> ((Mode)this.mode.get()).equals(CevBreaker.Mode.Packet))).build());
      this.pauseAtHealth = this.sgPause.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Pause Health")).description("Pauses when you go below a certain health.")).defaultValue((double)5.0F).min((double)0.0F).build());
      this.eatPause = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause On Eat")).description("Pauses Crystal Aura when eating.")).defaultValue(true)).build());
      this.swing = this.sgSwing.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Swing")).description("Renders your swing client-side.")).defaultValue(true)).build());
      this.placeHand = this.sgSwing.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand)).build());
      this.renderPlace = this.sgRenderPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Render Place")).description("Renders the block where it is placed.")).defaultValue(true)).build());
      this.shapeMode = this.sgRenderPlace.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Sides)).build());
      this.lineColor = this.sgRenderPlace.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 255)).visible(() -> (Boolean)this.renderPlace.get() && (((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Lines) || ((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Both)))).build());
      this.sideColor = this.sgRenderPlace.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 50)).visible(() -> (Boolean)this.renderPlace.get() && (((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Sides) || ((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Both)))).build());
      this.renderBreak = this.sgRenderBreak.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Render Break")).description("Renders the block where it is breaking.")).defaultValue(true)).build());
      this.breakShapeMode = this.sgRenderBreak.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
      this.breakLineColor = this.sgRenderBreak.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("COLOR")).defaultValue(new SettingColor(255, 0, 0, 100)).build());
      this.breakSideColor = this.sgRenderBreak.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("COLOR")).defaultValue(new SettingColor(255, 0, 0, 100)).build());
      this.endBreakLineColor = this.sgRenderPlace.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("End Line Color")).description("COLOR")).defaultValue(new SettingColor(0, 255, 0, 0)).visible(() -> (Boolean)this.renderPlace.get() && (((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Lines) || ((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Both)))).build());
      this.endBreakSideColor = this.sgRenderPlace.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("End Side Color")).description("COLOR")).defaultValue(new SettingColor(0, 255, 0, 50)).visible(() -> (Boolean)this.renderPlace.get() && (((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Sides) || ((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Both)))).build());
      this.renderProgress = this.sgRenderBreak.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Render Progress")).description("Renders the progress of breaking block.")).defaultValue(true)).visible(() -> !((Mode)this.mode.get()).equals(CevBreaker.Mode.Instant))).build());
      this.scale = this.sgRenderBreak.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Scale")).description("The scale of rendered text")).defaultValue((double)1.5F).sliderRange(0.01, (double)3.0F).visible(() -> !((Mode)this.mode.get()).equals(CevBreaker.Mode.Instant) && (Boolean)this.renderProgress.get())).build());
      this.miningColor = this.sgRenderBreak.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Mining Color")).description("The text color when obsidian is mining.")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.endColor = this.sgRenderBreak.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("End Color")).description("The text color when obsidian is complete mining.")).defaultValue(new SettingColor(106, 255, 78, 255)).build());
      this.blockPos = null;
      this.pause = false;
      this.blacklisted = new ArrayList();
      this.crystals = new ArrayList();
      this.renderPlacing = new ArrayList();
   }

   @EventHandler
   public void onActivate() {
      this.target = null;
      this.startedYet = false;
      this.switchDelayLeft = 0;
      this.timer = 0;
      this.blacklisted.clear();
      this.toActivate = new ArrayList();
      if ((Boolean)this.toggleModules.get() && !((List<Module>)(List<?>)this.modules.get()).isEmpty() && this.mc.world != null && this.mc.player != null) {
         for(Module module : (List<Module>)(List<?>)this.modules.get()) {
            if (module.isActive()) {
               module.toggle();
               this.toActivate.add(module);
            }
         }
      }

   }

   public void onDeactivate() {
      if ((Boolean)this.toggleBack.get() && !this.toActivate.isEmpty() && this.mc.world != null && this.mc.player != null) {
         for(Module module : this.toActivate) {
            if (!module.isActive()) {
               module.toggle();
            }
         }
      }

   }

   @EventHandler
   private void onTick(TickEvent.Pre event) {
      if (Utils.canUpdate()) {
         --this.switchDelayLeft;
         --this.breakDelayLeft;
         --this.timer;
         int crystalSlot = InvUtils.findInHotbar(new Item[]{Items.END_CRYSTAL}).slot();
         int obsidianSlot = InvUtils.findInHotbar(new Item[]{Items.OBSIDIAN}).slot();
         int pickSlot = InvUtils.findInHotbar(new Item[]{Items.NETHERITE_PICKAXE}).slot();
         pickSlot = pickSlot == -1 ? InvUtils.findInHotbar(new Item[]{Items.DIAMOND_PICKAXE}).slot() : pickSlot;
         if ((crystalSlot != -1 || this.mc.player.getOffHandStack().getItem() instanceof EndCrystalItem) && obsidianSlot != -1 && pickSlot != -1) {
            this.getEntities();
            if (this.target == null) {
               this.toggle();
            } else if ((!(Boolean)this.eatPause.get() || !this.mc.player.isUsingItem()) && !((double)PlayerUtils.getTotalHealth() <= (Double)this.pauseAtHealth.get())) {
               this.pause = false;
               this.blockPos = this.getPlacePos(this.target);
               if (this.blockPos != null) {
                  BlockState blockState = this.mc.world.getBlockState(this.blockPos);
                  boolean crystalThere = false;

                  for(EndCrystalEntity crystal : this.crystals) {
                     if (crystal.getBlockPos().add(0, -1, 0).equals(this.blockPos)) {
                        crystalThere = true;
                        break;
                     }
                  }

                  if (!blockState.isOf(Blocks.OBSIDIAN) && !crystalThere && (this.mc.player.getMainHandStack().getItem().equals(Items.OBSIDIAN) || this.switchDelayLeft <= 0)) {
                     if (!BlockUtils.canPlace(this.blockPos)) {
                        this.blacklisted.add(this.target);
                        this.getEntities();
                        if (this.target == null) {
                           this.toggle();
                        }

                        return;
                     }

                     if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                        Managers.ROTATION.start(this.blockPos, this.progress, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}));
                     }

                     FindItemResult obsidian = !((SwitchMode)this.switchMode.get()).equals(CevBreaker.SwitchMode.InvSwitch) && !((SwitchMode)this.switchMode.get()).equals(CevBreaker.SwitchMode.PickSilent) ? InvUtils.findInHotbar(new Item[]{Items.OBSIDIAN}) : InvUtils.find(new Item[]{Items.OBSIDIAN});
                     switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                        case 0 -> InvUtils.swap(obsidian.slot(), true);
                        case 1 -> BOInvUtils.invSwitch(obsidian.slot());
                        case 2 -> BOInvUtils.pickSwitch(obsidian.slot());
                     }

                     for(BlockPos placePos : this.getValid(this.blockPos)) {
                        PlaceData data = SettingUtils.getPlaceData(placePos);
                        this.placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());
                     }

                     switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                        case 0 -> InvUtils.swapBack();
                        case 1 -> BOInvUtils.invSwapBack();
                        case 2 -> BOInvUtils.pickSwapBack();
                     }

                     if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                        Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
                     }
                  }

                  if (!BOBlockUtil.solid(this.blockPos)) {
                     this.progress = (double)0.0F;
                  }

                  boolean offhand = this.mc.player.getOffHandStack().getItem() instanceof EndCrystalItem;
                  boolean mainhand = this.mc.player.getMainHandStack().getItem() instanceof EndCrystalItem;
                  if (!crystalThere && blockState.isOf(Blocks.OBSIDIAN)) {
                     if (!offhand && !mainhand && this.switchDelayLeft > 0) {
                        return;
                     }

                     double x = (double)this.blockPos.up().getX();
                     double y = (double)this.blockPos.up().getY();
                     double z = (double)this.blockPos.up().getZ();
                     if (!this.mc.world.getOtherEntities((Entity)null, new Box(x, y, z, x + (double)1.0F, y + (double)2.0F, z + (double)1.0F)).isEmpty() || !this.mc.world.getBlockState(this.blockPos.up()).isAir()) {
                        this.blacklisted.add(this.target);
                        this.getEntities();
                        return;
                     }

                     if (!offhand && !mainhand) {
                        this.mc.player.getInventory().selectedSlot = crystalSlot;
                     }

                     Hand hand = offhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
                     if ((Boolean)this.swing.get()) {
                        this.clientSwing((SwingHand)this.placeHand.get(), Hand.MAIN_HAND);
                     }

                     if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                        Managers.ROTATION.start(this.blockPos, (double)25.0F, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "interact"}));
                     }

                     this.sendPacket(new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(this.mc.player.getPos(), (double)this.blockPos.getY() < this.mc.player.getY() ? Direction.UP : Direction.DOWN, this.blockPos, false), 0));
                     if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                        Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "interact"}));
                     }
                  }

                  if (blockState.isAir() && this.mode.get() == CevBreaker.Mode.Packet) {
                     this.startedYet = false;
                  }

                  if ((this.mc.player.getInventory().selectedSlot == pickSlot || this.switchDelayLeft <= 0) && crystalThere && blockState.isOf(Blocks.OBSIDIAN)) {
                     Direction direction = BOEntityUtils.rayTraceCheck(this.blockPos, true);
                     switch (((Mode)this.mode.get()).ordinal()) {
                        case 0:
                           if (this.progress < (double)1.0F) {
                              this.progress = (double)((IClientPlayerInteractionManager)this.mc.interactionManager).getBreakingProgress();
                           }

                           this.mc.player.getInventory().selectedSlot = pickSlot;
                           this.mc.interactionManager.updateBlockBreakingProgress(this.blockPos, direction);
                           break;
                        case 1:
                           if (this.progress < (double)1.0F) {
                              this.progress += BlockUtils.getBreakDelta(pickSlot, blockState);
                           }

                           this.timer = this.startedYet ? this.timer : BOBlockUtil.getBlockBreakingSpeed(blockState, this.blockPos, pickSlot);
                           if (!this.startedYet) {
                              this.mine(this.blockPos);
                              this.startedYet = true;
                           } else if (this.timer <= 0) {
                              this.mc.player.getInventory().selectedSlot = pickSlot;
                           }
                           break;
                        case 2:
                           if (!this.startedYet) {
                              this.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.blockPos, direction));
                              this.startedYet = true;
                           } else {
                              if ((Boolean)this.smartDelay.get() && this.target.hurtTime > 0) {
                                 return;
                              }

                              this.mc.player.getInventory().selectedSlot = pickSlot;
                              this.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, direction));
                           }
                     }
                  }

                  if (this.mode.get() != CevBreaker.Mode.Packet || this.breakDelayLeft < 0) {
                     for(EndCrystalEntity crystal : this.crystals) {
                        if (DamageUtils.crystalDamage(this.target, crystal.getPos()) >= 6.0F) {
                           if ((Boolean)this.swing.get()) {
                              this.clientSwing((SwingHand)this.placeHand.get(), Hand.MAIN_HAND);
                           } else {
                              this.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                           }

                           if (SettingUtils.shouldRotate(RotationType.Attacking)) {
                              Managers.ROTATION.start(crystal.getBoundingBox(), (double)this.priority, RotationType.Attacking, (long)Objects.hash(new Object[]{this.name + "attacking"}));
                           }

                           this.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, false));
                           if (SettingUtils.shouldRotate(RotationType.Attacking)) {
                              Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "attacking"}));
                           }
                           break;
                        }
                     }

                  }
               }
            } else {
               this.pause = true;
            }
         } else {
            this.toggle();
         }
      }
   }

   private void getEntities() {
      this.target = null;
      this.crystals.clear();

      for(Entity entity : this.mc.world.getEntities()) {
         if (entity.isInRange(this.mc.player, (double)6.0F) && entity.isAlive()) {
            if (entity instanceof PlayerEntity) {
               if (entity != this.mc.player && Friends.get().shouldAttack((PlayerEntity)entity) && (this.target == null || this.mc.player.distanceTo(entity) < this.mc.player.distanceTo(this.target)) && !this.blacklisted.contains(entity)) {
                  this.target = (PlayerEntity)entity;
               }
            } else if (entity instanceof EndCrystalEntity) {
               this.crystals.add((EndCrystalEntity)entity);
            }
         }
      }

   }

   private void mine(BlockPos blockPos) {
      Direction direction = this.mc.player.getY() > (double)blockPos.getY() ? Direction.UP : Direction.DOWN;
      this.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, blockPos, direction));
      if ((Boolean)this.swing.get()) {
         this.clientSwing((SwingHand)this.placeHand.get(), Hand.MAIN_HAND);
      } else {
         this.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
      }

      this.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, blockPos, direction));
      if (SettingUtils.shouldRotate(RotationType.Mining)) {
         Managers.ROTATION.start(blockPos, (double)10.0F, RotationType.Mining, (long)Objects.hash(new Object[]{this.name + "mining"}));
      }

   }

   @EventHandler
   private void onUpdateSelectedSlot(PacketEvent.Send event) {
      if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
         this.switchDelayLeft = 1;
         this.breakDelayLeft = (Integer)this.switchDelay.get();
      }

   }

   @EventHandler
   private void onRender3D(Render3DEvent event) {
      if ((Boolean)this.renderPlace.get()) {
         this.renderPlacing.removeIf((r) -> System.currentTimeMillis() - r.time > 1000L);
         this.renderPlacing.forEach((r) -> {
            double progress = (double)1.0F - (double)Math.min(System.currentTimeMillis() - r.time, 700L) / (double)700.0F;
            event.renderer.box(r.pos, new Color(((SettingColor)this.sideColor.get()).r, ((SettingColor)this.sideColor.get()).g, ((SettingColor)this.sideColor.get()).b, (int)Math.round((double)((SettingColor)this.sideColor.get()).a * progress)), new Color(((SettingColor)this.lineColor.get()).r, ((SettingColor)this.lineColor.get()).g, ((SettingColor)this.lineColor.get()).b, (int)Math.round((double)((SettingColor)this.lineColor.get()).a * progress)), (ShapeMode)this.shapeMode.get(), 0);
         });
      }

      if ((Boolean)this.renderBreak.get()) {
         if (((Mode)this.mode.get()).equals(CevBreaker.Mode.Instant)) {
            return;
         }

         if (this.blockPos == null) {
            return;
         }

         double min = this.progress / (double)2.0F;
         Vec3d vec3d = this.blockPos.toCenterPos();
         Box box = new Box(vec3d.x - min, vec3d.y - min, vec3d.z - min, vec3d.x + min, vec3d.y + min, vec3d.z + min);
         event.renderer.box(box, this.progress >= 0.98 ? (Color)this.endBreakSideColor.get() : (Color)this.breakSideColor.get(), this.progress >= 0.98 ? (Color)this.endBreakLineColor.get() : (Color)this.breakLineColor.get(), (ShapeMode)this.breakShapeMode.get(), 0);
      }

   }

   @EventHandler
   private void onRender2D(Render2DEvent event) {
      if (this.blockPos != null && (Boolean)this.renderProgress.get() && !((Mode)this.mode.get()).equals(CevBreaker.Mode.Instant)) {
         Vector3d pos = new Vector3d((double)this.blockPos.getX() + (double)0.5F, (double)this.blockPos.getY() + (double)0.5F, (double)this.blockPos.getZ() + (double)0.5F);
         if (NametagUtils.to2D(pos, (Double)this.scale.get())) {
            TextRenderer textRenderer = TextRenderer.get();
            NametagUtils.begin(pos);
            textRenderer.begin((double)1.0F, false, true);
            String progressText = ((Mode)this.mode.get()).equals(CevBreaker.Mode.Instant) ? (!BOBlockUtil.solid(this.blockPos) ? "Waiting" : "Mining") : String.format("%.2f", this.progress) + "%";
            textRenderer.render(progressText, -textRenderer.getWidth(progressText) / (double)2.0F, (double)0.0F, this.progress >= 0.98 ? (Color)this.endColor.get() : (Color)this.miningColor.get());
            textRenderer.end();
            NametagUtils.end();
         }

      }
   }

   public String getInfoString() {
      return this.target != null ? EntityUtils.getName(this.target) : null;
   }

   private BlockPos getPlacePos(PlayerEntity player) {
      if (player == null) {
         return null;
      } else {
         BlockPos pos = player.getBlockPos();
         if (!this.mc.world.getBlockState(pos.up(3)).isAir() || !this.mc.world.getBlockState(pos.up(2)).isOf(Blocks.OBSIDIAN) && !this.mc.world.getBlockState(pos.up(2)).isOf(Blocks.AIR)) {
            List<BlockPos> posList = new ArrayList();

            for(Direction dir : Direction.values()) {
               if (dir != Direction.UP && dir != Direction.DOWN && this.mc.world.getBlockState(pos.offset(dir).up(2)).isAir() && (this.mc.world.getBlockState(pos.offset(dir).up(1)).isOf(Blocks.OBSIDIAN) || this.mc.world.getBlockState(pos.offset(dir).up(1)).isOf(Blocks.AIR))) {
                  posList.add(pos.offset(dir).up(1));
               }
            }

            posList.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
            return posList.isEmpty() ? null : (BlockPos)posList.get(0);
         } else {
            return pos.up(2);
         }
      }
   }

   private List<BlockPos> getValid(BlockPos block) {
      List<BlockPos> list = new ArrayList();
      if (!BOBlockUtil.replaceable(block)) {
         return list;
      } else {
         PlaceData data = SettingUtils.getPlaceData(block);
         if (data.valid() && SettingUtils.inPlaceRange(data.pos())) {
            this.renderPlacing.add(new Render(block, System.currentTimeMillis()));
            if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block)), (entity) -> !entity.isSpectator() && !(entity instanceof ItemEntity))) {
               list.add(block);
            }

            return list;
         } else {
            Direction support1 = this.getSupport(block);
            if (support1 != null) {
               this.renderPlacing.add(new Render(block, System.currentTimeMillis()));
               this.renderPlacing.add(new Render(block.offset(support1), System.currentTimeMillis()));
               if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block.offset(support1))), (entity) -> !entity.isSpectator() && !(entity instanceof ItemEntity))) {
                  list.add(block.offset(support1));
               }

               return list;
            } else {
               for(Direction dir : Direction.values()) {
                  if (BOBlockUtil.replaceable(block.offset(dir)) && SettingUtils.inPlaceRange(block.offset(dir))) {
                     Direction support2 = this.getSupport(block.offset(dir));
                     if (support2 != null) {
                        this.renderPlacing.add(new Render(block, System.currentTimeMillis()));
                        this.renderPlacing.add(new Render(block.offset(dir), System.currentTimeMillis()));
                        this.renderPlacing.add(new Render(block.offset(dir).offset(support2), System.currentTimeMillis()));
                        if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(block.offset(dir).offset(support2))), (entity) -> !entity.isSpectator() && !(entity instanceof ItemEntity))) {
                           list.add(block.offset(dir).offset(support2));
                        }

                        return list;
                     }
                  }
               }

               return list;
            }
         }
      }
   }

   private Direction getSupport(BlockPos position) {
      Direction cDir = null;
      double cDist = (double)1000.0F;
      int value = -1;

      for(Direction dir : Direction.values()) {
         PlaceData data = SettingUtils.getPlaceData(position.offset(dir));
         if (data.valid() && SettingUtils.inPlaceRange(data.pos())) {
            if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(position.offset(dir))), (entity) -> !entity.isSpectator() && entity.getType() != EntityType.ITEM)) {
               double dist = this.mc.player.getEyePos().distanceTo(position.offset(dir).toCenterPos());
               if (dist < cDist || value < 2) {
                  value = 2;
                  cDir = dir;
                  cDist = dist;
               }
            }

            if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(position.offset(dir))), (entity) -> !entity.isSpectator() && entity.getType() != EntityType.ITEM && entity.getType() != EntityType.END_CRYSTAL)) {
               double dist = this.mc.player.getEyePos().distanceTo(position.offset(dir).toCenterPos());
               if (dist < cDist || value < 1) {
                  value = 1;
                  cDir = dir;
                  cDist = dist;
               }
            }
         }
      }

      return cDir;
   }

   public static enum Mode {
      Normal,
      Packet,
      Instant;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Normal, Packet, Instant};
      }
   }

   public static enum SwitchMode {
      Silent,
      InvSwitch,
      PickSilent;

      // $FF: synthetic method
      private static SwitchMode[] $values() {
         return new SwitchMode[]{Silent, InvSwitch, PickSilent};
      }
   }

   public static record Render(BlockPos pos, long time) {
   }
}
