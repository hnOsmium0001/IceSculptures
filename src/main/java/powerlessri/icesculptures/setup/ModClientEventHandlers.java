package powerlessri.icesculptures.setup;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import powerlessri.icesculptures.IceSculptures;
import powerlessri.icesculptures.block.MeshTileEntity;
import powerlessri.icesculptures.render.SculptureRenderer;

@EventBusSubscriber(modid = IceSculptures.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModClientEventHandlers {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(MeshTileEntity.class, new SculptureRenderer());
    }
}
