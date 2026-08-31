package espada.spacex.aurora.modules.miscplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.enums.HoleType;
import espada.spacex.aurora.modules.combatplus.SurroundPlus;
import espada.spacex.aurora.utils.HoleUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class Automation extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<Boolean> holeSurround;
   private BlockPos lastPos;
   private SurroundPlus surround;

   public Automation() {
      super(Aurora.MiscPlus, "Automation", "Automatically enables modules in certain situations.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.holeSurround = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Hole Surround")).description("Enables surround when you enter a hole.")).defaultValue(true)).build());
      this.lastPos = null;
      this.surround = null;
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.mc.player != null && this.mc.world != null) {
         if (this.surround == null) {
            this.surround = (SurroundPlus)meteordevelopment.meteorclient.systems.modules.Modules.get().get(SurroundPlus.class);
         }

         if (!this.mc.player.getBlockPos().equals(this.lastPos) && this.inAHole(this.mc.player) && (Boolean)this.holeSurround.get() && !this.surround.isActive()) {
            this.surround.toggle();
            this.surround.sendToggledMsg("enabled by Automation");
         }

         this.lastPos = this.mc.player.getBlockPos();
      }
   }

   private boolean inAHole(PlayerEntity player) {
      BlockPos pos = player.getBlockPos();
      if (HoleUtils.getHole(pos, 1).type == HoleType.Single) {
         return true;
      } else if (HoleUtils.getHole(pos, 1).type != HoleType.DoubleX && HoleUtils.getHole(pos.add(-1, 0, 0), 1).type != HoleType.DoubleX) {
         if (HoleUtils.getHole(pos, 1).type != HoleType.DoubleZ && HoleUtils.getHole(pos.add(0, 0, -1), 1).type != HoleType.DoubleZ) {
            return HoleUtils.getHole(pos, 1).type == HoleType.Quad || HoleUtils.getHole(pos.add(-1, 0, -1), 1).type == HoleType.Quad || HoleUtils.getHole(pos.add(-1, 0, 0), 1).type == HoleType.Quad || HoleUtils.getHole(pos.add(0, 0, -1), 1).type == HoleType.Quad;
         } else {
            return true;
         }
      } else {
         return true;
      }
   }
}
