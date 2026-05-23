package surreal.textiles.items;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import surreal.textiles.blocks.BlockSack;
import surreal.textiles.tiles.TileSack;
import surreal.textiles.util.ReadOnlyItemBlockInventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ItemBlockSack extends ItemBlockBase {

    public ItemBlockSack(final BlockSack block) {
        super(block);
        setMaxStackSize(1);
        setHasSubtypes(true);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, @Nullable final NBTTagCompound nbt) {
        return new ReadOnlyItemBlockInventory(stack, TileSack.getConfiguredSize());
    }

    @Override
    public void registerModels() {
        final ResourceLocation regName = Objects.requireNonNull(getRegistryName());
        ModelLoader.setCustomModelResourceLocation(
                this, 0, new ModelResourceLocation(regName, "dyed=false,open=false"));
        ModelLoader.setCustomModelResourceLocation(
                this, 1, new ModelResourceLocation(regName, "dyed=true,open=false"));
    }

    @Nonnull
    @Override
    public String getTranslationKey(@Nonnull final ItemStack stack) {
        return block.getTranslationKey();
    }

    @Override
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos,
                                           final EnumFacing side, final float hitX, final float hitY, final float hitZ,
                                           final EnumHand hand) {
        final ItemStack stack = player.getHeldItem(hand);
        if (stack.getMetadata() == 0) return EnumActionResult.PASS;
        final IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockCauldron cauldronBlock)) return EnumActionResult.PASS;
        final int waterLevel = state.getValue(BlockCauldron.LEVEL);
        if (waterLevel <= 0) return EnumActionResult.PASS;
        if (!world.isRemote) {
            stack.setItemDamage(0);
            final NBTTagCompound stackData = stack.getTagCompound();
            if (stackData != null && stackData.hasKey("BlockEntityTag", Constants.NBT.TAG_COMPOUND)) {
                stackData.getCompoundTag("BlockEntityTag").removeTag("dye");
            }
            if (!player.capabilities.isCreativeMode) {
                cauldronBlock.setWaterLevel(world, pos, state, waterLevel - 1);
            }
        }
        return EnumActionResult.SUCCESS;
    }

    public static int getDyeColorForStack(final ItemStack stack) {
        if (stack.getMetadata() != 1) return -1;
        final NBTTagCompound stackData = stack.getTagCompound();
        if (stackData != null && stackData.hasKey("BlockEntityTag", Constants.NBT.TAG_COMPOUND)) {
            final NBTTagCompound tileData = stackData.getCompoundTag("BlockEntityTag");
            if (tileData.hasKey("dye", Constants.NBT.TAG_INT)) {
                return tileData.getInteger("dye");
            }
        }
        return -1;
    }

    public static void setDyeColorForStack(final ItemStack stack, final int col) {
        NBTTagCompound stackData = stack.getTagCompound();
        if (stackData == null) {
            stackData = new NBTTagCompound();
            stack.setTagCompound(stackData);
        }
        final NBTTagCompound tileData;
        if (stackData.hasKey("BlockEntityTag", Constants.NBT.TAG_COMPOUND)) {
            tileData = stackData.getCompoundTag("BlockEntityTag");
        } else {
            tileData = new NBTTagCompound();
            stackData.setTag("BlockEntityTag", tileData);
        }
        tileData.setInteger("dye", col);
    }

}
