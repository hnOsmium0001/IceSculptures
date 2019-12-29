package powerlessri.icesculptures.block;

import net.minecraft.tileentity.TileEntity;
import powerlessri.icesculptures.geometry.Mesh;
import powerlessri.icesculptures.setup.ModBlocks;

public class SculptureTileEntity extends TileEntity {

    private Mesh mesh;

    public SculptureTileEntity() {
        super(ModBlocks.sculptureTileEntity);
        mesh = new Mesh();
        mesh.setup();
        // TODO data sync
    }

    public Mesh getMesh() {
        return mesh;
    }

    @Override
    public void remove() {
        super.remove();
        mesh.cleanup();
    }
}
