package net.gegy1000.overworldtwo;



import net.gegy1000.overworldtwo.generator.OverworldTwoChunkGenerator;
import net.gegy1000.overworldtwo.generator.OverworldTwoGeneratorType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod("overworld_two")
public final class OverworldTwo {
    public static final String ID = "overworld_two";

    public static final Logger LOGGER = LogManager.getLogger("OverworldTwo");


    public OverworldTwo() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        OverworldTwoChunkGenerator.register();
    }

    private void clientSetup(FMLClientSetupEvent event) {
        OverworldTwoGeneratorType.register();
    }
}
