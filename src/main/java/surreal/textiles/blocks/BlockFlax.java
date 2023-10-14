package surreal.textiles.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import surreal.textiles.ModConfig;
import surreal.textiles.RegistryManager;
import surreal.textiles.items.ItemMaterial;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@SuppressWarnings("deprecation")
public class BlockFlax extends BlockCrops {

    protected static final PropertyInteger AGE = PropertyInteger.create("age", 0, 5);
    protected static final PropertyBool BOTTOM = PropertyBool.create("bottom");

    protected static final AxisAlignedBB[] BOUNDING_BOXES;

    public BlockFlax() {
        setDefaultState(getDefaultState().withProperty(BOTTOM, true));
        setHardness(0.7F);
    }

    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        int age = getAge(state);
        if (age > 3) return BOUNDING_BOXES[3];
        else if (age > 2) return BOUNDING_BOXES[2];
        else if (age > 1) return BOUNDING_BOXES[1];
        else return BOUNDING_BOXES[0];
    }

    @Override
    protected boolean canSustainBush(@Nonnull IBlockState state) {
        Material material = state.getMaterial();
        return material == Material.GROUND || material == Material.GRASS;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (fromPos.getY() < pos.getY()) {
            IBlockState fromState = worldIn.getBlockState(fromPos);
            if (fromState.getBlockFaceShape(worldIn, fromPos, EnumFacing.UP) != BlockFaceShape.SOLID || !canSustainBush(fromState)) {
                worldIn.scheduleBlockUpdate(pos, this, 1, 0); // Delay a tick to make shear trick work
            }
        }
    }

    private void handleUp(World world, BlockPos up, int ageToAdd) {
        IBlockState upState = world.getBlockState(up);
        Block upBlock = upState.getBlock();

        if (world.isAirBlock(up) || upBlock.isReplaceable(world, up)) {
            world.setBlockState(up, withAge(ageToAdd).withProperty(BOTTOM, false));
        }
        else if (upBlock == this) {
            world.setBlockState(up, upState.withProperty(AGE, Math.min(getMaxAge(), upState.getValue(AGE) + ageToAdd)));
        }
    }

    @Override
    public boolean canBlockStay(World worldIn, BlockPos pos, @Nonnull IBlockState state) {
        IBlockState soil = worldIn.getBlockState(pos.down());
        return (worldIn.getLight(pos) >= 8 || worldIn.canSeeSky(pos)) && (soil.getBlock() == this || soil.getBlock().canSustainPlant(soil, worldIn, pos.down(), EnumFacing.UP, this));
    }

    @ParametersAreNonnullByDefault
    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        int age = getAge(state);
        boolean bottom = state.getValue(BOTTOM);

        if (bottom && age == getMaxAge()) {
            BlockPos up = pos.up();
            IBlockState upState = worldIn.getBlockState(up);

            if (worldIn.isAirBlock(up) || upState.getBlock().isReplaceable(worldIn, up)) {
                worldIn.setBlockState(up, getDefaultState().withProperty(BOTTOM, false));
            }

        } else super.updateTick(worldIn, pos, state, rand);
    }

    @Override
    public void grow(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        int increasedAge = this.getAge(state) + this.getBonemealAgeIncrease(worldIn);
        int maxAge = this.getMaxAge();

        if (increasedAge > maxAge) {
            boolean bottom = state.getValue(BOTTOM);

            if (bottom) {
                handleUp(worldIn, pos.up(), increasedAge - maxAge);
            }

            increasedAge = maxAge;
        }

        if (getAge(state) != maxAge) worldIn.setBlockState(pos, state.withProperty(AGE, increasedAge), 2);
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        boolean bottom = state.getValue(BOTTOM); // if it's bottom block or not
        boolean grow = super.canGrow(worldIn, pos, state, isClient); // if it can grow or not

        if (bottom && !grow) {
            BlockPos up = pos.up();
            IBlockState upState = worldIn.getBlockState(up);

            // Returns if top is replaceable or if top can grow
            return (worldIn.isAirBlock(up) && upState.getBlock().isReplaceable(worldIn, up)) || (upState.getBlock() == this && canGrow(worldIn, up, upState, isClient));
        }

        return grow;
    }

    @Override
    protected int getBonemealAgeIncrease(@Nonnull World worldIn) {
        return MathHelper.getInt(worldIn.rand, 1, 3);
    }

    // Item Stuff
    @ParametersAreNonnullByDefault
    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        if (!player.isCreative() && (stack.getItem() == Items.SHEARS || stack.getItem().getHarvestLevel(stack, "shears", player, state) > -1) && getAge(state) == getMaxAge()) {
            worldIn.setBlockState(pos, state.withProperty(AGE, 4), 2);

            ItemMaterial.Type blossomType = null;

            float chance = worldIn.rand.nextFloat();

            if (chance < 0.2F) blossomType = ItemMaterial.Type.EXQUISITE_FLAX_BLOSSOMS;
            else if (chance < 0.4F) blossomType = ItemMaterial.Type.VIBRANT_FLAX_BLOSSOMS;
            else if (chance < 0.8F) blossomType = ItemMaterial.Type.PALE_FLAX_BLOSSOMS;

            if (blossomType != null) spawnAsEntity(worldIn, pos, RegistryManager.INSTANCE.getMaterial(blossomType));
        }
        else super.harvestBlock(worldIn, player, pos, state, te, stack);
    }

    @Nonnull
    @Override
    protected Item getSeed() {
        return RegistryManager.FLAX_SEEDS;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        int age = getAge(state);
        boolean isBottom = state.getValue(BOTTOM);

        Random rand = world instanceof World ? ((World) world).rand : RANDOM;

        int seedAmount = MathHelper.getInt(rand, 0 , Math.min(age, 2));
        if (age > 0 && seedAmount > 0) drops.add(new ItemStack(getSeed(), seedAmount));
        else if (ModConfig.drops.alwaysDropSeeds) drops.add(new ItemStack(getSeed(), 1));

        ItemMaterial.Type blossomType = null;

        if (age > 0) {
            int stalkAmount = MathHelper.getInt(rand, 1, 2);
            if (!isBottom && stalkAmount == 2) {
                stalkAmount = 1;
                float chance = rand.nextFloat();

                if (chance < 0.2F) blossomType = ItemMaterial.Type.EXQUISITE_FLAX_BLOSSOMS;
                else if (chance < 0.4F) blossomType = ItemMaterial.Type.VIBRANT_FLAX_BLOSSOMS;
                else if (chance < 0.8F) blossomType = ItemMaterial.Type.PALE_FLAX_BLOSSOMS;
            }

            ItemStack stalk = RegistryManager.INSTANCE.getMaterial(ItemMaterial.Type.FLAX_STALKS);
            stalk.setCount(stalkAmount);

            drops.add(stalk);
            if (blossomType != null) drops.add(RegistryManager.INSTANCE.getMaterial(blossomType));
        }
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(getSeed());
    }

    // Property Stuff
    @Nonnull
    @Override
    protected PropertyInteger getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        int age = getAge(state);
        int bottom = state.getValue(BOTTOM) ? 0 : getMaxAge() + 1;

        return bottom + age;
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        int age = meta % 6;
        boolean bottom = (meta / 6) >= 1;

        return withAge(age).withProperty(BOTTOM, !bottom);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE, BOTTOM);
    }

    @Override
    protected int getAge(@Nonnull IBlockState state) {
        return state.getValue(getAgeProperty());
    }

    @Override
    public int getMaxAge() {
        return 5;
    }

    @Override
    public boolean isMaxAge(@Nonnull IBlockState state) {
        return getAge(state) == 5;
    }

    static {
        double pixel = 1D/16;
        double aP = pixel * 6;
        double bP = pixel * 10;
        double cP = 1D/6;

        BOUNDING_BOXES = new AxisAlignedBB[] {
                new AxisAlignedBB(aP, 0, aP, bP, cP, bP),
                new AxisAlignedBB(aP, 0, aP, bP, cP * 4, bP),
                new AxisAlignedBB(aP, 0, aP, bP, cP * 5, bP),
                new AxisAlignedBB(aP, 0, aP, bP, 1, bP)
        };
    }
}
