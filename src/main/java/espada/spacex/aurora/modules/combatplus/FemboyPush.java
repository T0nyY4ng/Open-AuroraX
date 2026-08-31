package espada.spacex.aurora.modules.combatplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.enums.HoleType;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.HoleUtils;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.SettingUtils;
import java.util.Arrays;
import java.util.Comparator;
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
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.ItemEntity;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction.Type;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;

public class FemboyPush extends Module {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgDelay;
   private final SettingGroup sgSwing;
   private final SettingGroup sgRender;
   private final Setting<Boolean> pauseEat;
   private final Setting<Redstone> redstone;
   private final Setting<Boolean> onlyHole;
   private final Setting<Boolean> toggleMove;
   private final Setting<SwitchMode> pistonSwitch;
   private final Setting<SwitchMode> redstoneSwitch;
   private final Setting<Double> prDelay;
   private final Setting<Double> rmDelay;
   private final Setting<Double> mpDelay;
   private final Setting<Boolean> pistonSwing;
   private final Setting<SwingHand> pistonHand;
   private final Setting<Boolean> redstoneSwing;
   private final Setting<SwingHand> redstoneHand;
   private final Setting<ShapeMode> pistonShape;
   private final Setting<SettingColor> psColor;
   private final Setting<SettingColor> plColor;
   private final Setting<ShapeMode> redstoneShape;
   private final Setting<SettingColor> rsColor;
   private final Setting<SettingColor> rlColor;
   private long pistonTime;
   private long redstoneTime;
   private long mineTime;
   private boolean minedThisTick;
   private boolean pistonPlaced;
   private boolean redstonePlaced;
   private boolean mined;
   private BlockPos pistonPos;
   private BlockPos redstonePos;
   private Direction pistonDir;
   private PlaceData pistonData;
   private PlaceData redstoneData;
   private BlockPos lastPiston;
   private BlockPos lastRedstone;
   private Direction lastDirection;
   private BlockPos startPos;
   private BlockPos currentPos;

   public FemboyPush() {
      super(Aurora.CombatPlus, "Piston Push", "Pushes people out of their safe holes.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgDelay = this.settings.createGroup("Delay");
      this.sgSwing = this.settings.createGroup("Swing");
      this.sgRender = this.settings.createGroup("Render");
      this.pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause Eat")).description("Pauses when eating.")).defaultValue(false)).build());
      this.redstone = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Redstone")).description("What kind of redstone to use.")).defaultValue(FemboyPush.Redstone.Torch)).build());
      this.onlyHole = this.sgSwing.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Only Hole")).description("Toggles when enemy moves.")).defaultValue(true)).build());
      this.toggleMove = this.sgSwing.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Toggle Move")).description("Toggles when enemy moves.")).defaultValue(true)).build());
      this.pistonSwitch = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Piston Switch")).description("Method of switching. Silent is the most reliable.")).defaultValue(FemboyPush.SwitchMode.Silent)).build());
      this.redstoneSwitch = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Redstone Switch")).description("Method of switching. Silent is the most reliable.")).defaultValue(FemboyPush.SwitchMode.Silent)).build());
      this.prDelay = this.sgDelay.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Piston > Redstone")).description("How many seconds to wait between placing piston and redstone.")).defaultValue((double)0.0F).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.rmDelay = this.sgDelay.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Redstone > Mine")).description("How many seconds to wait between placing redstone and starting to mine it.")).defaultValue(0.2).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.mpDelay = this.sgDelay.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Mine > Piston")).description("How many seconds to wait after mining the redstone before starting a new cycle.")).defaultValue(0.2).min((double)0.0F).sliderRange((double)0.0F, (double)20.0F).build());
      this.pistonSwing = this.sgSwing.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Piston Swing")).description("Renders swing animation when placing a piston.")).defaultValue(true)).build());
      SettingGroup var10001 = this.sgSwing;
      EnumSetting.Builder var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Piston Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      Setting<Boolean> var10003 = this.pistonSwing;
      Objects.requireNonNull(var10003);
      this.pistonHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.redstoneSwing = this.sgSwing.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Redstone Swing")).description("Renders swing animation when placing redstone.")).defaultValue(true)).build());
      var10001 = this.sgSwing;
      var10002 = (EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Redstone Swing Hand")).description("Which hand should be swung.")).defaultValue(SwingHand.RealHand);
      var10003 = this.redstoneSwing;
      Objects.requireNonNull(var10003);
      this.redstoneHand = var10001.add(((EnumSetting.Builder)var10002.visible(var10003::get)).build());
      this.pistonShape = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Piston Shape Mode")).description("Which parts should be rendered.")).defaultValue(ShapeMode.Both)).build());
      this.psColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Piston Side Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 50)).build());
      this.plColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Piston Line Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
      this.redstoneShape = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Redstone Shape Mode")).description("Which parts should be rendered.")).defaultValue(ShapeMode.Both)).build());
      this.rsColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Redstone Side Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 50)).build());
      this.rlColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Redstone Line Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.pistonTime = 0L;
      this.redstoneTime = 0L;
      this.mineTime = 0L;
      this.minedThisTick = false;
      this.pistonPlaced = false;
      this.redstonePlaced = false;
      this.mined = false;
      this.pistonPos = null;
      this.redstonePos = null;
      this.pistonDir = null;
      this.pistonData = null;
      this.redstoneData = null;
      this.lastPiston = null;
      this.lastRedstone = null;
      this.lastDirection = null;
      this.startPos = null;
      this.currentPos = null;
   }

   public void onActivate() {
      this.lastPiston = null;
      this.lastRedstone = null;
      this.lastDirection = null;
      this.startPos = null;
      this.redstonePlaced = false;
      this.pistonPlaced = false;
      this.mined = false;
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
         if (this.startPos != null && (Boolean)this.toggleMove.get() && !this.startPos.equals(this.currentPos)) {
            this.toggle();
            this.info("Toggled off because enemy moved.", new Object[0]);
         } else {
            this.update();
            if (this.pistonPos == null) {
               this.lastPiston = null;
               this.lastRedstone = this.redstonePos;
               this.lastDirection = this.pistonDir;
            } else {
               event.renderer.box(this.getBox(this.pistonPos), (Color)this.psColor.get(), (Color)this.plColor.get(), (ShapeMode)this.pistonShape.get(), 0);
               event.renderer.box(this.getBox(this.redstonePos), (Color)this.rsColor.get(), (Color)this.rlColor.get(), (ShapeMode)this.redstoneShape.get(), 0);
               if ((double)(System.currentTimeMillis() - this.mineTime) > (Double)this.mpDelay.get() * (double)1000.0F && this.redstonePlaced && this.pistonPlaced && this.mined || !this.pistonPos.equals(this.lastPiston) || !this.redstonePos.equals(this.lastRedstone) || !this.pistonDir.equals(this.lastDirection)) {
                  this.redstonePlaced = false;
                  this.pistonPlaced = false;
                  this.mined = false;
               }

               this.lastPiston = this.pistonPos;
               this.lastRedstone = this.redstonePos;
               this.lastDirection = this.pistonDir;
               if (!(Boolean)this.pauseEat.get() || !this.mc.player.isUsingItem()) {
                  this.placePiston();
                  this.placeRedstone();
                  this.mineUpdate();
               }
            }
         }
      }
   }

   private void placePiston() {
      if (!this.pistonPlaced) {
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
            if (this.mc.player.isOnGround()) {
               if (!EntityUtils.intersectsWithEntity(Box.from(new BlockBox(this.pistonPos)), (entity) -> !entity.isSpectator() && !(entity instanceof ItemEntity))) {
                  if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(this.pistonData.pos(), (double)1.0F, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.toString() + "piston"}))) {
                     if (this.mc.getNetworkHandler() != null) {
                        this.mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(this.pistonDir.asRotation(), Managers.ROTATION.lastDir[1], Managers.ON_GROUND.isOnGround()));
                     }

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
                        hand = hand == null ? Hand.MAIN_HAND : hand;
                        this.placeBlock(hand, this.pistonData.pos().toCenterPos(), this.pistonData.dir(), this.pistonData.pos());
                        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                           Managers.ROTATION.end((long)Objects.hash(new Object[]{this.toString() + "piston"}));
                        }

                        this.pistonTime = System.currentTimeMillis();
                        this.pistonPlaced = true;
                        if ((Boolean)this.pistonSwing.get()) {
                           this.clientSwing((SwingHand)this.pistonHand.get(), hand);
                        }

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
   }

   private void placeRedstone() {
      if (this.pistonPlaced && !this.redstonePlaced) {
         if (!((double)(System.currentTimeMillis() - this.pistonTime) < (Double)this.prDelay.get() * (double)1000.0F)) {
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
               if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(this.redstoneData.pos(), (double)1.0F, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.toString() + "redstone"}))) {
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
                        Managers.ROTATION.end((long)Objects.hash(new Object[]{this.toString() + "redstone"}));
                     }

                     this.redstonePlaced = true;
                     this.redstoneTime = System.currentTimeMillis();
                     if ((Boolean)this.redstoneSwing.get()) {
                        this.clientSwing((SwingHand)this.redstoneHand.get(), hand);
                     }

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

   private Box getBox(BlockPos pos) {
      return new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 1), (double)(pos.getZ() + 1));
   }

   private void mineUpdate() {
      if (this.pistonPlaced && this.redstonePlaced) {
         if (!this.minedThisTick) {
            if (!((double)(System.currentTimeMillis() - this.redstoneTime) < (Double)this.rmDelay.get() * (double)1000.0F)) {
               if (this.redstonePos != null) {
                  if (this.redstone.get() != FemboyPush.Redstone.Torch || this.mc.world.getBlockState(this.redstonePos).getBlock() instanceof RedstoneTorchBlock) {
                     if (this.redstone.get() != FemboyPush.Redstone.Block || this.mc.world.getBlockState(this.redstonePos).getBlock() == Blocks.REDSTONE_BLOCK) {
                        Direction mineDir = SettingUtils.getPlaceOnDirection(this.redstonePos);
                        if (mineDir != null && this.mc.getNetworkHandler() != null) {
                           this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.redstonePos, mineDir));
                           this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.redstonePos, mineDir));
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
         }
      }
   }

   private void update() {
      this.pistonPos = null;

      for(PlayerEntity player : this.mc.world.getPlayers()) {
         if (!Friends.get().isFriend(player) && player != this.mc.player && !(this.mc.player.distanceTo(player) > 10.0F) && !(player.getHealth() <= 0.0F) && !player.isSpectator()) {
            if (!OLEPOSSUtils.solid2(player.getBlockPos()) && (Boolean)this.onlyHole.get() && HoleUtils.getHole(player.getBlockPos(), true, true, false, 1, true).type == HoleType.NotHole) {
               return;
            }

            this.updatePos(player);
            if (this.pistonPos != null) {
               return;
            }
         }
      }

   }

   private void updatePos(PlayerEntity player) {
      BlockPos eyePos = BlockPos.ofFloored(player.getEyePos());
      if (!OLEPOSSUtils.solid2(eyePos.up())) {
         for(Direction dir : Type.HORIZONTAL.stream().sorted(Comparator.comparingDouble((d) -> eyePos.offset(d).toCenterPos().distanceTo(this.mc.player.getEyePos()))).toList()) {
            this.resetPos();
            BlockPos pos = eyePos.offset(dir);
            if (this.upCheck(pos) && (OLEPOSSUtils.replaceable(pos) || this.mc.world.getBlockState(pos).getBlock() instanceof PistonBlock || this.mc.world.getBlockState(pos).getBlock() == Blocks.MOVING_PISTON) && !OLEPOSSUtils.solid2(eyePos.offset(dir.getOpposite())) && !OLEPOSSUtils.solid2(eyePos.offset(dir.getOpposite()).up()) && OLEPOSSUtils.solid2(eyePos.offset(dir.getOpposite()).down())) {
               PlaceData data = SettingUtils.getPlaceData(pos);
               if (data != null && data.valid()) {
                  this.pistonData = data;
                  this.pistonDir = dir;
                  this.updateRedstone(pos);
                  if (this.redstonePos != null) {
                     if (this.startPos == null) {
                        this.startPos = player.getBlockPos();
                     }

                     this.currentPos = player.getBlockPos();
                     this.pistonPos = pos;
                     return;
                  }
               }
            }
         }

      }
   }

   private void updateRedstone(BlockPos pos) {
      if (this.redstone.get() == FemboyPush.Redstone.Torch) {
         for(Direction direction : Arrays.stream(Direction.values()).sorted(Comparator.comparingDouble((i) -> pos.offset(i).toCenterPos().distanceTo(this.mc.player.getEyePos()))).toList()) {
            if (direction != this.pistonDir.getOpposite() && direction != Direction.DOWN && direction != Direction.UP) {
               BlockPos position = pos.offset(direction);
               if (OLEPOSSUtils.replaceable(position) || this.mc.world.getBlockState(position).getBlock() instanceof RedstoneTorchBlock) {
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
                     this.redstonePos = position;
                     return;
                  }
               }
            }
         }

         this.redstonePos = null;
      } else {
         for(Direction direction : Arrays.stream(Direction.values()).sorted(Comparator.comparingDouble((i) -> pos.offset(i).toCenterPos().distanceTo(this.mc.player.getEyePos()))).toList()) {
            if (direction != this.pistonDir.getOpposite() && direction != Direction.DOWN) {
               BlockPos position = pos.offset(direction);
               if ((OLEPOSSUtils.replaceable(position) || this.mc.world.getBlockState(position).getBlock() == Blocks.REDSTONE_BLOCK) && !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(position)), (entity) -> !entity.isSpectator() && entity instanceof PlayerEntity)) {
                  Objects.requireNonNull(pos);
                  this.redstoneData = SettingUtils.getPlaceDataOR(position, pos::equals);
                  if (this.redstoneData.valid()) {
                     this.redstonePos = position;
                     return;
                  }
               }
            }
         }

         this.redstonePos = null;
      }
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
      this.pistonPos = null;
      this.redstonePos = null;
      this.pistonDir = null;
      this.pistonData = null;
      this.redstoneData = null;
   }

   private void placeBlock(Hand hand, Vec3d pos, Direction dir, BlockPos blockPos) {
      if (this.mc.interactionManager != null && this.mc.player != null) {
         this.mc.interactionManager.interactBlock(this.mc.player, hand, new BlockHitResult(pos, dir, blockPos, false));
      }

   }

   private void clientSwing(SwingHand swingHand, Hand hand) {
      if (this.mc.player != null) {
         this.mc.player.swingHand(hand);
      }

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
