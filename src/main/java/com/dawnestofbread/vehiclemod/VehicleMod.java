package com.dawnestofbread.vehiclemod;

import com.dawnestofbread.vehiclemod.client.handlers.PlayerTransformHandler;
import com.dawnestofbread.vehiclemod.network.PacketHandler;
import com.eliotlash.mclib.math.Variable;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.core.molang.MolangParser;

import static com.dawnestofbread.vehiclemod.registries.EntityRendererRegistry.RegisterAllRenderers;
import static com.dawnestofbread.vehiclemod.registries.SoundEventRegistry.RegisterAllSoundEvents;
import static com.dawnestofbread.vehiclemod.registries.VehicleRegistry.RegisterAllVehicles;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("vehiclemod")
public class VehicleMod
{
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public VehicleMod()
    {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        // Register ourselves for server and other game events we are interested in
        RegisterAllVehicles();
        RegisterAllSoundEvents();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new PlayerTransformHandler());
        MolangParser parser = MolangParser.INSTANCE;
        parser.register(new Variable("query.wheel0_xRot", 0));
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        PacketHandler.registerAllMessages();
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
    }

    private void processIMC(final InterModProcessEvent event)
    {
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientBus {
        @SubscribeEvent
        public void entityAttributeCreationEvent(EntityAttributeCreationEvent event) {
        }
    }
    @Mod.EventBusSubscriber(modid = "vehiclemod", bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            RegisterAllRenderers();
        }
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }
}
