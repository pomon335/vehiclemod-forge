package com.dawnestofbread.vehiclemod.vehicles.models;

import com.dawnestofbread.vehiclemod.vehicles.entities.Annihilator;
import net.minecraft.resources.ResourceLocation;

public class AnnihilatorModel extends AbstractWheeledVehicleModel<Annihilator> {
    @Override
    public ResourceLocation getModelResource(Annihilator object) {
        return new ResourceLocation(MODID, "geo/annihilator.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Annihilator object) {
        return new ResourceLocation(MODID, "textures/annihilator_livery_trevor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Annihilator object) {
        return new ResourceLocation(MODID, "animations/annihilator.animation.json");
    }
}
