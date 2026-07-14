package com.qweasd.tweakformods;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue IMMERSIVE_PETROLEUM_RENDER_TYPES = BUILDER
            .comment("Fix Immersive Petroleum custom render types when Iris is installed.")
            .define("immersivePetroleum.renderTypes", true);

    private static final ModConfigSpec.BooleanValue IMMERSIVE_PETROLEUM_OIL_TANK = BUILDER
            .comment("Fix the oil tank background and dynamic fluid port rendering.")
            .define("immersivePetroleum.oilTankRenderer", true);

    private static final ModConfigSpec.BooleanValue IMMERSIVE_PETROLEUM_PROJECTOR = BUILDER
            .comment("Fix projector visibility, flickering, depth ordering, and missing-template crashes.")
            .define("immersivePetroleum.projectorRenderer", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static volatile boolean immersivePetroleumRenderTypes = true;
    private static volatile boolean immersivePetroleumOilTank = true;
    private static volatile boolean immersivePetroleumProjector = true;

    private Config() {
    }

    public static boolean immersivePetroleumRenderTypes() {
        return immersivePetroleumRenderTypes;
    }

    public static boolean immersivePetroleumOilTank() {
        return immersivePetroleumOilTank;
    }

    public static boolean immersivePetroleumProjector() {
        return immersivePetroleumProjector;
    }

    static void onConfigChanged(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }

        immersivePetroleumRenderTypes = IMMERSIVE_PETROLEUM_RENDER_TYPES.getAsBoolean();
        immersivePetroleumOilTank = IMMERSIVE_PETROLEUM_OIL_TANK.getAsBoolean();
        immersivePetroleumProjector = IMMERSIVE_PETROLEUM_PROJECTOR.getAsBoolean();
    }
}
