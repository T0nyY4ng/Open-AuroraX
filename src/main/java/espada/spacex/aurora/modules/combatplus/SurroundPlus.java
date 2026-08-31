package espada.spacex.aurora.modules.combatplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.timers.TimerList;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.meteor.BODamageUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Direction.Type;

public class SurroundPlus extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgToggle;
   private final SettingGroup sgSpeed;
   private final SettingGroup sgBlocks;
   private final SettingGroup sgAttack;
   private final SettingGroup sgRender;
   private final Setting<Boolean> toggleModules;
   private final Setting<Boolean> toggleBack;
   private final Setting<List<Module>> modules;
   private final Setting<Boolean> center;
   private final Setting<Boolean> smartCenter;
   private final Setting<Boolean> phaseCenter;
   private final Setting<Boolean> pauseEat;
   private final Setting<Boolean> packet;
   private final Setting<SwitchMode> switchMode;
   private final Setting<Boolean> extend;
   private final Setting<Boolean> mandatory;
   private final Setting<Boolean> doubleHeight;
   private final Setting<Boolean> toggleMove;
   private final Setting<VerticalToggleMode> toggleVertical;
   private final Setting<PlaceDelayMode> placeDelayMode;
   private final Setting<Integer> placeDelayT;
   private final Setting<Double> placeDelayS;
   private final Setting<Integer> places;
   private final Setting<Double> cooldown;
   private final Setting<Double> singleCooldown;
   private final Setting<List<Block>> blocks;
   private final Setting<List<Block>> supportBlocks;
   private final Setting<Boolean> attack;
   private final Setting<Double> attackSpeed;
   private final Setting<Boolean> alwaysAttack;
   private final Setting<Boolean> antiCev;
   private final Setting<Boolean> placeSwing;
   private final Setting<SwingHand> placeHand;
   private final Setting<Boolean> attackSwing;
   private final Setting<SwingHand> attackHand;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<SettingColor> lineColor;
   private final Setting<SettingColor> sideColor;
   private final Setting<ShapeMode> supportShapeMode;
   private final Setting<SettingColor> supportLineColor;
   private final Setting<SettingColor> supportSideColor;
   private int tickTimer;
   private double timer;
   private final List<BlockPos> insideBlocks;
   public final List<BlockPos> surroundBlocks;
   private final List<BlockPos> supportPositions;
   private final List<BlockPos> valids;
   private final TimerList<BlockPos> placed;
   private final List<Render> render;
   private boolean support;
   private Hand hand;
   private int blocksLeft;
   private int placesLeft;
   private FindItemResult result;
   private boolean switched;
   private BlockPos lastPos;
   private boolean centered;
   private long lastAttack;
   private BlockPos currentPos;
   public static boolean placing = false;
   private final ArrayList<Module> toActivate;

   public SurroundPlus() {
      super(Aurora.CombatPlus, "Surround+", "Places blocks around your legs to protect from explosions.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgToggle = this.settings.createGroup("Toggle");
      this.sgSpeed = this.settings.createGroup("Speed");
      this.sgBlocks = this.settings.createGroup("Blocks");
      this.sgAttack = this.settings.createGroup("Attack");
      this.sgRender = this.settings.createGroup("Render");
      this.toggleModules = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Toggle Modules")).description("Turn off other modules when Cev Breaker is activated.")).defaultValue(false)).build());
      SettingGroup var10001 = this.sgGeneral;
      BoolSetting.Builder var10002 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Toggle Back On")).description("Turn the modules back on when Cev Breaker is deactivated.")).defaultValue(false);
      Setting<Boolean> var10003 = this.toggleModules;
      Objects.requireNonNull(var10003);
      this.toggleBack = var10001.add(((BoolSetting.Builder)var10002.visible(var10003::get)).build());
      var10001 = this.sgGeneral;
      ModuleListSetting.Builder var6 = (ModuleListSetting.Builder)((ModuleListSetting.Builder)(new ModuleListSetting.Builder()).name("modules")).description("Which modules to toggle.");
      var10003 = this.toggleModules;
      Objects.requireNonNull(var10003);
      this.modules = var10001.add(((ModuleListSetting.Builder)var6.visible(var10003::get)).build());
      this.center = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Center")).description("Moves to block center before surrounding.")).defaultValue(false)).build());
      var10001 = this.sgGeneral;
      BoolSetting.Builder var7 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Smart Center")).description("Only moves until whole hitbox is inside target block.")).defaultValue(true);
      var10003 = this.center;
      Objects.requireNonNull(var10003);
      this.smartCenter = var10001.add(((BoolSetting.Builder)var7.visible(var10003::get)).build());
      var10001 = this.sgGeneral;
      var7 = (BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Phase Friendly")).description("Doesn't center if clipped inside a block.")).defaultValue(true);
      var10003 = this.center;
      Objects.requireNonNull(var10003);
      this.phaseCenter = var10001.add(((BoolSetting.Builder)var7.visible(var10003::get)).build());
      this.pauseEat = this.addPauseEat(this.sgGeneral);
      this.packet = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Packet")).description("Use packet-based block placement.")).defaultValue(false)).build());
      this.switchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("Method of switching. Silent is the most reliable but delays crystals on some servers.")).defaultValue(SurroundPlus.SwitchMode.Silent)).build());
      this.extend = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Extend")).description(".")).defaultValue(true)).build());
      this.mandatory = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Mandatory")).description("Force placement of support blocks when no valid positions are available.")).defaultValue(false)).build());
      this.doubleHeight = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Double Height")).description("Place an additional layer of blocks above the surround to increase safety.")).defaultValue(false)).build());
      this.toggleMove = this.sgToggle.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Toggle Move")).description(".")).defaultValue(false)).build());
      this.toggleVertical = this.sgToggle.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Toggle Vertical")).description(".")).defaultValue(SurroundPlus.VerticalToggleMode.Up)).build());
      this.placeDelayMode = this.sgSpeed.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Place Delay Mode")).description(".")).defaultValue(SurroundPlus.PlaceDelayMode.Ticks)).build());
      this.placeDelayT = this.sgSpeed.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Place Tick Delay")).description("Tick delay between places.")).defaultValue(1)).min(1).sliderRange(0, 20).visible(() -> this.placeDelayMode.get() == SurroundPlus.PlaceDelayMode.Ticks)).build());
      this.placeDelayS = this.sgSpeed.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Place Delay")).description("Delay between places.")).defaultValue(0.1).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).visible(() -> this.placeDelayMode.get() == SurroundPlus.PlaceDelayMode.Seconds)).build());
      this.places = this.sgSpeed.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Places")).description("How many blocks to place each time.")).defaultValue(1)).min(1).sliderRange(0, 20).build());
      this.cooldown = this.sgSpeed.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Multi Cooldown")).description("Waits x seconds before trying to place at the same position if there is more than 1 missing block.")).defaultValue(0.3).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).build());
      this.singleCooldown = this.sgSpeed.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Single Cooldown")).description("Waits x seconds before trying to place at the same position if there is only 1 missing block.")).defaultValue(0.02).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).build());
      this.blocks = this.sgBlocks.add(((BlockListSetting.Builder)((BlockListSetting.Builder)(new BlockListSetting.Builder()).name("Blocks")).description("Blocks to use.")).defaultValue(new Block[]{Blocks.OBSIDIAN}).build());
      this.supportBlocks = this.sgBlocks.add(((BlockListSetting.Builder)((BlockListSetting.Builder)(new BlockListSetting.Builder()).name("Support Blocks")).description("Blocks to use for support.")).defaultValue(new Block[]{Blocks.OBSIDIAN}).build());
      this.attack = this.sgAttack.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Attack")).description("Attacks crystals blocking surround.")).defaultValue(false)).build());
      this.attackSpeed = this.sgAttack.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Attack Speed")).description("How many times to attack every second.")).defaultValue((double)4.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.alwaysAttack = this.sgAttack.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Always Attack")).description("Attacks crystals even when surround block isn't broken.")).defaultValue(false)).build());
      this.antiCev = this.sgAttack.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Anti CEV")).description("Attacks crystals placed on surround blocks.")).defaultValue(false)).build());
      this.placeSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Place Swing")).description("Renders swing animation when placing a block.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      EnumSetting.Builder var9 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Place Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.placeSwing;
      Objects.requireNonNull(var10003);
      this.placeHand = var10001.add(((EnumSetting.Builder)var9.visible(var10003::get)).build());
      this.attackSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Attack Swing")).description("Renders swing animation when placing a crystal.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      var9 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Attack Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.attackSwing;
      Objects.requireNonNull(var10003);
      this.attackHand = var10001.add(((EnumSetting.Builder)var9.visible(var10003::get)).build());
      this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description("Which parts of boxes should be rendered.")).defaultValue(ShapeMode.Both)).build());
      this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.supportShapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Support Shape Mode")).description("Which parts of boxes should be rendered.")).defaultValue(ShapeMode.Both)).build());
      this.supportLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Support Line Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 150)).build());
      this.supportSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Support Side Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.tickTimer = 0;
      this.timer = (double)0.0F;
      this.insideBlocks = new ArrayList();
      this.surroundBlocks = new ArrayList();
      this.supportPositions = new ArrayList();
      this.valids = new ArrayList();
      this.placed = new TimerList<BlockPos>();
      this.render = new ArrayList();
      this.support = false;
      this.hand = null;
      this.blocksLeft = 0;
      this.placesLeft = 0;
      this.result = null;
      this.switched = false;
      this.lastPos = null;
      this.centered = false;
      this.lastAttack = 0L;
      this.currentPos = null;
      this.toActivate = new ArrayList();
   }

   public void onActivate() {
      if ((Boolean)this.toggleModules.get() && !((List<Module>)(List<?>)this.modules.get()).isEmpty() && this.mc.world != null && this.mc.player != null) {
         for(Module module : (List<Module>)(List<?>)this.modules.get()) {
            if (module.isActive()) {
               module.toggle();
               this.toActivate.add(module);
            }
         }
      }

      this.tickTimer = (Integer)this.placeDelayT.get();
      this.timer = (Double)this.placeDelayS.get();
      this.placesLeft = (Integer)this.places.get();
      this.centered = false;
      this.lastPos = this.getPos();
      this.currentPos = this.getPos();
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

   @EventHandler(
      priority = 200
   )
   private void onBlock(BlockUpdateEvent event) {
      if (event.oldState.getBlock() != event.newState.getBlock() && !OLEPOSSUtils.replaceable(event.pos) && this.surroundBlocks.contains(event.pos)) {
         this.render.add(new Render(event.pos, System.currentTimeMillis()));
      }

   }

   @EventHandler(
      priority = 200
   )
   private void onTick(TickEvent.Pre event) {
      ++this.tickTimer;
   }

   @EventHandler(
      priority = 200
   )
   private void onRender(Render3DEvent event) {
      this.placed.update();
      placing = false;
      this.timer += event.frameTime;
      this.lastPos = this.currentPos;
      this.currentPos = this.getPos();
      this.setBB();
      if (!this.checkToggle()) {
         this.updateBlocks();
         this.updateSupport();
         this.surroundBlocks.stream().filter(OLEPOSSUtils::replaceable).forEach((block) -> event.renderer.box(block, (Color)this.sideColor.get(), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0));
         if ((Boolean)this.doubleHeight.get()) {
            this.surroundBlocks.stream().map(BlockPos::up).filter(OLEPOSSUtils::replaceable).forEach((block) -> event.renderer.box(block, (Color)this.sideColor.get(), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0));
         }

         this.supportPositions.forEach((block) -> event.renderer.box(block, (Color)this.supportSideColor.get(), (Color)this.supportLineColor.get(), (ShapeMode)this.supportShapeMode.get(), 0));
         this.render.removeIf((r) -> System.currentTimeMillis() - r.time > 1000L);
         this.render.forEach((r) -> {
            double progress = (double)1.0F - (double)Math.min(System.currentTimeMillis() - r.time, 500L) / (double)500.0F;
            event.renderer.box(r.pos, new Color(((SettingColor)this.sideColor.get()).r, ((SettingColor)this.sideColor.get()).g, ((SettingColor)this.sideColor.get()).b, (int)Math.round((double)((SettingColor)this.sideColor.get()).a * progress)), new Color(((SettingColor)this.lineColor.get()).r, ((SettingColor)this.lineColor.get()).g, ((SettingColor)this.lineColor.get()).b, (int)Math.round((double)((SettingColor)this.lineColor.get()).a * progress)), (ShapeMode)this.shapeMode.get(), 0);
         });
         if (!(Boolean)this.pauseEat.get() || !this.mc.player.isUsingItem()) {
            this.placeBlocks();
         }
      }
   }

   private void updateAttack() {
      if ((Boolean)this.attack.get()) {
         if (!((double)(System.currentTimeMillis() - this.lastAttack) < (double)1000.0F / (Double)this.attackSpeed.get())) {
            Entity blocking = this.getBlocking();
            if (blocking != null) {
               if (!SettingUtils.shouldRotate(RotationType.Attacking) || Managers.ROTATION.start(blocking.getBoundingBox(), (double)this.priority - 0.1, RotationType.Attacking, (long)Objects.hash(new Object[]{this.name + "attacking"}))) {
                  SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.MAIN_HAND);
                  this.sendPacket(PlayerInteractEntityC2SPacket.attack(blocking, this.mc.player.isSneaking()));
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

   private Entity getBlocking() {
      Entity crystal = null;
      double lowest = (double)1000.0F;

      for(Entity entity : this.mc.world.getEntities()) {
         if (entity instanceof EndCrystalEntity && !(this.mc.player.distanceTo(entity) > 5.0F) && SettingUtils.inAttackRange(entity.getBoundingBox())) {
            if ((Boolean)this.antiCev.get()) {
               for(BlockPos pos : this.surroundBlocks) {
                  if (entity.getBlockPos().equals(pos.up())) {
                     double dmg = Math.max((double)10.0F, BODamageUtils.crystal(this.mc.player, this.mc.player.getBoundingBox(), entity.getPos(), (BlockPos)null, false));
                     if (dmg < lowest) {
                        lowest = dmg;
                        crystal = entity;
                     }
                  }
               }
            }

            for(BlockPos pos : (Boolean)this.alwaysAttack.get() ? this.surroundBlocks : this.valids) {
               if (Box.from(new BlockBox(pos)).intersects(entity.getBoundingBox())) {
                  double dmg = BODamageUtils.crystal(this.mc.player, this.mc.player.getBoundingBox(), entity.getPos(), (BlockPos)null, false);
                  if (dmg < lowest) {
                     crystal = entity;
                     lowest = dmg;
                  }
               }
            }
         }
      }

      return crystal;
   }

   private void setBB() {
      if (!this.centered && (Boolean)this.center.get() && this.mc.player.isOnGround() && (!(Boolean)this.phaseCenter.get() || !OLEPOSSUtils.inside(this.mc.player, this.mc.player.getBoundingBox().shrink(0.01, 0.01, 0.01)))) {
         double targetX;
         double targetZ;
         if ((Boolean)this.smartCenter.get()) {
            targetX = MathHelper.clamp(this.mc.player.getX(), (double)this.currentPos.getX() + 0.31, (double)this.currentPos.getX() + 0.69);
            targetZ = MathHelper.clamp(this.mc.player.getZ(), (double)this.currentPos.getZ() + 0.31, (double)this.currentPos.getZ() + 0.69);
         } else {
            targetX = (double)this.currentPos.getX() + (double)0.5F;
            targetZ = (double)this.currentPos.getZ() + (double)0.5F;
         }

         double dist = (new Vec3d(targetX, (double)0.0F, targetZ)).distanceTo(new Vec3d(this.mc.player.getX(), (double)0.0F, this.mc.player.getZ()));
         if (dist < 0.2873) {
            this.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(targetX, this.mc.player.getY(), targetZ, Managers.ON_GROUND.isOnGround()));
         }

         double x = this.mc.player.getX();
         double z = this.mc.player.getZ();

         for(int i = 0; (double)i < Math.ceil(dist / 0.2873); ++i) {
            double yaw = Rotations.getYaw(new Vec3d(targetX, (double)0.0F, targetZ)) + (double)90.0F;
            x += Math.cos(Math.toRadians(yaw)) * 0.2873;
            z += Math.sin(Math.toRadians(yaw)) * 0.2873;
            this.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, this.mc.player.getY(), z, Managers.ON_GROUND.isOnGround()));
         }

         this.mc.player.setPos(targetX, this.mc.player.getY(), targetZ);
         this.mc.player.setBoundingBox(new Box(targetX - 0.3, this.mc.player.getY(), targetZ - 0.3, targetX + 0.3, this.mc.player.getY() + (this.mc.player.getBoundingBox().maxY - this.mc.player.getBoundingBox().minY), targetZ + 0.3));
         this.centered = true;
      }

   }

   private boolean checkToggle() {
      if (this.lastPos != null) {
         if ((Boolean)this.toggleMove.get() && (this.currentPos.getX() != this.lastPos.getX() || this.currentPos.getZ() != this.lastPos.getZ())) {
            this.toggle();
            this.sendToggledMsg("moved horizontally");
            return true;
         }

         if ((this.toggleVertical.get() == SurroundPlus.VerticalToggleMode.Up || this.toggleVertical.get() == SurroundPlus.VerticalToggleMode.Any) && this.currentPos.getY() > this.lastPos.getY()) {
            this.toggle();
            this.sendToggledMsg("moved up");
            return true;
         }

         if ((this.toggleVertical.get() == SurroundPlus.VerticalToggleMode.Down || this.toggleVertical.get() == SurroundPlus.VerticalToggleMode.Any) && this.currentPos.getY() < this.lastPos.getY()) {
            this.toggle();
            this.sendToggledMsg("moved down");
            return true;
         }
      }

      return false;
   }

   private void placeBlocks() {
      List<BlockPos> positions = new ArrayList();
      this.setSupport();
      if ((Boolean)this.doubleHeight.get()) {
         List<BlockPos> upperBlocks = new ArrayList();
         this.surroundBlocks.forEach((pos) -> {
            BlockPos upperPos = pos.up();
            if (OLEPOSSUtils.replaceable(upperPos) && !positions.contains(upperPos)) {
               upperBlocks.add(upperPos);
            }

         });
         positions.addAll(upperBlocks);
      }

      if (this.support) {
         positions.addAll(this.supportPositions);
      } else {
         positions.addAll(this.surroundBlocks);
      }

      this.valids.clear();
      this.valids.addAll(positions.stream().filter(this::validBlock).toList());
      this.updateAttack();
      this.updateResult();
      this.updatePlaces();
      this.blocksLeft = Math.min(this.placesLeft, this.result.count());
      this.hand = this.getHand();
      this.switched = false;
      List<BlockPos> positionsToPlace = this.valids;
      if ((Boolean)this.mandatory.get() && this.valids.isEmpty()) {
         positionsToPlace = this.supportPositions.stream().filter(this::validBlock).toList();
      }

      positionsToPlace.stream().filter((pos) -> !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(pos)), this::validEntity)).sorted(Comparator.comparingDouble(Rotations::getYaw)).forEach(this::place);
      if (this.switched && this.hand == null) {
         switch (((SwitchMode)this.switchMode.get()).ordinal()) {
            case 2 -> InvUtils.swapBack();
            case 3 -> BOInvUtils.pickSwapBack();
            case 4 -> BOInvUtils.swapBack();
         }
      }

   }

   private void updatePlaces() {
      switch (((PlaceDelayMode)this.placeDelayMode.get()).ordinal()) {
         case 0:
            if (this.placesLeft >= (Integer)this.places.get() || this.tickTimer >= (Integer)this.placeDelayT.get()) {
               this.placesLeft = (Integer)this.places.get();
               this.tickTimer = 0;
            }
            break;
         case 1:
            if (this.placesLeft >= (Integer)this.places.get() || this.timer >= (Double)this.placeDelayS.get()) {
               this.placesLeft = (Integer)this.places.get();
               this.timer = (double)0.0F;
            }
      }

   }

   private boolean validBlock(BlockPos pos) {
      if (!OLEPOSSUtils.replaceable(pos)) {
         return false;
      } else {
         TimerList var10001 = this.placed;
         Objects.requireNonNull(var10001);
         PlaceData data = SettingUtils.getPlaceDataOR(pos, var10001::contains);
         if (!data.valid()) {
            return false;
         } else if (!SettingUtils.inPlaceRange(data.pos())) {
            return false;
         } else {
            return !this.placed.contains(pos);
         }
      }
   }

   private void place(BlockPos pos) {
      if (this.blocksLeft > 0) {
         TimerList var10001 = this.placed;
         Objects.requireNonNull(var10001);
         PlaceData data = SettingUtils.getPlaceDataOR(pos, var10001::contains);
         if (data != null && data.valid()) {
            placing = true;
            if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(data.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}))) {
               if (!this.switched && this.hand == null) {
                  switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                     case 1:
                     case 2:
                        InvUtils.swap(this.result.slot(), true);
                        this.switched = true;
                        break;
                     case 3:
                        this.switched = BOInvUtils.pickSwitch(this.result.slot());
                        break;
                     case 4:
                        this.switched = BOInvUtils.invSwitch(this.result.slot());
                  }
               }

               if (this.switched || this.hand != null) {
                  this.placeBlock(this.hand == null ? Hand.MAIN_HAND : this.hand, data.pos().toCenterPos(), data.dir(), data.pos());
                  if ((Boolean)this.placeSwing.get()) {
                     this.clientSwing((SwingHand)this.placeHand.get(), this.hand == null ? Hand.MAIN_HAND : this.hand);
                  }

                  this.placed.add(pos, this.oneMissing() ? (Double)this.singleCooldown.get() : (Double)this.cooldown.get());
                  --this.blocksLeft;
                  --this.placesLeft;
                  if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                     Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
                  }

               }
            }
         }
      }
   }

   private boolean oneMissing() {
      boolean alreadyFound = false;

      for(BlockPos pos : this.surroundBlocks) {
         if (OLEPOSSUtils.replaceable(pos)) {
            if (alreadyFound) {
               return false;
            }

            alreadyFound = true;
         }
      }

      return true;
   }

   private void setSupport() {
      this.support = false;
      double min = (double)10000.0F;

      for(BlockPos pos : this.surroundBlocks) {
         if (this.validBlock(pos)) {
            double y = Rotations.getYaw(pos.toCenterPos());
            if (y < min) {
               this.support = false;
               min = y;
            }
         }
      }

      for(BlockPos pos : this.supportPositions) {
         if (this.validBlock(pos)) {
            double y = Rotations.getYaw(pos.toCenterPos());
            if (y < min) {
               this.support = true;
               min = y;
            }
         }
      }

   }

   private boolean valid(ItemStack stack) {
      Item var3 = stack.getItem();
      boolean var10000;
      if (var3 instanceof BlockItem block) {
         if (((List)(this.support ? this.supportBlocks : this.blocks).get()).contains(block.getBlock())) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   private void updateResult() {
      FindItemResult var10001;
      switch (((SwitchMode)this.switchMode.get()).ordinal()) {
         case 0:
            var10001 = null;
            break;
         case 1:
         case 2:
            var10001 = InvUtils.findInHotbar(this::valid);
            break;
         case 3:
         case 4:
            var10001 = InvUtils.find(this::valid);
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      this.result = var10001;
   }

   private Hand getHand() {
      if (this.valid(Managers.HOLDING.getStack())) {
         return Hand.MAIN_HAND;
      } else {
         return this.valid(this.mc.player.getOffHandStack()) ? Hand.OFF_HAND : null;
      }
   }

   private void updateSupport() {
      this.supportPositions.clear();
      this.surroundBlocks.forEach(this::addSupport);
   }

   private void addSupport(BlockPos pos) {
      if (OLEPOSSUtils.replaceable(pos)) {
         if (!this.hasSupport(pos, true)) {
            PlaceData data = SettingUtils.getPlaceData(pos);
            if (!data.valid()) {
               for(Direction dir : Direction.values()) {
                  if (dir != Direction.UP && !this.surroundBlocks.contains(pos.offset(dir)) && !this.insideBlocks.contains(pos.offset(dir)) && !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(pos.offset(dir))), (entity) -> entity instanceof PlayerEntity && !entity.isSpectator()) && SettingUtils.getPlaceData(pos.offset(dir)).valid() && SettingUtils.inPlaceRange(pos.offset(dir))) {
                     this.supportPositions.add(pos.offset(dir));
                     return;
                  }
               }

            }
         }
      }
   }

   private boolean hasSupport(BlockPos pos, boolean checkNext) {
      for(Direction dir : Direction.values()) {
         if (this.supportPositions.contains(pos.offset(dir)) || checkNext && this.hasSupport(pos.offset(dir), false)) {
            return true;
         }
      }

      return false;
   }

   private void updateBlocks() {
      this.updateInsideBlocks();
      this.getSurroundBlocks();
      this.insideBlocks.forEach((pos) -> this.surroundBlocks.add(pos.down()));
   }

   private void updateInsideBlocks() {
      this.insideBlocks.clear();
      this.addBlocks(this.getPos(), this.getSize(this.mc.player));
      if ((Boolean)this.extend.get()) {
         this.mc.world.getPlayers().stream().filter((player) -> this.mc.player.distanceTo(player) < 5.0F && player != this.mc.player).sorted(Comparator.comparingDouble((player) -> (double)this.mc.player.distanceTo(player))).forEach((player) -> {
            if (this.intersects(player)) {
               this.addBlocks(player.getBlockPos(), this.getSize(player));
            }
         });
      }

   }

   private boolean intersects(PlayerEntity player) {
      this.getSurroundBlocks();

      for(BlockPos pos : this.surroundBlocks) {
         if (player.getBoundingBox().intersects(Box.from(new BlockBox(pos)))) {
            return true;
         }
      }

      return false;
   }

   private void getSurroundBlocks() {
      this.surroundBlocks.clear();
      this.insideBlocks.forEach((pos) -> {
         for(Direction dir : Type.HORIZONTAL) {
            if (!this.surroundBlocks.contains(pos.offset(dir)) && !this.insideBlocks.contains(pos.offset(dir))) {
               this.surroundBlocks.add(pos.offset(dir));
            }
         }

      });
   }

   private void addBlocks(BlockPos pos, int[] size) {
      for(int x = size[0]; x <= size[1]; ++x) {
         for(int z = size[2]; z <= size[3]; ++z) {
            BlockPos p = pos.add(x, 0, z);
            if ((!(this.mc.world.getBlockState(p).getBlock().getBlastResistance() > 600.0F) || p.equals(this.currentPos)) && !this.insideBlocks.contains(pos.add(x, 0, z).withY(this.currentPos.getY()))) {
               this.insideBlocks.add(pos.add(x, 0, z).withY(this.currentPos.getY()));
            }
         }
      }

   }

   private boolean validEntity(Entity entity) {
      if (entity instanceof EndCrystalEntity && System.currentTimeMillis() - this.lastAttack < 100L) {
         return false;
      } else {
         return !(entity instanceof ItemEntity);
      }
   }

   private int[] getSize(PlayerEntity player) {
      int[] size = new int[4];
      double x = player.getX() - (double)player.getBlockX();
      double z = player.getZ() - (double)player.getBlockZ();
      if (x < 0.3) {
         size[0] = -1;
      }

      if (x > 0.7) {
         size[1] = 1;
      }

      if (z < 0.3) {
         size[2] = -1;
      }

      if (z > 0.7) {
         size[3] = 1;
      }

      return size;
   }

   public BlockPos getPos() {
      return new BlockPos(this.mc.player.getBlockX(), (int)Math.round(this.mc.player.getY()), this.mc.player.getBlockZ());
   }

   public static record Render(BlockPos pos, long time) {
   }

   public static enum SwitchMode {
      Disabled,
      Normal,
      Silent,
      PickSilent,
      InvSwitch;

      // $FF: synthetic method
      private static SwitchMode[] $values() {
         return new SwitchMode[]{Disabled, Normal, Silent, PickSilent, InvSwitch};
      }
   }

   public static enum VerticalToggleMode {
      Disabled,
      Up,
      Down,
      Any;

      // $FF: synthetic method
      private static VerticalToggleMode[] $values() {
         return new VerticalToggleMode[]{Disabled, Up, Down, Any};
      }
   }

   public static enum PlaceDelayMode {
      Ticks,
      Seconds;

      // $FF: synthetic method
      private static PlaceDelayMode[] $values() {
         return new PlaceDelayMode[]{Ticks, Seconds};
      }
   }
}
