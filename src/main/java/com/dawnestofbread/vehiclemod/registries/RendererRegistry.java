package com.dawnestofbread.vehiclemod.registries;

import com.dawnestofbread.vehiclemod.vehicles.renderers.AnnihilatorRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class RendererRegistry {
    public static void RegisterAllRenderers() {
        EntityRenderers.register(VehicleRegistry.ANNIHILATOR.get(), AnnihilatorRenderer::new);
    }
}
