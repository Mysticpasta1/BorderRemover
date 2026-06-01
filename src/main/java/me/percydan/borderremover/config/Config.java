package me.percydan.borderremover.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {
    public static Config INSTANCE;
    public static ForgeConfigSpec COMMON_CONFIG;

    public ForgeConfigSpec.BooleanValue enableFarlands;
    public ForgeConfigSpec.BooleanValue shardFarlands;
    public ForgeConfigSpec.ConfigValue<String> genOffset;
    public ForgeConfigSpec.DoubleValue xzCoordinateScale;
    public ForgeConfigSpec.DoubleValue yCoordinateScale;
    public ForgeConfigSpec.ConfigValue<String> xzScaleMultiplier;
    public ForgeConfigSpec.ConfigValue<String> yScaleMultiplier;

    private Config(ForgeConfigSpec.Builder builder) {
        builder.push("worldgen");

        enableFarlands = builder
                .comment("Enable farlands")
                .define("enableFarlands", false);

        shardFarlands = builder
                .comment("Shard farlands")
                .define("shardFarlands", false);

        genOffset = builder
                .comment("Terrain generation offset")
                .define("genOffset", "0");

        xzCoordinateScale = builder
                .comment("X/Z coordinate scale")
                .defineInRange("xzCoordinateScale", 684.412, Double.MIN_VALUE, Double.MAX_VALUE);

        yCoordinateScale = builder
                .comment("Y coordinate scale")
                .defineInRange("yCoordinateScale", 684.412, Double.MIN_VALUE, Double.MAX_VALUE);

        xzScaleMultiplier = builder
                .comment("X/Z coordinate scale multiplier")
                .define("xzScaleMultiplier", "default");

        yScaleMultiplier = builder
                .comment("Y coordinate scale multiplier")
                .define("yScaleMultiplier", "default");

        builder.pop();
    }

    public static void register() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        INSTANCE = new Config(builder);
        COMMON_CONFIG = builder.build();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
    }
}