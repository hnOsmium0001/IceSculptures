package powerlessri.icesculptures.geometry;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.OctavesNoiseGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;

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

    public boolean isEmpty() {
        return triangles.isEmpty();
    }

    private static OctavesNoiseGenerator noise = new OctavesNoiseGenerator(new Random(), 4);

    public void fill(BlockPos pos) {
        double t = 0D;
        for (int x = 1; x < ppa - 1; x++) {
            for (int y = 1; y < ppa - 1; y++) {
                for (int z = 1; z < ppa - 1; z++) {
                    double v = noise.func_215462_a(
                            pos.getX() * 8 + x / 2D,
                            pos.getY() * 8 + y / 2D,
                            pos.getZ() * 8 + z / 2D,
                            0D, 0D, false);
                    if (v > t) {
                        lattice.set(indexFromPos(x, y, z));
                    }
                }
            }
        }
    }

    public void march() {
        triangles.clear();
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
    public void createGLStates() {
        // Create the VBO
        vbo = glGenBuffers();

        // Create and bind the VAO
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        {
            // Setup the attributes, note that attributes can live in different VBOs, we only use one here for simplicity
            // Technically we only need to bind the VBO once here, but I left it for clarity

            // Position + normal
            int stride = Float.BYTES * 3 + Float.BYTES * 3;

            // Position attribute
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
            // Normal attribute
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, Float.BYTES * 3 /* Size of a position attribute */);
//            // Color attribute
//            glBindBuffer(GL_ARRAY_BUFFER, vbo);
//            glEnableVertexAttribArray(2);
//            glVertexAttribPointer(2, 3, GL_FLOAT, false, stride, Float.BYTES * 3 * 2 /* Size of a position and a normal attribute */);

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

            data[di++] = triangle.v0.x;
            data[di++] = triangle.v0.y;
            data[di++] = triangle.v0.z;
            data[di++] = triangle.n0.x;
            data[di++] = triangle.n0.y;
            data[di++] = triangle.n0.z;

            data[di++] = triangle.v1.x;
            data[di++] = triangle.v1.y;
            data[di++] = triangle.v1.z;
            data[di++] = triangle.n1.x;
            data[di++] = triangle.n1.y;
            data[di++] = triangle.n1.z;

            data[di++] = triangle.v2.x;
            data[di++] = triangle.v2.y;
            data[di++] = triangle.v2.z;
            data[di++] = triangle.n2.x;
            data[di++] = triangle.n2.y;
            data[di++] = triangle.n2.z;
        }
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    @OnlyIn(Dist.CLIENT)
    public void draw() {
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, triangles.size() * 3);
        glBindVertexArray(0);
    }

    public void read(CompoundNBT compound) {
        lattice = BitSet.valueOf(compound.getLongArray("Lattice"));
        march();
    }

    public CompoundNBT write(CompoundNBT compound) {
        compound.putLongArray("Lattice", lattice.toLongArray());
        return compound;
    }
}