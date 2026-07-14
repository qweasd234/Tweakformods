package com.qweasd.tweakformods.mixins.immersivepetroleum;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.qweasd.tweakformods.Config;
import flaxbeard.immersivepetroleum.client.render.multiblock.MultiblockOilTankRenderer;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.OilTankLogic.State;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MultiblockOilTankRenderer.class)
public abstract class MultiblockOilTankRendererMixin {

    @Unique
    private static final ResourceLocation TWEAKFORMODS$OIL_TANK_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "immersivepetroleum",
            "textures/multiblock/oiltank.png"
    );

    @Unique
    private static final ResourceLocation TWEAKFORMODS$BACKGROUND_TEXTURE = ResourceLocation.withDefaultNamespace(
            "textures/block/white_concrete.png"
    );

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
                    ordinal = 0
            )
    )
    private VertexConsumer tweakformods$useIrisCompatibleBackground(
            MultiBufferSource buffer,
            RenderType originalRenderType,
            MultiblockBlockEntityMaster<State> blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource originalBuffer,
            int combinedLight,
            int combinedOverlay
    ) {
        if (!Config.immersivePetroleumOilTank()) {
            return buffer.getBuffer(originalRenderType);
        }
        return new BackgroundLightVertexConsumer(
                buffer.getBuffer(RenderType.entityCutoutNoCull(TWEAKFORMODS$BACKGROUND_TEXTURE)),
                poseStack.last(),
                combinedLight
        );
    }

    @Redirect(
            method = "quad",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
            )
    )
    private VertexConsumer tweakformods$useIrisCompatiblePortTexture(
            MultiBufferSource buffer,
            RenderType originalRenderType
    ) {
        if (!Config.immersivePetroleumOilTank()) {
            return buffer.getBuffer(originalRenderType);
        }
        return buffer.getBuffer(RenderType.entityCutoutNoCull(TWEAKFORMODS$OIL_TANK_TEXTURE));
    }

    @Unique
    private record BackgroundLightVertexConsumer(
            VertexConsumer delegate,
            PoseStack.Pose pose,
            int packedLight
    ) implements VertexConsumer {
        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            delegate.setColor(red, green, blue, alpha)
                    .setUv(0.0F, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(packedLight)
                    .setNormal(pose, 0.0F, 0.0F, 1.0F);
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
