package com.dawnestofbread.vehiclemod.registries;

import com.dawnestofbread.vehiclemod.vehicles.entities.Annihilator;
import com.dawnestofbread.vehiclemod.vehicles.entities.Twinkie;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class VehicleRegistry {
    public static final String MODID = "vehiclemod";
    public static void RegisterAllVehicles() {
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final RegistryObject<EntityType<Annihilator>> ANNIHILATOR = ENTITIES.register(
            "annihilator", () -> EntityType.Builder.of(Annihilator::new, MobCategory.MISC).sized(2, 1.375f).build("vehiclemod:annihilator")
    );
    public static final RegistryObject<EntityType<Twinkie>> TWINKIE = ENTITIES.register(
            "twinkie", () -> EntityType.Builder.of(Twinkie::new, MobCategory.MISC).sized(.75f, 1.5625f).build("vehiclemod:twinkie")
    );
}
