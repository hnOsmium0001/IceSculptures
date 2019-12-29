package powerlessri.icesculptures.geometry;

import net.minecraft.client.renderer.model.BlockPart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.SimplexNoiseGenerator;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import static powerlessri.icesculptures.geometry.GeometryUtils.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    private BitSet lattice = new BitSet(ppa * ppa * ppa);
    private List<Triangle> triangles = new ArrayList<>();

    private int vao;
    private int vbo;

    public Mesh() {
        Random rand = new Random();
        for (int x = 0; x < ppa; x++) {
            for (int y = 0; y < ppa; y++) {
                for (int z = 0; z < ppa; z++) {
                    if (rand.nextBoolean()) {
                        lattice.set(indexFromPos(x, y, z));
                    }
                }
            }
        }
        for (int x = 0; x < ppa; x++) {
            for (int y = 0; y < ppa; y++) {
                for (int z = 0; z < ppa; z++) {
                    marchCube(triangles, lattice, x, y, z);
                }
            }
        }

        createGLStates();
        populateVBO();
    }

    private void createGLStates() {
        // Create and bind the VAO
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Create the VBO
        vbo = glGenBuffers();

        // Enable attributes to set them up later
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        // Setup the attributes
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, Float.SIZE * 3 /* Color attribute */, 0);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, Float.SIZE * 3 /* Position attribute */, Float.SIZE * 3 /* Position attribute */);

        // Disable attribute pointers for setting up attributes
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(0);

        // Unbind VAB
        glBindVertexArray(0);
    }

    public void populateVBO() {
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // TODO add color
        float r = 0F;
        float g = 0F;
        float b = 1F;

        // 3 vertices per triangle, 2 attributes per triangle, 3 values per attribute
        float[] data = new float[triangles.size() * 3 * 2 * 3];
        for (int i = 0; i < triangles.size(); i++) {

            Triangle triangle = triangles.get(i);
            data[i] = triangle.v0.x;
            data[i + 1] = triangle.v0.y;
            data[i + 2] = triangle.v0.z;
            data[i] = r;
            data[i + 1] = g;
            data[i + 2] = b;

            data[i + 6] = triangle.v1.x;
            data[i + 6 + 1] = triangle.v1.y;
            data[i + 6 + 2] = triangle.v1.z;
            data[i + 6] = r;
            data[i + 6 + 1] = g;
            data[i + 6 + 2] = b;

            data[i + 12] = triangle.v2.x;
            data[i + 12 + 1] = triangle.v2.y;
            data[i + 12 + 2] = triangle.v2.z;
            data[i + 12] = r;
            data[i + 12 + 1] = g;
            data[i + 12 + 2] = b;
        }
        glBufferData(GL_ARRAY_BUFFER, data, GL_DYNAMIC_DRAW);
    }

    public void draw(BlockPos pos) {
        // TODO get location
        glUniform3i(0, pos.getX(), pos.getY(), pos.getZ());
        // TODO bind shader

        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, triangles.size() * 3);
        glBindVertexArray(0);
    }
}
