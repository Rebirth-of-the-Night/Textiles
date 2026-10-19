package surreal.textiles.entities;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import surreal.textiles.blocks.BlockCushion;

public class EntitySitDummy extends Entity {

    private static final int CHECK_INTERVAL = 20;

    private BlockPos blockPos;

    private int checkTimer = 0;

    public EntitySitDummy(final World world) {
        super(world);
        setSize(0F, 0F);
    }

    public EntitySitDummy(final World world, final double x, final double y, final double z, final BlockPos blockPos) {
        this(world);
        this.blockPos = blockPos;
        setPosition(x, y, z);
    }

    @Override
    protected void entityInit() {}

    @Override
    public double getMountedYOffset() {
        return -0.1875D;
    }

    @Override
    public void onUpdate() {
        if (world.isRemote) return;
        checkTimer++;
        if (checkTimer >= CHECK_INTERVAL) {
            checkTimer = 0;
            if (!isBeingRidden() || !(world.getBlockState(blockPos).getBlock() instanceof BlockCushion)) {
                setDead();
            }
        }
    }

    @Override
    protected void writeEntityToNBT(final NBTTagCompound compound) {
        compound.setInteger("X", blockPos.getX());
        compound.setInteger("Y", blockPos.getY());
        compound.setInteger("Z", blockPos.getZ());
    }

    @Override
    protected void readEntityFromNBT(final NBTTagCompound compound) {
        blockPos = new BlockPos(compound.getInteger("X"), compound.getInteger("Y"), compound.getInteger("Z"));
    }

    @Override
    public EnumPushReaction getPushReaction() {
        return EnumPushReaction.IGNORE;
    }

}
