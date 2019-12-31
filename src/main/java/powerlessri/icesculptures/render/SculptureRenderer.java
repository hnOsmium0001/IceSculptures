package powerlessri.icesculptures.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import powerlessri.icesculptures.Config;
import powerlessri.icesculptures.block.MeshTileEntity;
import powerlessri.icesculptures.geometry.ShaderUtils;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class SculptureRenderer extends TileEntityRenderer<MeshTileEntity> {

    @Override
    public void render(MeshTileEntity tile, double x, double y, double z, float partialTicks, int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, z);
        float[] modelView = new float[16];
        glGetFloatv(GL_MODELVIEW_MATRIX, modelView);
        GlStateManager.popMatrix();
        float[] projection = new float[16];
        glGetFloatv(GL_PROJECTION_MATRIX, projection);

        World world = Objects.requireNonNull(tile.getWorld());
        BlockPos pos = tile.getPos();
        // TODO figure out why does this and #getLight doesn't seem to work (returns 0)
        int light = Math.max(world.getLightFor(LightType.SKY, pos), world.getLightFor(LightType.BLOCK, pos));
//        System.out.println(light + " " + map(light, 0F, 15F, 0F, 0.4F));

        glUseProgram(ShaderUtils.mesh);
        glUniformMatrix4fv(ShaderUtils.mesh_modelView, false, modelView);
        glUniformMatrix4fv(ShaderUtils.mesh_projection, false, projection);
        glUniform3f(ShaderUtils.mesh_lightPos, Config.CLIENT.lightX.get().floatValue(), Config.CLIENT.lightY.get().floatValue(), Config.CLIENT.lightZ.get().floatValue());
        glUniform1f(ShaderUtils.mesh_ambientStrength, map(light, 0F, 15F, 0F, 0.4F));

        tile.getMesh().draw();

        glUseProgram(0);
    }

    private static float map(float n, float fromStart, float fromEnd, float toStart, float toEnd) {
        return ((n - fromStart) / (fromEnd - fromStart)) * (toEnd - toStart) + toStart;
    }
}
