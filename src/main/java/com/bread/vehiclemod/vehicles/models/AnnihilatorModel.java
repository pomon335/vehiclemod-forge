package com.bread.vehiclemod.vehicles.models;

import com.bread.vehiclemod.vehicles.entities.Annihilator;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AnnihilatorModel extends GeoModel<Annihilator> {
    public static String MODID = "vehiclemod";
    private final ResourceLocation model = new ResourceLocation(MODID, "geo/annihilator.geo.json");
    private final ResourceLocation texture = new ResourceLocation(MODID, "textures/annihilator_livery_trevor.png");
    private final ResourceLocation animations = new ResourceLocation(MODID, "animations/annihilator.animation.json");

    protected long lastFrame;


//    @Override
//    public void setCustomAnimations(Annihilator animatable, long instanceId, AnimationState<Annihilator> animationState) {
//        super.setCustomAnimations(animatable, instanceId, animationState);
//
//        double deltaTime = (double) (new Date().getTime() - lastFrame) / 1000;
//        lastFrame = new Date().getTime();
//
//        var controller = animationState.getController();
//
//        GeoBone body = getBone("body").orElse(null);
//        if (body != null) { body.setRotX(fInterpToExp(body.getRotX(), (float) (animatable.weightTransfer * animatable.maxBodyPitch) * (Mth.PI/180), 1.5f, (float) deltaTime)); }
//
//        for (int i = 0; i < animatable.Wheels.length; i++) {
//            GeoBone bone = getBone("wheel" + String.valueOf(i)).orElse(null);
//            if (bone != null) {
//                bone.setRotX(bone.getRotX() - (float) (animatable.Wheels[i].angularVelocity * deltaTime));
//                if (animatable.Wheels[i].affectedByTurn) bone.setRotY(fInterpToExp(bone.getRotY(), (float) - (animatable.steering * animatable.steeringAngle) * (Mth.PI/180), 2.5f, (float) deltaTime));
//            }
//        }
//    }

    @Override
    public ResourceLocation getModelResource(Annihilator object) {
        return this.model;
    }

    @Override
    public ResourceLocation getTextureResource(Annihilator object) {
        return this.texture;
    }

    @Override
    public ResourceLocation getAnimationResource(Annihilator object) {
        return this.animations;
    }
}
