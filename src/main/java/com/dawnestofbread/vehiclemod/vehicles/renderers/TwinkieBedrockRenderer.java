package com.dawnestofbread.vehiclemod.vehicles.renderers;

import com.dawnestofbread.vehiclemod.geo.BedrockEntityRenderer;
import com.dawnestofbread.vehiclemod.vehicles.entities.Twinkie;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class TwinkieBedrockRenderer extends BedrockEntityRenderer<Twinkie> {
    public TwinkieBedrockRenderer(EntityRendererProvider.Context context) {
        super(context, new ResourceLocation("vehiclemod", "geometry/test.geo.json"));
    }
}
