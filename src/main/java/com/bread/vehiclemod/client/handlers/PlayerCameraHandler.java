package com.bread.vehiclemod.client.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerCameraHandler {
    @SubscribeEvent
    public void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null) {
            // Base position: Player's current position
            Vec3 playerPos = mc.player.getPosition(1.0F); // Interpolated position

            // Offset the camera's position
            double offsetX = 5.0; // Example X offset
            double offsetY = 2.0; // Example Y offset
            double offsetZ = 5.0; // Example Z offset

            // Adjust the camera's coordinates

            // Modify the camera's position indirectly
            event.getCamera().getEntity().setPos(offsetX, offsetY, offsetZ);

            // Optionally, adjust angles for tilt or rotation
            event.setPitch(event.getPitch() + 10.0F); // Tilt up/down
            event.setYaw(event.getYaw() + 20.0F); // Rotate left/right
        }
    }
}
