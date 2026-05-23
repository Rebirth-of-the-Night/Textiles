package surreal.textiles.client.guis;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import surreal.textiles.tiles.TileBasket;
import surreal.textiles.tiles.TileSack;
import surreal.textiles.tiles.containers.ContainerBasket;
import surreal.textiles.tiles.containers.ContainerSack;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileBasket basket) {
            return new ContainerBasket(basket, player);
        } else if (te instanceof TileSack sack) {
            return new ContainerSack(sack, player);
        }

        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileBasket basket) {
            return new GuiBasket(basket);
        } else if (te instanceof TileSack sack) {
            return new GuiSack(sack);
        }

        return null;
    }
}
