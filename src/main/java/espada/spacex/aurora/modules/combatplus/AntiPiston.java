package espada.spacex.aurora.modules.combatplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.RotationType;
import espada.spacex.aurora.managers.Managers;
import espada.spacex.aurora.utils.BOBlockUtil;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.SettingUtils;
import java.util.Objects;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.PistonBlock;

public class AntiPiston extends Modules {
   int slot = -1;

   public AntiPiston() {
      super(Aurora.CombatPlus, "AntiPiston", "1");
   }

   @EventHandler
   public void onTick(TickEvent.Post event) {
      if (!nullCheck()) {
         if (this.mc.player.isOnGround()) {
            this.slot = InvUtils.findInHotbar(new Item[]{Items.OBSIDIAN}).slot();
            if (this.slot != -1) {
               BlockPos eyePos = BlockPos.ofFloored(this.mc.player.getEyePos());
               if (BOBlockUtil.isAir(eyePos.up())) {
                  for(Direction direction : Direction.values()) {
                     if (direction != Direction.DOWN && direction != Direction.UP && (BOBlockUtil.getBlock(eyePos.offset(direction)) instanceof PistonBlock || BOBlockUtil.getBlock(eyePos.offset(direction)) == Blocks.MOVING_PISTON || BOBlockUtil.getBlock(eyePos.offset(direction)) == Blocks.PISTON_HEAD)) {
                        this.doPlace(Hand.MAIN_HAND, eyePos.offset(direction.getOpposite()));
                        this.doPlace(Hand.MAIN_HAND, eyePos.offset(direction).up());
                     }
                  }

               }
            }
         }
      }
   }

   public void doPlace(Hand hand, BlockPos pos) {
      if (BOBlockUtil.isAir(pos) && !BOBlockUtil.cantBlockPlace(pos)) {
         PlaceData data = SettingUtils.getPlaceData(pos);
         if (data.valid()) {
            if (!SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(data.pos(), (double)this.priority, RotationType.BlockPlace, (long)Objects.hash(new Object[]{this.name + "placing"}))) {
               InvUtils.swap(this.slot, true);
               this.placeBlock(hand, data.pos().toCenterPos(), data.dir(), data.pos());
               InvUtils.swapBack();
               if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
                  Managers.ROTATION.end((long)Objects.hash(new Object[]{this.name + "placing"}));
               }

            }
         }
      }
   }
}
