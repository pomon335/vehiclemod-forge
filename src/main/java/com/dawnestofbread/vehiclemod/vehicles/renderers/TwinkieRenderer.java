package com.dawnestofbread.vehiclemod.vehicles.renderers;

import com.dawnestofbread.vehiclemod.vehicles.entities.Twinkie;
import com.dawnestofbread.vehiclemod.vehicles.models.TwinkieModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class TwinkieRenderer extends AbstractMotorcycleRenderer<Twinkie> {
    public TwinkieRenderer(EntityRendererProvider.Context context) {
        super(context, new TwinkieModel());
    }
}
