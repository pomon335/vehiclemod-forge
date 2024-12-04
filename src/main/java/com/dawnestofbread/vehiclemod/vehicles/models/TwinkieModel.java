package com.dawnestofbread.vehiclemod.vehicles.models;

import com.dawnestofbread.vehiclemod.vehicles.entities.Twinkie;
import net.minecraft.resources.ResourceLocation;

public class TwinkieModel extends AbstractMotorcycleModel<Twinkie> {
    @Override
    public ResourceLocation getModelResource(Twinkie object) {
        return new ResourceLocation(MODID, "geo/twinkie.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Twinkie object) {
        return new ResourceLocation(MODID, "textures/twinkie.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Twinkie object) {
        return new ResourceLocation(MODID, "animations/twinkie.animation.json");
    }
}
