package powerlessri.icesculptures.geometry;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.world.gen.OctavesNoiseGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static powerlessri.icesculptures.geometry.GeometryUtils.*;

public class Mesh {

    private BitSet lattice = new BitSet(ppa * ppa * ppa);
    private List<Triangle> triangles = new ArrayList<>();

    private int vao = 0;
    private int vbo = 0;

    public void setup() {
        fill();
        march();

        if (EffectiveSide.get() == LogicalSide.CLIENT) {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
                createGLStates();
                populateVBO();
            });
        }
    }

    public void fill() {
//        Random rand = new Random();
//        for (int x = 0; x < ppa; x++) {
//            for (int y = 0; y < ppa; y++) {
//                for (int z = 0; z < ppa; z++) {
//                    if (rand.nextBoolean()) {
//                        lattice.set(indexFromPos(x, y, z));
//                    }
//                }
//            }
//        }

        OctavesNoiseGenerator noise = new OctavesNoiseGenerator(new Random(), 4);
        double t = 0D;
        for (int x = 1; x < ppa - 1; x++) {
            for (int y = 1; y < ppa - 1; y++) {
                for (int z = 1; z < ppa - 1; z++) {
                    double v = noise.func_215462_a(x / 2D, y / 2D, z / 2D, 0D, 0D, false);
                    if (v > t) {
                        lattice.set(indexFromPos(x, y, z));
                    }
                }
            }
        }

//        int mx = 8;
//        int my = 8;
//        int mz = 8;
//        double r = 6D;
//        for (int x = 0; x < ppa; x++) {
//            for (int y = 0; y < ppa; y++) {
//                for (int z = 0; z < ppa; z++) {
//                    double d = Math.sqrt((x - mx) * (x - mx) + (y - my) * (y - my) + (z - mz) * (z - mz));
//                    if (d > r) {
//                        continue;
//                    }
//                    lattice.set(indexFromPos(x, y, z));
//                }
//            }
//        }

//        for (int x = 1; x < ppa - 1; x++) {
//            for (int y = 1; y < ppa - 1; y++) {
//                for (int z = 1; z < ppa - 1; z++) {
//                    lattice.set(indexFromPos(x, y, z));
//                }
//            }
//        }
    }

    public void march() {
        // Each method call on #marchCube covers from (x,y,z) to (x+1,y+1,z+1)
        // not subtracting 1 causes ArrayIndexOutOfBoundsException
        for (int x = 0; x < ppa - 1; x++) {
            for (int y = 0; y < ppa - 1; y++) {
                for (int z = 0; z < ppa - 1; z++) {
                    marchCube(triangles, lattice, x, y, z);
                }
            }
        }
    }

    public void cleanup() {
        // Don't call GL stuff on any server thread
        if (EffectiveSide.get() != LogicalSide.CLIENT) {
            return;
        }
        // Prevent loading GL stuff on dedicated server
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            glDeleteBuffers(vbo);
            vbo = 0;
            glDeleteVertexArrays(vao);
            vao = 0;
        });
    }

    @OnlyIn(Dist.CLIENT)
    private void createGLStates() {
        // Create the VBO
        vbo = glGenBuffers();

        // Create and bind the VAO
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        {
            // Setup the attributes, note that attributes can live in different VBOs, we only use one here for simplicity
            // Technically we only need to bind the VBO once here, but I left it for clarity

            // Position attribute
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, Float.BYTES * 3 * 2 /* Size of color + normal attribute */, 0);
            // Normal attribute
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, Float.BYTES * 3 * 2 /* Size of color + position attribute */, Float.BYTES * 3 /* Size of a position attribute */);
//            // Color attribute
//            glBindBuffer(GL_ARRAY_BUFFER, vbo);
//            glEnableVertexAttribArray(2);
//            glVertexAttribPointer(2, 3, GL_FLOAT, false, Float.BYTES * 3 * 2 /* Size of position + normal attribute */, Float.BYTES * 3 * 2 /* Size of a position and a normal attribute */);

            // VAO does not track bound VBO directly, unbind to avoid accidental modification
            glBindBuffer(GL_ARRAY_BUFFER, 0);

            // No need to disable attributes since they are directly controlled by the VAO
            // However if this is done without a VAO, attributes need to be disabled for attribute pointers to be invalidated
            // otherwise either the client segfaults due to dangling pointers or OpenGL errors due to invalid VRAM address
        }
        // Unbind VAO to prevent accidental modification
        glBindVertexArray(0);
    }

    @OnlyIn(Dist.CLIENT)
    public void populateVBO() {
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // 3 vertices per triangle, 2 attributes per triangle, 3 values per attribute
        float[] data = new float[triangles.size() * 3 * 2 * 3];
        for (int i = 0, di = 0; i < triangles.size(); i++) {
            Triangle triangle = triangles.get(i);

            // TODO fix normalization
            // TODO use vertex normal
            Vector3f edge1 = new Vector3f();
            Vector3f edge2 = new Vector3f();
            edge1.sub(triangle.v1, triangle.v0);
            edge2.sub(triangle.v2, triangle.v0);
            Vector3f normal = new Vector3f();
            normal.cross(edge2, edge1);
            normal.normalize();

            data[di++] = triangle.v0.x;
            data[di++] = triangle.v0.y;
            data[di++] = triangle.v0.z;
            data[di++] = normal.x;
            data[di++] = normal.y;
            data[di++] = normal.z;

            data[di++] = triangle.v1.x;
            data[di++] = triangle.v1.y;
            data[di++] = triangle.v1.z;
            data[di++] = normal.x;
            data[di++] = normal.y;
            data[di++] = normal.z;

            data[di++] = triangle.v2.x;
            data[di++] = triangle.v2.y;
            data[di++] = triangle.v2.z;
            data[di++] = normal.x;
            data[di++] = normal.y;
            data[di++] = normal.z;
        }
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    @OnlyIn(Dist.CLIENT)
    public void draw(double x, double y, double z) {
        // TODO precompute all matrices in CPU code
        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, z);
        float[] modelView = new float[16];
        glGetFloatv(GL_MODELVIEW_MATRIX, modelView);
        GlStateManager.popMatrix();
        float[] projection = new float[16];
        glGetFloatv(GL_PROJECTION_MATRIX, projection);

        glUseProgram(ShaderUtils.sculpture);
        glUniformMatrix4fv(ShaderUtils.sculpture_modelView, false, modelView);
        glUniformMatrix4fv(ShaderUtils.sculpture_projection, false, projection);

        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, triangles.size() * 3);
        glBindVertexArray(0);

        glUseProgram(0);

//        Tessellator t = Tessellator.getInstance();
//        BufferBuilder b = t.getBuffer();
//        {
//            b.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
//            int i = 0;
//            for (Triangle triangle : triangles) {
//                if (i == 128) {
//                    t.draw();
//                    b.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
//                }
//                b.pos(triangle.v0.x, triangle.v0.y, triangle.v0.z).color(0F, 0F, 1F, 1F).endVertex();
//                b.pos(triangle.v1.x, triangle.v1.y, triangle.v1.z).color(0F, 0F, 1F, 1F).endVertex();
//                b.pos(triangle.v2.x, triangle.v2.y, triangle.v2.z).color(0F, 0F, 1F, 1F).endVertex();
//                i++;
//            }
//            t.draw();
//        }
//        {
//            glLineWidth(0.5F);
//            b.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
//            int i = 0;
//            for (Triangle triangle : triangles) {
//                if (i == 128) {
//                    t.draw();
//                    b.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
//                }
//                b.pos(triangle.v0.x, triangle.v0.y, triangle.v0.z).color(0F, 0F, 0F, 1F).endVertex();
//                b.pos(triangle.v1.x, triangle.v1.y, triangle.v1.z).color(0F, 0F, 0F, 1F).endVertex();
//
//                b.pos(triangle.v1.x, triangle.v1.y, triangle.v1.z).color(0F, 0F, 0F, 1F).endVertex();
//                b.pos(triangle.v2.x, triangle.v2.y, triangle.v2.z).color(0F, 0F, 0F, 1F).endVertex();
//
//                b.pos(triangle.v2.x, triangle.v2.y, triangle.v2.z).color(0F, 0F, 0F, 1F).endVertex();
//                b.pos(triangle.v0.x, triangle.v0.y, triangle.v0.z).color(0F, 0F, 0F, 1F).endVertex();
//                i++;
//            }
//            t.draw();
//        }
    }
}