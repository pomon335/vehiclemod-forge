package com.dawnestofbread.vehiclemod.registries;

import com.dawnestofbread.vehiclemod.vehicles.entities.Annihilator;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class VehicleRegistry {
    public static String MODID = "vehiclemod";
    public static void RegisterAllVehicles() {
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final RegistryObject<EntityType<Annihilator>> ANNIHILATOR = ENTITIES.register(
            "annihilator", () -> EntityType.Builder.of(Annihilator::new, MobCategory.MISC).sized(2, 1.375f).build("vehiclemod:annihilator")
    );
}
