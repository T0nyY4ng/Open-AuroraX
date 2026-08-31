package espada.spacex.aurora.mixins;

import java.util.Map;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ShaderProgram.class})
public interface IShaderProgram {
   @Accessor("loadedUniforms")
   Map<String, GlUniform> getUniformsHook();
}
