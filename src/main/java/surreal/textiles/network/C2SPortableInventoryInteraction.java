package surreal.textiles.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import surreal.textiles.ModConfig;
import surreal.textiles.event.InventoryInteractionHandler;

import java.util.List;

public class C2SPortableInventoryInteraction implements IMessage {

    private int windowId;
    private int slot;

    public C2SPortableInventoryInteraction() {}

    public C2SPortableInventoryInteraction(final int windowId, final int slot) {
        this.windowId = windowId;
        this.slot = slot;
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeByte(windowId).writeShort(slot);
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        windowId = buf.readByte();
        slot = buf.readShort();
    }

    public static class Handler implements IMessageHandler<C2SPortableInventoryInteraction, IMessage> {

        @Override
        public IMessage onMessage(final C2SPortableInventoryInteraction message, final MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (!ModConfig.sack.inventoryInteraction) return;
                final int slotIndex = message.slot;
                if (slotIndex < 0) return;
                final Container container = player.openContainer;
                if (container.windowId != message.windowId) return;
                final List<Slot> slots = container.inventorySlots;
                if (slotIndex >= slots.size()) return;
                final Slot slot = slots.get(slotIndex);
                if (InventoryInteractionHandler.handleSlotClick(player, slot, false) == EnumActionResult.SUCCESS) {
                    player.updateHeldItem();
                }
            });
            return null;
        }

    }

}
