package com.qweasd.tweakformods.mixins.immersivepetroleum;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.qweasd.tweakformods.Config;
import com.qweasd.tweakformods.Tweakformods;
import flaxbeard.immersivepetroleum.client.IPShaders;
import flaxbeard.immersivepetroleum.common.items.ProjectorItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ProjectorItem.ClientRenderHandler.class)
public abstract class ProjectorRenderMixin {

    @Unique
    private static final ThreadLocal<Float> TWEAKFORMODS$PROJECTION_ALPHA = ThreadLocal.withInitial(() -> 0.5F);

    @Unique
    private static final RenderType TWEAKFORMODS$IRIS_PROJECTION = RenderType.create(
            "tweakformods:iris_projection",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            RenderType.BIG_BUFFER_SIZE,
            true,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.CULL)
                    .createCompositeState(false)
    );

    @Unique
    private static boolean tweakformods$projectionDisabled;

    @Redirect(
            method = "renderSchematic",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/RandomSource;nextFloat()F"
            )
    )
    private static float tweakformods$removeProjectionFlicker(RandomSource random) {
        if (!Config.immersivePetroleumProjector()) {
            return random.nextFloat();
        }
        return 0.5F;
    }

    @Redirect(
            method = "renderPhantom",
            at = @At(
                    value = "INVOKE",
                    target = "Lflaxbeard/immersivepetroleum/client/IPShaders;projNoise(FF)V"
            )
    )
    private static void tweakformods$captureProjectionAlpha(float alpha, float time) {
        if (!Config.immersivePetroleumProjector()) {
            IPShaders.projNoise(alpha, time);
            return;
        }
        TWEAKFORMODS$PROJECTION_ALPHA.set(alpha);
    }

    @Redirect(
            method = "renderPhantom",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
            )
    )
    private static VertexConsumer tweakformods$useIrisCompatibleProjection(
            MultiBufferSource.BufferSource buffer,
            RenderType originalRenderType
    ) {
        if (!Config.immersivePetroleumProjector()) {
            return buffer.getBuffer(originalRenderType);
        }
        return new ProjectionAlphaVertexConsumer(
                buffer.getBuffer(TWEAKFORMODS$IRIS_PROJECTION),
                TWEAKFORMODS$PROJECTION_ALPHA.get()
        );
    }

    @WrapOperation(
            method = "renderLevelStage",
            at = @At(
                    value = "INVOKE",
                    target = "Lflaxbeard/immersivepetroleum/common/items/ProjectorItem$ClientRenderHandler;renderProjection(Lnet/neoforged/neoforge/client/event/RenderLevelStageEvent;)V"
            )
    )
    private static void tweakformods$renderProjectionSafely(
            RenderLevelStageEvent event,
            Operation<Void> original
    ) {
        if (!Config.immersivePetroleumProjector()) {
            original.call(event);
            return;
        }

        if (tweakformods$projectionDisabled) {
            return;
        }

        try {
            original.call(event);
        } catch (RuntimeException exception) {
            String message = exception.getMessage();
            if (message == null || !message.startsWith("Template ") || !message.endsWith(" does not exist!")) {
                throw exception;
            }

            tweakformods$projectionDisabled = true;
            Tweakformods.LOGGER.error(
                    "Disabled Immersive Petroleum projector rendering for this session because a multiblock template is unavailable: {}",
                    message
            );
        }
    }

    @Unique
    private record ProjectionAlphaVertexConsumer(VertexConsumer delegate, float alpha) implements VertexConsumer {

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            delegate.setColor(red, green, blue, Math.round(alpha * this.alpha));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            delegate.setNormal(normalX, normalY, normalZ);
            return this;
        }
    }
}
