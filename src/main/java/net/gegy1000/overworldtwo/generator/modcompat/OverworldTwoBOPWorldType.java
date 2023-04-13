package net.gegy1000.overworldtwo.generator.modcompat;

import biomesoplenty.common.world.BOPBiomeProvider;
import biomesoplenty.common.world.BOPDimensionType;
import biomesoplenty.init.ModConfig;
import net.gegy1000.overworldtwo.OverworldTwo;
import net.gegy1000.overworldtwo.generator.OverworldTwoChunkGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.BiomeGeneratorTypeScreens;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.screen.WorldOptionsScreen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;

public class OverworldTwoBOPWorldType extends ForgeWorldType {
    public OverworldTwoBOPWorldType() {
        super(null);
    }

    @Override
    public ChunkGenerator createChunkGenerator(Registry<Biome> biomeRegistry, Registry<DimensionSettings> dimensionSettingsRegistry, long seed, String generatorSettings) {
        return new OverworldTwoChunkGenerator(new BOPBiomeProvider(seed, biomeRegistry), seed, OverworldTwoChunkGenerator.OVERWORLD);
    }

    @Override
    public DimensionGeneratorSettings createSettings(DynamicRegistries dynamicRegistries, long seed, boolean generateStructures, boolean generateLoot, String generatorSettings) {

        Registry<Biome> biomeRegistry = dynamicRegistries.getRegistry(Registry.BIOME_KEY);
        Registry<DimensionSettings> dimensionSettingsRegistry = dynamicRegistries.getRegistry(Registry.NOISE_SETTINGS_KEY);
        Registry<DimensionType> dimensionTypeRegistry = dynamicRegistries.getRegistry(Registry.DIMENSION_TYPE_KEY);
        return new DimensionGeneratorSettings(seed, generateStructures, generateLoot, DimensionGeneratorSettings.func_242749_a(dimensionTypeRegistry, BOPDimensionType.bopDimensions(biomeRegistry, dimensionSettingsRegistry, seed), this.createChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed, (String)null)));
    }

    private void onWorldScreenOpen(GuiOpenEvent event) {
        if(ModConfig.ClientConfig.useWorldType.get() && event.getGui() instanceof CreateWorldScreen && Minecraft.getInstance().currentScreen instanceof WorldSelectionScreen) {
            WorldOptionsScreen optionsScreen = ((CreateWorldScreen)event.getGui()).field_238934_c_;
            // Use Overworld Two BoP by default, if BoP was made the default already
            if(optionsScreen.field_239040_n_.isPresent()) {
                BiomeGeneratorTypeScreens curType = optionsScreen.field_239040_n_.get();
                ITextComponent name = curType.func_239077_a_();
                if(name instanceof TranslationTextComponent
                        && ((TranslationTextComponent)name).getKey().equals("generator.minecraft.biomesoplenty")) {
                    BiomeGeneratorTypeScreens ourType = getType();
                    if(ourType == null)
                        return;
                    optionsScreen.field_239040_n_ = Optional.of(ourType);
                    optionsScreen.field_239039_m_ = ourType.func_241220_a_(optionsScreen.field_239038_l_,
                            optionsScreen.field_239039_m_.getSeed(),
                            optionsScreen.field_239039_m_.doesGenerateFeatures(),
                            optionsScreen.field_239039_m_.hasBonusChest());
                }
            }
        }
    }

    private static BiomeGeneratorTypeScreens getType() {
        List<BiomeGeneratorTypeScreens> presets = ObfuscationReflectionHelper.getPrivateValue(BiomeGeneratorTypeScreens.class, null, "field_239068_c_");
        if(presets == null)
            return null;
        for(BiomeGeneratorTypeScreens screen : presets) {
            if(screen.func_239077_a_() instanceof TranslationTextComponent) {
                if(((TranslationTextComponent)screen.func_239077_a_()).getKey().equals("generator.overworld_two.biomesoplenty"))
                    return screen;
            }
        }
        return null;
    }

    public static void register() {
        OverworldTwoBOPWorldType bopWorldType = new OverworldTwoBOPWorldType();
        bopWorldType.setRegistryName(new ResourceLocation(OverworldTwo.ID, "biomesoplenty"));
        ForgeRegistries.WORLD_TYPES.register(bopWorldType);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, bopWorldType::onWorldScreenOpen);
    }
}
