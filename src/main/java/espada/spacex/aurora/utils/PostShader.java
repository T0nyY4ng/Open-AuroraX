package espada.spacex.aurora.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import espada.spacex.aurora.mixins.IPostEffectProcessor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gl.Uniform;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.JsonEffectShaderProgram;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;

public class PostShader {
   protected final MinecraftClient mc = MinecraftClient.getInstance();
   protected PostEffectProcessor shader;
   public final Consumer<PostShader> initCallback;
   private final Identifier location;

   public PostShader(Identifier id, Consumer<PostShader> initCallback) {
      this.initCallback = initCallback;
      this.location = id;
      this.initShader();
   }

   public ShaderUniform set(String name) {
      return this.findUniform(name);
   }

   public void render(float tickDelta) {
      PostEffectProcessor sg = this.getShader();
      if (sg != null) {
         RenderSystem.disableBlend();
         RenderSystem.disableDepthTest();
         RenderSystem.resetTextureMatrix();
         sg.render(tickDelta);
         MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
         RenderSystem.disableBlend();
         RenderSystem.blendFunc(770, 771);
         RenderSystem.enableDepthTest();
      }

   }

   protected ShaderUniform findUniform(String name) {
      if (this.shader == null) {
         this.initShader();
      }

      List<Uniform> uniforms = new ArrayList();

      for(PostEffectPass pass : ((IPostEffectProcessor)this.shader).getPasses()) {
         JsonEffectShaderProgram program = pass.getProgram();
         uniforms.add(program.getUniformByNameOrDummy(name));
      }

      return new ShaderUniform(uniforms);
   }

   public PostEffectProcessor getShader() {
      if (this.shader == null) {
         this.initShader();
      }

      return this.shader;
   }

   protected PostEffectProcessor parseShader(MinecraftClient mc, Identifier location) throws IOException {
      return new PostEffectProcessor(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), location);
   }

   private void initShader() {
      try {
         this.shader = this.parseShader(this.mc, this.location);
         this.shader.setupDimensions(this.mc.getWindow().getFramebufferWidth(), this.mc.getWindow().getFramebufferHeight());
         if (this.initCallback != null) {
            this.initCallback.accept(this);
         }

      } catch (IOException e) {
         throw new RuntimeException("Failed to initialized post shader program", e);
      }
   }

   public void set(String name, int value) {
      this.set(name).set(value);
   }

   public void set(String name, float value) {
      this.set(name).set(value);
   }

   public void set(String name, float v0, float v1) {
      this.set(name).set(v0, v1);
   }

   public void set(String name, float v0, float v1, float v2, float v3) {
      this.set(name).set(v0, v1, v2, v3);
   }

   public void set(String name, float v0, float v1, float v2) {
      this.set(name).set(v0, v1, v2);
   }
}
