package powerlessri.icesculptures.geometry;

import com.mojang.blaze3d.platform.GLX;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.gen.OctavesNoiseGenerator;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.SimplexNoiseGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.SidedProvider;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.forgespi.Environment;

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

    @OnlyIn(Dist.CLIENT)
    private int vao = 0;
    @OnlyIn(Dist.CLIENT)
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
        // TODO actual mesh generation based on iso value
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
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double t = 0D;
        for (int x = 0; x < ppa; x++) {
            for (int y = 0; y < ppa; y++) {
                for (int z = 0; z < ppa; z++) {
                    double v = noise.func_215462_a(x, y, z, 0D, 0D, false);
                    if (v > t) {
                        lattice.set(indexFromPos(x, y, z));
                    }
                    min = Math.min(min, v);
                    max = Math.max(max, v);
                }
            }
        }
        System.out.println(min + " " + max);

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
        for (Triangle triangle : triangles) {

        }
    }

    public void cleanup() {
        // Don't call GL stuff on any server thread
        if (EffectiveSide.get() != LogicalSide.CLIENT) {
            return;
        }
        // Prevent loading GL stuff on dedicated server
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            System.out.println("cleanup");
            glDeleteBuffers(vbo);
            vbo = 0;
            glDeleteVertexArrays(vao);
            vao = 0;
        });
    }

    @OnlyIn(Dist.CLIENT)
    private void createGLStates() {
        System.out.println("create vbo");
        // Create the VBO
        vbo = glGenBuffers();

        // Setup VAO
        {
            // Create and bind the VAO
            vao = glGenVertexArrays();
            glBindVertexArray(vao);

            // Attributes must be enabled for setting them up and using them in shaders
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);

            // Setup the attributes, note that attributes can live in different VBOs, we only use one here for simplicity
            // Technically we only need to bind the VBO once here, but I left it for clarity

            // Position attribute
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, Float.SIZE * 3 /* Size of color attribute */, 0);
            // Color attribute
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, Float.SIZE * 3 /* Size of position attribute */, Float.SIZE * 3 /* Size of one position attribute */);

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);

            // Unbind VAO to prevent accidental modification
            glBindVertexArray(0);
        }

        // Unbind everything else to prevent accidental modification
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    @OnlyIn(Dist.CLIENT)
    public void populateVBO() {
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // TODO add color
        float r = 0F;
        float g = 0F;
        float b = 1F;

        // 3 vertices per triangle, 2 attributes per triangle, 3 values per attribute
        float[] data = new float[triangles.size() * 3 * 2 * 3];
        for (int i = 0, di = 0; i < triangles.size(); i++) {
            Triangle triangle = triangles.get(i);
            data[di++] = triangle.v0.x;
            data[di++] = triangle.v0.y;
            data[di++] = triangle.v0.z;
            data[di++] = r;
            data[di++] = g;
            data[di++] = b;

            data[di++] = triangle.v1.x;
            data[di++] = triangle.v1.y;
            data[di++] = triangle.v1.z;
            data[di++] = r;
            data[di++] = g;
            data[di++] = b;

            data[di++] = triangle.v2.x;
            data[di++] = triangle.v2.y;
            data[di++] = triangle.v2.z;
            data[di++] = r;
            data[di++] = g;
            data[di++] = b;
        }
        glBufferData(GL_ARRAY_BUFFER, data, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    @OnlyIn(Dist.CLIENT)
    public void draw() {
//        glUseProgram(ShaderUtils.sculpture);
//        glBindVertexArray(vao);
//        glDrawArrays(GL_TRIANGLES, 0, triangles.size() * 3);
//        glBindVertexArray(0);
//        glUseProgram(0);

        Tessellator t = Tessellator.getInstance();
        BufferBuilder b = t.getBuffer();
        b.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        int i = 0;
        for (Triangle triangle : triangles) {
            if (i == 64) {
                t.draw();
                b.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
            }
            b.pos(triangle.v0.x, triangle.v0.y, triangle.v0.z).color(0F, 0F, 1F, 1F).endVertex();
            b.pos(triangle.v1.x, triangle.v1.y, triangle.v1.z).color(0F, 0F, 1F, 1F).endVertex();
            b.pos(triangle.v2.x, triangle.v2.y, triangle.v2.z).color(0F, 0F, 1F, 1F).endVertex();
            i++;
        }
        t.draw();
    }
}