package com.dawnestofbread.vehiclemod.vehicles.models;

import com.dawnestofbread.vehiclemod.AbstractVehicle;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.core.molang.MolangQueries;
import software.bernie.geckolib.model.GeoModel;

public abstract class AbstractVehicleModel<T extends AbstractVehicle> extends GeoModel<T> {
    protected static final String MODID = "vehiclemod";
    protected long lastFrame = 0L;
}
