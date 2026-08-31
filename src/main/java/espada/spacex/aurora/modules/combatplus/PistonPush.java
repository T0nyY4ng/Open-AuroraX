package espada.spacex.aurora.modules.combatplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.modules.combatplus.automine.AuroraMine;
import espada.spacex.aurora.utils.BOBlockUtil;
import espada.spacex.aurora.utils.BOInvUtils;
import espada.spacex.aurora.utils.EntityInfo;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.block.PistonBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockBox;

public class PistonPush extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<Integer> targetRange;
   private final Setting<Boolean> deBug;
   private final Setting<Boolean> pullPlace;
   private final Setting<Boolean> pullMiner;
   private final Setting<Boolean> selfnoground;
   private final Setting<Boolean> targetnoground;
   private final Setting<Boolean> pauseEat;
   private final Setting<Boolean> toggleModules;
   private final Setting<Boolean> toggleBack;
   private final Setting<Integer> delay;
   private final ArrayList<Module> toActivate = new ArrayList();
   private final Setting<List<Module>> modules;
   int redBlock;
   int redTorch;
   int pistonBlock;
   int place;
   BlockPos Mine;
   private PlayerEntity target;
   private final Timer timer = new Timer();

   public PistonPush() {
      super(Aurora.CombatPlus, "PistonPush", "Push your lover.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Delay")).defaultValue(35)).min(0).sliderRange(0, 2000).build());
      this.targetRange = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Target Range")).description("The range players can be targeted.")).defaultValue(5)).sliderRange(0, 7).build());
      this.pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause Eat")).description("Pauses when you are eating.")).defaultValue(true)).build());
      this.pullPlace = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pull Place")).description("")).defaultValue(true)).build());
      this.pullMiner = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pull Miner")).description("")).defaultValue(true)).build());
      this.selfnoground = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("PauseSelfNoGround")).description("")).defaultValue(true)).build());
      this.targetnoground = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("PauseTargetNoGround")).description("")).defaultValue(true)).build());
      this.deBug = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("de Bug")).description("")).defaultValue(false)).build());
      this.toggleModules = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("toggleModules")).description("")).defaultValue(false)).build());
      this.toggleBack = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("toggleBack")).description("")).defaultValue(false)).build());
      this.modules = this.sgGeneral.add(((ModuleListSetting.Builder)((ModuleListSetting.Builder)(new ModuleListSetting.Builder()).name("toggleBack")).description("")).build());
      this.place = 0;
   }

   public String getInfoString() {
      return this.target != null ? this.target.getGameProfile().getName() : null;
   }

   @EventHandler
   public void onTick(TickEvent.Post event) {
      if (this.timer.passedMs((long)(Integer)this.delay.get())) {
         if (nullCheck()) {
            return;
         }

         if (!this.mc.player.isOnGround() && (Boolean)this.selfnoground.get()) {
            return;
         }

         if (!this.pauseCheck()) {
            return;
         }

         this.redBlock = BOInvUtils.findHotbarBlock(Blocks.REDSTONE_BLOCK);
         this.redTorch = BOInvUtils.findHotbarBlock(Blocks.REDSTONE_TORCH);
         this.pistonBlock = BOInvUtils.findHotbarBlock(Blocks.PISTON);
         if (this.redBlock == -1 && this.redTorch == -1 || this.pistonBlock == -1) {
            return;
         }

         this.place = 0;

         for(PlayerEntity player : this.mc.world.getPlayers()) {
            if (!player.isOnGround() && (Boolean)this.targetnoground.get()) {
               return;
            }

            if (!this.HeadBlock(player) && !this.skip(player) && !Friends.get().isFriend(player) && player != this.mc.player) {
               this.target = player;
               int i = 0;

               for(Direction position : Direction.values()) {
                  BlockPos enemy = EntityInfo.playerPos(this.target);
                  BlockPos push = EntityInfo.playerPos(this.target).up();
                  if (!position.equals(Direction.DOWN) && !position.equals(Direction.UP)) {
                     ++i;
                     if (this.isPiston(push.offset(position), position.getOpposite()) && (BOBlockUtil.isAir(push.offset(position.getOpposite())) && BOBlockUtil.isAir(push.offset(position.getOpposite()).up()) || !BOBlockUtil.isAir(enemy) && this.mineCheck(push.offset(position).up()) && this.HardBlock(push.offset(position).down()) && this.redBlock != -1 && (Boolean)this.pullPlace.get()) && this.getRedStone(push.offset(position)) != null) {
                        if ((Boolean)this.deBug.get()) {
                           this.error(position.getName(), new Object[0]);
                        }

                        if ((Boolean)this.deBug.get()) {
                           this.error(String.valueOf(i), new Object[0]);
                        }

                        this.place(this.getRedStone(push.offset(position)), push.offset(position), this.getYaw(i));
                        if ((Boolean)this.deBug.get()) {
                           this.error(String.valueOf(this.place), new Object[0]);
                        }
                        break;
                     }
                  }
               }
            }
         }
      }

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

      super.onActivate();
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

   private int getYaw(int i) {
      if (i == 1) {
         return 180;
      } else if (i == 2) {
         return 0;
      } else {
         return i == 3 ? 90 : 270;
      }
   }

   private void place(BlockPos redStone, BlockPos piston, int yaw) {
      this.Mine = piston;
      float pitch = 0.0F;
      int tempSlot = this.redBlock != -1 ? this.redBlock : this.redTorch;
      if (BOBlockUtil.isAir(piston) && redStone.equals(piston.up(1)) && !this.HelpBlock(redStone) && this.redBlock != -1) {
         if ((Boolean)this.deBug.get()) {
            this.error("place piston", new Object[0]);
         }

         InvUtils.swap(this.pistonBlock, true);
         this.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround((float)yaw, pitch, Managers.ON_GROUND.isOnGround()));
         this.placePiston(piston);
      }

      if (this.RedStoneCheck(piston) == null) {
         if ((Boolean)this.deBug.get()) {
            this.error("place redStone", new Object[0]);
         }

         InvUtils.swap(tempSlot, true);
         this.placeRedStone(redStone);
      }

      if (BOBlockUtil.getBlock(piston) == Blocks.AIR) {
         if ((Boolean)this.deBug.get()) {
            this.error("place piston", new Object[0]);
         }

         InvUtils.swap(this.pistonBlock, true);
         this.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround((float)yaw, pitch, Managers.ON_GROUND.isOnGround()));
         this.placePiston(piston);
      }

      InvUtils.swapBack();
      if ((this.redBlock != -1 || this.redTorch != -1) && (Boolean)this.pullPlace.get() && (Boolean)this.pullMiner.get()) {
         BlockPos block = this.RedStoneCheck(piston) == null ? redStone : this.RedStoneCheck(piston);
         if (block != null) {
            AuroraMine autoMine = (AuroraMine)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class);
            if (meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(AuroraMine.class) && block.equals(((AuroraMine)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class)).targetPos())) {
               return;
            }

            Direction dir = SettingUtils.getPlaceOnDirection(block);
            autoMine.onStart(block, dir == null ? Direction.UP : dir);
         }
      }

   }

   private void placeRedStone(BlockPos pos) {
      PlaceData data = SettingUtils.getPlaceData(pos);
      if (data.valid() && (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(data.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"})))) {
         this.placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());
         if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
         }
      }

   }

   private void placePiston(BlockPos pos) {
      PlaceData data = SettingUtils.getPlaceData(pos);
      if (data.valid()) {
         this.placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());
      }

   }

   private boolean isPiston(BlockPos block, Direction facing) {
      double sum = BOBlockUtil.getPushDistance(this.mc.player, (double)block.getX() + (double)0.5F, (double)block.getZ() + (double)0.5F);
      return BOBlockUtil.getBlock(block) == Blocks.AIR || BOBlockUtil.getBlock(block) instanceof PistonBlock && ((Direction)BOBlockUtil.getState(block).get(Properties.FACING)).equals(facing) && !Managers.BREAK.isMine(block, true) && this.CanPlace(block) && (sum > 2.4 || this.target.getY() == this.mc.player.getY() || this.target.getY() + (double)1.0F == this.mc.player.getY());
   }

   private boolean skip(PlayerEntity player) {
      BlockPos target = EntityInfo.playerPos(player);
      return BOBlockUtil.getBlock(target.add(0, 1, 0)) == Blocks.OBSIDIAN || BOBlockUtil.getBlock(target.add(0, 1, 0)) == Blocks.BEDROCK || BOBlockUtil.getBlock(target) == Blocks.COBWEB;
   }

   private boolean HeadBlock(PlayerEntity player) {
      BlockPos target = EntityInfo.playerPos(player);
      return BOBlockUtil.getBlock(target.add(1, 0, 0)) != Blocks.AIR && BOBlockUtil.getBlock(target.add(-1, 0, 0)) != Blocks.AIR && BOBlockUtil.getBlock(target.add(0, 0, 1)) != Blocks.AIR && BOBlockUtil.getBlock(target.add(0, 0, -1)) != Blocks.AIR && BOBlockUtil.getBlock(target.add(0, 2, 0)) != Blocks.AIR;
   }

   private boolean CanPlace(BlockPos pos) {
      return !EntityUtils.intersectsWithEntity(Box.from(new BlockBox(pos)), (entity) -> !entity.isSpectator() && !(entity instanceof ItemEntity)) && PlayerUtils.isWithin(pos, (double)(Integer)this.targetRange.get());
   }

   private boolean mineCheck(BlockPos block) {
      return BOBlockUtil.getBlock(block) != Blocks.OBSIDIAN && BOBlockUtil.getBlock(block) != Blocks.BEDROCK;
   }

   private boolean HardBlock(BlockPos pos) {
      return BOBlockUtil.getBlock(pos) == Blocks.OBSIDIAN || BOBlockUtil.getBlock(pos) == Blocks.BEDROCK || BOBlockUtil.getBlock(pos) instanceof PistonBlock && this.redBlock != -1;
   }

   public BlockPos getRedStone(BlockPos pos) {
      if (this.isRedStone(pos.up()) && this.redBlock != -1) {
         return pos.up();
      } else if (this.isRedStone(pos.down())) {
         return pos.down();
      } else if (this.isRedStone(pos.add(1, 0, 0))) {
         return pos.add(1, 0, 0);
      } else if (this.isRedStone(pos.add(0, 0, 1))) {
         return pos.add(0, 0, 1);
      } else if (this.isRedStone(pos.add(-1, 0, 0))) {
         return pos.add(-1, 0, 0);
      } else if (this.isRedStone(pos.add(0, 0, -1))) {
         return pos.add(0, 0, -1);
      } else if (BOBlockUtil.isAir(pos.add(0, 1, 0)) && this.HelpBlock(pos) && this.redBlock != -1) {
         return pos.add(0, 1, 0);
      } else if (this.isRedStone(pos.add(1, 1, 0)) && this.HardBlock(pos.add(1, 0, 0)) && this.redBlock == -1) {
         return pos.add(1, 1, 0);
      } else if (this.isRedStone(pos.add(0, 1, 1)) && this.HardBlock(pos.add(0, 0, 1)) && this.redBlock == -1) {
         return pos.add(0, 1, 1);
      } else if (this.isRedStone(pos.add(-1, 1, 0)) && this.HardBlock(pos.add(-1, 0, 0)) && this.redBlock == -1) {
         return pos.add(-1, 1, 0);
      } else if (this.isRedStone(pos.add(0, 1, -1)) && this.HardBlock(pos.add(0, 0, -1)) && this.redBlock == -1) {
         return pos.add(0, 1, -1);
      } else if (this.isRedStone(pos.add(1, -1, 0)) && this.HardBlock(pos.add(1, 0, 0)) && this.redBlock == -1) {
         return pos.add(1, -1, 0);
      } else if (this.isRedStone(pos.add(0, -1, 1)) && this.HardBlock(pos.add(0, 0, 1)) && this.redBlock == -1) {
         return pos.add(0, -1, 1);
      } else if (this.isRedStone(pos.add(-1, -1, 0)) && this.HardBlock(pos.add(-1, 0, 0)) && this.redBlock == -1) {
         return pos.add(-1, -1, 0);
      } else if (this.isRedStone(pos.add(0, -1, -1)) && this.HardBlock(pos.add(0, 0, -1)) && this.redBlock == -1) {
         return pos.add(0, -1, -1);
      } else {
         return this.isRedStone(pos.add(0, -2, 0)) && this.HardBlock(pos.add(0, -1, 0)) && this.redBlock == -1 ? pos.add(0, -2, 0) : null;
      }
   }

   private BlockPos RedStoneCheck(BlockPos block) {
      for(Direction face : Direction.values()) {
         if (this.RedStone(block.offset(face))) {
            return block.offset(face);
         }
      }

      return null;
   }

   private boolean RedStone(BlockPos block) {
      return BOBlockUtil.getBlock(block) == Blocks.REDSTONE_BLOCK || BOBlockUtil.getBlock(block) == Blocks.REDSTONE_TORCH;
   }

   private boolean isRedStone(BlockPos block) {
      return (BOBlockUtil.getBlock(block) == Blocks.AIR || BOBlockUtil.getBlock(block) == Blocks.REDSTONE_BLOCK || BOBlockUtil.getBlock(block) == Blocks.REDSTONE_TORCH) && this.HelpBlock(block) && !Managers.BREAK.isMine(block, true) && this.CanPlace(block);
   }

   private boolean HelpBlock(BlockPos pos) {
      return this.HardBlock(pos.add(0, 1, 0)) && this.redBlock != -1 || this.HardBlock(pos.add(0, -1, 0)) || this.HardBlock(pos.add(1, 0, 0)) || this.HardBlock(pos.add(-1, 0, 0)) || this.HardBlock(pos.add(0, 0, 1)) || this.HardBlock(pos.add(0, 0, -1));
   }

   private boolean pauseCheck() {
      return !(Boolean)this.pauseEat.get() || !this.mc.player.isUsingItem();
   }
}
