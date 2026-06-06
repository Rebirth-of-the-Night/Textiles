package surreal.textiles.tiles;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.util.Constants;
import surreal.textiles.blocks.BlockSpindle;
import surreal.textiles.items.ItemBlockSpindle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TileSpindle extends TileEntity {

    // ensure that the spindle amount isn't invalid at initialization
    private final List<BlockSpindle.Type> spindles = Lists.newArrayList(BlockSpindle.Type.PLAIN);

    public List<BlockSpindle.Type> getSpindles() {
        return spindles;
    }

    public void placeSpindles(final EntityPlayer player, final ItemStack stack) {
        if (stack.isEmpty()) return; // failsafe
        final int amount;
        if (player.capabilities.isCreativeMode) {
            amount = player.isSneaking() ? BlockSpindle.MAX_AMOUNT : 1;
        } else {
            amount = player.isSneaking() ? Math.min(stack.getCount(), BlockSpindle.MAX_AMOUNT) : 1;
            stack.shrink(amount);
        }
        final BlockSpindle.Type type = BlockSpindle.Type.fromMeta(stack.getMetadata());
        spindles.clear();
        for (int i = 0; i < amount; i++) {
            spindles.add(type);
        }
        markDirty();

        world.playSound(player, pos, SoundEvents.BLOCK_CLOTH_PLACE, SoundCategory.BLOCKS, 1F, 0.8F);
        world.markBlockRangeForRenderUpdate(pos, pos);
    }

    public boolean handleInteraction(final EntityPlayer player, final EnumHand hand) {
        if (spindles.size() >= BlockSpindle.MAX_AMOUNT) return false;
        final ItemStack stack = player.getHeldItem(hand);
        if (!(stack.getItem() instanceof ItemBlockSpindle)) return false;

        if (!player.isCreative()) stack.shrink(1);
        spindles.add(BlockSpindle.Type.fromMeta(stack.getMetadata()));
        markDirty();

        world.playSound(player, pos, SoundEvents.BLOCK_CLOTH_PLACE, SoundCategory.BLOCKS, 1F, 0.8F);
        world.markBlockRangeForRenderUpdate(pos, pos);
        return true;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList spindlesTag = new NBTTagList();
        for (final BlockSpindle.Type spindle : spindles) {
            spindlesTag.appendTag(new NBTTagString(spindle.getName()));
        }
        compound.setTag("spindles", spindlesTag);
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        spindles.clear();
        if (compound.hasKey("spindles", Constants.NBT.TAG_LIST)) {
            final NBTTagList spindlesTag = compound.getTagList("spindles", Constants.NBT.TAG_STRING);
            for (int i = 0; i < spindlesTag.tagCount(); i++) {
                spindles.add(BlockSpindle.Type.fromName(spindlesTag.getStringTagAt(i)));
            }
        } else {
            spindles.add(BlockSpindle.Type.PLAIN); // failsafe
        }
        if (world != null && pos != null) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

}
