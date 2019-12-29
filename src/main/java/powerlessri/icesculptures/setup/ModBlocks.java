package powerlessri.icesculptures.setup;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import powerlessri.icesculptures.IceSculptures;
import powerlessri.icesculptures.block.SculptureBlock;
import powerlessri.icesculptures.block.SculptureTileEntity;
import powerlessri.icesculptures.render.SculptureRenderer;

@EventBusSubscriber(modid = IceSculptures.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModBlocks {

    private ModBlocks() {
    }

    @ObjectHolder("icesculptures:sculpture_block")
    public static SculptureBlock sculptureBlock;
    @ObjectHolder("icesculptures:sculpture_block")
    public static TileEntityType<SculptureTileEntity> sculptureTileEntity;

    @SubscribeEvent
    public static void onBlockRegister(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> r = event.getRegistry();
        r.register(new SculptureBlock(Block.Properties.create(Material.ICE).slipperiness(0.98F).hardnessAndResistance(0.5F).sound(SoundType.GLASS)).setRegistryName(new ResourceLocation(IceSculptures.MODID, "sculpture_block")));
    }

    @SubscribeEvent
    public static void onItemRegister(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> r = event.getRegistry();
        r.register(new BlockItem(sculptureBlock, new Item.Properties().group(ItemGroup.MISC)).setRegistryName(sculptureBlock.getRegistryName()));
    }

    @SubscribeEvent
    public static void onTileEntityRegister(RegistryEvent.Register<TileEntityType<?>> event) {
        IForgeRegistry<TileEntityType<?>> r = event.getRegistry();
        r.register(TileEntityType.Builder.create(SculptureTileEntity::new, sculptureBlock).build(null).setRegistryName(sculptureBlock.getRegistryName()));
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(SculptureTileEntity.class, new SculptureRenderer());
    }
}
