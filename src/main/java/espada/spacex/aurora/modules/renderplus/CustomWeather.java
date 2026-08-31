package espada.spacex.aurora.modules.renderplus;

import com.mojang.blaze3d.systems.RenderSystem;
import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.events.WeatherRenderEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.world.biome.Biome.Precipitation;
import net.minecraft.client.render.VertexFormat.DrawMode;

public class CustomWeather extends Modules {
   private static final Identifier RAIN = Identifier.of("textures/environment/rain.png");
   private static final Identifier SNOW = Identifier.of("textures/environment/snow.png");
   private final SettingGroup sgGeneral;
   private final SettingGroup sgValue;
   private final Setting<PrecipitationType> precipitationSetting;
   private final Setting<Integer> height;
   private final Setting<Double> strength;
   private final Setting<SettingColor> weatherColor;
   private final Setting<Integer> expandSize;
   private final Setting<Double> snowFallingSpeedMultiplier;
   private int ticks;
   private static final float[] weatherXCoords = new float[1024];
   private static final float[] weatherYCoords = new float[1024];

   public CustomWeather() {
      super(Aurora.RenderPlus, "Weather", "Custom Weather");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgValue = this.settings.createGroup("Value");
      this.precipitationSetting = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("precipitation")).defaultValue(CustomWeather.PrecipitationType.Snow)).build());
      this.height = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("weather-height")).defaultValue(0)).min(0).max(320).build());
      this.strength = this.sgGeneral.add(((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("weather-strength")).defaultValue(0.8).range(0.1, (double)2.0F).build());
      this.weatherColor = this.sgGeneral.add(((ColorSetting.Builder)(new ColorSetting.Builder()).name("weather-color")).defaultValue(SettingColor.WHITE).build());
      this.expandSize = this.sgValue.add(((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("expand-size")).defaultValue(5)).range(1, 10).build());
      this.snowFallingSpeedMultiplier = this.sgValue.add(((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("snow-falling-speed-multiplier")).defaultValue((double)1.0F).range((double)0.0F, (double)10.0F).build());
      this.ticks = 0;
   }

   @EventHandler
   private void onWeather(WeatherRenderEvent event) {
      if (((PrecipitationType)this.precipitationSetting.get()).equals(CustomWeather.PrecipitationType.Both)) {
         this.render(event, CustomWeather.PrecipitationType.Rain);
         this.render(event, CustomWeather.PrecipitationType.Snow);
         event.cancel();
      } else {
         this.render(event, (PrecipitationType)this.precipitationSetting.get());
         event.cancel();
      }
   }

   private void render(WeatherRenderEvent event, PrecipitationType precipitationType) {
      LightmapTextureManager manager = event.lightmapTextureManager;
      double cameraX = event.cameraX;
      double cameraY = event.cameraY;
      double cameraZ = event.cameraZ;
      float tickDelta = event.tickDelta;
      float f = ((Double)this.strength.get()).floatValue();
      float red = (float)((SettingColor)this.weatherColor.get()).r / 255.0F;
      float blue = (float)((SettingColor)this.weatherColor.get()).b / 255.0F;
      float green = (float)((SettingColor)this.weatherColor.get()).g / 255.0F;
      manager.enable();
      int cameraIntX = MathHelper.floor(cameraX);
      int cameraIntY = MathHelper.floor(cameraY);
      int cameraIntZ = MathHelper.floor(cameraZ);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferBuilder = null;
      RenderSystem.disableCull();
      RenderSystem.enableBlend();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
      RenderSystem.setShader(GameRenderer::getParticleProgram);
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      int expand = (Integer)this.expandSize.get();
      int tessPosition = -1;
      float fallingValue = (float)this.ticks + tickDelta;

      for(int zRange = cameraIntZ - expand; zRange <= cameraIntZ + expand; ++zRange) {
         for(int xRange = cameraIntX - expand; xRange <= cameraIntX + expand; ++xRange) {
            int coordPos = (zRange - cameraIntZ + 16) * 32 + xRange - cameraIntX + 16;
            if (coordPos >= 0 && coordPos <= 1023) {
               double xCoord = (double)weatherXCoords[coordPos] * (double)0.5F;
               double zCoord = (double)weatherYCoords[coordPos] * (double)0.5F;
               mutable.set((double)xRange, cameraY, (double)zRange);
               int maxHeight = (Integer)this.height.get();
               int minIntY = cameraIntY - expand;
               int expandedCameraY = cameraIntY + expand;
               if (minIntY < maxHeight) {
                  minIntY = maxHeight;
               }

               if (expandedCameraY < maxHeight) {
                  expandedCameraY = maxHeight;
               }

               int maxRenderY = Math.max(maxHeight, cameraIntY);
               if (minIntY != expandedCameraY) {
                  Random random = Random.create((long)xRange * (long)xRange * 3121L + (long)xRange * 45238971L ^ (long)zRange * (long)zRange * 418711L + (long)zRange * 13761L);
                  mutable.set(xRange, minIntY, zRange);
                  Biome.Precipitation precipitation = precipitationType.toMC();
                  if (precipitation == Precipitation.RAIN) {
                     if (tessPosition != 0) {
                        if (tessPosition >= 0) {
                           BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
                        }

                        tessPosition = 0;
                        RenderSystem.setShaderTexture(0, RAIN);
                        bufferBuilder = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                     }

                     int randomSeed = this.ticks + xRange * xRange * 3121 + xRange * 45238971 + zRange * zRange * 418711 + zRange * 13761 & 31;
                     float texTextureV = -((float)randomSeed + tickDelta) / 32.0F * (3.0F + random.nextFloat());
                     double xOffset = (double)xRange + (double)0.5F - cameraX;
                     double yOffset = (double)zRange + (double)0.5F - cameraZ;
                     float dLength = (float)Math.sqrt(xOffset * xOffset + yOffset * yOffset) / (float)expand;
                     float weatherAlpha = ((1.0F - dLength * dLength) * 0.5F + 0.5F) * f;
                     mutable.set(xRange, maxRenderY, zRange);
                     int lightmapCoord = WorldRenderer.getLightmapCoordinates(this.mc.world, mutable);
                     bufferBuilder.vertex((float)((double)xRange - cameraX - xCoord + (double)0.5F), (float)((double)expandedCameraY - cameraY), (float)((double)zRange - cameraZ - zCoord + (double)0.5F)).texture(0.0F, (float)minIntY * 0.25F + texTextureV).color(red, green, blue, weatherAlpha).light(lightmapCoord);
                     bufferBuilder.vertex((float)((double)xRange - cameraX + xCoord + (double)0.5F), (float)((double)expandedCameraY - cameraY), (float)((double)zRange - cameraZ + zCoord + (double)0.5F)).texture(1.0F, (float)minIntY * 0.25F + texTextureV).color(red, green, blue, weatherAlpha).light(lightmapCoord);
                     bufferBuilder.vertex((float)((double)xRange - cameraX + xCoord + (double)0.5F), (float)((double)minIntY - cameraY), (float)((double)zRange - cameraZ + zCoord + (double)0.5F)).texture(1.0F, (float)expandedCameraY * 0.25F + texTextureV).color(red, green, blue, weatherAlpha).light(lightmapCoord);
                     bufferBuilder.vertex((float)((double)xRange - cameraX - xCoord + (double)0.5F), (float)((double)minIntY - cameraY), (float)((double)zRange - cameraZ - zCoord + (double)0.5F)).texture(0.0F, (float)expandedCameraY * 0.25F + texTextureV).color(red, green, blue, weatherAlpha).light(lightmapCoord);
                  } else if (precipitation == Precipitation.SNOW) {
                     if (tessPosition != 1) {
                        if (tessPosition == 0) {
                           BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
                        }

                        tessPosition = 1;
                        RenderSystem.setShaderTexture(0, SNOW);
                        bufferBuilder = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                     }

                     float snowSmooth = -((float)(this.ticks & 511) + tickDelta) / 512.0F;
                     float texTextureV = (float)(random.nextDouble() + (double)fallingValue * 0.01 * (double)((float)random.nextGaussian()));
                     float fallingSpeed = (float)((double)((float)(random.nextDouble() + (double)(fallingValue * (float)random.nextGaussian()) * 0.001)) * (Double)this.snowFallingSpeedMultiplier.get());
                     double xOffset = (double)xRange + (double)0.5F - cameraX;
                     double yOffset = (double)zRange + (double)0.5F - cameraZ;
                     float weatherAlpha = (float)Math.sqrt(xOffset * xOffset + yOffset * yOffset) / (float)expand;
                     float snowAlpha = ((1.0F - weatherAlpha * weatherAlpha) * 0.3F + 0.5F) * f;
                     mutable.set(xRange, maxRenderY, zRange);
                     int lightMapCoord = WorldRenderer.getLightmapCoordinates(this.mc.world, mutable);
                     int lightmapCalcV = lightMapCoord >> 16 & '\uffff';
                     int lightmapCalcU = lightMapCoord & '\uffff';
                     int lightmapV = (lightmapCalcV * 3 + 240) / 4;
                     int lightmapU = (lightmapCalcU * 3 + 240) / 4;
                     bufferBuilder.vertex((float)((double)xRange - cameraX - xCoord + (double)0.5F), (float)((double)expandedCameraY - cameraY), (float)((double)zRange - cameraZ - zCoord + (double)0.5F)).texture(0.0F + texTextureV, (float)minIntY * 0.25F + snowSmooth + fallingSpeed).color(red, green, blue, snowAlpha).light(lightmapU, lightmapV);
                     bufferBuilder.vertex((float)((double)xRange - cameraX + xCoord + (double)0.5F), (float)((double)expandedCameraY - cameraY), (float)((double)zRange - cameraZ + zCoord + (double)0.5F)).texture(1.0F + texTextureV, (float)minIntY * 0.25F + snowSmooth + fallingSpeed).color(red, green, blue, snowAlpha).light(lightmapU, lightmapV);
                     bufferBuilder.vertex((float)((double)xRange - cameraX + xCoord + (double)0.5F), (float)((double)minIntY - cameraY), (float)((double)zRange - cameraZ + zCoord + (double)0.5F)).texture(1.0F + texTextureV, (float)expandedCameraY * 0.25F + snowSmooth + fallingSpeed).color(red, green, blue, snowAlpha).light(lightmapU, lightmapV);
                     bufferBuilder.vertex((float)((double)xRange - cameraX - xCoord + (double)0.5F), (float)((double)minIntY - cameraY), (float)((double)zRange - cameraZ - zCoord + (double)0.5F)).texture(0.0F + texTextureV, (float)expandedCameraY * 0.25F + snowSmooth + fallingSpeed).color(red, green, blue, snowAlpha).light(lightmapU, lightmapV);
                  }
               }
            }
         }
      }

      if (tessPosition >= 0) {
         BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
      }

      RenderSystem.enableCull();
      RenderSystem.disableBlend();
      manager.disable();
   }

   @EventHandler
   private void onTick(TickEvent.Pre e) {
      ++this.ticks;
   }

   static {
      for(int xRange = 0; xRange < 32; ++xRange) {
         for(int zRange = 0; zRange < 32; ++zRange) {
            float x = (float)(zRange - 16);
            float z = (float)(xRange - 16);
            float length = MathHelper.sqrt(x * x + z * z);
            weatherXCoords[xRange << 5 | zRange] = -z / length;
            weatherYCoords[xRange << 5 | zRange] = x / length;
         }
      }

   }

   public static enum PrecipitationType {
      None,
      Rain,
      Snow,
      Both;

      public Biome.Precipitation toMC() {
         Biome.Precipitation var10000;
         switch (this.ordinal()) {
            case 0 -> var10000 = Precipitation.NONE;
            case 1 -> var10000 = Precipitation.RAIN;
            case 2 -> var10000 = Precipitation.SNOW;
            case 3 -> var10000 = Precipitation.SNOW;
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }

      // $FF: synthetic method
      private static PrecipitationType[] $values() {
         return new PrecipitationType[]{None, Rain, Snow, Both};
      }
   }
}
