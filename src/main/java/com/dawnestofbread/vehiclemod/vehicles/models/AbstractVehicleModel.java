package com.dawnestofbread.vehiclemod.vehicles.models;

import com.dawnestofbread.vehiclemod.AbstractVehicle;
import software.bernie.geckolib.core.animation.AnimationProcessor;
import software.bernie.geckolib.model.GeoModel;

public abstract class AbstractVehicleModel<T extends AbstractVehicle> extends GeoModel<T> {
    protected static String MODID = "vehiclemod";
    protected long lastFrame;

//    @Override
//    public AnimationProcessor<T> getAnimationProcessor() {
//        if (this.animationProcessor == null) {
//            this.animationProcessor = new AnimationProcessor<>(this);
//        }
//        return this.animationProcessor;
//    }
}
