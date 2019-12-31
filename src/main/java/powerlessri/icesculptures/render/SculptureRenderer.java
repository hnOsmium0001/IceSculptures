package powerlessri.icesculptures.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import powerlessri.icesculptures.Config;
import powerlessri.icesculptures.block.SculptureTileEntity;
import powerlessri.icesculptures.geometry.ShaderUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class SculptureRenderer extends TileEntityRenderer<SculptureTileEntity> {

    @Override
    public void render(SculptureTileEntity tile, double x, double y, double z, float partialTicks, int destroyStage) {
        // TODO precompute all matrices in CPU code
        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, z);
        float[] modelView = new float[16];
        glGetFloatv(GL_MODELVIEW_MATRIX, modelView);
        GlStateManager.popMatrix();
        float[] projection = new float[16];
        glGetFloatv(GL_PROJECTION_MATRIX, projection);

        Minecraft mc = Minecraft.getInstance();
        int light = mc.world.getLight(tile.getPos());
        System.out.println(light + " " + map(light, 0F, 15F, 0F, 0.4F));

        glUseProgram(ShaderUtils.sculpture);
        glUniformMatrix4fv(ShaderUtils.sculpture_modelView, false, modelView);
        glUniformMatrix4fv(ShaderUtils.sculpture_projection, false, projection);
        glUniform3f(ShaderUtils.sculpture_lightPos, Config.CLIENT.lightX.get().floatValue(), Config.CLIENT.lightY.get().floatValue(), Config.CLIENT.lightZ.get().floatValue());
        glUniform1f(ShaderUtils.sculpture_ambientStrength, 0.1f);

        tile.getMesh().draw();

        glUseProgram(0);
    }

    private static float map(float n, float start1, float stop1, float start2, float stop2) {
        return ((n - start1) / (stop1 - start1)) * (stop2 - start2) + start2;
    }
}
