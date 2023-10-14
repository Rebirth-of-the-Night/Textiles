package surreal.textiles.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public class TileSpindle extends TileEntity {

    private EnumFacing.Axis axis;

    public TileSpindle() {
        axis = EnumFacing.Axis.X;
    }

    public EnumFacing.Axis getAxis() {
        return axis;
    }

    public void setAxis(EnumFacing.Axis axis) {
        this.axis = axis;
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("axis", axis.getName());
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        axis = EnumFacing.Axis.byName(compound.getString("axis"));
    }
}
