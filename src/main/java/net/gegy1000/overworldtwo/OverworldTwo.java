package net.gegy1000.overworldtwo;



import net.gegy1000.overworldtwo.generator.OverworldTwoChunkGenerator;
import net.gegy1000.overworldtwo.generator.OverworldTwoGeneratorType;
import net.gegy1000.overworldtwo.generator.modcompat.OverworldTwoBOPWorldType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod("overworld_two")
public final class OverworldTwo {
    public static final String ID = "overworld_two";

    public static final Logger LOGGER = LogManager.getLogger("OverworldTwo");


    public OverworldTwo() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        if(FMLEnvironment.dist == Dist.CLIENT) {
            registerGenTypes();
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        OverworldTwoChunkGenerator.register();
    }

    private void registerGenTypes() {
        OverworldTwoGeneratorType.register();
        if(ModList.get().isLoaded("biomesoplenty"))
            OverworldTwoBOPWorldType.register();
    }
}
