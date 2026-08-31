package espada.spacex.aurora.modules.combatplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.modules.combatplus.automine.AuroraMine;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.SettingUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.FireBlock;
import net.minecraft.util.math.Box;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonExtensionBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Direction.Type;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;

public class PistonCrystal extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgDelay;
   private final SettingGroup sgSwitch;
   private final SettingGroup sgToggle;
   private final SettingGroup sgSwing;
   private final SettingGroup sgRender;
   private final Setting<Boolean> pauseEat;
   private final Setting<Boolean> fire;
   private final Setting<Redstone> redstone;
   private final Setting<Boolean> alwaysAttack;
   private final Setting<Double> attackSpeed;
   private final Setting<Double> pcDelay;
   private final Setting<Double> cfDelay;
   private final Setting<Double> crDelay;
   private final Setting<Double> rmDelay;
   private final Setting<Double> mpDelay;
   private final Setting<SwitchMode> crystalSwitch;
   private final Setting<SwitchMode> pistonSwitch;
   private final Setting<SwitchMode> redstoneSwitch;
   private final Setting<SwitchMode> fireSwitch;
   private final Setting<Boolean> crystalSwing;
   private final Setting<SwingHand> crystalHand;
   private final Setting<Boolean> attackSwing;
   private final Setting<SwingHand> attackHand;
   private final Setting<Boolean> pistonSwing;
   private final Setting<SwingHand> pistonHand;
   private final Setting<Boolean> redstoneSwing;
   private final Setting<SwingHand> redstoneHand;
   private final Setting<Boolean> fireSwing;
   private final Setting<SwingHand> fireHand;
   private final Setting<Double> crystalHeight;
   private final Setting<ShapeMode> crystalShapeMode;
   private final Setting<SettingColor> crystalLineColor;
   public final Setting<SettingColor> crystalColor;
   private final Setting<Double> pistonHeight;
   private final Setting<ShapeMode> pistonShapeMode;
   private final Setting<SettingColor> pistonLineColor;
   public final Setting<SettingColor> pistonColor;
   private final Setting<Double> redstoneHeight;
   private final Setting<ShapeMode> redstoneShapeMode;
   private final Setting<SettingColor> redstoneLineColor;
   public final Setting<SettingColor> redstoneColor;
   private long lastAttack;
   public BlockPos crystalPos;
   private BlockPos pistonPos;
   private BlockPos firePos;
   private BlockPos redstonePos;
   private BlockPos lastCrystalPos;
   private BlockPos lastPistonPos;
   private BlockPos lastRedstonePos;
   private Entity lastTarget;
   private Direction pistonDir;
   private PlaceData pistonData;
   private Direction crystalPlaceDir;
   private Direction crystalDir;
   private PlaceData redstoneData;
   private Entity target;
   private BlockPos closestCrystalPos;
   private BlockPos closestPistonPos;
   private BlockPos closestRedstonePos;
   private Direction closestPistonDir;
   private PlaceData closestPistonData;
   private Direction closestCrystalPlaceDir;
   private Direction closestCrystalDir;
   private PlaceData closestRedstoneData;
   private long pistonTime;
   private long redstoneTime;
   private long mineTime;
   private long crystalTime;
   private boolean minedThisTick;
   private boolean pistonPlaced;
   private boolean redstonePlaced;
   private boolean mined;
   private boolean crystalPlaced;
   private boolean firePlaced;
   private double cd;
   private double d;

   public PistonCrystal() {
      super(Aurora.CombatPlus, "Piston Crystal", "Pushes crystals into your enemies to deal massive damage.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgDelay = this.settings.createGroup("Delay");
      this.sgSwitch = this.settings.createGroup("Switch");
      this.sgToggle = this.settings.createGroup("Toggle");
      this.sgSwing = this.settings.createGroup("Swing");
      this.sgRender = this.settings.createGroup("Render");
      this.pauseEat = this.addPauseEat(this.sgGeneral);
      this.fire = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Fire")).description("Uses fire to blow up the crystal.")).defaultValue(false)).build());
      this.redstone = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Redstone")).description("What kind of redstone to use.")).defaultValue(PistonCrystal.Redstone.Torch)).build());
      this.alwaysAttack = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Always Attack")).description("Attacks all crystals blocking crystal placing.")).defaultValue(false)).build());
      this.attackSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Attack Speed")).description("How many times to attack the crystal every second.")).defaultValue((double)4.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.pcDelay = this.sgDelay.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Piston > Crystal")).description("How many seconds to wait between placing piston and redstone.")).defaultValue((double)0.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.cfDelay = this.sgDelay.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Crystal > Fire")).description("How many seconds to wait after mining the redstone before starting a new cycle.")).defaultValue(0.2).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.crDelay = this.sgDelay.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Crystal > Redstone")).description("How many seconds to wait between placing redstone and starting to mine it.")).defaultValue(0.2).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.rmDelay = this.sgDelay.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Redstone > Mine")).description("How many seconds to wait after mining the redstone before starting a new cycle.")).defaultValue(0.2).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.mpDelay = this.sgDelay.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Mine > Piston")).description("How many seconds to wait after mining the redstone before starting a new cycle.")).defaultValue(0.2).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.crystalSwitch = this.sgSwitch.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Crystal Switch")).description("Method of switching. Silent is the most reliable.")).defaultValue(PistonCrystal.SwitchMode.Silent)).build());
      this.pistonSwitch = this.sgSwitch.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Piston Switch")).description("Method of switching. Silent is the most reliable.")).defaultValue(PistonCrystal.SwitchMode.Silent)).build());
      this.redstoneSwitch = this.sgSwitch.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Redstone Switch")).description("Method of switching. Silent is the most reliable.")).defaultValue(PistonCrystal.SwitchMode.Silent)).build());
      this.fireSwitch = this.sgSwitch.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Fire Switch")).description("Method of switching. Silent is the most reliable.")).defaultValue(PistonCrystal.SwitchMode.Silent)).build());
      this.crystalSwing = this.sgSwing.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Crystal Swing")).description("Renders swing animation when placing a crystal.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgSwing;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Crystal Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      Setting<Boolean> var10003 = this.crystalSwing;
      Objects.requireNonNull(var10003);
      this.crystalHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.attackSwing = this.sgSwing.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Attack Swing")).description("Renders swing animation when attacking a crystal.")).defaultValue(true)).build());
      var10001 = this.sgSwing;
      var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Attack Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.attackSwing;
      Objects.requireNonNull(var10003);
      this.attackHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.pistonSwing = this.sgSwing.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Piston Swing")).description("Renders swing animation when placing a piston.")).defaultValue(true)).build());
      var10001 = this.sgSwing;
      var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Piston Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.pistonSwing;
      Objects.requireNonNull(var10003);
      this.pistonHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.redstoneSwing = this.sgSwing.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Piston Swing")).description("Renders swing animation when placing redstone.")).defaultValue(true)).build());
      var10001 = this.sgSwing;
      var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Redstone Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.redstoneSwing;
      Objects.requireNonNull(var10003);
      this.redstoneHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.fireSwing = this.sgSwing.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Fire Swing")).description("Renders swing animation when placing fire.")).defaultValue(true)).build());
      var10001 = this.sgSwing;
      var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Fire Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.fireSwing;
      Objects.requireNonNull(var10003);
      this.fireHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.crystalHeight = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Crystal Height")).description(".")).defaultValue((double)0.25F).sliderRange((double)-1.0F, (double)1.0F).build());
      this.crystalShapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Crystal Shape Mode")).description(".")).defaultValue(ShapeMode.Both)).build());
      this.crystalLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Crystal Line Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.crystalColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Crystal Side Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.pistonHeight = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Piston Height")).description(".")).defaultValue((double)1.0F).sliderRange((double)-1.0F, (double)1.0F).build());
      this.pistonShapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Piston Shape Mode")).description(".")).defaultValue(ShapeMode.Both)).build());
      this.pistonLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Piston Line Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
      this.pistonColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Piston Side Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 50)).build());
      this.redstoneHeight = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Redstone Height")).description(".")).defaultValue((double)1.0F).sliderRange((double)-1.0F, (double)1.0F).build());
      this.redstoneShapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Redstone Shape Mode")).description(".")).defaultValue(ShapeMode.Both)).build());
      this.redstoneLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Redstone Line Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.redstoneColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Redstone Side Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.lastAttack = 0L;
      this.crystalPos = null;
      this.pistonPos = null;
      this.firePos = null;
      this.redstonePos = null;
      this.lastCrystalPos = null;
      this.lastPistonPos = null;
      this.lastRedstonePos = null;
      this.lastTarget = null;
      this.pistonDir = null;
      this.pistonData = null;
      this.crystalPlaceDir = null;
      this.crystalDir = null;
      this.redstoneData = null;
      this.target = null;
      this.closestCrystalPos = null;
      this.closestPistonPos = null;
      this.closestRedstonePos = null;
      this.closestPistonDir = null;
      this.closestPistonData = null;
      this.closestCrystalPlaceDir = null;
      this.closestCrystalDir = null;
      this.closestRedstoneData = null;
      this.pistonTime = 0L;
      this.redstoneTime = 0L;
      this.mineTime = 0L;
      this.crystalTime = 0L;
      this.minedThisTick = false;
      this.pistonPlaced = false;
      this.redstonePlaced = false;
      this.mined = false;
      this.crystalPlaced = false;
      this.firePlaced = false;
   }

   public void onActivate() {
      this.resetPos();
      this.lastCrystalPos = null;
      this.lastPistonPos = null;
      this.lastRedstonePos = null;
      this.pistonPlaced = false;
      this.redstonePlaced = false;
      this.mined = false;
      this.crystalPlaced = false;
      this.firePlaced = false;
   }

   @EventHandler(
      priority = 200
   )
   private void onTick(TickEvent.Pre event) {
      this.minedThisTick = false;
   }

   @EventHandler(
      priority = 200
   )
   private void onRender(Render3DEvent event) {
      if (this.mc.player != null && this.mc.world != null) {
         this.updatePos();
         if (this.crystalPos != null) {
            event.renderer.box(this.getBox(this.crystalPos, (Double)this.crystalHeight.get()), (Color)this.crystalColor.get(), (Color)this.crystalLineColor.get(), (ShapeMode)this.crystalShapeMode.get(), 0);
            event.renderer.box(this.getBox(this.pistonPos, (Double)this.pistonHeight.get()), (Color)this.pistonColor.get(), (Color)this.pistonLineColor.get(), (ShapeMode)this.pistonShapeMode.get(), 0);
            event.renderer.box(this.getBox(this.redstonePos, (Double)this.redstoneHeight.get()), (Color)this.redstoneColor.get(), (Color)this.redstoneLineColor.get(), (ShapeMode)this.redstoneShapeMode.get(), 0);
         }

         if (this.crystalPos != null) {
            if ((double)(System.currentTimeMillis() - this.mineTime) > (Double)this.mpDelay.get() * (double)1000.0F && this.crystalPlaced && this.redstonePlaced && this.pistonPlaced && this.mined && (this.firePlaced || !(Boolean)this.fire.get())) {
               this.redstonePlaced = false;
               this.pistonPlaced = false;
               this.mined = false;
               this.firePlaced = false;
               this.crystalPlaced = false;
               this.pistonTime = 0L;
               this.redstoneTime = 0L;
               this.mineTime = 0L;
               this.crystalTime = 0L;
               this.lastAttack = 0L;
            }

            if (!(Boolean)this.pauseEat.get() || !this.mc.player.isUsingItem()) {
               this.updateAttack();
               this.updatePiston();
               this.updateFire();
               this.updateCrystal();
               this.updateRedstone();
               this.mineUpdate();
            }
         }
      }
   }

   private Box getBox(BlockPos pos, double height) {
      return new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)pos.getY() + height, (double)(pos.getZ() + 1));
   }

   private void mineUpdate() {
      if (!((double)(System.currentTimeMillis() - this.redstoneTime) < (Double)this.rmDelay.get() * (double)1000.0F)) {
         if (this.redstonePlaced) {
            if (!this.minedThisTick) {
               AuroraMine autoMine = (AuroraMine)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class);
               if (autoMine.isActive()) {
                  if (this.redstonePos.equals(autoMine.targetPos())) {
                     return;
                  }

                  Direction dir = SettingUtils.getPlaceOnDirection(this.redstonePos);
                  autoMine.onStart(this.redstonePos, dir);
               } else {
                  Direction mineDir = SettingUtils.getPlaceOnDirection(this.redstonePos);
                  if (mineDir != null) {
                     this.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.redstonePos, mineDir));
                     this.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.redstonePos, mineDir));
                  }
               }

               if (!this.mined) {
                  this.mineTime = System.currentTimeMillis();
               }

               this.mined = true;
               this.minedThisTick = true;
            }
         }
      }
   }

   private void updateAttack() {
      if (this.redstonePlaced) {
         EndCrystalEntity crystal = null;
         double cd = (double)10000.0F;

         for(Entity entity : this.mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity) {
               EndCrystalEntity c = (EndCrystalEntity)entity;
               if ((c.getX() != (double)this.crystalPos.getX() + (double)0.5F || c.getZ() != (double)this.crystalPos.getZ() + (double)0.5F) && ((Boolean)this.alwaysAttack.get() || c.getX() - (double)c.getBlockX() != (double)0.5F || c.getZ() - (double)c.getBlockZ() != (double)0.5F) && c.getBoundingBox().intersects(Box.from(new BlockBox(this.crystalPos)).withMaxY((double)(this.crystalPos.getY() + 1)))) {
                  double d = this.mc.player.getEyePos().distanceTo(c.getPos());
                  if (d < cd) {
                     cd = d;
                     crystal = c;
                  }
               }
            }
         }

         if (crystal != null) {
            if (!SettingUtils.shouldRotate(RotationType.Attacking) || Managers.ROTATION.start(crystal.getBoundingBox(), (double)this.priority - 0.1, RotationType.Attacking, (long)Objects.hash(new Object[]{this.name + "attacking"}))) {
               if (!((double)(System.currentTimeMillis() - this.lastAttack) < (double)1000.0F / (Double)this.attackSpeed.get())) {
                  SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.MAIN_HAND);
                  this.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, this.mc.player.isSneaking()));
                  SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.MAIN_HAND);
                  if (SettingUtils.shouldRotate(RotationType.Attacking)) {
                     Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "attacking"}));
                  }

                  if ((Boolean)this.attackSwing.get()) {
                     this.clientSwing((SwingHand)this.attackHand.get(), Hand.MAIN_HAND);
                  }

                  this.lastAttack = System.currentTimeMillis();
               }
            }
         }
      }
   }

   private void updatePiston() {
      if (!this.pistonPlaced) {
         if (this.pistonData != null) {
            Hand hand = this.getHand(Items.PISTON);
            boolean available = hand != null;
            if (!available) {
               switch (((SwitchMode)this.pistonSwitch.get()).ordinal()) {
                  case 1:
                     available = InvUtils.findInHotbar(new Item[]{Items.PISTON}).found();
                     break;
                  case 2:
                  case 3:
                     available = InvUtils.find(new Item[]{Items.PISTON}).found();
               }
            }

            if (available) {
               if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(this.pistonData.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "piston"}))) {
                  boolean switched = false;
                  if (hand == null) {
                     switch (((SwitchMode)this.pistonSwitch.get()).ordinal()) {
                        case 1:
                           InvUtils.swap(InvUtils.findInHotbar(new Item[]{Items.PISTON}).slot(), true);
                           switched = true;
                           break;
                        case 2:
                           switched = BOInvUtils.pickSwitch(InvUtils.find(new Item[]{Items.PISTON}).slot());
                           break;
                        case 3:
                           switched = BOInvUtils.invSwitch(InvUtils.find(new Item[]{Items.PISTON}).slot());
                     }
                  }

                  if (hand != null || switched) {
                     this.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(this.pistonDir.getOpposite().asRotation(), Managers.ROTATION.lastDir[1], Managers.ON_GROUND.isOnGround()));
                     hand = hand == null ? Hand.MAIN_HAND : hand;
                     this.placeBlock(hand, this.pistonData.pos().toCenterPos(), this.pistonData.dir(), this.pistonData.pos());
                     if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                        Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "piston"}));
                     }

                     if ((Boolean)this.pistonSwing.get()) {
                        this.clientSwing((SwingHand)this.pistonHand.get(), hand);
                     }

                     this.pistonTime = System.currentTimeMillis();
                     this.pistonPlaced = true;
                     if (switched) {
                        switch (((SwitchMode)this.pistonSwitch.get()).ordinal()) {
                           case 1 -> InvUtils.swapBack();
                           case 2 -> BOInvUtils.pickSwapBack();
                           case 3 -> BOInvUtils.swapBack();
                        }
                     }

                  }
               }
            }
         }
      }
   }

   private void updateCrystal() {
      if (this.pistonPlaced && !this.crystalPlaced) {
         if (!((double)(System.currentTimeMillis() - this.pistonTime) < (Double)this.pcDelay.get() * (double)1000.0F)) {
            if (this.crystalPlaceDir != null) {
               if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(this.crystalPos)), (entity) -> !entity.isSpectator() && !(entity instanceof EndCrystalEntity))) {
                  Hand hand = this.getHand(Items.END_CRYSTAL);
                  boolean available = hand != null;
                  if (!available) {
                     switch (((SwitchMode)this.crystalSwitch.get()).ordinal()) {
                        case 1:
                           available = InvUtils.findInHotbar(new Item[]{Items.END_CRYSTAL}).found();
                           break;
                        case 2:
                        case 3:
                           available = InvUtils.find(new Item[]{Items.END_CRYSTAL}).found();
                     }
                  }

                  if (available) {
                     if (!SettingUtils.shouldRotate(RotationType.Interact) || Managers.ROTATION.start(this.crystalPos.down(), (double)this.priority, RotationType.Interact, (long)Objects.hash(new Object[]{this.name + "crystal"}))) {
                        boolean switched = false;
                        if (hand == null) {
                           switch (((SwitchMode)this.crystalSwitch.get()).ordinal()) {
                              case 1:
                                 InvUtils.swap(InvUtils.findInHotbar(new Item[]{Items.END_CRYSTAL}).slot(), true);
                                 switched = true;
                                 break;
                              case 2:
                                 switched = BOInvUtils.pickSwitch(InvUtils.find(new Item[]{Items.END_CRYSTAL}).slot());
                                 break;
                              case 3:
                                 switched = BOInvUtils.invSwitch(InvUtils.find(new Item[]{Items.END_CRYSTAL}).slot());
                           }
                        }

                        if (hand != null || switched) {
                           hand = hand == null ? Hand.MAIN_HAND : hand;
                           this.interactBlock(hand, this.crystalPos.down().toCenterPos(), this.crystalPlaceDir, this.crystalPos.down());
                           if (SettingUtils.shouldRotate(RotationType.Interact)) {
                              Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "crystal"}));
                           }

                           if ((Boolean)this.crystalSwing.get()) {
                              this.clientSwing((SwingHand)this.crystalHand.get(), hand);
                           }

                           this.crystalTime = System.currentTimeMillis();
                           this.crystalPlaced = true;
                           if (switched) {
                              switch (((SwitchMode)this.crystalSwitch.get()).ordinal()) {
                                 case 1 -> InvUtils.swapBack();
                                 case 2 -> BOInvUtils.pickSwapBack();
                                 case 3 -> BOInvUtils.swapBack();
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

   private void updateRedstone() {
      if (this.crystalPlaced && !this.redstonePlaced) {
         if (!((double)(System.currentTimeMillis() - this.crystalTime) < (Double)this.crDelay.get() * (double)1000.0F)) {
            if (this.redstoneData != null) {
               Hand hand = this.getHand(((Redstone)this.redstone.get()).i);
               boolean available = hand != null;
               if (!available) {
                  switch (((SwitchMode)this.redstoneSwitch.get()).ordinal()) {
                     case 1:
                        available = InvUtils.findInHotbar(new Item[]{((Redstone)this.redstone.get()).i}).found();
                        break;
                     case 2:
                     case 3:
                        available = InvUtils.find(new Item[]{((Redstone)this.redstone.get()).i}).found();
                  }
               }

               if (available) {
                  if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(this.redstoneData.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "redstone"}))) {
                     boolean switched = false;
                     if (hand == null) {
                        switch (((SwitchMode)this.redstoneSwitch.get()).ordinal()) {
                           case 1:
                              InvUtils.swap(InvUtils.findInHotbar(new Item[]{((Redstone)this.redstone.get()).i}).slot(), true);
                              switched = true;
                              break;
                           case 2:
                              switched = BOInvUtils.pickSwitch(InvUtils.find(new Item[]{((Redstone)this.redstone.get()).i}).slot());
                              break;
                           case 3:
                              switched = BOInvUtils.invSwitch(InvUtils.find(new Item[]{((Redstone)this.redstone.get()).i}).slot());
                        }
                     }

                     if (hand != null || switched) {
                        hand = hand == null ? Hand.MAIN_HAND : hand;
                        this.placeBlock(hand, this.redstoneData.pos().toCenterPos(), this.redstoneData.dir(), this.redstoneData.pos());
                        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                           Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "redstone"}));
                        }

                        if ((Boolean)this.redstoneSwing.get()) {
                           this.clientSwing((SwingHand)this.redstoneHand.get(), hand);
                        }

                        this.redstoneTime = System.currentTimeMillis();
                        this.redstonePlaced = true;
                        if (switched) {
                           switch (((SwitchMode)this.redstoneSwitch.get()).ordinal()) {
                              case 1 -> InvUtils.swapBack();
                              case 2 -> BOInvUtils.pickSwapBack();
                              case 3 -> BOInvUtils.swapBack();
                           }
                        }

                     }
                  }
               }
            }
         }
      }
   }

   private void updateFire() {
      if ((Boolean)this.fire.get()) {
         if (this.crystalPlaced && !this.firePlaced) {
            if (!((double)(System.currentTimeMillis() - this.crystalTime) < (Double)this.cfDelay.get() * (double)1000.0F)) {
               double closesD = (double)10000.0F;
               this.firePos = null;
               PlaceData data = null;
               boolean found = false;

               for(int x = this.crystalDir.getOpposite().getOffsetX() == 0 ? -1 : Math.min(0, this.crystalDir.getOffsetX()); x <= (this.crystalDir.getOpposite().getOffsetX() == 0 ? 1 : Math.max(0, this.crystalDir.getOpposite().getOffsetX())); ++x) {
                  for(int y = 0; y <= 1; ++y) {
                     for(int z = this.crystalDir.getOpposite().getOffsetZ() == 0 ? -1 : Math.min(0, this.crystalDir.getOffsetZ()); z <= (this.crystalDir.getOpposite().getOffsetZ() == 0 ? 1 : Math.max(0, this.crystalDir.getOpposite().getOffsetZ())) && !found; ++z) {
                        BlockPos pos = this.crystalPos.offset(this.crystalDir.getOpposite()).add(x, y, z);
                        if (!pos.equals(this.crystalPos) && !pos.equals(this.pistonPos) && !pos.equals(this.redstonePos) && !pos.equals(this.pistonPos.offset(this.pistonDir.getOpposite()))) {
                           if (this.mc.world.getBlockState(pos).getBlock() instanceof FireBlock) {
                              found = true;
                              this.firePos = pos;
                              data = SettingUtils.getPlaceData(pos);
                           }

                           if (OLEPOSSUtils.solid(pos.down()) && this.mc.world.getBlockState(pos).getBlock() instanceof AirBlock) {
                              double d = pos.toCenterPos().distanceTo(this.mc.player.getEyePos());
                              if (!(d >= closesD)) {
                                 PlaceData da = SettingUtils.getPlaceData(pos);
                                 if (da.valid() && SettingUtils.inPlaceRange(da.pos())) {
                                    data = da;
                                    closesD = d;
                                    this.firePos = pos;
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               if (this.firePos == null) {
                  this.firePlaced = true;
               } else if (data != null && data.valid()) {
                  Hand hand = this.getHand(Items.FLINT_AND_STEEL);
                  boolean available = hand != null;
                  if (!available) {
                     switch (((SwitchMode)this.fireSwitch.get()).ordinal()) {
                        case 1:
                           available = InvUtils.findInHotbar(new Item[]{Items.FLINT_AND_STEEL}).found();
                           break;
                        case 2:
                        case 3:
                           available = InvUtils.find(new Item[]{Items.FLINT_AND_STEEL}).found();
                     }
                  }

                  if (available) {
                     if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(data.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "fire"}))) {
                        boolean switched = false;
                        if (hand == null) {
                           switch (((SwitchMode)this.fireSwitch.get()).ordinal()) {
                              case 1:
                                 InvUtils.swap(InvUtils.findInHotbar(new Item[]{Items.FLINT_AND_STEEL}).slot(), true);
                                 switched = true;
                                 break;
                              case 2:
                                 switched = BOInvUtils.pickSwitch(InvUtils.find(new Item[]{Items.FLINT_AND_STEEL}).slot());
                                 break;
                              case 3:
                                 switched = BOInvUtils.invSwitch(InvUtils.find(new Item[]{Items.FLINT_AND_STEEL}).slot());
                           }
                        }

                        if (hand != null || switched) {
                           hand = hand == null ? Hand.MAIN_HAND : hand;
                           this.interactBlock(hand, data.pos().toCenterPos(), data.dir(), data.pos());
                           if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                              Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "fire"}));
                           }

                           if ((Boolean)this.fireSwing.get()) {
                              this.clientSwing((SwingHand)this.fireHand.get(), hand);
                           }

                           this.firePlaced = true;
                           if (switched) {
                              switch (((SwitchMode)this.fireSwitch.get()).ordinal()) {
                                 case 1 -> InvUtils.swapBack();
                                 case 2 -> BOInvUtils.pickSwapBack();
                                 case 3 -> BOInvUtils.swapBack();
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

   private void updatePos() {
      this.lastCrystalPos = this.crystalPos;
      this.lastPistonPos = this.pistonPos;
      this.lastRedstonePos = this.redstonePos;
      this.lastTarget = this.target;
      this.closestCrystalPos = null;
      this.closestPistonPos = null;
      this.closestRedstonePos = null;
      this.closestPistonDir = null;
      this.closestPistonData = null;
      this.closestCrystalPlaceDir = null;
      this.closestCrystalDir = null;
      this.closestRedstoneData = null;
      this.resetPos();
      this.mc.world.getPlayers().stream().filter((player) -> player != this.mc.player && player.getPos().distanceTo(this.mc.player.getPos()) < (double)10.0F && player.getHealth() > 0.0F && !Friends.get().isFriend(player) && !player.isSpectator()).sorted(Comparator.comparingDouble((i) -> i.getPos().distanceTo(this.mc.player.getPos()))).forEach((player) -> {
         if (this.crystalPos == null) {
            this.update(player, true);
            if (this.crystalPos != null) {
               return;
            }

            this.update(player, false);
         }

      });
   }

   private void update(PlayerEntity player, boolean top) {
      this.cd = (double)10000.0F;

      for(Direction dir : Type.HORIZONTAL) {
         this.resetPos();
         BlockPos cPos = top ? BlockPos.ofFloored(player.getEyePos()).offset(dir).up() : BlockPos.ofFloored(player.getEyePos()).offset(dir);
         this.d = cPos.toCenterPos().distanceTo(this.mc.player.getPos());
         if (cPos.equals(this.lastCrystalPos) || !(this.d > this.cd)) {
            Block b = this.mc.world.getBlockState(cPos).getBlock();
            if (b instanceof AirBlock || b == Blocks.PISTON_HEAD || b == Blocks.MOVING_PISTON) {
               b = this.mc.world.getBlockState(cPos.up()).getBlock();
               if ((!SettingUtils.oldCrystals() || b instanceof AirBlock || b == Blocks.PISTON_HEAD || b == Blocks.MOVING_PISTON) && (this.mc.world.getBlockState(cPos.down()).getBlock() == Blocks.OBSIDIAN || this.mc.world.getBlockState(cPos.down()).getBlock() == Blocks.BEDROCK) && !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(cPos)).withMaxY((double)(cPos.getY() + (SettingUtils.cc() ? 1 : 2))), (entity) -> !entity.isSpectator() && entity instanceof PlayerEntity) && SettingUtils.inPlaceRange(cPos)) {
                  Direction cDir = SettingUtils.getPlaceOnDirection(cPos);
                  if (cDir != null) {
                     this.getPistonPos(cPos, dir);
                     if (this.pistonPos != null) {
                        this.cd = this.d;
                        this.crystalPos = cPos;
                        this.crystalPlaceDir = cDir;
                        this.crystalDir = dir;
                        this.closestCrystalPos = this.crystalPos;
                        this.closestPistonPos = this.pistonPos;
                        this.closestRedstonePos = this.redstonePos;
                        this.closestPistonDir = this.pistonDir;
                        this.closestPistonData = this.pistonData;
                        this.closestCrystalPlaceDir = this.crystalPlaceDir;
                        this.closestCrystalDir = this.crystalDir;
                        this.closestRedstoneData = this.redstoneData;
                        if (this.crystalPos.equals(this.lastCrystalPos)) {
                           break;
                        }
                     }
                  }
               }
            }
         }
      }

      this.crystalPos = this.closestCrystalPos;
      this.pistonPos = this.closestPistonPos;
      this.redstonePos = this.closestRedstonePos;
      this.pistonDir = this.closestPistonDir;
      this.pistonData = this.closestPistonData;
      this.crystalPlaceDir = this.closestCrystalPlaceDir;
      this.crystalDir = this.closestCrystalDir;
      this.redstoneData = this.closestRedstoneData;
      this.target = player;
   }

   private void getPistonPos(BlockPos pos, Direction dir) {
      List<BlockPos> pistonBlocks = this.pistonBlocks(pos, dir);
      this.cd = (double)10000.0F;
      BlockPos cPos = null;
      PlaceData cData = null;
      Direction cDir = null;
      BlockPos cRedstonePos = null;
      PlaceData cRedstoneData = null;

      for(BlockPos position : pistonBlocks) {
         this.d = this.mc.player.getEyePos().distanceTo(position.toCenterPos());
         if (position.equals(this.lastPistonPos) || !(this.cd < this.d)) {
            PlaceData placeData = SettingUtils.getPlaceDataAND(position, (d) -> true, (b) -> !this.isRedstone(b) && !(this.mc.world.getBlockState(b).getBlock() instanceof PistonBlock) && !(this.mc.world.getBlockState(b).getBlock() instanceof PistonHeadBlock) && !(this.mc.world.getBlockState(b).getBlock() instanceof PistonExtensionBlock) && this.mc.world.getBlockState(b).getBlock() != Blocks.MOVING_PISTON && !(this.mc.world.getBlockState(b).getBlock() instanceof FireBlock));
            if (placeData.valid() && SettingUtils.inPlaceRange(placeData.pos())) {
               this.redstonePos(position, dir.getOpposite(), pos);
               if (this.redstonePos != null) {
                  this.cd = this.d;
                  cRedstonePos = this.redstonePos;
                  cRedstoneData = this.redstoneData;
                  cPos = position;
                  cDir = dir.getOpposite();
                  cData = placeData;
                  if (position.equals(this.lastPistonPos)) {
                     break;
                  }
               }
            }
         }
      }

      this.pistonPos = cPos;
      this.pistonDir = cDir;
      this.pistonData = cData;
      this.redstonePos = cRedstonePos;
      this.redstoneData = cRedstoneData;
   }

   private List<BlockPos> pistonBlocks(BlockPos pos, Direction dir) {
      List<BlockPos> blocks = new ArrayList();

      for(int x = dir.getOffsetX() == 0 ? -1 : dir.getOffsetX(); x <= (dir.getOffsetX() == 0 ? 1 : dir.getOffsetX()); ++x) {
         for(int z = dir.getOffsetZ() == 0 ? -1 : dir.getOffsetZ(); z <= (dir.getOffsetZ() == 0 ? 1 : dir.getOffsetZ()); ++z) {
            for(int y = 0; y <= 1; ++y) {
               if ((x != 0 || y != 0 || z != 0) && (!SettingUtils.oldCrystals() || x != 0 || y != 1 || z != 0) && this.upCheck(pos.add(x, y, z))) {
                  blocks.add(pos.add(x, y, z));
               }
            }
         }
      }

      return blocks.stream().filter((b) -> {
         if (this.blocked(b.offset(dir.getOpposite()))) {
            return false;
         } else if (EntityUtils.intersectsWithEntity(Box.from(new BlockBox(b)), (entity) -> !entity.isSpectator() && entity instanceof PlayerEntity)) {
            return false;
         } else {
            return !(this.mc.world.getBlockState(b).getBlock() instanceof PistonBlock) && this.mc.world.getBlockState(b).getBlock() != Blocks.MOVING_PISTON && !(this.mc.world.getBlockState(b).getBlock() instanceof FireBlock) ? OLEPOSSUtils.replaceable(b) : true;
         }
      }).toList();
   }

   private void redstonePos(BlockPos pos, Direction pDir, BlockPos cPos) {
      this.cd = (double)10000.0F;
      this.redstonePos = null;
      BlockPos cRedstonePos = null;
      PlaceData cRedstoneData = null;
      if (this.redstone.get() == PistonCrystal.Redstone.Torch) {
         for(Direction direction : Direction.values()) {
            if (direction != pDir && direction != Direction.DOWN) {
               BlockPos position = pos.offset(direction);
               this.d = position.toCenterPos().distanceTo(this.mc.player.getEyePos());
               if ((position.equals(this.lastPistonPos) || !(this.cd < this.d)) && !position.equals(cPos) && (!SettingUtils.oldCrystals() || !position.equals(cPos.up())) && (OLEPOSSUtils.replaceable(position) || this.mc.world.getBlockState(position).getBlock() instanceof RedstoneTorchBlock || this.mc.world.getBlockState(position).getBlock() instanceof FireBlock)) {
                  this.redstoneData = SettingUtils.getPlaceDataAND(position, (d) -> {
                     if (d == Direction.UP && !OLEPOSSUtils.solid(position.down())) {
                        return false;
                     } else {
                        return direction != d.getOpposite();
                     }
                  }, (b) -> {
                     if (pos.equals(b)) {
                        return false;
                     } else if (this.mc.world.getBlockState(b).getBlock() instanceof TorchBlock) {
                        return false;
                     } else {
                        return !(this.mc.world.getBlockState(b).getBlock() instanceof PistonBlock) && !(this.mc.world.getBlockState(b).getBlock() instanceof PistonHeadBlock);
                     }
                  });
                  if (this.redstoneData.valid() && SettingUtils.inPlaceRange(this.redstoneData.pos()) && SettingUtils.inMineRange(position)) {
                     this.cd = this.d;
                     cRedstonePos = position;
                     cRedstoneData = this.redstoneData;
                     if (position.equals(this.lastRedstonePos)) {
                        break;
                     }
                  }
               }
            }
         }

         this.redstonePos = cRedstonePos;
         this.redstoneData = cRedstoneData;
      } else {
         for(Direction direction : Direction.values()) {
            if (direction != pDir) {
               BlockPos position = pos.offset(direction);
               this.d = position.toCenterPos().distanceTo(this.mc.player.getEyePos());
               if ((position.equals(this.lastPistonPos) || !(this.cd < this.d)) && !position.equals(cPos) && (OLEPOSSUtils.replaceable(position) || this.mc.world.getBlockState(position).getBlock() == Blocks.REDSTONE_BLOCK) && !Box.from(new BlockBox(position)).intersects(OLEPOSSUtils.getCrystalBox(cPos)) && !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(position)), (entity) -> !entity.isSpectator() && entity instanceof PlayerEntity)) {
                  Objects.requireNonNull(pos);
                  this.redstoneData = SettingUtils.getPlaceDataOR(position, pos::equals);
                  if (this.redstoneData.valid()) {
                     this.cd = this.d;
                     cRedstonePos = position;
                     cRedstoneData = this.redstoneData;
                     if (position.equals(this.lastRedstonePos)) {
                        break;
                     }
                  }
               }
            }
         }

         this.redstonePos = cRedstonePos;
         this.redstoneData = cRedstoneData;
      }
   }

   private Entity crystalAt() {
      for(Entity entity : this.mc.world.getEntities()) {
         if (entity.getBlockPos().equals(this.crystalPos)) {
            return entity;
         }
      }

      return null;
   }

   private boolean upCheck(BlockPos pos) {
      double dx = this.mc.player.getEyePos().x - (double)pos.getX() - (double)0.5F;
      double dz = this.mc.player.getEyePos().z - (double)pos.getZ() - (double)0.5F;
      return Math.sqrt(dx * dx + dz * dz) > Math.abs(this.mc.player.getEyePos().y - (double)pos.getY() - (double)0.5F);
   }

   private boolean isRedstone(BlockPos pos) {
      return this.mc.world.getBlockState(pos).emitsRedstonePower();
   }

   private boolean blocked(BlockPos pos) {
      Block b = this.mc.world.getBlockState(pos).getBlock();
      if (b == Blocks.MOVING_PISTON) {
         return false;
      } else if (b == Blocks.PISTON_HEAD) {
         return false;
      } else if (b == Blocks.REDSTONE_TORCH) {
         return false;
      } else if (b instanceof FireBlock) {
         return false;
      } else {
         return !(this.mc.world.getBlockState(pos).getBlock() instanceof AirBlock);
      }
   }

   private Hand getHand(Item item) {
      return Managers.HOLDING.isHolding(item) ? Hand.MAIN_HAND : (this.mc.player.getOffHandStack().getItem() == item ? Hand.OFF_HAND : null);
   }

   private void resetPos() {
      this.crystalPos = null;
      this.pistonPos = null;
      this.firePos = null;
      this.redstonePos = null;
      this.pistonDir = null;
      this.pistonData = null;
      this.crystalPlaceDir = null;
      this.crystalDir = null;
      this.redstoneData = null;
   }

   public static enum SwitchMode {
      Disabled,
      Silent,
      PickSilent,
      InvSwitch;

      // $FF: synthetic method
      private static SwitchMode[] $values() {
         return new SwitchMode[]{Disabled, Silent, PickSilent, InvSwitch};
      }
   }

   public static enum Redstone {
      Torch(Items.REDSTONE_TORCH, Blocks.REDSTONE_TORCH),
      Block(Items.REDSTONE_BLOCK, Blocks.REDSTONE_BLOCK);

      public final Item i;
      public final Block b;

      private Redstone(Item i, Block b) {
         this.i = i;
         this.b = b;
      }

      // $FF: synthetic method
      private static Redstone[] $values() {
         return new Redstone[]{Torch, Block};
      }
   }
}
