package powerlessri.icesculptures;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import powerlessri.icesculptures.block.MeshTileEntity;
import powerlessri.icesculptures.geometry.ShaderUtils;
import powerlessri.icesculptures.render.SculptureRenderer;
import powerlessri.icesculptures.setup.ModBlocks;

@Mod(IceSculptures.MODID)
public class IceSculptures {

    public static final String MODID = "icesculptures";

    public static IceSculptures instance;

    public static Logger logger = LogManager.getLogger(MODID);

    public IceSculptures() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::setup);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            ShaderUtils.setup();
        });
    }

    private void setup(final FMLCommonSetupEvent event) {
        instance = this;
    }
}
