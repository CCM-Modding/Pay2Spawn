package ccm.pay2spawn.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class KilldataMessage implements IMessage
{
    private NBTTagCompound data;

    public KilldataMessage(NBTTagCompound data)
    {
        this.data = data;
    }

    public KilldataMessage()
    {

    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, data);
    }

    public static class Handler implements IMessageHandler<KilldataMessage, IMessage>
    {
        @Override
        public IMessage onMessage(KilldataMessage message, MessageContext ctx)
        {
            //TODO: Make this work
            return null;
        }
    }
}
