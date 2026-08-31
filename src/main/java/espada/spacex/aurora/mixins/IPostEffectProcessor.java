package espada.spacex.aurora.mixins;

import java.util.List;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.PostEffectPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({PostEffectProcessor.class})
public interface IPostEffectProcessor {
   @Accessor("passes")
   List<PostEffectPass> getPasses();
}
