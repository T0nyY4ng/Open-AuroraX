package espada.spacex.aurora;

import espada.spacex.aurora.enums.SwingHand;
import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import espada.spacex.aurora.modules.renderplus.SwingModifier;
import espada.spacex.aurora.utils.PriorityUtils;
import espada.spacex.aurora.utils.SettingUtils;
import espada.spacex.aurora.utils.Util;
import espada.spacex.aurora.mixins.IClientPlayerInteractionManager;
import java.util.Objects;
import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;

public class Modules extends Module {
   private final String prefix;
   public final int priority;

   public Modules(Category category, String name, String description) {
      super(category, name, description);
      this.prefix = String.valueOf(Formatting.BOLD);
      this.priority = PriorityUtils.get(this);
   }

   public void sendToggledMsg() {
      if ((Boolean)Config.get().chatFeedback.get() && this.chatFeedback && this.mc.world != null) {
         ChatUtils.forceNextPrefixClass(this.getClass());
         String var10000 = this.prefix;
         String msg = var10000 + String.valueOf(Formatting.BOLD) + this.name + (this.isActive() ? String.valueOf(Formatting.GREEN) + " enabled" : String.valueOf(Formatting.RED) + " disabled");
         this.sendMessage(Text.of(msg), this.hashCode());
      }

   }

   public static boolean nullCheck() {
      return Util.mc.player == null || Util.mc.world == null;
   }

   public void sendToggledMsg(String message) {
      if ((Boolean)Config.get().chatFeedback.get() && this.chatFeedback && this.mc.world != null) {
         ChatUtils.forceNextPrefixClass(this.getClass());
         String var10000 = this.prefix;
         String msg = var10000 + String.valueOf(Formatting.BOLD) + this.name + (this.isActive() ? String.valueOf(Formatting.GREEN) + " enabled " : String.valueOf(Formatting.RED) + " disabled ");
         this.sendMessage(Text.of(msg), this.hashCode());
      }

   }

   public void sendDisableMsg(String text) {
      if (this.mc.world != null) {
         ChatUtils.forceNextPrefixClass(this.getClass());
         String var10000 = this.prefix;
         String msg = var10000 + String.valueOf(Formatting.BOLD) + this.name + String.valueOf(Formatting.RED) + " disabled " + String.valueOf(Formatting.GRAY) + text;
         this.sendMessage(Text.of(msg), this.hashCode());
      }

   }

   public void sendBOInfo(String text) {
      if (this.mc.world != null) {
         ChatUtils.forceNextPrefixClass(this.getClass());
         String msg = this.prefix + " " + String.valueOf(Formatting.BOLD) + this.name + text;
         this.sendMessage(Text.of(msg), Objects.hash(new Object[]{this.name + "-info"}));
      }

   }

   public void debug(String text) {
      if (this.mc.world != null) {
         ChatUtils.forceNextPrefixClass(this.getClass());
         String var10000 = this.prefix;
         String msg = var10000 + " " + String.valueOf(Formatting.BOLD) + this.name + String.valueOf(Formatting.AQUA) + text;
         this.sendMessage(Text.of(msg), 0);
      }

   }

   public void sendMessage(Text text, int id) {
      ((IChatHud)this.mc.inGameHud.getChatHud()).meteor$add(text, id);
   }

   public void sendPacket(Packet<?> packet) {
      if (this.mc.getNetworkHandler() != null) {
         this.mc.getNetworkHandler().sendPacket(packet);
      }
   }

   public void sendSequenced(SequencedPacketCreator packetCreator) {
      if (this.mc.interactionManager != null && this.mc.world != null && this.mc.getNetworkHandler() != null) {
         ((IClientPlayerInteractionManager)this.mc.interactionManager).aurora$sendSequencedPacket(this.mc.world, packetCreator);
      }
   }

   public void placeBlock(Hand hand, Vec3d blockHitVec, Direction blockDirection, BlockPos pos) {
      Vec3d eyes = this.mc.player.getEyePos();
      boolean inside = eyes.x > (double)pos.getX() && eyes.x < (double)(pos.getX() + 1) && eyes.y > (double)pos.getY() && eyes.y < (double)(pos.getY() + 1) && eyes.z > (double)pos.getZ() && eyes.z < (double)(pos.getZ() + 1);
      SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);
      this.sendSequenced((s) -> new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(blockHitVec, blockDirection, pos, inside), s));
      SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);
   }

   public boolean placeBlock(BlockPos blockPos, FindItemResult findItemResult, boolean checkEntities) {
      return findItemResult.isOffhand() ? this.place(blockPos, Hand.OFF_HAND, this.mc.player.getInventory().selectedSlot, checkEntities) : this.place(blockPos, Hand.MAIN_HAND, findItemResult.slot(), checkEntities);
   }

   private boolean place(BlockPos blockPos, Hand hand, int slot, boolean checkEntities) {
      if (slot >= 0 && slot <= 8) {
         if (!BlockUtils.canPlace(blockPos, checkEntities)) {
            return false;
         } else {
            Vec3d hitPos = blockPos.toCenterPos();
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
            return true;
         }
      } else {
         return false;
      }
   }

   public void interactBlock(Hand hand, Vec3d blockHitVec, Direction blockDirection, BlockPos pos) {
      Vec3d eyes = this.mc.player.getEyePos();
      boolean inside = eyes.x > (double)pos.getX() && eyes.x < (double)(pos.getX() + 1) && eyes.y > (double)pos.getY() && eyes.y < (double)(pos.getY() + 1) && eyes.z > (double)pos.getZ() && eyes.z < (double)(pos.getZ() + 1);
      SettingUtils.swing(SwingState.Pre, SwingType.Interact, hand);
      this.sendSequenced((s) -> new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(blockHitVec, blockDirection, pos, inside), s));
      SettingUtils.swing(SwingState.Post, SwingType.Interact, hand);
   }

   public void useItem(Hand hand) {
      SettingUtils.swing(SwingState.Pre, SwingType.Using, hand);
      this.sendSequenced((s) -> new PlayerInteractItemC2SPacket(hand, s, this.mc.player.getYaw(), this.mc.player.getPitch()));
      SettingUtils.swing(SwingState.Post, SwingType.Using, hand);
   }

   public void clientSwing(SwingHand swingHand, Hand realHand) {
      Hand var10000;
      switch (swingHand) {
         case MainHand -> var10000 = Hand.MAIN_HAND;
         case OffHand -> var10000 = Hand.OFF_HAND;
         case RealHand -> var10000 = realHand;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      Hand hand = var10000;
      this.mc.player.swingHand(hand, true);
      ((SwingModifier)meteordevelopment.meteorclient.systems.modules.Modules.get().get(SwingModifier.class)).startSwing(hand);
   }

   public Setting<Boolean> addPauseEat(SettingGroup group) {
      return group.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause Eat")).description("Pauses when eating")).defaultValue(false)).build());
   }
}
