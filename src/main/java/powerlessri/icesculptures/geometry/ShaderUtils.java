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

import static org.lwjgl.opengl.GL11.GL_FALSE;

// Adapted from Botania
public final class ShaderUtils {

    private ShaderUtils() {
    }

    // TODO utiltity to use/release shader

    public static int sculpture = 0;

    public static void init() {
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

        sculpture = deleteProgram(sculpture);

        loadPrograms(manager);
    }

    private static void loadPrograms(IResourceManager manager) {
        sculpture = createProgram(manager, "sculpture.vsh", "sculpture.fsh");
    }

    // Return int as a convenience and shorter way to clear shader IDs
    private static int deleteProgram(int shader) {
        if (shader != 0) {
            GLX.glDeleteProgram(shader);
        }
        return 0;
    }

    private static int createProgram(IResourceManager manager, String vert, String frag) {
        int vertId = 0, fragId = 0, program;
        if (vert != null)
            vertId = createShader(manager, vert, GLX.GL_VERTEX_SHADER);
        if (frag != null)
            fragId = createShader(manager, frag, GLX.GL_FRAGMENT_SHADER);

        program = GLX.glCreateProgram();
        if (program == 0)
            return 0;

        if (vert != null)
            GLX.glAttachShader(program, vertId);
        if (frag != null)
            GLX.glAttachShader(program, fragId);

        GLX.glLinkProgram(program);
        if (GLX.glGetProgrami(program, GLX.GL_LINK_STATUS) == GL11.GL_FALSE) {
            IceSculptures.logger.warn("Error encountered when linking program containing VS {} and FS {}. Log output:", vert, frag);
            IceSculptures.logger.warn(GLX.glGetProgramInfoLog(program, 32768));
            return 0;
        }

        // "Delete" the shader objects so that we only have to track the program
        // When the program is deleted (which we do on resource reload), the shaders will be deleted automatically as well
        // because there is not programs linked to it
        if (vert != null)
            GLX.glDeleteShader(vertId);
        if (frag != null)
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

            if (GLX.glGetShaderi(shader, GLX.GL_COMPILE_STATUS) == GL_FALSE) {
                String s1 = StringUtils.trim(GLX.glGetShaderInfoLog(shader, 32768));
                throw new IOException("Couldn't compile " + filename + ": " + s1);
            }

            return shader;
        } catch (Exception e) {
            GLX.glDeleteShader(shader);
            e.printStackTrace();
            return -1;
        }
    }

    private static String readFileAsString(IResourceManager manager, String filename) throws Exception {
        InputStream in = manager.getResource(new ResourceLocation(IceSculptures.MODID, filename)).getInputStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
