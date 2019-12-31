package powerlessri.icesculptures.geometry;

import com.mojang.blaze3d.platform.GLX;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;
import powerlessri.icesculptures.IceSculptures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;

// Adapted from Botania
public final class ShaderUtils {

    private ShaderUtils() {
    }

    public static int mesh = 0;
    public static int mesh_modelView = 0;
    public static int mesh_projection = 0;
    public static int mesh_lightPos = 0;
    public static int mesh_ambientStrength = 0;

    public static void setup() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getResourceManager() instanceof IReloadableResourceManager) {
            IReloadableResourceManager current = (IReloadableResourceManager) mc.getResourceManager();
            current.addReloadListener((ISelectiveResourceReloadListener) ShaderUtils::reloadPrograms);
        }
    }

    private static void reloadPrograms(IResourceManager manager, Predicate<IResourceType> predicate) {
        if (!predicate.test(VanillaResourceType.SHADERS)) {
            return;
        }

        mesh = deleteProgram(mesh);
        mesh_modelView = 0;
        mesh_projection = 0;
        mesh_lightPos = 0;
        mesh_ambientStrength = 0;

        loadPrograms(manager);
    }

    private static void loadPrograms(IResourceManager manager) {
        mesh = createProgram(manager, "mesh.vsh", "mesh.fsh");
        mesh_modelView = glGetUniformLocation(mesh, "modelView");
        mesh_projection = glGetUniformLocation(mesh, "projection");
        mesh_lightPos = glGetUniformLocation(mesh, "lightPos");
        mesh_ambientStrength = glGetUniformLocation(mesh, "ambientStrength");
    }

    // Return int as a convenience and shorter way to clear shader IDs
    private static int deleteProgram(int shader) {
        if (shader != 0) {
            GLX.glDeleteProgram(shader);
        }
        return 0;
    }

    private static int createProgram(IResourceManager manager, String vert, String frag) {
        int vertId = 0;
        if (vert != null) {
            vertId = createShader(manager, vert, GLX.GL_VERTEX_SHADER);
        }
        int fragId = 0;
        if (frag != null) {
            fragId = createShader(manager, frag, GLX.GL_FRAGMENT_SHADER);
        }

        int program = GLX.glCreateProgram();
        // Failed to create program
        if (program == 0) {
            GLX.glDeleteShader(vertId);
            GLX.glDeleteShader(fragId);
            return 0;
        }

        if (vert != null) {
            GLX.glAttachShader(program, vertId);
        }
        if (frag != null) {
            GLX.glAttachShader(program, fragId);
        }

        // Failed to link program to shaders into an executable on GPU
        GLX.glLinkProgram(program);
        if (GLX.glGetProgrami(program, GLX.GL_LINK_STATUS) == GL11.GL_FALSE) {
            IceSculptures.logger.warn("Error encountered when linking program containing VS {} and FS {}. Log output:", vert, frag);
            IceSculptures.logger.warn(GLX.glGetProgramInfoLog(program, 32768));
            GLX.glDeleteProgram(program);
            GLX.glDeleteShader(vertId);
            GLX.glDeleteShader(fragId);
            return 0;
        }

        // "Delete" the shader objects so that we only have to track the program
        // When the program is deleted (which we do on resource reload), the shaders will be deleted automatically as well
        // because there is not programs linked to it
        GLX.glDeleteShader(vertId);
        GLX.glDeleteShader(fragId);

        return program;
    }

    private static int createShader(IResourceManager manager, String filename, int shaderType) {
        int shader = 0;
        try {
            shader = GLX.glCreateShader(shaderType);

            if (shader == 0)
                return 0;

            GLX.glShaderSource(shader, readFileAsString(manager, filename));
            GLX.glCompileShader(shader);

            if (GLX.glGetShaderi(shader, GLX.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                String s1 = StringUtils.trim(GLX.glGetShaderInfoLog(shader, 32768));
                throw new IOException("Couldn't compile " + filename + ": " + s1);
            }

            return shader;
        } catch (Exception e) {
            GLX.glDeleteShader(shader);
            e.printStackTrace();
            return 0;
        }
    }

    private static String readFileAsString(IResourceManager manager, String filename) throws Exception {
        InputStream in = manager.getResource(new ResourceLocation(IceSculptures.MODID, "shaders/" + filename)).getInputStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
