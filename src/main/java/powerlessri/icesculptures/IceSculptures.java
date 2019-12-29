package powerlessri.icesculptures;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import powerlessri.icesculptures.geometry.ShaderUtils;

@Mod(IceSculptures.MODID)
public class IceSculptures {

    public static final String MODID = "icesculptures";

    public static IceSculptures instance;

    public static Logger logger = LogManager.getLogger(MODID);

    public IceSculptures() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::setup);
        eventBus.addListener(this::loadComplete);
        eventBus.addListener(Config::onLoad);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> eventBus.addListener(this::clientSetup));
    }

    private void setup(final FMLCommonSetupEvent event) {
        instance = this;
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ShaderUtils.setup();
    }

    private void loadComplete(final FMLLoadCompleteEvent event) {
    }
}
