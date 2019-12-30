package powerlessri.icesculptures.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import powerlessri.icesculptures.block.SculptureTileEntity;

import static org.lwjgl.opengl.GL11.*;

public class SculptureRenderer extends TileEntityRenderer<SculptureTileEntity> {

    @Override
    public void render(SculptureTileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {
        tileEntityIn.getMesh().draw(x, y, z);
    }
}
