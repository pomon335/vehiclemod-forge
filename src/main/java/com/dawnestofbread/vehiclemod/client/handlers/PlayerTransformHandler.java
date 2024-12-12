package com.dawnestofbread.vehiclemod.client.handlers;

import com.dawnestofbread.vehiclemod.AbstractVehicle;
import com.dawnestofbread.vehiclemod.utils.SeatData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
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
            this.applyPassengerTransformations(vehicle, player, event.getPoseStack(), event.getPartialTick());
        }
    }

    private void applyPassengerTransformations(AbstractVehicle vehicle, Player player, PoseStack poseStack, float partialTick)
    {
        int seatIndex = vehicle.SeatManager.indexOf(player.getUUID());
        if(seatIndex == -1) return;

        SeatData seat = vehicle.Seats[seatIndex];
        poseStack.mulPose(Axis.of(vehicle.getForward().toVector3f()).rotationDegrees(-vehicle.passengerZRot));
        poseStack.mulPose(Axis.of(vehicle.getForward().yRot(Mth.HALF_PI).toVector3f()).rotationDegrees(-vehicle.passengerXRot));

        /* This would be a good time to complain about Minecraft's vectors and transformations
        *  Why is Y up? Huh? It should be Z. And why is Z forward?! It should be X!
        *  This really throws me off as I'm used to X being roll, Y being pitch, and Z being yaw
        *  And why is Entity.setRot in YX order? Guess I'll never know! */
    }
}
