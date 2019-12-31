package powerlessri.icesculptures;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.fml.config.ModConfig;

public final class Config {

    private Config() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Common config
    ///////////////////////////////////////////////////////////////////////////

    public static final CommonCategory COMMON;

    public static final class CommonCategory {

        private CommonCategory(ForgeConfigSpec.Builder builder) {
            builder.comment("General config options").push("common");
            builder.pop();
        }
    }

    static final ForgeConfigSpec COMMON_SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new CommonCategory(builder);
        COMMON_SPEC = builder.build();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Client config
    ///////////////////////////////////////////////////////////////////////////

    public static final ClientCategory CLIENT;

    public static final class ClientCategory {

        public final DoubleValue lightX;
        public final DoubleValue lightY;
        public final DoubleValue lightZ;

        private ClientCategory(ForgeConfigSpec.Builder builder) {
            builder.comment("General client config options").push("client");
            lightX = builder.defineInRange("lightX", 0.5, 0.0, 1.0);
            lightY = builder.defineInRange("lightY", 0.5, 0.0, 1.0);
            lightZ = builder.defineInRange("lightZ", 0.5, 0.0, 1.0);
            builder.pop();
        }
    }

    static final ForgeConfigSpec CLIENT_SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CLIENT = new ClientCategory(builder);
        CLIENT_SPEC = builder.build();
    }

    static void onLoad(ModConfig.Loading event) {
        IceSculptures.logger.debug("Loaded {} config file {}", IceSculptures.MODID, event.getConfig().getFileName());
    }
}
