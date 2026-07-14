package surreal.textiles.entities;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import surreal.textiles.RegistryManager;

import javax.annotation.Nullable;
import java.util.List;

public class EntityWoolCushion extends Entity implements IEntityAdditionalSpawnData {

    private static final int MAX_HEALTH = 41;
    private static final int CHECK_INTERVAL = 100;

    private EnumDyeColor variant;

    @Nullable
    private AxisAlignedBB checkBox = null;
    private int checkTimer = 0;
    private int health = MAX_HEALTH;

    public EntityWoolCushion(final World world) {
        super(world);
        setSize(1F, 0.25F);
    }

    public EntityWoolCushion(final EnumDyeColor variant, final World world, final BlockPos pos, final double hitY) {
        this(world);
        this.variant = variant;
        setPosition(pos.getX() + 0.5D, pos.getY() + hitY, pos.getZ() + 0.5D);
    }

    @Override
    protected void entityInit() {}

    public EnumDyeColor getVariant() {
        return variant;
    }

    @Override
    public void setEntityBoundingBox(final AxisAlignedBB bb) {
        super.setEntityBoundingBox(bb);
        checkBox = null;
    }

    private AxisAlignedBB getCheckBox() {
        if (checkBox != null) return checkBox;
        final AxisAlignedBB bb = getEntityBoundingBox();
        return checkBox = new AxisAlignedBB(bb.minX, bb.minY - 0.015625D, bb.minZ, bb.maxX, bb.minY, bb.maxZ);
    }

    public boolean isSupported() {
        for (final Entity other : world.getEntitiesWithinAABB(EntityWoolCushion.class, getEntityBoundingBox(), null)) {
            if (other != this) return false;
        }
        final AxisAlignedBB bb = getCheckBox();
        final int maxX = MathHelper.ceil(bb.maxX) - 1;
        final int maxY = MathHelper.ceil(bb.maxY) - 1;
        final int maxZ = MathHelper.ceil(bb.maxZ) - 1;
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = MathHelper.floor(bb.minX); x <= maxX; x++) {
            for (int y = MathHelper.floor(bb.minY); y <= maxY; y++) {
                for (int z = MathHelper.floor(bb.minZ); z <= maxZ; z++) {
                    pos.setPos(x, y, z);
                    if (world.isAirBlock(pos)) continue;
                    final IBlockState state = world.getBlockState(pos);
                    final AxisAlignedBB o = state.getBoundingBox(world, pos);
                    if (bb.intersects(x + o.minX, y + o.minY, z + o.minZ, x + o.maxX, y + o.maxY, z + o.maxZ)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean hitByEntity(final Entity attacker) {
        if (attacker instanceof EntityPlayer player) {
            damageCushion(player);
            return true;
        }
        return false;
    }

    @Override
    public boolean attackEntityFrom(final DamageSource source, final float amount) {
        if (isEntityInvulnerable(source)) return false;
        damageCushion(source.getTrueSource() instanceof EntityPlayer player ? player : null);
        return true;
    }

    @Override
    public void onStruckByLightning(final EntityLightningBolt lightningBolt) {} // wool is a great insulator

    private void damageCushion(@Nullable final EntityPlayer attacker) {
        if (world.isRemote) return;
        if (attacker != null) {
            if (attacker.capabilities.isCreativeMode) {
                breakCushion(false);
                return;
            }
            final ItemStack stack = attacker.getHeldItem(EnumHand.MAIN_HAND);
            if (stack.isEmpty()) {
                health -= 20;
            } else {
                health -= MathHelper.ceil(stack.getDestroySpeed(Blocks.WOOL.getDefaultState()) * 20);
            }
        } else {
            health -= 20;
        }
        if (health <= 0) {
            breakCushion(true);
        }
    }

    @Override
    public boolean processInitialInteract(final EntityPlayer player, final EnumHand hand) {
        if (player.isSneaking()) return false;
        if (isBeingRidden()) return true;
        if (!world.isRemote) {
            player.startRiding(this);
        }
        return true;
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (!world.isRemote) {
            if (health < MAX_HEALTH) {
                health++;
            }
            checkTimer++;
            if (checkTimer >= CHECK_INTERVAL) {
                checkTimer = 0;
                if (!isDead && !isSupported()) {
                    breakCushion(true);
                }
            }
        }
    }

    private void breakCushion(final boolean doDrops) {
        setDead();
        if (!world.getGameRules().getBoolean("doEntityDrops")) return;
        playSound(SoundEvents.BLOCK_CLOTH_BREAK, 1F, 1.2F);
        if (doDrops) {
            entityDropItem(RegistryManager.WOOL_CUSHION.newStack(variant, 1), 0F);
        }
    }

    @Nullable
    @Override
    public EntityItem entityDropItem(final ItemStack stack, final float offsetY) {
        if (stack.isEmpty()) return null;
        EntityItem entity = new EntityItem(world, posX, posY + offsetY, posZ, stack);
        entity.setDefaultPickupDelay();
        world.spawnEntity(entity);
        return entity;
    }

    @Override
    public void writeSpawnData(final ByteBuf buffer) {
        buffer.writeByte(variant.getMetadata());
    }

    @Override
    public void readSpawnData(final ByteBuf additionalData) {
        variant = EnumDyeColor.byMetadata(additionalData.readByte());
    }

    @Override
    protected void writeEntityToNBT(final NBTTagCompound compound) {
        compound.setString("Color", variant.name());
    }

    @Override
    protected void readEntityFromNBT(final NBTTagCompound compound) {
        try {
            variant = EnumDyeColor.valueOf(compound.getString("Color"));
        } catch (IllegalArgumentException e) {
            variant = EnumDyeColor.WHITE;
        }
    }

}
