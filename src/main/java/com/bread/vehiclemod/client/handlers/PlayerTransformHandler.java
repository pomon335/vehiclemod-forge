package com.bread.vehiclemod.client.handlers;

import com.bread.vehiclemod.AbstractVehicle;
import com.bread.vehiclemod.utils.SeatData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerTransformHandler {
    @SubscribeEvent
    public void onPreRender(RenderPlayerEvent.Pre event)
    {
        Player player = event.getEntity();
        Entity rider = player.getVehicle();
        if(rider instanceof AbstractVehicle vehicle)
        {
            this.applyWheelieTransformations(vehicle, player, event.getPoseStack(), event.getPartialTick());
        }
    }

    private void applyWheelieTransformations(AbstractVehicle vehicle, Player player, PoseStack poseStack, float partialTick)
    {
        int seatIndex = vehicle.SeatManager.indexOf(player.getUUID());
        if(seatIndex == -1) return;

        SeatData seat = vehicle.Seats[seatIndex];
        //Vec3 seatVec = seat.getPosition().add(0, properties.getAxleOffset() + properties.getWheelOffset(), 0).scale(properties.getBodyTransform().getScale()).scale(0.0625);
        poseStack.mulPose(Axis.XP.rotationDegrees(-vehicle.passengerXAdditional));
    }
}
