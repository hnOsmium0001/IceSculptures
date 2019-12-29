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
        // TODO use uniform instead
        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, z);
        GlStateManager.scalef(2F, 2F, 2F);

        GlStateManager.depthMask(false);
        GlStateManager.disableTexture();
        Minecraft.getInstance().gameRenderer.disableLightmap();
        GlStateManager.disableLighting();
        GlStateManager.enableAlphaTest();
//        GlStateManager.disableCull();
        GlStateManager.color3f(1F, 1F, 1F);

        tileEntityIn.getMesh().draw();

        GlStateManager.enableTexture();
        GlStateManager.disableAlphaTest();
        RenderHelper.enableStandardItemLighting();
        Minecraft.getInstance().gameRenderer.enableLightmap();

        GlStateManager.popMatrix();
    }
}
