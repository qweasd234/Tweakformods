package com.qweasd.tweakformods.mixins.immersivepetroleum;

import com.qweasd.tweakformods.Config;
import flaxbeard.immersivepetroleum.client.render.IPRenderTypes;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(IPRenderTypes.class)
public abstract class IPRenderTypesMixin {

    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;setTransparencyState(Lnet/minecraft/client/renderer/RenderStateShard$TransparencyStateShard;)Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;",
                    ordinal = 3
            )
    )
    private static RenderType.CompositeState.CompositeStateBuilder tweakformods$configurePositionColor(
            RenderType.CompositeState.CompositeStateBuilder builder,
            RenderStateShard.TransparencyStateShard transparencyState
    ) {
        builder.setTransparencyState(transparencyState);
        if (Config.immersivePetroleumRenderTypes()) {
            builder.setLightmapState(RenderStateShard.LIGHTMAP)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST);
        }
        return builder;
    }

    @Redirect(
            method = "machineExtra",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;setOverlayState(Lnet/minecraft/client/renderer/RenderStateShard$OverlayStateShard;)Lnet/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder;"
            )
    )
    private static RenderType.CompositeState.CompositeStateBuilder tweakformods$configureMachineOverlay(
            RenderType.CompositeState.CompositeStateBuilder builder,
            RenderStateShard.OverlayStateShard overlayState
    ) {
        builder.setOverlayState(overlayState);
        if (Config.immersivePetroleumRenderTypes()) {
            builder.setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST);
        }
        return builder;
    }
}
