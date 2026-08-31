package espada.spacex.aurora.modules.playerplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.RenderUtils;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.meteor.BODamageUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
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
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockBox;

public class BurrowPlus extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgRubberband;
   private final SettingGroup sgAttack;
   private final SettingGroup sgRender;
   private final Setting<Boolean> onlyOnGround;
   private final Setting<List<Block>> block;
   private final Setting<SwitchMode> switchMode;
   private final Setting<LagBackMode> lagBackMode;
   private final Setting<Integer> tryCount;
   private final Setting<Double> rubberbandOffset;
   private final Setting<Integer> rubberbandPackets;
   private final Setting<Boolean> attack;
   private final Setting<Double> attackSpeed;
   private final Setting<Double> minHealth;
   private final Setting<Boolean> placeSwing;
   private final Setting<SwingHand> placeHand;
   private final Setting<Boolean> attackSwing;
   private final Setting<SwingHand> attackHand;
   private final Setting<Boolean> render;
   private final Setting<Double> renderTime;
   private final Setting<Double> fadeTime;
   private final Setting<ShapeMode> shapeMode;
   private final Setting<SettingColor> lineColor;
   private final Setting<SettingColor> sideColor;
   private int count;
   private long lastAttack;
   private final List<BlockPos> placePositions;
   private final List<Render> renderBlocks;

   public BurrowPlus() {
      super(Aurora.PlayerPlus, "BurrowPlus", "Let you clip into block.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgRubberband = this.settings.createGroup("Rubberband");
      this.sgAttack = this.settings.createGroup("Attack");
      this.sgRender = this.settings.createGroup("Render");
      this.onlyOnGround = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Only On Ground")).description("Only burrow on ground.")).defaultValue(false)).build());
      this.block = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)(new BlockListSetting.Builder()).name("Block To Use")).description("Which blocks used for burrow.")).defaultValue(new Block[]{Blocks.OBSIDIAN, Blocks.ENDER_CHEST}).build());
      this.switchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Switch Mode")).description("The mode to switch obsidian.")).defaultValue(BurrowPlus.SwitchMode.Silent)).build());
      this.lagBackMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("LagBack Mode")).description("")).defaultValue(BurrowPlus.LagBackMode.Troll)).build());
      this.tryCount = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Try Count")).description("How many time to try burrow.")).defaultValue(0)).range(0, 20).sliderRange(0, 20).build());
      this.rubberbandOffset = this.sgRubberband.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Rubberband Offset")).description("Y offset of rubberband packet.")).defaultValue((double)9.0F).sliderRange((double)-10.0F, (double)10.0F).visible(() -> ((LagBackMode)this.lagBackMode.get()).equals(BurrowPlus.LagBackMode.OBS))).build());
      this.rubberbandPackets = this.sgRubberband.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Rubberband Packets")).description("How many offset packets to send.")).defaultValue(1)).min(0).sliderRange(0, 10).visible(() -> ((LagBackMode)this.lagBackMode.get()).equals(BurrowPlus.LagBackMode.OBS))).build());
      this.attack = this.sgAttack.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Attack")).description("Attacks crystals blocking surround.")).defaultValue(true)).build());
      this.attackSpeed = this.sgAttack.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Attack Speed")).description("How many times to attack every second.")).defaultValue((double)4.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      SettingGroup var10001 = this.sgAttack;
      DoubleSetting.Builder var10002 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Min Health")).description(".")).defaultValue((double)6.0F);
      Setting<Boolean> var10003 = this.attack;
      Objects.requireNonNull(var10003);
      this.minHealth = var10001.add(((DoubleSetting.Builder)var10002.visible(var10003::get)).build());
      this.placeSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Swing")).description("Renders swing animation when placing a block.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      EnumSetting.Builder var4 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.placeSwing;
      Objects.requireNonNull(var10003);
      this.placeHand = var10001.add(((EnumSetting.Builder)var4.visible(var10003::get)).build());
      this.attackSwing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Attack Swing")).description("Renders swing animation when placing a crystal.")).defaultValue(true)).build());
      var10001 = this.sgRender;
      var4 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Attack Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.attackSwing;
      Objects.requireNonNull(var10003);
      this.attackHand = var10001.add(((EnumSetting.Builder)var4.visible(var10003::get)).build());
      this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Render")).description("")).defaultValue(true)).build());
      this.renderTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Render Time")).description("How long the box should remain in full alpha.")).defaultValue(0.3).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.fadeTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fade Time")).description("How long the fading should take.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)10.0F).build());
      var10001 = this.sgRender;
      var4 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Shape Mode")).description("Which parts of the boxes should be rendered.")).defaultValue(ShapeMode.Sides);
      var10003 = this.render;
      Objects.requireNonNull(var10003);
      this.shapeMode = var10001.add(((EnumSetting.Builder)var4.visible(var10003::get)).build());
      this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Line Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 255)).visible(() -> (Boolean)this.render.get() && (((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Lines) || ((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Both)))).build());
      this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Side Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 20)).visible(() -> (Boolean)this.render.get() && (((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Sides) || ((ShapeMode)this.shapeMode.get()).equals(ShapeMode.Both)))).build());
      this.lastAttack = 0L;
      this.placePositions = new ArrayList();
      this.renderBlocks = new ArrayList();
      MeteorClient.EVENT_BUS.subscribe(new Renderer());
   }

   public void onActivate() {
      this.placePositions.clear();
      this.count = 0;
   }

   public void onDeactivate() {
      this.placePositions.clear();
   }

   @EventHandler
   private void onTick(TickEvent.Pre event) {
      if (!(Boolean)this.onlyOnGround.get() || this.mc.player.isOnGround()) {
         this.getPlacePos();
         BlockPos burBlock = null;

         for(BlockPos placePosition : this.placePositions) {
            burBlock = placePosition;
         }

         List<Vec3d> fakeJumpOffsets = this.getFakeJumpOffset(burBlock, (double)burBlock.getY() >= this.mc.player.getY() + 0.4);
         if (fakeJumpOffsets.size() != 4) {
            this.toggle();
         } else if (!BlockUtils.canPlace(burBlock, false)) {
            this.toggle();
         } else {
            this.updateFakeJump(fakeJumpOffsets);
            this.updatePlace(burBlock);
            this.updateLagBack();
            ++this.count;
            if (this.count >= (Integer)this.tryCount.get()) {
               this.toggle();
            }

         }
      }
   }

   private List<Vec3d> getFakeJumpOffset(BlockPos burBlock, boolean isHeadBurrow) {
      List<Vec3d> offsets = new LinkedList();
      if (isHeadBurrow) {
         if (this.fakeBoxCheckFeet(this.mc.player, new Vec3d((double)0.0F, (double)2.5F, (double)0.0F))) {
            Vec3d offVec = this.getTwoBlockFjPos(burBlock);
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.42132, this.mc.player.getY() + 0.41999998688698, this.mc.player.getZ() + offVec.z * 0.42132));
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.95, this.mc.player.getY() + 0.7500019, this.mc.player.getZ() + offVec.z * 0.95));
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.03, this.mc.player.getY() + 0.9999962, this.mc.player.getZ() + offVec.z * 1.03));
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.0933, this.mc.player.getY() + 1.17000380178814, this.mc.player.getZ() + offVec.z * 1.0933));
         } else {
            Vec3d offVec = this.getTwoBlockFjPos(burBlock);
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.42132, this.mc.player.getY() + 0.12160004615784, this.mc.player.getZ() + offVec.z * 0.42132));
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.95, this.mc.player.getY() + 0.200000047683716, this.mc.player.getZ() + offVec.z * 0.95));
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.03, this.mc.player.getY() + 0.200000047683716, this.mc.player.getZ() + offVec.z * 1.03));
            offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.0933, this.mc.player.getY() + 0.12160004615784, this.mc.player.getZ() + offVec.z * 1.0933));
         }
      } else if (this.fakeBoxCheckFeet(this.mc.player, new Vec3d((double)0.0F, (double)2.5F, (double)0.0F))) {
         offsets.add(new Vec3d(this.mc.player.getX(), this.mc.player.getY() + 0.41999998688698, this.mc.player.getZ()));
         offsets.add(new Vec3d(this.mc.player.getX(), this.mc.player.getY() + 0.7500019, this.mc.player.getZ()));
         offsets.add(new Vec3d(this.mc.player.getX(), this.mc.player.getY() + 0.9999962, this.mc.player.getZ()));
         offsets.add(new Vec3d(this.mc.player.getX(), this.mc.player.getY() + 1.17000380178814, this.mc.player.getZ()));
      } else {
         Vec3d offVec = this.getTwoBlockFjPos(burBlock);
         offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.42132, this.mc.player.getY() + 0.12160004615784, this.mc.player.getZ() + offVec.z * 0.42132));
         offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 0.95, this.mc.player.getY() + 0.200000047683716, this.mc.player.getZ() + offVec.z * 0.95));
         offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.03, this.mc.player.getY() + 0.200000047683716, this.mc.player.getZ() + offVec.z * 1.03));
         offsets.add(new Vec3d(this.mc.player.getX() + offVec.x * 1.0933, this.mc.player.getY() + 0.12160004615784, this.mc.player.getZ() + offVec.z * 1.0933));
      }

      return offsets;
   }

   private boolean isAir(Vec3d vec3d) {
      return this.mc.world.getBlockState(this.vec3dToBlockPos(vec3d, true)).getBlock().equals(Blocks.AIR);
   }

   private BlockPos vec3dToBlockPos(Vec3d vec3d, boolean Yfloor) {
      return Yfloor ? BlockPos.ofFloored(Math.floor(vec3d.x), Math.floor(vec3d.y), Math.floor(vec3d.z)) : BlockPos.ofFloored(Math.floor(vec3d.x), (double)Math.round(vec3d.y), Math.floor(vec3d.z));
   }

   private boolean fakeBoxCheckFeet(PlayerEntity player, Vec3d offset) {
      Vec3d futurePos = player.getPos().add(offset);
      return this.isAir(futurePos.add(0.3, (double)0.0F, 0.3)) && this.isAir(futurePos.add(-0.3, (double)0.0F, 0.3)) && this.isAir(futurePos.add(0.3, (double)0.0F, -0.3)) && this.isAir(futurePos.add(-0.3, (double)0.0F, 0.3));
   }

   private void updateFakeJump(List<Vec3d> offsets) {
      if (offsets != null) {
         offsets.forEach((vec) -> {
            if (vec != null && !vec.equals(Vec3d.ZERO)) {
               this.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(vec.x, vec.y, vec.z, false));
            }

         });
      }
   }

   public Vec3d getTwoBlockFjPos(BlockPos burBlockPos) {
      Vec3d v = (new Vec3d((double)burBlockPos.getX(), (double)burBlockPos.getY(), (double)burBlockPos.getZ())).add((double)0.5F, (double)0.5F, (double)0.5F);
      BlockPos pPos = this.mc.player.getBlockPos();
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

         for(Direction f3 : Direction.values()) {
            if (f3 != Direction.UP && f3 != Direction.DOWN && this.mc.world.isAir(pPos.offset(f3)) && this.mc.world.isAir(pPos.offset(f3).offset(Direction.UP))) {
               facList.add(f3);
            }
         }

         Vec3d vec3d = this.mc.player.getPos();
         Vec3d[] offVec1 = new Vec3d[1];
         Vec3d[] offVec2 = new Vec3d[1];
         facList.sort((f1, f2) -> {
            offVec1[0] = vec3d.add((new Vec3d((double)f1.getOffsetX(), (double)f1.getOffsetY(), (double)f1.getOffsetZ())).multiply((double)0.5F));
            offVec2[0] = vec3d.add((new Vec3d((double)f2.getOffsetX(), (double)f2.getOffsetY(), (double)f2.getOffsetZ())).multiply((double)0.5F));
            return (int)(Math.sqrt(this.mc.player.squaredDistanceTo(offVec1[0].x, this.mc.player.getY(), offVec1[0].z)) - Math.sqrt(this.mc.player.squaredDistanceTo(offVec2[0].x, this.mc.player.getY(), offVec2[0].z)));
         });
         if (facList.size() > 0) {
            off = new Vec3d((double)((Direction)facList.get(0)).getOffsetX(), (double)((Direction)facList.get(0)).getOffsetY(), (double)((Direction)facList.get(0)).getOffsetZ());
         }
      }

      return off;
   }

   private void updateLagBack() {
      switch (((LagBackMode)this.lagBackMode.get()).ordinal()) {
         case 0:
            for(int i = 0; i < (Integer)this.rubberbandPackets.get(); ++i) {
               this.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + (Double)this.rubberbandOffset.get(), this.mc.player.getZ(), false));
            }
            break;
         case 1:
            if (this.mc.player.getY() >= (double)3.0F) {
               for(int i = -10; i < 10; ++i) {
                  if (i == -1) {
                     i = 4;
                  }

                  if (this.mc.world.getBlockState(this.mc.player.getBlockPos().add(0, i, 0)).getBlock().equals(Blocks.AIR) && this.mc.world.getBlockState(this.mc.player.getBlockPos().add(0, i + 1, 0)).getBlock().equals(Blocks.AIR)) {
                     BlockPos pos = this.mc.player.getBlockPos().add(0, i, 0);
                     this.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround((double)pos.getX() + 0.3, (double)pos.getY(), (double)pos.getZ() + 0.3, false));
                     return;
                  }
               }
            }

            this.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() - (double)5.0F, this.mc.player.getZ(), false));
            break;
         case 2:
            this.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + 3.3400880035762786, this.mc.player.getZ(), false));
            this.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() - (double)1.0F, this.mc.player.getZ(), false));
            break;
         case 3:
            this.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() - (double)7.0F, this.mc.player.getZ(), false));
      }

   }

   private void getPlacePos() {
      this.placePositions.clear();
      this.placePositions.add(BlockPos.ofFloored(this.mc.player.getPos()));
      this.placePositions.add(BlockPos.ofFloored(new Vec3d(this.mc.player.getX() - 0.2, this.mc.player.getY(), this.mc.player.getZ())));
      this.placePositions.add(BlockPos.ofFloored(new Vec3d(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ() - 0.2)));
      this.placePositions.add(BlockPos.ofFloored(new Vec3d(this.mc.player.getX() - 0.2, this.mc.player.getY(), this.mc.player.getZ())));
      this.placePositions.add(BlockPos.ofFloored(new Vec3d(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ() - 0.2)));
      this.placePositions.add(BlockPos.ofFloored(new Vec3d(this.mc.player.getX() - 0.2, this.mc.player.getY(), this.mc.player.getZ())));
      this.placePositions.add(BlockPos.ofFloored(new Vec3d(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ() + 0.2)));
      this.placePositions.add(BlockPos.ofFloored(new Vec3d(this.mc.player.getX() + 0.2, this.mc.player.getY(), this.mc.player.getZ())));
      this.placePositions.add(BlockPos.ofFloored(new Vec3d(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ() + 0.2)));
      this.placePositions.add(BlockPos.ofFloored(new Vec3d(this.mc.player.getX() + 0.2, this.mc.player.getY(), this.mc.player.getZ() + 0.2)));
      this.placePositions.add(BlockPos.ofFloored(new Vec3d(this.mc.player.getX() - 0.2, this.mc.player.getY(), this.mc.player.getZ() - 0.2)));
   }

   private void updatePlace(BlockPos blockPos) {
      label51: {
         FindItemResult item = !((SwitchMode)this.switchMode.get()).equals(BurrowPlus.SwitchMode.Silent) ? InvUtils.find((itemStack) -> ((List)this.block.get()).contains(Block.getBlockFromItem(itemStack.getItem()))) : InvUtils.findInHotbar((itemStack) -> ((List)this.block.get()).contains(Block.getBlockFromItem(itemStack.getItem())));
         if (!((SwitchMode)this.switchMode.get()).equals(BurrowPlus.SwitchMode.Silent)) {
            if (!item.found()) {
               break label51;
            }
         } else if (!item.isHotbar()) {
            break label51;
         }

         this.updateAttack(blockPos);
         if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.start(blockPos, (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}));
         }

         switch (((SwitchMode)this.switchMode.get()).ordinal()) {
            case 0 -> InvUtils.swap(item.slot(), true);
            case 1 -> BOInvUtils.invSwitch(item.slot());
            case 2 -> BOInvUtils.pickSwitch(item.slot());
         }

         this.placePositions.stream().filter((placePos) -> !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(placePos)), (entity) -> entity instanceof EndCrystalEntity && System.currentTimeMillis() - this.lastAttack > 100L)).forEach((placePos) -> this.placeBlock(placePos, item));
         switch (((SwitchMode)this.switchMode.get()).ordinal()) {
            case 0 -> InvUtils.swapBack();
            case 1 -> BOInvUtils.swapBack();
            case 2 -> BOInvUtils.pickSwapBack();
         }

         if ((Boolean)this.placeSwing.get()) {
            this.clientSwing((SwingHand)this.placeHand.get(), Hand.MAIN_HAND);
         }

         if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
         }

         return;
      }

      this.toggle();
      this.sendDisableMsg("correct blocks not found");
   }

   private void placeBlock(BlockPos blockPos, FindItemResult item) {
      this.place(blockPos, item);
      this.renderBlocks.add(new Render(blockPos, System.currentTimeMillis()));
   }

   private void place(BlockPos blockPos, FindItemResult findItemResult) {
      if (findItemResult.isOffhand()) {
         this.place(Hand.OFF_HAND, blockPos, this.mc.player.getInventory().selectedSlot);
      }

      this.place(Hand.MAIN_HAND, blockPos, findItemResult.slot());
   }

   private void place(Hand hand, BlockPos blockPos, int slot) {
      if (slot >= 0 && slot <= 8) {
         Vec3d hitPos = Vec3d.ofCenter(blockPos);
         Direction side = BlockUtils.getPlaceSide(blockPos);
         BlockPos neighbour;
         if (side == null) {
            side = Direction.UP;
            neighbour = blockPos;
         } else {
            neighbour = blockPos.offset(side);
            hitPos = hitPos.add((double)side.getOffsetX() * (double)0.5F, (double)side.getOffsetY() * (double)0.5F, (double)side.getOffsetZ() * (double)0.5F);
         }

         this.placeBlock(hand, hitPos, side.getOpposite(), neighbour);
         if ((Boolean)this.placeSwing.get()) {
            this.clientSwing((SwingHand)this.placeHand.get(), hand);
         }

      }
   }

   private Entity getBlocking(BlockPos blockPos) {
      Entity crystal = null;
      double lowest = (double)1000.0F;

      for(Entity entity : this.mc.world.getEntities()) {
         if (entity instanceof EndCrystalEntity && !(this.mc.player.distanceTo(entity) > 5.0F) && SettingUtils.inAttackRange(entity.getBoundingBox()) && Box.from(new BlockBox(blockPos)).intersects(entity.getBoundingBox())) {
            double dmg = BODamageUtils.crystal(this.mc.player, this.mc.player.getBoundingBox(), entity.getPos(), (BlockPos)null, false);
            if (dmg < lowest) {
               crystal = entity;
               lowest = dmg;
            }
         }
      }

      return crystal;
   }

   private void updateAttack(BlockPos blockPos) {
      if ((Boolean)this.attack.get()) {
         if (!((double)this.mc.player.getHealth() < (Double)this.minHealth.get())) {
            if (!((double)(System.currentTimeMillis() - this.lastAttack) < (double)1000.0F / (Double)this.attackSpeed.get())) {
               Entity blocking = this.getBlocking(blockPos);
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

   public static enum LagBackMode {
      OBS,
      Seija,
      Troll,
      Old;

      // $FF: synthetic method
      private static LagBackMode[] $values() {
         return new LagBackMode[]{OBS, Seija, Troll, Old};
      }
   }

   public static record Render(BlockPos blockPos, long time) {
   }

   private class Renderer {
      @EventHandler
      private void onRender(Render3DEvent event) {
         if ((Boolean)BurrowPlus.this.render.get()) {
            BurrowPlus.this.renderBlocks.removeIf((r) -> System.currentTimeMillis() - r.time > 1000L);
            BurrowPlus.this.renderBlocks.forEach((r) -> {
               double progress = (double)1.0F - Math.min((double)(System.currentTimeMillis() - r.time) + (Double)BurrowPlus.this.renderTime.get() * (double)1000.0F, (Double)BurrowPlus.this.fadeTime.get() * (double)1000.0F) / ((Double)BurrowPlus.this.fadeTime.get() * (double)1000.0F);
               event.renderer.box(r.blockPos, RenderUtils.injectAlpha((Color)BurrowPlus.this.sideColor.get(), (int)Math.round((double)((SettingColor)BurrowPlus.this.sideColor.get()).a * progress)), RenderUtils.injectAlpha((Color)BurrowPlus.this.lineColor.get(), (int)Math.round((double)((SettingColor)BurrowPlus.this.lineColor.get()).a * progress)), (ShapeMode)BurrowPlus.this.shapeMode.get(), 0);
            });
         }
      }
   }
}
