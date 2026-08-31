package espada.spacex.aurora.modules.playerplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.utils.BOBlockUtil;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.EntityInfo;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.RenderUtils;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.meteor.BOEntityUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.hit.BlockHitResult;

public class BurrowPlus2 extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgAttack;
   private final SettingGroup sgRender;
   private final List<Render> renderBlocks;
   private final Setting<SwitchMode> switchMode;
   private final Setting<LagBackMode> lagBackMode;
   private final Setting<List<Block>> blocks;
   private final Setting<Boolean> multiPlace;
   private final Setting<Boolean> lagBack;
   private final Setting<Boolean> fillHead;
   private final Setting<Boolean> AntiWebLag;
   private final Setting<Double> attackSpeed;
   private final Setting<Boolean> placeSwing;
   private final Setting<SwingHand> placeHand;
   private final Setting<Boolean> render;
   private final Setting<Double> renderTime;
   private final Setting<Double> fadeTime;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<SettingColor> lineColor;
   private final Setting<SettingColor> sideColor;
   private long lastAttack;
   private final Predicate<ItemStack> predicate;

   public BurrowPlus2() {
      super(Aurora.PlayerPlus, "BlockLag", "Places a block inside your feet.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgAttack = this.settings.createGroup("Attack");
      this.sgRender = this.settings.createGroup("Render");
      this.renderBlocks = new ArrayList();
      this.switchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("The mode to switch obsidian.")).defaultValue(BurrowPlus2.SwitchMode.Silent)).build());
      this.lagBackMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("LagBack Mode")).description("")).defaultValue(BurrowPlus2.LagBackMode.XIN)).build());
      this.blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)(new BlockListSetting.Builder()).name("Block To Use")).description("Which blocks used for burrow.")).defaultValue(new Block[]{Blocks.OBSIDIAN, Blocks.ENDER_CHEST}).build());
      this.multiPlace = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Multi Place")).description("bypass2?.")).defaultValue(true)).build());
      this.lagBack = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Lag Back")).description("bypass2.")).defaultValue(true)).build());
      this.fillHead = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Fill Head")).description("MaoJunQing")).defaultValue(false)).build());
      this.AntiWebLag = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("AntiWebLag")).description("Pause when player is stuck by cobweb.")).defaultValue(false)).build());
      this.attackSpeed = this.sgAttack.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Attack Speed")).description("How many times to attack every second.")).defaultValue((double)4.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.placeSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Swing")).description("Renders swing animation when placing a block.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgRender;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      Setting<Boolean> var10003 = this.placeSwing;
      Objects.requireNonNull(var10003);
      this.placeHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Render")).description("")).defaultValue(true)).build());
      this.renderTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Render Time")).description("How long the box should remain in full alpha.")).defaultValue(0.3).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.fadeTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fade Time")).description("How long the fading should take.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      var10001 = this.sgRender;
      var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description("Which parts of the boxes should be rendered.")).defaultValue(ShapeMode.Sides);
      var10003 = this.render;
      Objects.requireNonNull(var10003);
      this.shapeMode = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 255)).visible(() -> (Boolean)this.render.get() && (((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Lines) || ((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Both)))).build());
      this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 20)).visible(() -> (Boolean)this.render.get() && (((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Sides) || ((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Both)))).build());
      MeteorClient.EVENT_BUS.subscribe(new Renderer());
      this.lastAttack = 0L;
      this.predicate = (itemStack) -> {
         Item patt5513$temp = itemStack.getItem();
         if (patt5513$temp instanceof BlockItem block) {
            return ((List)this.blocks.get()).contains(block.getBlock());
         } else {
            return false;
         }
      };
   }

   @EventHandler
   private void onTick(TickEvent.Pre event) {
      if (!(Boolean)this.AntiWebLag.get() || !BOEntityUtils.isWebbed(this.mc.player)) {
         if (this.mc.player != null && this.mc.world != null && this.mc.player.isOnGround()) {
            BlockPos selfPos = this.getFillBlock();
            if (selfPos == null) {
               this.toggle();
               this.sendToggledMsg();
            } else {
               PlaceData data = SettingUtils.getPlaceData(selfPos);
               if (data.valid()) {
                  boolean headFillMode = (double)selfPos.getY() > this.mc.player.getY();
                  List<Vec3d> fakeJumpOffsets = this.getFakeJumpOffset(selfPos, headFillMode);
                  if (fakeJumpOffsets.size() != 4) {
                     this.toggle();
                  } else {
                     Hand hand = this.predicate.test(Managers.HOLDING.getStack()) ? Hand.MAIN_HAND : (this.predicate.test(this.mc.player.getOffHandStack()) ? Hand.OFF_HAND : null);
                     boolean blocksPresent = hand != null;
                     if (!blocksPresent) {
                        switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                           case 1:
                           case 2:
                              blocksPresent = InvUtils.findInHotbar(this.predicate).found();
                              break;
                           case 3:
                           case 4:
                              blocksPresent = InvUtils.find(this.predicate).found();
                        }
                     }

                     if (blocksPresent) {
                        this.attackCrystal(selfPos);
                        if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(data.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}))) {
                           boolean switched = hand != null;
                           if (!switched) {
                              boolean var10000;
                              switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                                 case 1:
                                 case 2:
                                    var10000 = InvUtils.swap(InvUtils.findInHotbar(this.predicate).slot(), true);
                                    break;
                                 case 3:
                                    var10000 = BOInvUtils.pickSwitch(InvUtils.find(this.predicate).slot());
                                    break;
                                 case 4:
                                    var10000 = BOInvUtils.invSwitch(InvUtils.find(this.predicate).slot());
                                    break;
                                 default:
                                    throw new IncompatibleClassChangeError();
                              }

                              switched = var10000;
                           }

                           if (switched) {
                              this.doFakeJump(fakeJumpOffsets);
                              if ((Boolean)this.multiPlace.get()) {
                                 this.multiPlace(headFillMode);
                              } else {
                                 this.placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());
                              }

                              if ((Boolean)this.placeSwing.get()) {
                                 this.clientSwing((SwingHand)this.placeHand.get(), Hand.MAIN_HAND);
                              }

                              BlockPos yxPos = this.mc.player.getBlockPos();
                              if ((Boolean)this.lagBack.get()) {
                                 this.doLagBack(yxPos);
                              }

                              switch (((SwitchMode)this.switchMode.get()).ordinal()) {
                                 case 1 -> InvUtils.swapBack();
                                 case 2 -> InvUtils.swapBack();
                                 case 3 -> BOInvUtils.pickSwapBack();
                                 case 4 -> BOInvUtils.swapBack();
                              }

                              if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                                 Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
                              }
                           }

                           this.toggle();
                           this.sendToggledMsg();
                        }
                     }
                  }
               }
            }
         }

      }
   }

   private void multiPlace(boolean headFillMode) {
      if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add((double)0.0F, (double)0.0F, (double)0.0F)))) {
         this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add((double)0.0F, (double)0.0F, (double)0.0F)));
      }

      if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, (double)0.0F, 0.3)))) {
         this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, (double)0.0F, 0.3)));
      }

      if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, (double)0.0F, 0.3)))) {
         this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, (double)0.0F, 0.3)));
      }

      if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, (double)0.0F, -0.3)))) {
         this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, (double)0.0F, -0.3)));
      }

      if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, (double)0.0F, -0.3)))) {
         this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, (double)0.0F, -0.3)));
      }

      if (headFillMode) {
         if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add((double)0.0F, (double)1.0F, (double)0.0F)))) {
            this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add((double)0.0F, (double)1.0F, (double)0.0F)));
         }

         if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, (double)1.0F, 0.3)))) {
            this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, (double)1.0F, 0.3)));
         }

         if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, (double)1.0F, 0.3)))) {
            this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, (double)1.0F, 0.3)));
         }

         if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, (double)1.0F, -0.3)))) {
            this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, (double)1.0F, -0.3)));
         }

         if (BOBlockUtil.isAir(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, (double)1.0F, -0.3)))) {
            this.mPlace(Hand.MAIN_HAND, BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, (double)1.0F, -0.3)));
         }
      }

   }

   private void attackCrystal(BlockPos pos) {
      if (!((double)(System.currentTimeMillis() - this.lastAttack) < (double)1000.0F / (Double)this.attackSpeed.get()) && EntityInfo.CrystalCheck(pos)) {
         Entity blocking = this.getBlocking();
         if (blocking != null && (!SettingUtils.shouldRotate(RotationType.Attacking) || Managers.ROTATION.start(blocking.getBoundingBox(), (double)this.priority - 0.1, RotationType.Attacking, (long)Objects.hash(new Object[]{this.name + "attacking"})))) {
            SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.MAIN_HAND);
            this.sendPacket(PlayerInteractEntityC2SPacket.attack(blocking, this.mc.player.isSneaking()));
            SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.MAIN_HAND);
            if (SettingUtils.shouldRotate(RotationType.Attacking)) {
               Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "attacking"}));
            }

            this.lastAttack = System.currentTimeMillis();
         }
      }

   }

   public double ez() {
      if (!BOBlockUtil.isAir(EntityInfo.playerPos(this.mc.player).up(3))) {
         return 1.2;
      } else {
         double lol = 2.2;

         for(int i = 4; i < 6; ++i) {
            if (!BOBlockUtil.isAir(EntityInfo.playerPos(this.mc.player).up(i))) {
               return lol + (double)i - (double)4.0F;
            }
         }

         return (double)10.0F;
      }
   }

   private void doLagBack(BlockPos selfPos) {
      switch (((LagBackMode)this.lagBackMode.get()).ordinal()) {
         case 1:
            for(int i = 10; i > 0; --i) {
               if (BOBlockUtil.isAir(selfPos.add(0, i, 0)) && BOBlockUtil.isAir(selfPos.add(0, i, 0).up())) {
                  BlockPos lagPos = selfPos.add(0, i, 0);
                  this.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround((double)lagPos.getX() + (double)0.5F, (double)lagPos.getY(), (double)lagPos.getZ() + (double)0.5F, true));
               }
            }
         case 2:
            this.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + (double)2.0F, this.mc.player.getZ(), true));
         case 3:
            break;
         default:
            return;
      }

      this.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + this.ez(), this.mc.player.getZ(), true));
   }

   private void doFakeJump(List<Vec3d> offsets) {
      if (offsets != null) {
         offsets.forEach((vec) -> {
            if (vec != null && !vec.equals(new Vec3d((double)0.0F, (double)0.0F, (double)0.0F))) {
               this.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(vec.x, vec.y, vec.z, true));
            }

         });
      }

   }

   private List<Vec3d> getFakeJumpOffset(BlockPos burBlock, boolean headFillMode) {
      List<Vec3d> offsets = new LinkedList();
      if (headFillMode) {
         if (BOBlockUtil.fakeBBoxCheckFeet(this.mc.player, new Vec3d((double)0.0F, (double)2.0F, (double)0.0F))) {
            Vec3d offVec = this.getVec3dDirection(burBlock);
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.42132, this.mc.player.getY() + 0.4199999868869781, this.mc.player.getZ() + offVec.z * 0.42132));
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.95, this.mc.player.getY() + 0.7531999805212017, this.mc.player.getZ() + offVec.z * 0.95));
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.03, this.mc.player.getY() + 0.9999957640154541, this.mc.player.getZ() + offVec.z * 1.03));
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.0933, this.mc.player.getY() + 1.1661092609382138, this.mc.player.getZ() + offVec.z * 1.0933));
         } else {
            Vec3d offVec = this.getVec3dDirection(burBlock);
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.42132, this.mc.player.getY() + 0.12160004615784, this.mc.player.getZ() + offVec.z * 0.42132));
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.95, this.mc.player.getY() + 0.200000047683716, this.mc.player.getZ() + offVec.z * 0.95));
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.03, this.mc.player.getY() + 0.200000047683716, this.mc.player.getZ() + offVec.z * 1.03));
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.0933, this.mc.player.getY() + 0.12160004615784, this.mc.player.getZ() + offVec.z * 1.0933));
         }
      } else if (BOBlockUtil.fakeBBoxCheckFeet(this.mc.player, new Vec3d((double)0.0F, (double)2.0F, (double)0.0F))) {
         offsets.add(new Vec3d(this.mc.player.getX(), this.mc.player.getY() + 0.4199999868869781, this.mc.player.getZ()));
         offsets.add(new Vec3d(this.mc.player.getX(), this.mc.player.getY() + 0.7531999805212017, this.mc.player.getZ()));
         offsets.add(new Vec3d(this.mc.player.getX(), this.mc.player.getY() + 0.9999957640154541, this.mc.player.getZ()));
         offsets.add(new Vec3d(this.mc.player.getX(), this.mc.player.getY() + 1.1661092609382138, this.mc.player.getZ()));
      } else {
         Vec3d offVec = this.getVec3dDirection(burBlock);
         offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.42132, this.mc.player.getY() + 0.12160004615784, this.mc.player.getZ() + offVec.z * 0.42132));
         offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.95, this.mc.player.getY() + 0.200000047683716, this.mc.player.getZ() + offVec.z * 0.95));
         offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.03, this.mc.player.getY() + 0.200000047683716, this.mc.player.getZ() + offVec.z * 1.03));
         offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.0933, this.mc.player.getY() + 0.12160004615784, this.mc.player.getZ() + offVec.z * 1.0933));
      }

      return offsets;
   }

   public Vec3d getVec3dDirection(BlockPos burBlockPos) {
      Vec3d v = (new Vec3d((double)burBlockPos.getX(), (double)burBlockPos.getY(), (double)burBlockPos.getZ())).add((double)0.5F, (double)0.5F, (double)0.5F);
      BlockPos pPos = BOBlockUtil.getFlooredPosition(this.mc.player);
      Vec3d s = this.mc.player.getPos().subtract(v);
      Vec3d off = new Vec3d((double)0.0F, (double)0.0F, (double)0.0F);
      if (Math.abs(s.x) >= Math.abs(s.z) && Math.abs(s.x) > 0.2) {
         if (s.x > (double)0.0F) {
            off = new Vec3d(0.8 - s.x, (double)0.0F, (double)0.0F);
         } else {
            off = new Vec3d(-0.8 - s.x, (double)0.0F, (double)0.0F);
         }
      } else if (Math.abs(s.z) >= Math.abs(s.x) && Math.abs(s.z) > 0.2) {
         if (s.z > (double)0.0F) {
            off = new Vec3d((double)0.0F, (double)0.0F, 0.8 - s.z);
         } else {
            off = new Vec3d((double)0.0F, (double)0.0F, -0.8 - s.z);
         }
      } else if (burBlockPos.equals(pPos)) {
         List<Direction> facList = new ArrayList();
         Direction[] var7 = Direction.values();
         int var8 = var7.length;

         for(Direction f : var7) {
            if (f != Direction.UP && f != Direction.DOWN && BOBlockUtil.isAir(pPos.offset(f)) && BOBlockUtil.isAir(pPos.offset(f).offset(Direction.UP))) {
               facList.add(f);
            }
         }

         facList.sort((f1, f2) -> {
            Vec3d offVec1 = v.add((new Vec3d(f1.getUnitVector())).multiply((double)0.5F));
            Vec3d offVec2 = v.add((new Vec3d(f2.getUnitVector())).multiply((double)0.5F));
            return (int)(PlayerUtils.distanceTo(offVec1.x, this.mc.player.getY(), offVec1.z) - PlayerUtils.distanceTo(offVec2.x, this.mc.player.getY(), offVec2.z));
         });
         if (facList.size() > 0) {
            off = new Vec3d(((Direction)facList.get(0)).getUnitVector());
         }
      }

      return off;
   }

   private Entity getBlocking() {
      Entity crystal = null;
      if (this.mc.world != null && this.mc.player != null) {
         for(Entity entity : this.mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity && SettingUtils.inAttackRange(entity.getBoundingBox())) {
               crystal = entity;
            }
         }
      }

      return crystal;
   }

   protected BlockPos getFillBlock() {
      LinkedHashSet<BlockPos> feetBlock = this.getFeetBlock(0);
      List<BlockPos> collect = feetBlock.stream().filter(BOBlockUtil::isAir).filter((p) -> !BOBlockUtil.cantBlockPlace(p)).limit(1L).toList();
      return collect.size() == 0 ? null : (BlockPos)collect.get(0);
   }

   public LinkedHashSet<BlockPos> getFeetBlock(int yOff) {
      LinkedHashSet<BlockPos> set = new LinkedHashSet();
      set.add(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add((double)0.0F, (double)yOff, (double)0.0F)));
      set.add(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, (double)yOff, 0.3)));
      set.add(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, (double)yOff, 0.3)));
      set.add(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(0.3, (double)yOff, -0.3)));
      set.add(BOBlockUtil.vec3toBlockPos(this.mc.player.getPos().add(-0.3, (double)yOff, -0.3)));
      if ((Boolean)this.fillHead.get() && yOff == 0) {
         set.addAll(this.getFeetBlock(1));
      }

      return set;
   }

   public void mPlace(Hand hand, BlockPos pos) {
      Vec3d eyes = this.mc.player.getEyePos();
      boolean inside = eyes.x > (double)pos.getX() && eyes.x < (double)(pos.getX() + 1) && eyes.y > (double)pos.getY() && eyes.y < (double)(pos.getY() + 1) && eyes.z > (double)pos.getZ() && eyes.z < (double)(pos.getZ() + 1);
      PlaceData data = SettingUtils.getPlaceData(pos);
      if (data.valid()) {
         this.renderBlocks.add(new Render(pos, System.currentTimeMillis()));
         SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);
         this.sendSequenced((s) -> new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(data.pos().toCenterPos(), data.dir(), data.pos(), inside), s));
         SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);
      }

   }

   public static enum SwitchMode {
      Normal,
      Silent,
      PickSilent,
      InvSwitch;

      // $FF: synthetic method
      private static SwitchMode[] $values() {
         return new SwitchMode[]{Normal, Silent, PickSilent, InvSwitch};
      }
   }

   public static enum LagBackMode {
      OBS,
      XIN,
      OLD;

      // $FF: synthetic method
      private static LagBackMode[] $values() {
         return new LagBackMode[]{OBS, XIN, OLD};
      }
   }

   public static record Render(BlockPos blockPos, long time) {
   }

   private class Renderer {
      @EventHandler
      private void onRender(Render3DEvent event) {
         if ((Boolean)BurrowPlus2.this.render.get()) {
            BurrowPlus2.this.renderBlocks.removeIf((r) -> System.currentTimeMillis() - r.time > 1000L);
            BurrowPlus2.this.renderBlocks.forEach((r) -> {
               double progress = (double)1.0F - Math.min((double)(System.currentTimeMillis() - r.time) + (Double)BurrowPlus2.this.renderTime.get() * (double)1000.0F, (Double)BurrowPlus2.this.fadeTime.get() * (double)1000.0F) / ((Double)BurrowPlus2.this.fadeTime.get() * (double)1000.0F);
               event.renderer.box(r.blockPos, RenderUtils.injectAlpha((Color)BurrowPlus2.this.sideColor.get(), (int)Math.round((double)((SettingColor)BurrowPlus2.this.sideColor.get()).a * progress)), RenderUtils.injectAlpha((Color)BurrowPlus2.this.lineColor.get(), (int)Math.round((double)((SettingColor)BurrowPlus2.this.lineColor.get()).a * progress)), (ShapeMode)BurrowPlus2.this.shapeMode.get(), 0);
            });
         }
      }
   }
}
