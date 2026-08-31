package espada.spacex.aurora.modules.renderplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.events.Render2DEvent;
import espada.spacex.aurora.utils.RenderUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3d;

public class NewNameTags extends Modules {
   private final List<Entity> entityList = new ArrayList();
   private final Vector3d pos = new Vector3d();
   private final SettingGroup sgGeneral;
   private final Setting<Set<EntityType<?>>> entities;
   private final Setting<Boolean> ignoreSelf;
   private final Setting<Boolean> ignoreFriends;
   private final Setting<Boolean> ignoreBots;
   private final Setting<Boolean> culling;
   private final Setting<Double> maxCullRange;

   public NewNameTags() {
      super(Aurora.RenderPlus, "NewNameTags", "1");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)(new EntityTypeListSetting.Builder()).name("entities")).description("Select entities to draw nametags on.")).defaultValue(new EntityType[]{EntityType.PLAYER, EntityType.ITEM}).build());
      this.ignoreSelf = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("ignore-self")).description("Ignore yourself when in third person or freecam.")).defaultValue(true)).build());
      this.ignoreFriends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("ignore-friends")).description("Ignore rendering nametags for friends.")).defaultValue(false)).build());
      this.ignoreBots = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("ignore-bots")).description("Only render non-bot nametags.")).defaultValue(true)).build());
      this.culling = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("culling")).description("Only render a certain number of nametags at a certain distance.")).defaultValue(false)).build());
      SettingGroup var10001 = this.sgGeneral;
      DoubleSetting.Builder var10002 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("culling-range")).description("Only render nametags within this distance of your player.")).defaultValue((double)20.0F).min((double)0.0F).sliderMax((double)200.0F);
      Setting<Boolean> var10003 = this.culling;
      Objects.requireNonNull(var10003);
      this.maxCullRange = var10001.add(((DoubleSetting.Builder)var10002.visible(var10003::get)).build());
   }

   @EventHandler
   private void onTick(TickEvent.Post event) {
      this.entityList.clear();
      boolean freecamNotActive = !meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(Freecam.class);
      boolean notThirdPerson = this.mc.options.getPerspective().isFirstPerson();
      Vec3d cameraPos = this.mc.gameRenderer.getCamera().getPos();

      for(Entity entity : this.mc.world.getEntities()) {
         EntityType<?> type = entity.getType();
         if (((Set)this.entities.get()).contains(type) && (type != EntityType.PLAYER || (!(Boolean)this.ignoreSelf.get() && (!freecamNotActive || !notThirdPerson) || entity != this.mc.player) && (EntityUtils.getGameMode((PlayerEntity)entity) != null || !(Boolean)this.ignoreBots.get()) && (!Friends.get().isFriend((PlayerEntity)entity) || !(Boolean)this.ignoreFriends.get())) && (!(Boolean)this.culling.get() || PlayerUtils.isWithinCamera(entity, (Double)this.maxCullRange.get()))) {
            this.entityList.add(entity);
         }
      }

      this.entityList.sort(Comparator.comparing((e) -> e.squaredDistanceTo(cameraPos)));
   }

   @EventHandler
   private void Render2d(Render2DEvent event) {
      int count = this.getRenderCount();

      for(int i = count - 1; i > -1; --i) {
         Entity entity = (Entity)this.entityList.get(i);
         this.pos.add((double)0.0F, this.getHeight(entity), (double)0.0F);
         EntityType<?> type = entity.getType();
         if (type == EntityType.PLAYER) {
            this.drawText(event.context, "text", entity.getPos().add(new Vec3d((double)0.0F, (double)entity.getHeight() + (double)0.5F, (double)0.0F)));
         }
      }

   }

   public boolean excludeBots() {
      return (Boolean)this.ignoreBots.get();
   }

   public boolean playerNametags() {
      return this.isActive() && ((Set)this.entities.get()).contains(EntityType.PLAYER);
   }

   private double getHeight(Entity entity) {
      double height = (double)entity.getEyeHeight(entity.getPose());
      if (entity.getType() != EntityType.ITEM && entity.getType() != EntityType.ITEM_FRAME) {
         height += (double)0.5F;
      } else {
         height += 0.2;
      }

      return height;
   }

   private int getRenderCount() {
      int count = this.entityList.size();
      count = MathHelper.clamp(count, 0, this.entityList.size());
      return count;
   }

   private void drawText(DrawContext context, String text, Vec3d vec) {
      Vec3d vector = RenderUtils.worldSpaceToScreenSpace(new Vec3d(vec.x, vec.y, vec.z));
      if (vector.z > (double)0.0F && vector.z < (double)1.0F) {
         float posX = (float)vector.x;
         float posY = (float)vector.y;
         float endPosX = (float)Math.max(vector.x, vector.z);
         float scale = 1.0F;
         float diff = (endPosX - posX) / 2.0F;
         float textWidth = (float)this.mc.textRenderer.getWidth(text) * scale;
         float tagX = posX + diff - textWidth / 2.0F;
         context.getMatrices().push();
         context.getMatrices().scale(scale, scale, scale);
         double var10000 = (double)(posY - 11.0F);
         Objects.requireNonNull(this.mc.textRenderer);
         float y = (float)((var10000 + (double)9.0F * 1.2) / (double)scale);
         MatrixStack var13 = context.getMatrices();
         float var10001 = tagX / scale - 2.0F;
         float var10002 = y - 3.0F;
         float var10003 = (float)this.mc.textRenderer.getWidth(text) + 4.0F;
         Objects.requireNonNull(this.mc.textRenderer);
         RenderUtils.drawRect(var13, var10001, var10002, var10003, 9.0F + 6.0F, (new Color(0, 0, 0, 140)).getPacked());
         context.drawText(this.mc.textRenderer, text, (int)(tagX / scale), (int)y, Color.WHITE.getPacked(), true);
         context.getMatrices().pop();
      }

   }
}
