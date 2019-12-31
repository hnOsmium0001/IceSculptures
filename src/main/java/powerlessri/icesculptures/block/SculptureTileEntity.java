package powerlessri.icesculptures.block;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import powerlessri.icesculptures.geometry.Mesh;
import powerlessri.icesculptures.setup.ModBlocks;

import javax.annotation.Nullable;

public class SculptureTileEntity extends TileEntity implements ITickableTileEntity {

    private Mesh mesh;

    public SculptureTileEntity() {
        super(ModBlocks.sculptureTileEntity);
        mesh = new Mesh();
    }

    @Override
    public void tick() {
        Preconditions.checkNotNull(world);
        if (!world.isRemote && mesh.isEmpty()) {
            mesh.fill();
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), Constants.BlockFlags.DEFAULT);
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, 0, mesh.write(new CompoundNBT()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        mesh.read(pkt.getNbtCompound());
        mesh.createGLStates();
        mesh.march();
        mesh.populateVBO();
    }

    public Mesh getMesh() {
        return mesh;
    }

    @Override
    public void remove() {
        super.remove();
        mesh.cleanup();
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        mesh.read(compound.getCompound("Mesh"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put("Mesh", mesh.write(new CompoundNBT()));
        return super.write(compound);
    }
}
