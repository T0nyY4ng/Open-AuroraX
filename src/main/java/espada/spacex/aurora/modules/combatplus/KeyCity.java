package espada.spacex.aurora.modules.combatplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.modules.combatplus.automine.AuroraMine;
import espada.spacex.aurora.utils.BOBlockUtil;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.Timer;
import espada.spacex.aurora.utils.Util;
import java.util.Objects;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;

public class KeyCity extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<Double> targetRange;
   private final Setting<Boolean> mineHead;
   private final Setting<Boolean> onlybur;
   private final Setting<Integer> delay;
   private final Setting<Boolean> pauseEat;
   private PlayerEntity target;
   private final Timer timer = new Timer();
   AuroraMine autoMine = (AuroraMine)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class);

   public KeyCity() {
      super(Aurora.CombatPlus, "AntiSurround", "Breaks target's surround with PacketMine.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Delay")).defaultValue(35)).min(0).sliderRange(0, 2000).build());
      this.targetRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Target Range")).description("The range players can be targeted.")).defaultValue((double)5.0F).sliderRange((double)0.0F, (double)7.0F).build());
      this.mineHead = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Mine Head")).description("an.")).defaultValue(true)).build());
      this.onlybur = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Only Burrow")).description("an.")).defaultValue(true)).build());
      this.pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause On Eat")).description("Pause while eating.")).defaultValue(false)).build());
   }

   @EventHandler
   public void onTick(TickEvent.Post event) {
      if (!(Boolean)this.pauseEat.get() || !this.mc.player.isUsingItem()) {
         if (this.timer.passedMs((long)(Integer)this.delay.get())) {
            this.target = TargetUtils.getPlayerTarget((double)((Double)this.targetRange.get()).intValue(), SortPriority.LowestDistance);
            if (TargetUtils.isBadTarget(this.target, (double)((Double)this.targetRange.get()).intValue())) {
               return;
            }

            if (!InvUtils.findInHotbar(new Item[]{Items.IRON_PICKAXE, Items.NETHERITE_PICKAXE, Items.DIAMOND_PICKAXE}).found()) {
               return;
            }

            this.surroundMine();
         }

      }
   }

   private void surroundMine() {
      if (this.target != null) {
         BlockPos feet = this.target.getBlockPos();
         if (this.canMine(BOBlockUtil.vec3toBlockPos(this.target.getPos().add(0.2, (double)0.0F, 0.2)))) {
            this.surroundMine(BOBlockUtil.vec3toBlockPos(this.target.getPos().add(0.2, (double)0.0F, 0.2)));
         } else if (this.canMine(BOBlockUtil.vec3toBlockPos(this.target.getPos().add(-0.2, (double)0.0F, 0.2)))) {
            this.surroundMine(BOBlockUtil.vec3toBlockPos(this.target.getPos().add(-0.2, (double)0.0F, 0.2)));
         } else if (this.canMine(BOBlockUtil.vec3toBlockPos(this.target.getPos().add(-0.2, (double)0.0F, -0.2)))) {
            this.surroundMine(BOBlockUtil.vec3toBlockPos(this.target.getPos().add(-0.2, (double)0.0F, -0.2)));
         } else if (this.canMine(BOBlockUtil.vec3toBlockPos(this.target.getPos().add(0.2, (double)0.0F, -0.2)))) {
            this.surroundMine(BOBlockUtil.vec3toBlockPos(this.target.getPos().add(0.2, (double)0.0F, -0.2)));
         } else if ((Boolean)this.mineHead.get() && this.canMine2(feet.up().up())) {
            this.surroundMine(feet.up().up());
         } else if ((Boolean)this.mineHead.get() && this.canMine2(feet.up().up().up())) {
            this.surroundMine(feet.up().up().up());
         } else {
            if (!(Boolean)this.onlybur.get() && !this.CheckMineSur()) {
               if (this.canMine2(feet.east())) {
                  this.surroundMine(feet.east());
                  return;
               }

               if (this.canMine2(feet.west())) {
                  this.surroundMine(feet.west());
                  return;
               }

               if (this.canMine2(feet.south())) {
                  this.surroundMine(feet.south());
                  return;
               }

               if (this.canMine2(feet.north())) {
                  this.surroundMine(feet.north());
               }
            }

         }
      }
   }

   private void surroundMine(BlockPos position) {
      Direction dir = SettingUtils.getPlaceOnDirection(position);
      if (dir != null) {
         this.mc.interactionManager.attackBlock(position, dir);
         this.timer.reset();
      }
   }

   private boolean canMine(BlockPos block) {
      if (!this.isSelf(block) && !this.isFriend(block)) {
         Direction dir = SettingUtils.getPlaceOnDirection(block);
         if (dir == null) {
            return false;
         } else if (this.isMine2()) {
            return false;
         } else if (this.isMine1() && this.CheckMinePos1(block)) {
            return false;
         } else if (!SettingUtils.inMineRange(block)) {
            return false;
         } else {
            return !this.isAir(block) && !this.godBlock(block) && BOBlockUtil.getBlock(block) != Blocks.COBWEB;
         }
      } else {
         return false;
      }
   }

   private boolean canMine2(BlockPos block) {
      if (!this.isSelf(block) && !this.isFriend(block)) {
         Direction dir = SettingUtils.getPlaceOnDirection(block);
         if (dir == null) {
            return false;
         } else if (this.isMine2()) {
            return false;
         } else if (this.isMine1() && this.CheckMinePos1(block)) {
            return false;
         } else if (!SettingUtils.inMineRange(block)) {
            return false;
         } else {
            return !this.isAir(block) && !this.godBlock(block) && BOBlockUtil.getBlock(block) != Blocks.COBWEB && BOBlockUtil.getBlock(block) != Blocks.RESPAWN_ANCHOR;
         }
      } else {
         return false;
      }
   }

   private boolean isSelf(BlockPos pos) {
      for(Entity entity : Util.mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
         if (entity == Util.mc.player) {
            return true;
         }
      }

      return false;
   }

   private boolean isFriend(BlockPos pos) {
      for(PlayerEntity entity : Util.mc.world.getNonSpectatingEntities(PlayerEntity.class, new Box(pos))) {
         if (Friends.get().isFriend(entity)) {
            return true;
         }
      }

      return false;
   }

   private boolean godBlock(BlockPos block) {
      return BOBlockUtil.getBlock(block) == Blocks.BEDROCK;
   }

   private boolean isAir(BlockPos block) {
      return BOBlockUtil.getBlock(block) == Blocks.AIR;
   }

   public String getInfoString() {
      return this.target != null ? this.target.getGameProfile().getName() : null;
   }

   private boolean isMine1() {
      return meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(AuroraMine.class) && ((AuroraMine)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class)).targetPos() != null;
   }

   private boolean CheckMinePos1(BlockPos pos) {
      return meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(AuroraMine.class) && pos.equals(((AuroraMine)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class)).targetPos());
   }

   private boolean isMine2() {
      return meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(AuroraMine.class) && ((AuroraMine)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class)).breakPos != null;
   }

   private BlockPos getMinePos() {
      return ((AuroraMine)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AuroraMine.class)).targetPos();
   }

   private boolean CheckMineSur() {
      BlockPos feet = this.target.getBlockPos();
      if (Objects.equals(this.getMinePos(), feet.east())) {
         return true;
      } else if (Objects.equals(this.getMinePos(), feet.west())) {
         return true;
      } else if (Objects.equals(this.getMinePos(), feet.south())) {
         return true;
      } else {
         return Objects.equals(this.getMinePos(), feet.north());
      }
   }
}
