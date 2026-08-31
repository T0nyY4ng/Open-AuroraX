package espada.spacex.aurora.modules.combatplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.RotationUtils;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.meteor.BODamageUtils;
import java.util.ArrayList;
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
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Direction.Type;

public class BedAuraPlus extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgPlacing;
   private final SettingGroup sgDamage;
   private final SettingGroup sgRender;
   private final Setting<Boolean> fiveB;
   private final Setting<Boolean> pauseEat;
   private final Setting<Boolean> doubleInteract;
   private final Setting<LogicMode> logicMode;
   private final Setting<SwitchMode> switchMode;
   private final Setting<RotationMode> rotMode;
   private final Setting<SpeedMode> speedMode;
   private final Setting<Double> speed;
   private final Setting<Double> damageSpeed;
   private final Setting<Double> maxSpeed;
   private final Setting<Double> minDmg;
   private final Setting<Double> maxDmg;
   private final Setting<Double> maxFriendDmg;
   private final Setting<Double> minRatio;
   private final Setting<Double> minFriendRatio;
   private final Setting<Double> forcePop;
   private final Setting<Double> antiPop;
   private final Setting<Double> antiFriendPop;
   private final Setting<Boolean> friendSacrifice;
   private final Setting<Boolean> placeSwing;
   private final Setting<SwingHand> placeHand;
   private final Setting<Boolean> interactSwing;
   private final Setting<SwingHand> interactHand;
   public final Setting<ShapeMode> shapeMode;
   private final Setting<SettingColor> lineColor;
   public final Setting<SettingColor> color;
   private final Setting<SettingColor> fLineColor;
   public final Setting<SettingColor> fColor;
   private int lastIndex;
   private int length;
   private long tickTime;
   private double bestDmg;
   private long lastTime;
   private BlockPos placePos;
   private Direction bedDir;
   private PlaceData placeData;
   private BlockPos calcPos;
   private Direction calcDir;
   private PlaceData calcData;
   private BlockPos renderPos;
   private Direction renderDir;
   private BlockPos[] blocks;
   private final List<PlayerEntity> targets;
   private final List<PlayerEntity> friends;
   private final List<Bed> beds;
   private double timer;
   private double dmg;
   private double enemyHP;
   private double self;
   private double selfHP;
   private double friend;
   private double friendHP;

   public BedAuraPlus() {
      super(Aurora.CombatPlus, "Bed Aura+", "Automatically places and breaks beds to cause damage to your opponents but better.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgPlacing = this.settings.createGroup("Placing");
      this.sgDamage = this.settings.createGroup("Damage");
      this.sgRender = this.settings.createGroup("Render");
      this.fiveB = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("5B5T")).description("For example requires floor for both bed blocks and allows placing inside entities.")).defaultValue(false)).build());
      this.pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause Eat")).description("Pauses when you are eating.")).defaultValue(true)).build());
      this.doubleInteract = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Double Interact")).description("Clicks both bed blocks every time.")).defaultValue(true)).build());
      this.logicMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Logic Mode")).description("Logic for bullying kids.")).defaultValue(BedAuraPlus.LogicMode.PlaceBreak)).build());
      this.switchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("Method of switching. Silent is the most reliable.")).defaultValue(BedAuraPlus.SwitchMode.Silent)).build());
      this.rotMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Rotation Mode")).description("Packet- Sends 1 rotation packet for each bed. Manager- Modifies movement packets to set rotation.")).defaultValue(BedAuraPlus.RotationMode.Manager)).build());
      this.speedMode = this.sgPlacing.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Speed Mode")).description("Normal mode should be used in everywhere else than 5B.")).defaultValue(BedAuraPlus.SpeedMode.Normal)).build());
      this.speed = this.sgPlacing.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Speed")).description("How many beds to blow up every second.")).defaultValue((double)2.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).visible(() -> this.speedMode.get() == BedAuraPlus.SpeedMode.Normal)).build());
      this.damageSpeed = this.sgPlacing.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Damage Speed Factor")).description("Sets speed to damage multiplied by factor.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).visible(() -> this.speedMode.get() == BedAuraPlus.SpeedMode.Damage)).build());
      this.maxSpeed = this.sgPlacing.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Damage Speed")).description("Maximum speed for damage mode.")).defaultValue((double)12.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).visible(() -> this.speedMode.get() == BedAuraPlus.SpeedMode.Damage)).build());
      this.minDmg = this.sgDamage.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Min Damage")).description("Minimum damage to place.")).defaultValue((double)8.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.maxDmg = this.sgDamage.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Max Damage")).description("Maximum self damage to place.")).defaultValue((double)6.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.maxFriendDmg = this.sgDamage.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Max Friend Damage")).description("Maximum friend damage to place.")).defaultValue((double)6.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.minRatio = this.sgDamage.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Min Damage Ratio")).description("Minimum damage ratio between self damage and enemy damage.")).defaultValue((double)2.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.minFriendRatio = this.sgDamage.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Min Friend Damage Ratio")).description("Minimum damage ratio between friend damage and enemy damage.")).defaultValue((double)2.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.forcePop = this.sgDamage.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Force Pop")).description("Ignores damage checks if enemy would pop after x explodes.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.antiPop = this.sgDamage.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Anti Pop")).description("Cancels actions if you would pop after x explodes.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.antiFriendPop = this.sgDamage.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Anti Friend Pop")).description("Cancels actions if any friend would pop after x explodes.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.friendSacrifice = this.sgDamage.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Friend Sacrifice")).description("Kills your friend if you can also kill any enemy with same bed.")).defaultValue(true)).build());
      this.placeSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Place Swing")).description("Renders swing animation when placing the crafting table.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgRender;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Place Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      Setting<Boolean> var10003 = this.placeSwing;
      Objects.requireNonNull(var10003);
      this.placeHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.interactSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Interact Swing")).description("Renders swing animation when interacting with a block.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Interact Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.interactSwing;
      Objects.requireNonNull(var10003);
      this.interactHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description("Which parts of the render should be rendered.")).defaultValue(ShapeMode.Both)).build());
      this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Head Line Color")).description("Line color of head block.")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.color = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Head Side Color")).description("Side color of head block.")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.fLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Feet Line Color")).description("Line color of feet block")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.fColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Feet Side Color")).description("Side color of feet block")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.lastIndex = 0;
      this.length = 0;
      this.tickTime = -1L;
      this.bestDmg = (double)0.0F;
      this.lastTime = 0L;
      this.placePos = null;
      this.bedDir = null;
      this.placeData = null;
      this.calcPos = null;
      this.calcDir = null;
      this.calcData = null;
      this.renderPos = null;
      this.renderDir = null;
      this.blocks = new BlockPos[0];
      this.targets = new ArrayList();
      this.friends = new ArrayList();
      this.beds = new ArrayList();
      this.timer = (double)0.0F;
   }

   @EventHandler(
      priority = 200
   )
   private void onTickPre(TickEvent.Post event) {
      this.calculate(this.length - 1);
      this.renderPos = this.calcPos;
      this.placePos = this.calcPos;
      this.renderDir = this.calcDir;
      this.bedDir = this.calcDir;
      this.placeData = this.calcData;
      this.blocks = this.getBlocks(this.mc.player.getEyePos(), Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()));
      this.tickTime = System.currentTimeMillis();
      this.length = this.blocks.length;
      this.lastIndex = 0;
      this.bestDmg = (double)0.0F;
      this.calcPos = null;
      this.calcDir = null;
      this.calcData = null;
      this.updateTargets();
   }

   @EventHandler(
      priority = 200
   )
   private void onRender(Render3DEvent event) {
      double delta = (double)((float)(System.currentTimeMillis() - this.lastTime) / 1000.0F);
      this.timer += delta;
      this.lastTime = System.currentTimeMillis();
      List<Bed> toRemove = new ArrayList();
      this.beds.forEach((bed) -> {
         if (System.currentTimeMillis() - bed.time > 500L) {
            toRemove.add(bed);
         }

      });
      List var10001 = this.beds;
      Objects.requireNonNull(var10001);
      toRemove.forEach(var10001::remove);
      if (this.tickTime >= 0L && this.mc.player != null && this.mc.world != null) {
         if (this.pauseCheck()) {
            this.update();
         }

         int index = Math.min((int)Math.ceil((double)((float)(System.currentTimeMillis() - this.tickTime) / 50.0F * (float)this.length)), this.length - 1);
         this.calculate(index);
         if (this.renderPos != null && this.pauseCheck()) {
            event.renderer.box(this.bedBox(this.renderPos), (Color)this.color.get(), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0);
            if (this.renderDir != null) {
               event.renderer.box(this.bedBox(this.renderPos.offset(this.renderDir)), (Color)this.fColor.get(), (Color)this.fLineColor.get(), (ShapeMode)this.shapeMode.get(), 0);
            }
         }

      }
   }

   private boolean pauseCheck() {
      return !(Boolean)this.pauseEat.get() || !this.mc.player.isUsingItem();
   }

   private void calculate(int index) {
      for(int i = this.lastIndex; i < index; ++i) {
         BlockPos pos = this.blocks[i];
         this.damageCalc(pos);
         if (this.dmgCheck()) {
            for(Direction dir : Type.HORIZONTAL) {
               PlaceData data = this.getData(pos, dir);
               if (data.valid() && (OLEPOSSUtils.replaceable(pos.offset(dir)) || this.mc.world.getBlockState(pos.offset(dir)).getBlock() instanceof BedBlock) && SettingUtils.inPlaceRange(data.pos()) && ((Boolean)this.fiveB.get() || !EntityUtils.intersectsWithEntity(new Box(pos.offset(dir)), (entity) -> !(entity instanceof ItemEntity)))) {
                  this.calcData = data;
                  this.calcPos = pos;
                  this.calcDir = dir;
                  this.bestDmg = this.dmg;
               }
            }
         }
      }

      this.lastIndex = index;
   }

   private void updateTargets() {
      this.friends.clear();
      this.targets.clear();
      List<PlayerEntity> players = new ArrayList();
      double closestDist = (double)1000.0F;

      for(int i = 3; i > 0; --i) {
         PlayerEntity closest = null;

         for(PlayerEntity player : this.mc.world.getPlayers()) {
            if (!players.contains(player) && !Friends.get().isFriend(player) && player != this.mc.player) {
               double dist = (double)player.distanceTo(this.mc.player);
               if (!(dist > (double)15.0F) && (closest == null || dist < closestDist)) {
                  closestDist = dist;
                  closest = player;
               }
            }
         }

         if (closest != null) {
            players.add(closest);
            if (Friends.get().isFriend(closest)) {
               this.friends.add(closest);
            } else {
               this.targets.add(closest);
            }
         }
      }

   }

   private BlockPos[] getBlocks(Vec3d middle, double radius) {
      ArrayList<BlockPos> result = new ArrayList();
      int i = (int)Math.ceil(radius);

      for(int x = -i; x <= i; ++x) {
         for(int y = -i; y <= i; ++y) {
            for(int z = -i; z <= i; ++z) {
               BlockPos pos = BlockPos.ofFloored(middle).add(x, y, z);
               if ((OLEPOSSUtils.replaceable(pos) || this.mc.world.getBlockState(pos).getBlock() instanceof BedBlock) && (!(Boolean)this.fiveB.get() || this.mc.world.getBlockState(pos.down()).getBlock() != Blocks.AIR && !this.mc.world.getBlockState(pos.down()).hasBlockEntity()) && this.inRangeToTargets(pos)) {
                  result.add(pos);
               }
            }
         }
      }

      return (BlockPos[])result.toArray(new BlockPos[0]);
   }

   private boolean inRangeToTargets(BlockPos pos) {
      for(PlayerEntity target : this.targets) {
         if (target.getPos().add((double)0.0F, (double)1.0F, (double)0.0F).distanceTo(pos.toCenterPos()) < (double)3.5F) {
            return true;
         }
      }

      return false;
   }

   private void update() {
      if (this.placePos != null && this.placeData != null && this.placeData.valid() && this.bedDir != null) {
         if (this.logicMode.get() == BedAuraPlus.LogicMode.PlaceBreak) {
            List<BlockPos> in = this.interactUpdate();
            if (in != null && !in.isEmpty()) {
               in.forEach(this::removeBed);
            }

            if (this.timer <= (double)1.0F / this.getSpeed()) {
               return;
            }

            if (OLEPOSSUtils.replaceable(this.placePos) && OLEPOSSUtils.replaceable(this.placePos.offset(this.bedDir)) && this.placeUpdate()) {
               this.removeBed2(this.placePos);
               this.beds.add(new Bed(this.placePos, this.placePos.offset(this.bedDir), true, System.currentTimeMillis()));
               this.timer = (double)0.0F;
            }
         } else {
            if (!this.isBed(this.placePos) && !this.isBed(this.placePos.offset(this.bedDir)) && this.placeUpdate()) {
               this.removeBed2(this.placePos);
               this.beds.add(new Bed(this.placePos, this.placePos.offset(this.bedDir), true, System.currentTimeMillis()));
            }

            if (this.timer <= (double)1.0F / this.getSpeed()) {
               return;
            }

            List<BlockPos> in = this.interactUpdate();
            if (in != null && !in.isEmpty()) {
               in.forEach(this::removeBed);
               this.timer = (double)0.0F;
            }
         }

      }
   }

   private void removeBed(BlockPos pos) {
      List<Bed> toRemove = new ArrayList();
      this.beds.forEach((bed) -> {
         if (bed.feetBlock.equals(pos) || bed.headBlock.equals(pos)) {
            toRemove.add(bed);
         }

      });
      toRemove.forEach((bed) -> {
         this.beds.remove(bed);
         this.beds.add(new Bed(bed.feetBlock, bed.headBlock, false, System.currentTimeMillis()));
      });
   }

   private void removeBed2(BlockPos pos) {
      List<Bed> toRemove = new ArrayList();
      this.beds.forEach((bed) -> {
         if (bed.feetBlock.equals(pos) || bed.headBlock.equals(pos)) {
            toRemove.add(bed);
         }

      });
      List var10001 = this.beds;
      Objects.requireNonNull(var10001);
      toRemove.forEach(var10001::remove);
   }

   private void place(Hand hand) {
      this.placeBlock(hand, this.placeData.pos().toCenterPos(), this.placeData.dir(), this.placeData.pos());
      if ((Boolean)this.placeSwing.get()) {
         this.clientSwing((SwingHand)this.placeHand.get(), hand);
      }

   }

   private List<BlockPos> interactUpdate() {
      if ((Boolean)this.doubleInteract.get()) {
         if (SettingUtils.shouldRotate(RotationType.Interact) && !Managers.ROTATION.start(this.placePos, (double)this.priority, RotationType.Interact, (long)Objects.hash(new Object[]{this.name + "explode"}))) {
            return null;
         } else {
            List<BlockPos> list = new ArrayList();
            if (this.isBed(this.placePos) || this.isBed(this.placePos.offset(this.bedDir))) {
               if (SettingUtils.inPlaceRange(this.placePos) && this.interact(this.placePos)) {
                  list.add(this.placePos);
               }

               if (SettingUtils.inPlaceRange(this.placePos.offset(this.bedDir)) && this.interact(this.placePos.offset(this.bedDir))) {
                  list.add(this.placePos.offset(this.bedDir));
               }
            }

            if (SettingUtils.shouldRotate(RotationType.Interact)) {
               Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "explode"}));
            }

            return list;
         }
      } else {
         BlockPos interactPos = this.getInteractPos();
         if (interactPos == null) {
            return null;
         } else {
            Direction interactDir = SettingUtils.getPlaceOnDirection(interactPos);
            if (interactDir == null) {
               return null;
            } else if (SettingUtils.shouldRotate(RotationType.Interact) && !Managers.ROTATION.start(interactPos, (double)this.priority, RotationType.Interact, (long)Objects.hash(new Object[]{this.name + "explode"}))) {
               return null;
            } else {
               this.interactBlock(Hand.MAIN_HAND, interactPos.toCenterPos(), interactDir, interactPos);
               if ((Boolean)this.interactSwing.get()) {
                  this.clientSwing((SwingHand)this.interactHand.get(), Hand.MAIN_HAND);
               }

               if (SettingUtils.shouldRotate(RotationType.Interact)) {
                  Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "explode"}));
               }

               List<BlockPos> list = new ArrayList();
               list.add(interactPos);
               return list;
            }
         }
      }
   }

   private boolean interact(BlockPos pos) {
      Direction dir = SettingUtils.getPlaceOnDirection(pos);
      if (dir == null) {
         return false;
      } else {
         this.interactBlock(Hand.MAIN_HAND, pos.toCenterPos(), dir, pos);
         if ((Boolean)this.interactSwing.get()) {
            this.clientSwing((SwingHand)this.interactHand.get(), Hand.MAIN_HAND);
         }

         return true;
      }
   }

   private BlockPos getInteractPos() {
      if (this.isBed(this.placePos.offset(this.bedDir)) && SettingUtils.inPlaceRange(this.placePos.offset(this.bedDir)) && SettingUtils.getPlaceOnDirection(this.placePos.offset(this.bedDir)) != null) {
         return this.placePos.offset(this.bedDir);
      } else {
         return this.isBed(this.placePos) && SettingUtils.inPlaceRange(this.placePos) && SettingUtils.getPlaceOnDirection(this.placePos) != null ? this.placePos : null;
      }
   }

   private boolean isBed(BlockPos pos) {
      for(Bed bed : this.beds) {
         if (bed.feetBlock.equals(pos) || bed.headBlock.equals(pos)) {
            return bed.isBed;
         }
      }

      return this.mc.world.getBlockState(pos).getBlock() instanceof BedBlock;
   }

   private boolean placeUpdate() {
      Hand hand = Managers.HOLDING.getStack().getItem() instanceof BedItem ? Hand.MAIN_HAND : (this.mc.player.getOffHandStack().getItem() instanceof BedItem ? Hand.OFF_HAND : null);
      int beds = hand == Hand.MAIN_HAND ? Managers.HOLDING.getStack().getCount() : (hand == Hand.OFF_HAND ? this.mc.player.getOffHandStack().getCount() : 0);
      if (hand == null) {
         switch (((SwitchMode)this.switchMode.get()).ordinal()) {
            case 0:
            case 1:
               FindItemResult result1 = InvUtils.findInHotbar((item) -> item.getItem() instanceof BedItem);
               beds = result1.count();
               break;
            case 2:
            case 3:
               FindItemResult result2 = InvUtils.find((item) -> item.getItem() instanceof BedItem);
               beds = result2.slot() >= 0 ? result2.count() : -1;
         }
      }

      if (beds <= 0) {
         return false;
      } else if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(this.placeData.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}))) {
         return false;
      } else {
         boolean switched = hand != null;
         if (this.rotMode.get() == BedAuraPlus.RotationMode.Packet) {
            this.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(this.bedDir.getOpposite().asRotation(), Managers.ROTATION.lastDir[1], Managers.ON_GROUND.isOnGround()));
         } else {
            Managers.ROTATION.startYaw((double)this.bedDir.getOpposite().asRotation(), (double)this.priority, RotationType.Other, (long)Objects.hash(new Object[]{this.name + "placing"}));
            if (Math.abs(RotationUtils.yawAngle((double)Managers.ROTATION.lastDir[0], (double)this.bedDir.getOpposite().asRotation())) > (double)45.0F) {
               return false;
            }
         }

         if (!switched) {
            switch (((SwitchMode)this.switchMode.get()).ordinal()) {
               case 0:
               case 1:
                  FindItemResult result3 = InvUtils.findInHotbar((item) -> item.getItem() instanceof BedItem);
                  InvUtils.swap(result3.slot(), true);
                  switched = true;
                  break;
               case 2:
                  FindItemResult result4 = InvUtils.find((item) -> item.getItem() instanceof BedItem);
                  switched = BOInvUtils.pickSwitch(result4.slot());
                  break;
               case 3:
                  FindItemResult result5 = InvUtils.find((item) -> item.getItem() instanceof BedItem);
                  switched = BOInvUtils.invSwitch(result5.slot());
            }
         }

         if (!switched) {
            return false;
         } else {
            this.place(hand == null ? Hand.MAIN_HAND : hand);
            if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
               Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
            }

            if (hand == null) {
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

            return true;
         }
      }
   }

   private boolean dmgCheck() {
      if (this.dmg < this.bestDmg) {
         return false;
      } else if (this.self * (Double)this.antiPop.get() >= this.selfHP) {
         return false;
      } else if (!(Boolean)this.friendSacrifice.get() && this.friendHP >= (double)0.0F && this.friend * (Double)this.antiFriendPop.get() >= this.friendHP) {
         return false;
      } else if (this.enemyHP >= (double)0.0F && this.dmg * (Double)this.forcePop.get() >= this.enemyHP) {
         return true;
      } else if (this.friendHP >= (double)0.0F && this.friend * (Double)this.antiFriendPop.get() >= this.friendHP) {
         return false;
      } else if (this.dmg < (Double)this.minDmg.get()) {
         return false;
      } else if (this.self > (Double)this.maxDmg.get()) {
         return false;
      } else if (this.friend > (Double)this.maxFriendDmg.get()) {
         return false;
      } else if (this.dmg / this.self < (Double)this.minRatio.get()) {
         return false;
      } else {
         return !(this.friendHP >= (double)0.0F) || !(this.dmg / this.friend < (Double)this.minFriendRatio.get());
      }
   }

   private double getDmg(BlockPos pos) {
      double highest = (double)-1.0F;

      for(PlayerEntity target : this.targets) {
         highest = Math.max(highest, BODamageUtils.bedDamage(target, target.getBoundingBox(), new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F), (BlockPos)null));
      }

      return highest;
   }

   private void damageCalc(BlockPos pos) {
      double highest = (double)-1.0F;
      double highestHP = (double)-1.0F;

      for(PlayerEntity target : this.targets) {
         if (!(target.getHealth() <= 0.0F)) {
            highest = Math.max(highest, BODamageUtils.bedDamage(target, target.getBoundingBox(), new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F), (BlockPos)null));
            highestHP = (double)(target.getHealth() + target.getAbsorptionAmount());
         }
      }

      this.dmg = highest;
      this.enemyHP = highestHP;
      this.self = BODamageUtils.bedDamage(this.mc.player, this.mc.player.getBoundingBox(), new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F), (BlockPos)null);
      this.selfHP = (double)(this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount());
      highest = (double)-1.0F;
      highestHP = (double)-1.0F;

      for(PlayerEntity friend : this.friends) {
         if (!(friend.getHealth() <= 0.0F)) {
            highest = Math.max(highest, BODamageUtils.bedDamage(friend, friend.getBoundingBox(), new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F), (BlockPos)null));
            highestHP = (double)(friend.getHealth() + friend.getAbsorptionAmount());
         }
      }

      this.friend = highest;
      this.friendHP = highestHP;
   }

   private Box bedBox(BlockPos pos) {
      return new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)pos.getY() + (double)0.5F, (double)(pos.getZ() + 1));
   }

   private PlaceData getData(BlockPos pos, Direction dir) {
      return (Boolean)this.fiveB.get() ? SettingUtils.getPlaceDataAND(pos.offset(dir), (direction) -> direction == Direction.DOWN, (pos1) -> !(this.mc.world.getBlockState(pos1).getBlock() instanceof BedBlock)) : SettingUtils.getPlaceDataAND(pos.offset(dir), (direction) -> direction != dir, (pos1) -> !(this.mc.world.getBlockState(pos1).getBlock() instanceof BedBlock));
   }

   private double getSpeed() {
      switch (((SpeedMode)this.speedMode.get()).ordinal()) {
         case 0:
            return (Double)this.speed.get();
         case 1:
            if (this.placePos == null) {
               return (Double)this.maxSpeed.get();
            }

            double dmg = this.getDmg(this.placePos);
            return Math.min(dmg * (Double)this.damageSpeed.get(), (Double)this.maxSpeed.get());
         default:
            return (double)2.0F;
      }
   }

   public static enum LogicMode {
      PlaceBreak,
      BreakPlace;

      // $FF: synthetic method
      private static LogicMode[] $values() {
         return new LogicMode[]{PlaceBreak, BreakPlace};
      }
   }

   public static enum RotationMode {
      Packet,
      Manager;

      // $FF: synthetic method
      private static RotationMode[] $values() {
         return new RotationMode[]{Packet, Manager};
      }
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

   public static enum SpeedMode {
      Normal,
      Damage;

      // $FF: synthetic method
      private static SpeedMode[] $values() {
         return new SpeedMode[]{Normal, Damage};
      }
   }

   private static record Bed(BlockPos feetBlock, BlockPos headBlock, boolean isBed, long time) {
   }
}
