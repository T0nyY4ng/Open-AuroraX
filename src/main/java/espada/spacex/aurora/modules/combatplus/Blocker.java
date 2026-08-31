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
import espada.spacex.aurora.utils.HoleUtils;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.meteor.BODamageUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
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
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.math.BlockBox;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;

public class Blocker extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgProtection;
   private final SettingGroup sgSpeed;
   private final SettingGroup sgBlocks;
   private final SettingGroup sgAttack;
   private final SettingGroup sgDamage;
   private final SettingGroup sgRender;
   private final Setting<Boolean> oldVer;
   private final Setting<Boolean> pauseEat;
   private final Setting<SwitchMode> switchMode;
   private final Setting<Double> mineTime;
   private final Setting<Double> maxMineTime;
   private final Setting<Boolean> packet;
   private final Setting<Boolean> onlyHole;
   private final Setting<Boolean> surroundFloor;
   private final Setting<Boolean> surroundFloorBottom;
   private final Setting<Boolean> surroundSides;
   private final Setting<Boolean> surroundTop;
   private final Setting<Boolean> surroundBottom;
   private final Setting<Boolean> trapCev;
   private final Setting<Boolean> cev;
   private final Setting<SurroundPlus.PlaceDelayMode> placeDelayMode;
   private final Setting<Integer> placeDelayT;
   private final Setting<Double> placeDelayS;
   private final Setting<Integer> places;
   private final Setting<Double> cooldown;
   private final Setting<List<Block>> blocks;
   private final Setting<Boolean> attack;
   private final Setting<Double> attackSpeed;
   private final Setting<Boolean> always;
   private final Setting<Double> maxDmg;
   private final Setting<Boolean> placeSwing;
   private final Setting<SwingHand> placeHand;
   private final Setting<Boolean> attackSwing;
   private final Setting<SwingHand> attackHand;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<SettingColor> lineColor;
   private final Setting<SettingColor> sideColor;
   private final List<MineStart> mining;
   private MineStart mineStart;
   private final List<ProtectBlock> toProtect;
   private final List<BlockPos> placePositions;
   private final List<Render> render;
   private final TimerList<BlockPos> placed;
   private int blocksLeft;
   private int placesLeft;
   private FindItemResult result;
   private boolean switched;
   private Hand hand;
   private int tickTimer;
   private double timer;
   private long lastTime;
   private long lastAttack;

   public Blocker() {
      super(Aurora.CombatPlus, "Blocker", "Covers your surround blocks if any enemy tries to mine them.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgProtection = this.settings.createGroup("Protection");
      this.sgSpeed = this.settings.createGroup("Speed");
      this.sgBlocks = this.settings.createGroup("Blocks");
      this.sgAttack = this.settings.createGroup("Attack");
      this.sgDamage = this.settings.createGroup("Damage");
      this.sgRender = this.settings.createGroup("Render");
      this.oldVer = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("1.12 Crystals")).description("Uses 1.12.2 crystal mechanics.")).defaultValue(false)).build());
      this.pauseEat = this.addPauseEat(this.sgGeneral);
      this.switchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("Method of switching. Silent is the most reliable.")).defaultValue(Blocker.SwitchMode.Silent)).build());
      this.mineTime = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Mine Time")).description("How long do we let enemies mine our surround for before protecting it.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.maxMineTime = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Max Mine Time")).description("Ignores mining after x seconds.")).defaultValue((double)5.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.packet = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Packet")).description(".")).defaultValue(false)).build());
      this.onlyHole = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Only Hole")).description("Only protects when you are in a hole.")).defaultValue(true)).build());
      this.surroundFloor = this.sgProtection.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Surround Floor")).description("Places blocks around to surround floor blocks.")).defaultValue(true)).build());
      this.surroundFloorBottom = this.sgProtection.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Surround Floor Bottom")).description("Places blocks under surround floor blocks.")).defaultValue(true)).build());
      this.surroundSides = this.sgProtection.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Surround Sides")).description("Places blocks next to surround blocks.")).defaultValue(true)).build());
      this.surroundTop = this.sgProtection.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Surround Side Top")).description("Places a block on top of surround.")).defaultValue(true)).build());
      this.surroundBottom = this.sgProtection.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Surround Side Bottom")).description("Places a block on bottom of surround.")).defaultValue(true)).build());
      this.trapCev = this.sgProtection.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Trap Cev")).description("Places on top of trap side block.")).defaultValue(true)).build());
      this.cev = this.sgProtection.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Cev")).description("Places on top of trap top blocks.")).defaultValue(true)).build());
      this.placeDelayMode = this.sgSpeed.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Place Delay Mode")).description(".")).defaultValue(SurroundPlus.PlaceDelayMode.Ticks)).build());
      this.placeDelayT = this.sgSpeed.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Place Tick Delay")).description("Tick delay between places.")).defaultValue(1)).min(1).sliderRange(0, 20).visible(() -> this.placeDelayMode.get() == SurroundPlus.PlaceDelayMode.Ticks)).build());
      this.placeDelayS = this.sgSpeed.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Place Delay")).description("Delay between places.")).defaultValue(0.1).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).visible(() -> this.placeDelayMode.get() == SurroundPlus.PlaceDelayMode.Seconds)).build());
      this.places = this.sgSpeed.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Places")).description("How many blocks to place each time.")).defaultValue(1)).min(1).sliderRange(0, 20).build());
      this.cooldown = this.sgSpeed.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Cooldown")).description("Waits x seconds before trying to place at the same position if there is only 1 missing block.")).defaultValue((double)0.5F).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).build());
      this.blocks = this.sgBlocks.add(((BlockListSetting.Builder)((BlockListSetting.Builder)(new BlockListSetting.Builder()).name("Blocks")).description("Blocks to use.")).defaultValue(new Block[]{Blocks.OBSIDIAN}).build());
      this.attack = this.sgAttack.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Attack")).description("Attacks crystals blocking surround.")).defaultValue(false)).build());
      this.attackSpeed = this.sgAttack.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Attack Speed")).description("How many times to attack every second.")).defaultValue((double)4.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.always = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Always")).description("Doesn't check for damages.")).defaultValue(true)).build());
      this.maxDmg = this.sgDamage.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Min Damage")).description("Doesn't place if you would take less damage than this.")).defaultValue((double)6.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).visible(() -> !(Boolean)this.always.get())).build());
      this.placeSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Place Swing")).description("Renders swing animation when placing a block.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgRender;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Place Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      Setting<Boolean> var10003 = this.placeSwing;
      Objects.requireNonNull(var10003);
      this.placeHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.attackSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Attack Swing")).description("Renders swing animation when placing a crystal.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Attack Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.attackSwing;
      Objects.requireNonNull(var10003);
      this.attackHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description("Which parts of boxes should be rendered.")).defaultValue(ShapeMode.Both)).build());
      this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.mining = new ArrayList();
      this.mineStart = null;
      this.toProtect = new ArrayList();
      this.placePositions = new ArrayList();
      this.render = new ArrayList();
      this.placed = new TimerList<BlockPos>();
      this.blocksLeft = 0;
      this.placesLeft = 0;
      this.result = null;
      this.switched = false;
      this.hand = null;
      this.tickTimer = 0;
      this.timer = (double)0.0F;
      this.lastTime = 0L;
      this.lastAttack = 0L;
   }

   @EventHandler(
      priority = 200
   )
   private void onBlock(BlockUpdateEvent event) {
      if (event.oldState.getBlock() != event.newState.getBlock() && !OLEPOSSUtils.replaceable(event.pos) && this.placePositions.contains(event.pos)) {
         this.render.add(new Render(event.pos, System.currentTimeMillis()));
      }

   }

   @EventHandler(
      priority = 200
   )
   private void onTickPre(TickEvent.Pre event) {
      ++this.tickTimer;
   }

   @EventHandler(
      priority = 200
   )
   private void onRender(Render3DEvent event) {
      this.placed.update();
      if (this.mc.player != null && this.mc.world != null) {
         this.timer += (double)(System.currentTimeMillis() - this.lastTime) / (double)1000.0F;
         this.lastTime = System.currentTimeMillis();
         this.updateBlocks();
         if (this.mineStart != null && this.contains()) {
            this.mineStart = null;
         }

         this.mining.removeIf((m) -> (double)System.currentTimeMillis() > (double)m.time + (Double)this.maxMineTime.get() * (double)1000.0F || this.mineStart != null && m.id == this.mineStart.id || !OLEPOSSUtils.solid2(m.pos));
         if (this.mineStart != null) {
            this.mining.add(this.mineStart);
            this.mineStart = null;
         }

         this.updatePlacing();
         this.render.removeIf((r) -> System.currentTimeMillis() - r.time > 1000L);
         this.render.forEach((r) -> {
            double progress = (double)1.0F - (double)Math.min(System.currentTimeMillis() - r.time, 500L) / (double)500.0F;
            event.renderer.box(r.pos, new Color(((SettingColor)this.sideColor.get()).r, ((SettingColor)this.sideColor.get()).g, ((SettingColor)this.sideColor.get()).b, (int)Math.round((double)((SettingColor)this.sideColor.get()).a * progress)), new Color(((SettingColor)this.lineColor.get()).r, ((SettingColor)this.lineColor.get()).g, ((SettingColor)this.lineColor.get()).b, (int)Math.round((double)((SettingColor)this.lineColor.get()).a * progress)), (ShapeMode)this.shapeMode.get(), 0);
         });
      }
   }

   @EventHandler(
      priority = 200
   )
   private void onReceive(PacketEvent.Receive event) {
      Packet var3 = event.packet;
      if (var3 instanceof BlockBreakingProgressS2CPacket p) {
         this.mineStart = new MineStart(p.getPos(), p.getEntityId(), System.currentTimeMillis());
      }

   }

   private void updatePlacing() {
      if (!(Boolean)this.pauseEat.get() || !this.mc.player.isUsingItem()) {
         this.updateResult();
         this.updatePlaces();
         this.blocksLeft = Math.min(this.placesLeft, this.result.count());
         this.hand = this.getHand();
         this.switched = false;
         this.placePositions.clear();
         this.toProtect.stream().filter(this::shouldProtect).forEach(this::addPlacePositions);
         this.updateAttack();
         this.placePositions.stream().filter((pos) -> !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(pos)), (entity) -> entity instanceof EndCrystalEntity && System.currentTimeMillis() - this.lastAttack > 100L)).forEach(this::place);
         if (this.switched && this.hand == null) {
            switch (((SwitchMode)this.switchMode.get()).ordinal()) {
               case 0:
                  InvUtils.swapBack();
               case 1:
               default:
                  break;
               case 2:
                  BOInvUtils.pickSwapBack();
                  break;
               case 3:
                  BOInvUtils.swapBack();
            }
         }

      }
   }

   private void addPlacePositions(ProtectBlock p) {
      switch (p.type) {
         case 0:
         case 1:
            for(Direction dir : Direction.values()) {
               if (p.type == 1) {
                  if (!(Boolean)this.surroundSides.get() && dir.getAxis().isHorizontal() || !(Boolean)this.surroundTop.get() && dir == Direction.UP || !(Boolean)this.surroundBottom.get() && dir == Direction.DOWN) {
                     continue;
                  }
               } else if (dir == Direction.UP || !(Boolean)this.surroundFloor.get() && dir.getAxis().isHorizontal() || !(Boolean)this.surroundFloorBottom.get() && dir == Direction.DOWN) {
                  continue;
               }

               BlockPos pos = p.pos.offset(dir);
               if (OLEPOSSUtils.replaceable(pos)) {
                  PlaceData data = SettingUtils.getPlaceData(pos);
                  if (data.valid() && SettingUtils.inPlaceRange(data.pos()) && !EntityUtils.intersectsWithEntity(new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 1), (double)(pos.getZ() + 1)), this::validForIntersects)) {
                     this.placePositions.add(pos);
                  }
               }
            }
            break;
         case 2:
         case 3:
            BlockPos pos = p.pos.up();
            if (!OLEPOSSUtils.replaceable(pos)) {
               return;
            }

            PlaceData data = SettingUtils.getPlaceData(pos);
            if (!data.valid()) {
               return;
            }

            if (!SettingUtils.inPlaceRange(data.pos())) {
               return;
            }

            if (EntityUtils.intersectsWithEntity(new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 1), (double)(pos.getZ() + 1)), this::validForIntersects)) {
               return;
            }

            this.placePositions.add(pos);
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
            for(BlockPos pos : this.placePositions) {
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

   private Hand getHand() {
      if (this.valid(Managers.HOLDING.getStack())) {
         return Hand.MAIN_HAND;
      } else {
         return this.valid(this.mc.player.getOffHandStack()) ? Hand.OFF_HAND : null;
      }
   }

   private void updateResult() {
      FindItemResult var10001;
      switch (((SwitchMode)this.switchMode.get()).ordinal()) {
         case 0:
         case 1:
            var10001 = InvUtils.findInHotbar(this::valid);
            break;
         case 2:
         case 3:
            var10001 = InvUtils.find(this::valid);
            break;
         case 4:
            var10001 = null;
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      this.result = var10001;
   }

   private boolean valid(ItemStack stack) {
      Item var3 = stack.getItem();
      boolean var10000;
      if (var3 instanceof BlockItem block) {
         if (((List)this.blocks.get()).contains(block.getBlock())) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   private void updatePlaces() {
      switch ((SurroundPlus.PlaceDelayMode)this.placeDelayMode.get()) {
         case Ticks:
            if (this.placesLeft >= (Integer)this.places.get() || this.tickTimer >= (Integer)this.placeDelayT.get()) {
               this.placesLeft = (Integer)this.places.get();
               this.tickTimer = 0;
            }
            break;
         case Seconds:
            if (this.placesLeft >= (Integer)this.places.get() || this.timer >= (Double)this.placeDelayS.get()) {
               this.placesLeft = (Integer)this.places.get();
               this.timer = (double)0.0F;
            }
      }

   }

   private void place(BlockPos pos) {
      if (this.blocksLeft > 0) {
         TimerList var10001 = this.placed;
         Objects.requireNonNull(var10001);
         PlaceData data = SettingUtils.getPlaceDataOR(pos, var10001::contains);
         if (data != null && data.valid()) {
            if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(data.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}))) {
               if (!this.switched && this.hand == null) {
                  switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                     case 0:
                     case 1:
                        InvUtils.swap(this.result.slot(), true);
                        this.switched = true;
                        break;
                     case 2:
                        this.switched = BOInvUtils.pickSwitch(this.result.slot());
                        break;
                     case 3:
                        this.switched = BOInvUtils.invSwitch(this.result.slot());
                  }
               }

               if (this.switched || this.hand != null) {
                  this.placeBlock(this.hand == null ? Hand.MAIN_HAND : this.hand, data.pos().toCenterPos(), data.dir(), data.pos());
                  if ((Boolean)this.placeSwing.get()) {
                     this.clientSwing((SwingHand)this.placeHand.get(), this.hand == null ? Hand.MAIN_HAND : this.hand);
                  }

                  if (!(Boolean)this.packet.get()) {
                     this.setBlock(pos);
                  }

                  this.placed.add(pos, (Double)this.cooldown.get());
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

   private void setBlock(BlockPos pos) {
      Item item = this.mc.player.getInventory().getStack(this.result.slot()).getItem();
      if (item instanceof BlockItem block) {
         this.mc.world.setBlockState(pos, block.getBlock().getDefaultState());
         this.mc.world.playSound((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
      }
   }

   private boolean shouldProtect(ProtectBlock p) {
      BlockPos pos = p.pos;
      switch (p.type) {
         case 1:
            if (!OLEPOSSUtils.solid2(pos) || this.mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK) {
               return false;
            }
            break;
         case 2:
         case 3:
            if (this.mc.world.getBlockState(p.pos).getBlock() != Blocks.OBSIDIAN) {
               return false;
            }

            if (!(this.mc.world.getBlockState(p.pos.up()).getBlock() instanceof AirBlock)) {
               return false;
            }

            if ((Boolean)this.oldVer.get() && !(this.mc.world.getBlockState(p.pos.up(2)).getBlock() instanceof AirBlock)) {
               return false;
            }
      }

      if (!this.containsPos(pos)) {
         return false;
      } else {
         return this.damageCheck(pos, p.type);
      }
   }

   private void updateBlocks() {
      this.toProtect.clear();
      if (!(Boolean)this.onlyHole.get() || HoleUtils.inHole(this.mc.player)) {
         BlockPos e = BlockPos.ofFloored(this.mc.player.getX(), this.mc.player.getBoundingBox().maxY, this.mc.player.getZ());
         BlockPos pos = new BlockPos(this.mc.player.getBlockX(), (int)Math.round(this.mc.player.getY()), this.mc.player.getBlockZ());
         int[] size = new int[4];
         double xOffset = this.mc.player.getX() - (double)this.mc.player.getBlockX();
         double zOffset = this.mc.player.getZ() - (double)this.mc.player.getBlockZ();
         if (xOffset < 0.3) {
            size[0] = -1;
         }

         if (xOffset > 0.7) {
            size[1] = 1;
         }

         if (zOffset < 0.3) {
            size[2] = -1;
         }

         if (zOffset > 0.7) {
            size[3] = 1;
         }

         this.updateSurround(pos, size);
         if ((Boolean)this.trapCev.get()) {
            this.updateEyes(e, size);
         }

         if ((Boolean)this.cev.get()) {
            this.updateTop(e.up());
         }

      }
   }

   private void updateTop(BlockPos pos) {
      this.toProtect.add(new ProtectBlock(pos, 3));
   }

   private void updateEyes(BlockPos pos, int[] size) {
      for(int x = size[0] - 1; x <= size[1] + 1; ++x) {
         for(int z = size[2] - 1; z <= size[3] + 1; ++z) {
            if (x != size[0] - 1 && x != size[1] + 1 || z != size[2] - 1 && z != size[3] + 1) {
               this.toProtect.add(new ProtectBlock(pos.add(x, 0, z), 2));
            }
         }
      }

   }

   private void updateSurround(BlockPos pos, int[] size) {
      for(int y = -1; y <= 0; ++y) {
         for(int x = size[0] - 1; x <= size[1] + 1; ++x) {
            for(int z = size[2] - 1; z <= size[3] + 1; ++z) {
               boolean bx = x == size[0] - 1 || x == size[1] + 1;
               boolean by = y == -1;
               boolean bz = z == size[2] - 1 || z == size[3] + 1;
               if (by) {
                  if (!bx && !bz) {
                     this.toProtect.add(new ProtectBlock(pos.add(x, y, z), 0));
                  }
               } else if (!bx || !bz) {
                  this.toProtect.add(new ProtectBlock(pos.add(x, y, z), 1));
               }
            }
         }
      }

   }

   private boolean validForIntersects(Entity entity) {
      return !(entity instanceof ItemEntity) && !(entity instanceof EndCrystalEntity);
   }

   private boolean damageCheck(BlockPos blockPos, int type) {
      if ((Boolean)this.always.get()) {
         return true;
      } else {
         switch (type) {
            case 1:
               for(int x = -2; x <= 2; ++x) {
                  for(int y = -2; y <= 2; ++y) {
                     for(int z = -2; z <= 2; ++z) {
                        BlockPos pos = blockPos.add(x, y, z);
                        if (this.mc.world.getBlockState(pos).getBlock() instanceof AirBlock && (!(Boolean)this.oldVer.get() || this.mc.world.getBlockState(pos.up()).getBlock() instanceof AirBlock)) {
                           double self = BODamageUtils.crystal(this.mc.player, this.mc.player.getBoundingBox(), this.feet(pos), blockPos, true);
                           if (self >= (Double)this.maxDmg.get()) {
                              return true;
                           }
                        }
                     }
                  }
               }
               break;
            case 2:
            case 3:
               if (!(this.mc.world.getBlockState(blockPos).getBlock() instanceof AirBlock)) {
                  return false;
               }

               if ((Boolean)this.oldVer.get() && !(this.mc.world.getBlockState(blockPos.up()).getBlock() instanceof AirBlock)) {
                  return false;
               }

               double self = BODamageUtils.crystal(this.mc.player, this.mc.player.getBoundingBox(), this.feet(blockPos.up()), blockPos, true);
               if (self >= (Double)this.maxDmg.get()) {
                  return true;
               }
         }

         return false;
      }
   }

   private Vec3d feet(BlockPos pos) {
      return new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY(), (double)pos.getZ() + (double)0.5F);
   }

   private boolean contains() {
      for(MineStart m : this.mining) {
         if (m.id == this.mineStart.id && m.pos.equals(this.mineStart.pos)) {
            return true;
         }
      }

      return false;
   }

   private boolean containsPos(BlockPos pos) {
      for(MineStart m : this.mining) {
         if ((double)System.currentTimeMillis() > (double)m.time + (Double)this.mineTime.get() * (double)1000.0F && m.pos.equals(pos)) {
            return true;
         }
      }

      return false;
   }

   private static record MineStart(BlockPos pos, int id, long time) {
   }

   private static record ProtectBlock(BlockPos pos, int type) {
   }

   public static record Render(BlockPos pos, long time) {
   }

   public static enum SwitchMode {
      Silent,
      Normal,
      PickSilent,
      InvSwitch,
      Disabled;

      // $FF: synthetic method
      private static SwitchMode[] $values() {
         return new SwitchMode[]{Silent, Normal, PickSilent, InvSwitch, Disabled};
      }
   }
}
