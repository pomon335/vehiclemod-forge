package com.dawnestofbread.vehiclemod.vehicles.renderers;

import com.dawnestofbread.vehiclemod.vehicles.entities.Annihilator;
import com.dawnestofbread.vehiclemod.vehicles.models.AnnihilatorModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class AnnihilatorRenderer extends AbstractWheeledVehicleRenderer<Annihilator> {

    public AnnihilatorRenderer(EntityRendererProvider.Context context) {
        super(context, new AnnihilatorModel());
    }
}
