package surreal.textiles.entities;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

// Copy pasted EntityItem oh god...
public class EntityBasket extends Entity {

    private static final DataParameter<ItemStack> ITEM = EntityDataManager.createKey(EntityBasket.class, DataSerializers.ITEM_STACK);
    /** The age of this EntityItem (used to animate it up and down as well as expire it) */
    private int age;
    private int pickupDelay;
    private int health;
    private String thrower;
    private String owner;
    public float hoverStart;

    public int lifespan = 6000;

    public EntityBasket(World worldIn, double x, double y, double z)
    {
        super(worldIn);
        this.health = 5;
        this.hoverStart = (float)(Math.random() * Math.PI * 2.0D);
        this.setSize(0.25F, 0.25F);
        this.setPosition(x, y, z);
        this.rotationYaw = (float)(Math.random() * 360.0D);
        this.motionX = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D));
        this.motionY = 0.20000000298023224D;
        this.motionZ = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D));
    }

    public EntityBasket(World worldIn, double x, double y, double z, ItemStack stack)
    {
        this(worldIn, x, y, z);
        this.setItem(stack);
        this.lifespan = (stack.getItem() == null ? 6000 : stack.getItem().getEntityLifespan(stack, worldIn));
    }

    public EntityBasket(World world, BlockPos pos, ItemStack stack) {
        this(world, pos.getX() + (double)(world.rand.nextFloat() * 0.5F) + 0.25D, pos.getY() + (double)(world.rand.nextFloat() * 0.5F) + 0.25D, pos.getZ() + (double)(world.rand.nextFloat() * 0.5F) + 0.25D, stack);
        this.setDefaultPickupDelay();
    }

    public EntityBasket(World world, Entity entity, ItemStack stack) {
        this(world, entity.posX, entity.posY, entity.posZ, stack);
        this.motionX = entity.motionX;
        this.motionY = entity.motionY;
        this.motionZ = entity.motionZ;
        this.setPickupDelay(40);
    }

    public EntityBasket(World worldIn)
    {
        super(worldIn);
        this.health = 5;
        this.hoverStart = (float)(Math.random() * Math.PI * 2.0D);
        this.setSize(0.25F, 0.25F);
        this.setItem(ItemStack.EMPTY);
    }

    protected boolean canTriggerWalking()
    {
        return false;
    }

    protected void entityInit()
    {
        this.getDataManager().register(ITEM, ItemStack.EMPTY);
    }

    public void onUpdate()
    {
        if (this.getItem().isEmpty())
        {
            this.setDead();
        }
        else
        {
            super.onUpdate();

            if (this.pickupDelay > 0 && this.pickupDelay != 32767)
            {
                --this.pickupDelay;
            }

            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            double d0 = this.motionX;
            double d1 = this.motionY;
            double d2 = this.motionZ;

            if (!this.hasNoGravity())
            {
                this.motionY -= 0.03999999910593033D;
            }

            if (this.world.isRemote)
            {
                this.noClip = false;
            }
            else
            {
                this.noClip = this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
            }

            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            boolean flag = (int)this.prevPosX != (int)this.posX || (int)this.prevPosY != (int)this.posY || (int)this.prevPosZ != (int)this.posZ;

            if (flag || this.ticksExisted % 25 == 0)
            {
                if (this.world.getBlockState(new BlockPos(this)).getMaterial() == Material.LAVA)
                {
                    this.motionY = 0.20000000298023224D;
                    this.motionX = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
                    this.motionZ = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
                    this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
                }
            }

            float f = 0.98F;

            if (this.onGround)
            {
                BlockPos underPos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ));
                net.minecraft.block.state.IBlockState underState = this.world.getBlockState(underPos);
                f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.98F;
            }

            this.motionX *= (double)f;
            this.motionY *= 0.9800000190734863D;
            this.motionZ *= (double)f;

            if (this.onGround)
            {
                this.motionY *= -0.5D;
            }

            if (this.age != -32768)
            {
                ++this.age;
            }

            this.handleWaterMovement();

            if (!this.world.isRemote)
            {
                double d3 = this.motionX - d0;
                double d4 = this.motionY - d1;
                double d5 = this.motionZ - d2;
                double d6 = d3 * d3 + d4 * d4 + d5 * d5;

                if (d6 > 0.01D)
                {
                    this.isAirBorne = true;
                }
            }

            ItemStack item = this.getItem();

            if (!this.world.isRemote && this.age >= lifespan)
            {
                this.setDead();
            }
            if (item.isEmpty())
            {
                this.setDead();
            }
        }
    }

    public void setAgeToCreativeDespawnTime()
    {
        this.age = 4800;
    }

    public boolean handleWaterMovement()
    {
        if (this.world.handleMaterialAcceleration(this.getEntityBoundingBox(), Material.WATER, this))
        {
            if (!this.inWater && !this.firstUpdate)
            {
                this.doWaterSplashEffect();
            }

            this.inWater = true;
        }
        else
        {
            this.inWater = false;
        }

        return this.inWater;
    }

    protected void dealFireDamage(int amount)
    {
        this.attackEntityFrom(DamageSource.IN_FIRE, (float)amount);
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.world.isRemote || this.isDead) return false; //Forge: Fixes MC-53850
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else if (!this.getItem().isEmpty() && this.getItem().getItem() == Items.NETHER_STAR && source.isExplosion())
        {
            return false;
        }
        else
        {
            this.markVelocityChanged();
            this.health = (int)((float)this.health - amount);

            if (this.health <= 0)
            {
                this.setDead();
            }

            return false;
        }
    }

    public static void registerFixesItem(DataFixer fixer)
    {
        fixer.registerWalker(FixTypes.ENTITY, new ItemStackData(EntityItem.class, new String[] {"Item"}));
    }

    public void writeEntityToNBT(NBTTagCompound compound)
    {
        compound.setShort("Health", (short)this.health);
        compound.setShort("Age", (short)this.age);
        compound.setShort("PickupDelay", (short)this.pickupDelay);
        compound.setInteger("Lifespan", lifespan);

        if (this.getThrower() != null)
        {
            compound.setString("Thrower", this.thrower);
        }

        if (this.getOwner() != null)
        {
            compound.setString("Owner", this.owner);
        }

        if (!this.getItem().isEmpty())
        {
            compound.setTag("Item", this.getItem().writeToNBT(new NBTTagCompound()));
        }
    }

    public void readEntityFromNBT(NBTTagCompound compound)
    {
        this.health = compound.getShort("Health");
        this.age = compound.getShort("Age");

        if (compound.hasKey("PickupDelay"))
        {
            this.pickupDelay = compound.getShort("PickupDelay");
        }

        if (compound.hasKey("Owner"))
        {
            this.owner = compound.getString("Owner");
        }

        if (compound.hasKey("Thrower"))
        {
            this.thrower = compound.getString("Thrower");
        }

        NBTTagCompound nbttagcompound = compound.getCompoundTag("Item");
        this.setItem(new ItemStack(nbttagcompound));

        if (this.getItem().isEmpty())
        {
            this.setDead();
        }
        if (compound.hasKey("Lifespan")) lifespan = compound.getInteger("Lifespan");
    }

    public void onCollideWithPlayer(EntityPlayer entityIn)
    {
        if (!entityIn.isCreative()) {
            boolean hasEmptySlot = false;

            for (int i = 0; i < 9; i++) {
                if (entityIn.inventory.getStackInSlot(i).isEmpty()) {
                    hasEmptySlot = true;
                    break;
                }
            }

            if (!hasEmptySlot) {
                this.setPickupDelay(100);
                return;
            }
        }

        if (!this.world.isRemote)
        {
            if (this.pickupDelay > 0) return;
            ItemStack itemstack = this.getItem();
            Item item = itemstack.getItem();
            int i = itemstack.getCount();

            ItemStack clone = itemstack.copy();

            if (this.pickupDelay <= 0 && (this.owner == null || lifespan - this.age <= 200 || this.owner.equals(entityIn.getName())) && (i <= 0 || entityIn.inventory.addItemStackToInventory(itemstack) || clone.getCount() > this.getItem().getCount()))
            {
                clone.setCount(clone.getCount() - this.getItem().getCount());

                if (itemstack.isEmpty())
                {
                    EntityItem entityItem = new EntityItem(world, posX, posY, posZ, this.getItem());
                    entityItem.motionX = this.motionX;
                    entityItem.motionY = this.motionY;
                    entityItem.motionZ = this.motionZ;
                    entityItem.setDefaultPickupDelay();

                    world.spawnEntity(entityItem);
                    this.setDead();

                    entityIn.onItemPickup(entityItem, i);
                    entityItem.setDead();

                    itemstack.setCount(i);
                }

                entityIn.addStat(StatList.getObjectsPickedUpStats(item), i);
            }
        }
    }

    public String getName()
    {
        return this.hasCustomName() ? this.getCustomNameTag() : I18n.translateToLocal("item." + this.getItem().getTranslationKey());
    }

    public boolean canBeAttackedWithItem()
    {
        return false;
    }

    public ItemStack getItem()
    {
        return this.getDataManager().get(ITEM);
    }

    public void setItem(ItemStack stack)
    {
        this.getDataManager().set(ITEM, stack);
        this.getDataManager().setDirty(ITEM);
    }

    public String getOwner()
    {
        return this.owner;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    public String getThrower()
    {
        return this.thrower;
    }

    public void setThrower(String thrower)
    {
        this.thrower = thrower;
    }

    @SideOnly(Side.CLIENT)
    public int getAge()
    {
        return this.age;
    }

    public void setDefaultPickupDelay()
    {
        this.pickupDelay = 10;
    }

    public void setNoPickupDelay()
    {
        this.pickupDelay = 0;
    }

    public void setInfinitePickupDelay()
    {
        this.pickupDelay = 32767;
    }

    public void setPickupDelay(int ticks)
    {
        this.pickupDelay = ticks;
    }

    public boolean cannotPickup()
    {
        return this.pickupDelay > 0;
    }

    public void setNoDespawn()
    {
        this.age = -6000;
    }

    public void makeFakeItem()
    {
        this.setInfinitePickupDelay();
        this.age = getItem().getItem().getEntityLifespan(getItem(), world) - 1;
    }
}
