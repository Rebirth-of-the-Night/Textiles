package surreal.textiles.entities;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import surreal.textiles.blocks.BlockSack;

import javax.annotation.Nullable;

public class EntityFallingSack extends EntityFallingBlock implements IEntityAdditionalSpawnData {

    private int dyeColor = -1;

    public EntityFallingSack(final World world) {
        super(world);
    }

    public EntityFallingSack(final World world, final double x, final double y, final double z,
                             final IBlockState fallingBlockState) {
        super(world, x, y, z, fallingBlockState);
    }

    public void setDyeColor(final int dyeColor) {
        this.dyeColor = dyeColor;
    }

    public int getDyeColor() {
        return dyeColor;
    }

    @Nullable
    @Override
    public EntityItem entityDropItem(final ItemStack stack, final float offsetY) {
        // assume this is only called to drop the falling block as an item
        if (tileEntityData != null) {
            final NBTTagCompound stackData = new NBTTagCompound();
            stackData.setTag("BlockEntityTag", tileEntityData.copy());
            stack.setTagCompound(stackData);
            if (tileEntityData.hasKey("dye", Constants.NBT.TAG_INT) && tileEntityData.getInteger("dye") >= 0) {
                stack.setItemDamage(1);
            }
        }
        return super.entityDropItem(stack, offsetY);
    }

    @Override
    public void writeSpawnData(final ByteBuf buffer) {
        buffer.writeInt(dyeColor);
        buffer.writeShort(Block.getStateId(fallTile));
    }

    @Override
    public void readSpawnData(final ByteBuf additionalData) {
        dyeColor = additionalData.readInt();
        final IBlockState state = Block.getStateById(additionalData.readShort());
        if (dyeColor >= 0 && state.getBlock() instanceof BlockSack) { // sanity check
            fallTile = state.withProperty(BlockSack.DYED, true);
        } else {
            fallTile = state;
        }
    }

    @Override
    protected void writeEntityToNBT(final NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (dyeColor >= 0) {
            compound.setInteger("dye", dyeColor);
        }
    }

    @Override
    protected void readEntityFromNBT(final NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        dyeColor = compound.hasKey("dye", Constants.NBT.TAG_INT) ? compound.getInteger("dye") : -1;
    }

}
