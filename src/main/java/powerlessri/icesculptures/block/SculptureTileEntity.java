package powerlessri.icesculptures.block;

import net.minecraft.tileentity.TileEntity;
import powerlessri.icesculptures.geometry.Mesh;
import powerlessri.icesculptures.setup.ModBlocks;

public class SculptureTileEntity extends TileEntity {

    private Mesh mesh;

    public SculptureTileEntity() {
        super(ModBlocks.sculptureTileEntity);
        this.mesh = new Mesh();
        // TODO data sync
    }
}
