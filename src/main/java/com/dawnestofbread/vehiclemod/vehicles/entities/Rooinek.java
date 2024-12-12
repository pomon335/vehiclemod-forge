package com.dawnestofbread.vehiclemod.vehicles.entities;

import com.dawnestofbread.vehiclemod.AbstractMotorcycle;
import com.dawnestofbread.vehiclemod.client.audio.AudioManager;
import com.dawnestofbread.vehiclemod.utils.Curve;
import com.dawnestofbread.vehiclemod.utils.SeatData;
import com.dawnestofbread.vehiclemod.utils.WheelData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static com.dawnestofbread.vehiclemod.registries.SoundEventRegistry.*;

public class Rooinek extends AbstractMotorcycle {

    public Rooinek(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
        // This probably could be moved to setupSeats, but meh
        SeatManager = new ArrayList<>(1);
        SeatManager.add(0,UUID.fromString("00000000-0000-0000-0000-000000000000"));

        // WIP Set up collision boxes
        this.collision = new AABB[1];
        this.collision[0] = new AABB(-2.0625,0.75,-5.125, 2.0625,2.6875,0.5);

        // Some values required for maths; when getting them from Blockbench, divide the value by 8!
        this.frontAxle = new Vec3(0, 0.7146 , 1.5 );
        this.rearAxle = new Vec3(0, 0.7146 , -1);
        this.centreOfGeometry = new Vec3(0,0,0);
        // This is also really important
        this.width = 0.5625;
        this.height = 1.1875;
        this.length = 1.875;

        // Purely visual, should be set up to not clip into the tyres
        this.maxBodyPitch = 5;
        this.maxBodyRoll = 22.5;

        // Really important values here, you should try to get them right
        this.idleRPM = 333;
        this.maxRPM = 1733;
        this.mass = 10;
        this.brakingConstant = 100;

        // Brake multiplier applied when there's no input
        this.idleBrakeAmount = .175;
        // Unused
        this.corneringStiffness = 60;

        // Torque curve
        List<Double> tempTCurve = new LinkedList<>();
        tempTCurve.add(0, 10d);
        tempTCurve.add(1, 10d);
        tempTCurve.add(2, 40d);
        tempTCurve.add(3, 45d);
        tempTCurve.add(4, 30d);
        tempTCurve.add(5, 15d);
        tempTCurve.add(6, 5d);
        this.transmissionEfficiency = .72;
        this.torqueCurve = new Curve(tempTCurve);

        // Gear setup
        this.differentialRatio = 2.86;
        this.gearRatios = new double[10];
        this.gearRatios[0] = -.2; // Reverse
        this.gearRatios[1] = 1; // Neutral
        this.gearRatios[2] = 2.0; // 1st
        this.gearRatios[3] = 1.7; // 2nd and so on...
        this.gearRatios[4] = 1.45;
        this.gearRatios[5] = 1.21;
        this.gearRatios[6] = 0.83;
        this.gearRatios[7] = 0.51;
        this.gearRatios[8] = 0.29;
        this.gearRatios[9] = 0.11;

        // Used for the automatic gearbox
        this.shiftUpRPM = 1100;
        this.shiftDownRPM = 600;
        // In seconds
        this.timeToShift = .1;

        // How much the wheels turn; NOT using Ackermann steering geometry
        this.steeringAngle = 27;

        // Traction/grip while turning (0-1 range; 1 meaning great, 0 meaning awful)
        this.traction = 0.73;

        // 0-1
        this.exhaustFumeAmount = 0;
        this.exhaust = null;

        this.engineSounds = new HashMap<>();
        this.engineSounds.put(AudioManager.SoundType.ENGINE_IDLE, SCOOTER_IDLE.get());
        this.engineSounds.put(AudioManager.SoundType.ENGINE_MOVING, SCOOTER_MOVING.get());
    }

    // Create the seats and set their offsets
    @Override
    protected void setupSeats() {
        Seats = new SeatData[1];

        // This is divided by 16
        SeatData seat0 = new SeatData();
        seat0.seatOffset = new Vec3(0,1.03125,-0.171875);
        Seats[0] = seat0;

    }

    // Create the wheels and set their parameters
    @Override
    protected void setupWheels() {
        Wheels = new ArrayList<>(2);

        Wheels.add(0, new WheelData());
        Wheels.add(1, new WheelData());

        // Divide this by 16, because it works better and results in more realistic proportions
        Wheels.get(0).radius = 0.3125;
        Wheels.get(1).radius = 0.3125;

        Wheels.get(0).width = 0.0625;
        Wheels.get(1).width = 0.0625;

        Wheels.get(0).springMinLength = 0.1;
        Wheels.get(1).springMinLength = 0.1;
        Wheels.get(0).springMaxLength = 0.12;
        Wheels.get(1).springMaxLength = 0.12;

        // And this
        Wheels.get(0).startingRelativePosition = new Vec3(0, 0.7146 ,1.5);
        Wheels.get(1).startingRelativePosition = new Vec3(0, 0.7146 ,-1);

        Wheels.get(0).affectedBySteering = true;

        Wheels.get(0).mass = 0.5;
        Wheels.get(1).mass = 0.5;

        Wheels.get(1).affectedByEngine = true;

        Wheels.get(1).affectedByBrake = true;
        Wheels.get(0).affectedByHandbrake = true;
    }
}
