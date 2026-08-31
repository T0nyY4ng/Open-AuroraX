package espada.spacex.aurora.hud;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.utils.RenderUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.util.math.RotationAxis;

public class TargetHud extends HudElement {
   private final SettingGroup sgGeneral;
   private final Setting<Mode> mode;
   private final Setting<Double> scale;
   private final Setting<SettingColor> bgColor;
   private final Setting<SettingColor> textColor;
   private final Setting<SettingColor> healthColor;
   private final Setting<SettingColor> absorptionColor;
   private final Setting<Double> damageTilt;
   public static final HudElementInfo<TargetHud> INFO;
   private AbstractClientPlayerEntity target;
   private String renderName;
   private Identifier renderSkin;
   private float renderHealth;
   private float renderPing;
   private double scaleProgress;
   private long damageTime;
   private UUID lastTarget;
   private float lastHp;
   private boolean popped;
   private final Map<AbstractClientPlayerEntity, Integer> tog;

   public TargetHud() {
      super(INFO);
      this.sgGeneral = this.settings.getDefaultGroup();
      this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mode")).description("What mode to use for the TargetHud.")).defaultValue(TargetHud.Mode.Blackout)).build());
      this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Scale")).description("Scale to render at")).defaultValue((double)1.0F).range((double)0.0F, (double)5.0F).sliderRange((double)0.0F, (double)5.0F).build());
      this.bgColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Background Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(0, 0, 0, 200)).build());
      this.textColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Text Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
      this.healthColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Health Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 0, 0, 255)).build());
      this.absorptionColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)(new ColorSetting.Builder()).name("Absorption Color")).description("Color is the visual perception of different wavelengths of light as hue, saturation, and brightness")).defaultValue(new SettingColor(255, 255, 0, 255)).build());
      this.damageTilt = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Damage Tilt")).description("How many degrees should the box be rotated when enemy takes damage.")).defaultValue((double)10.0F).min((double)0.0F).sliderRange((double)0.0F, (double)45.0F).build());
      this.renderName = null;
      this.renderSkin = null;
      this.scaleProgress = (double)0.0F;
      this.lastTarget = null;
      this.lastHp = 0.0F;
      this.popped = false;
      this.tog = new HashMap();
      MeteorClient.EVENT_BUS.subscribe(this);
   }

   @EventHandler(
      priority = 10000
   )
   private void onTick(TickEvent.Pre event) {
      if (MeteorClient.mc.world != null && MeteorClient.mc.player != null) {
         List<AbstractClientPlayerEntity> toRemove = new ArrayList();

         for(Map.Entry<AbstractClientPlayerEntity, Integer> entry : this.tog.entrySet()) {
            if (!MeteorClient.mc.world.getPlayers().contains(entry.getKey()) || ((AbstractClientPlayerEntity)entry.getKey()).isSpectator() || !(((AbstractClientPlayerEntity)entry.getKey()).getHealth() > 0.0F)) {
               toRemove.add((AbstractClientPlayerEntity)entry.getKey());
            }
         }

         Map var10001 = this.tog;
         Objects.requireNonNull(var10001);
         toRemove.forEach(var10001::remove);
         MeteorClient.mc.world.getPlayers().forEach((player) -> {
            if (player.isOnGround()) {
               if (this.tog.containsKey(player)) {
                  this.tog.replace(player, (Integer)this.tog.get(player) + 1);
               } else {
                  this.tog.put(player, 1);
               }
            }

         });
         if (this.target != null) {
            if (this.target.getUuid().equals(this.lastTarget)) {
               float diff = Math.max(this.lastHp - this.target.getHealth() - this.target.getAbsorptionAmount(), 0.0F);
               if (diff > 1.0F) {
                  this.damageTime = System.currentTimeMillis();
               }
            }

            this.lastTarget = this.target.getUuid();
            this.lastHp = this.popped ? 0.0F : this.target.getHealth() + this.target.getAbsorptionAmount();
            this.popped = false;
         } else {
            this.lastTarget = null;
            this.lastHp = 0.0F;
            this.damageTime = 0L;
         }

      }
   }

   @EventHandler(
      priority = 10000
   )
   private void onReceive(PacketEvent.Receive event) {
      Packet var3 = event.packet;
      if (var3 instanceof EntityStatusS2CPacket packet) {
         if (packet.getStatus() == 35) {
            Entity entity = packet.getEntity(MeteorClient.mc.world);
            if (entity instanceof PlayerEntity) {
               PlayerEntity player = (PlayerEntity)entity;
               if (player == this.target) {
                  this.popped = true;
               }
            }

         }
      }
   }

   public void render(HudRenderer renderer) {
      if (this.mode.get() == TargetHud.Mode.Blackout) {
         int height = 100;
         int width = 200;
         this.setSize((double)width * (Double)this.scale.get(), (double)height * (Double)this.scale.get());
         this.updateTarget();
         if (this.renderName == null) {
            return;
         }

         MatrixStack stack = new MatrixStack();
         this.scaleProgress = MathHelper.clamp(this.scaleProgress + (this.target == null ? -renderer.delta : renderer.delta), (double)0.0F, (double)1.0F);
         float scaleAnimation = (float)(this.scaleProgress * this.scaleProgress * this.scaleProgress);
         stack.translate((float)this.x + (1.0F - scaleAnimation) * (float)this.getWidth() / 2.0F, (float)this.y + (1.0F - scaleAnimation) * (float)this.getHeight() / 2.0F, 0.0F);
         stack.scale((float)((double)scaleAnimation * (Double)this.scale.get()), (float)((double)scaleAnimation * (Double)this.scale.get()), 1.0F);
         float tilt = (float)((double)((float)Math.max(0L, 500L - System.currentTimeMillis() + this.damageTime) / 500.0F) * (Double)this.damageTilt.get());
         stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(tilt));
         RenderUtils.rounded(stack, 15.0F, 15.0F, (float)(width - 30), (float)(height - 30), 15.0F, 10, ((SettingColor)this.bgColor.get()).getPacked());
         this.drawFace(renderer, scaleAnimation * ((Double)this.scale.get()).floatValue(), (double)((float)this.x + (1.0F - scaleAnimation) * (float)this.getWidth() / 2.0F), (double)((float)this.y + (1.0F - scaleAnimation) * (float)this.getHeight() / 2.0F), tilt);
         RenderUtils.text(this.renderName, stack, 60.0F, 20.0F, ((SettingColor)this.textColor.get()).getPacked());
         RenderUtils.text(Math.round(this.renderPing) + "ms", stack, 60.0F, 30.0F, ((SettingColor)this.textColor.get()).getPacked());
         String var10000 = String.valueOf((float)Math.round(this.renderHealth * 10.0F) / 10.0F);
         Objects.requireNonNull(MeteorClient.mc.textRenderer);
         RenderUtils.text(var10000, stack, 20.0F, 81.0F - 9.0F / 2.0F, ((SettingColor)this.textColor.get()).getPacked());
         float barAnimation = MathHelper.lerp(MeteorClient.mc.getRenderTickCounter().getTickDelta(true) / 10.0F, this.lastHp, this.renderHealth);
         float barStart = (float)(Math.max(MeteorClient.mc.textRenderer.getWidth(String.valueOf((float)Math.round(this.renderHealth * 10.0F) / 10.0F)), MeteorClient.mc.textRenderer.getWidth("36.0")) + 28);
         if (barAnimation > 0.0F) {
            RenderUtils.rounded(stack, barStart, 80.0F, MathHelper.clamp(barAnimation / 20.0F, 0.0F, 1.0F) * ((float)(width - 30) - barStart), 2.0F, 3.0F, 10, ((SettingColor)this.healthColor.get()).getPacked());
         }

         if (barAnimation > 20.0F) {
            RenderUtils.rounded(stack, barStart, 80.0F, MathHelper.clamp((barAnimation - 20.0F) / 16.0F, 0.0F, 1.0F) * ((float)(width - 30) - barStart), 2.0F, 3.0F, 10, ((SettingColor)this.absorptionColor.get()).getPacked());
         }
      }

      if (this.mode.get() == TargetHud.Mode.ExhibitionOld) {
         int height = 60;
         int width = 240;
         this.setSize((double)width * (Double)this.scale.get(), (double)height * (Double)this.scale.get());
         this.updateTarget();
         MatrixStack stack = new MatrixStack();
         if (this.target == null || this.renderName == null) {
            return;
         }

         stack.translate((float)this.x, (float)this.y, 0.0F);
         stack.scale((float)((Double)this.scale.get() * (double)1.0F), (float)((Double)this.scale.get() * (double)1.0F), 1.0F);
         RenderUtils.quad(stack, 0.0F, 0.0F, (float)width, (float)height, ((SettingColor)this.bgColor.get()).getPacked());
         RenderUtils.quad(stack, 1.0F, 1.0F, 58.0F, 58.0F, (new Color(102, 102, 102, 255)).getPacked());
         this.drawFace(renderer, ((Double)this.scale.get()).floatValue(), (double)this.x, (double)this.y, 0.0F);
         stack.scale(2.0F, 2.0F, 1.0F);
         RenderUtils.text(this.renderName, stack, 33.0F, 2.0F, ((SettingColor)this.textColor.get()).getPacked());
         stack.scale(0.5F, 0.5F, 1.0F);
         String var23 = (float)Math.round(this.renderHealth * 10.0F) / 10.0F + " Dist: " + (float)Math.round(MeteorClient.mc.player.distanceTo(this.target) * 10.0F) / 10.0F;
         Objects.requireNonNull(MeteorClient.mc.textRenderer);
         RenderUtils.text(var23, stack, 66.0F, 35.0F - 9.0F / 2.0F, ((SettingColor)this.textColor.get()).getPacked());
         stack.scale(2.0F, 2.0F, 1.0F);
         int progress = (int)Math.ceil((double)MathHelper.clamp(this.renderHealth, 0.0F, 20.0F));

         for(int i = 0; i < 10; ++i) {
            RenderUtils.quad(stack, (float)(33 + i * 8), 11.0F, (float)(3 * Math.min(progress, 2)), 3.0F, (new Color(204, 204, 0, 255)).getPacked());
            progress -= 2;
            if (progress <= 0) {
               break;
            }
         }

         stack.scale(0.5F, 0.5F, 1.0F);
         var23 = "Yaw: " + (float)Math.round(this.target.getYaw() * 10.0F) / 10.0F + " Pitch: " + (float)Math.round(this.target.getPitch() * 10.0F) / 10.0F + " BodyYaw: " + (float)Math.round(this.target.getBodyYaw() * 10.0F) / 10.0F;
         Objects.requireNonNull(MeteorClient.mc.textRenderer);
         RenderUtils.text(var23, stack, 66.0F, 45.0F - 9.0F / 2.0F, ((SettingColor)this.textColor.get()).getPacked());
         var23 = "TOG: " + String.valueOf(this.tog.getOrDefault(this.target, 0)) + " HURT: " + (float)(this.target.hurtTime * 10) / 10.0F + " TE: " + this.target.age;
         Objects.requireNonNull(MeteorClient.mc.textRenderer);
         RenderUtils.text(var23, stack, 66.0F, 55.0F - 9.0F / 2.0F, ((SettingColor)this.textColor.get()).getPacked());
      }

      if (this.mode.get() == TargetHud.Mode.Exhibition) {
         int height = 60;
         int width = 190;
         this.setSize((double)width * (Double)this.scale.get(), (double)height * (Double)this.scale.get());
         this.updateTarget();
         MatrixStack stack = new MatrixStack();
         if (this.target == null || this.renderName == null) {
            return;
         }

         stack.translate((float)this.x, (float)this.y, 0.0F);
         stack.scale((float)((Double)this.scale.get() * (double)1.0F), (float)((Double)this.scale.get() * (double)1.0F), 1.0F);
         RenderUtils.quad(stack, -2.0F, -2.0F, (float)(width + 4), (float)(height + 4), (new Color(52, 52, 52, 255)).getPacked());
         RenderUtils.quad(stack, -1.0F, -1.0F, (float)(width + 2), (float)(height + 2), (new Color(32, 32, 32, 255)).getPacked());
         RenderUtils.quad(stack, 0.0F, 0.0F, (float)width, (float)height, (new Color(52, 52, 52, 255)).getPacked());
         stack.scale(1.5F, 1.5F, 1.0F);
         RenderUtils.text(this.renderName, stack, 41.0F, 2.0F, ((SettingColor)this.textColor.get()).getPacked());
         stack.scale(0.5F, 0.5F, 1.0F);
         String var26 = (float)Math.round(this.renderHealth * 10.0F) / 10.0F + " Dist: " + (float)Math.round(MeteorClient.mc.player.distanceTo(this.target) * 10.0F) / 10.0F;
         Objects.requireNonNull(MeteorClient.mc.textRenderer);
         RenderUtils.text(var26, stack, 83.0F, 40.0F - 9.0F / 2.0F, ((SettingColor)this.textColor.get()).getPacked());
         stack.scale(2.0F, 2.0F, 1.0F);
         int progress = (int)Math.ceil((double)MathHelper.clamp(this.renderHealth, 0.0F, 20.0F));

         for(int i = 0; i < 10; ++i) {
            RenderUtils.quad(stack, (float)(41 + i * 8), 12.0F, (float)(3 * Math.min(progress, 2)), 3.0F, (new Color(204, 204, 0, 255)).getPacked());
            progress -= 2;
            if (progress <= 0) {
               break;
            }
         }

         stack.scale(0.9F, 0.9F, 1.0F);
         MatrixStack drawStack = renderer.drawContext.getMatrices();
         drawStack.push();
         drawStack.translate((float)this.x, (float)this.y, 0.0F);
         drawStack.scale(((Double)this.scale.get()).floatValue() * 1.35F, ((Double)this.scale.get()).floatValue() * 1.35F, 1.0F);

         for(int i = 0; i < 4; ++i) {
            ItemStack itemStack = (ItemStack)this.target.getInventory().armor.get(i);
            renderer.drawContext.drawItem(itemStack, (3 - i) * 20 + 42, 25);
         }

         ItemStack itemStack = this.target.getMainHandStack();
         renderer.drawContext.drawItem(itemStack, 122, 25);
         drawStack.pop();
      }

   }

   private void drawFace(HudRenderer renderer, float scale, double x, double y, float tilt) {
      MatrixStack drawStack = renderer.drawContext.getMatrices();
      drawStack.push();
      drawStack.translate(x, y, (double)0.0F);
      drawStack.scale(scale, scale, 1.0F);
      drawStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(tilt));
      PlayerSkinDrawer.draw(renderer.drawContext, this.renderSkin, 20, 18, 32, false, false);
      drawStack.pop();
   }

   private void updateTarget() {
      this.target = null;
      if (MeteorClient.mc.world != null) {
         AbstractClientPlayerEntity closest = null;
         double distance = Double.MAX_VALUE;

         for(AbstractClientPlayerEntity player : MeteorClient.mc.world.getPlayers()) {
            if (player != MeteorClient.mc.player && !Friends.get().isFriend(player)) {
               double d = (double)MeteorClient.mc.player.distanceTo(player);
               if (d < distance) {
                  closest = player;
                  distance = d;
               }
            }
         }

         this.target = closest;
         if (this.target == null && this.isInEditor()) {
            this.target = MeteorClient.mc.player;
         }

         if (this.target != null) {
            this.renderName = this.target.getName().getString();
            this.renderSkin = this.target.getSkinTextures().texture();
            this.renderHealth = this.target.getHealth() + this.target.getAbsorptionAmount();
            PlayerListEntry playerListEntry = MeteorClient.mc.getNetworkHandler().getPlayerListEntry(this.target.getUuid());
            this.renderPing = playerListEntry == null ? -1.0F : (float)playerListEntry.getLatency();
         }

      }
   }

   static {
      INFO = new HudElementInfo(Aurora.HUD_EDIT, "TargetHud", "A target hud the fuck you thinkin bruv.", TargetHud::new);
   }

   public static enum Mode {
      Blackout,
      ExhibitionOld,
      Exhibition;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Blackout, ExhibitionOld, Exhibition};
      }
   }
}
