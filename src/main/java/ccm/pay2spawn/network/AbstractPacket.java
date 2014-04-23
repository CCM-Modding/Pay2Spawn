package ccm.pay2spawn.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;

/**
 * AbstractPacket class. Should be the parent of all packets wishing to use the PacketPipeline.
 *
 * @author sirgingalot
 *         http://www.minecraftforge.net/wiki/Netty_Packet_Handling
 */
public abstract class AbstractPacket
{
    public AbstractPacket() {}

    public abstract void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer);

    public abstract void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer);

    public abstract void handleClientSide(EntityPlayer player);

    public abstract void handleServerSide(EntityPlayer player);
}
