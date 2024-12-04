package com.dawnestofbread.vehiclemod.registries;

import com.dawnestofbread.vehiclemod.vehicles.renderers.AnnihilatorRenderer;
import com.dawnestofbread.vehiclemod.vehicles.renderers.TwinkieRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class RendererRegistry {
    public static void RegisterAllRenderers() {
        EntityRenderers.register(VehicleRegistry.ANNIHILATOR.get(), AnnihilatorRenderer::new);
        EntityRenderers.register(VehicleRegistry.TWINKIE.get(), TwinkieRenderer::new);
    }
}
