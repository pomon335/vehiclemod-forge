package com.dawnestofbread.vehiclemod.vehicles.renderers;

import com.dawnestofbread.vehiclemod.WheeledVehicle;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;

public abstract class AbstractMotorcycleRenderer<T extends WheeledVehicle> extends AbstractWheeledVehicleRenderer<T> {
    public AbstractMotorcycleRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        super(renderManager, model);
    }
}
