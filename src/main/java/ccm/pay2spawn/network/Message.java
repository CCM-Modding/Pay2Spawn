package ccm.pay2spawn.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * This is a copy paste class :p
 *
 * Converted from old system on CPW's recommendation. The old thing had memory leaks -_-
 *
 * @author Dries007
 */
public class Message implements IMessage
{
    @Override
    public void fromBytes(ByteBuf buf)
    {

    }

    @Override
    public void toBytes(ByteBuf buf)
    {

    }

    public static class Handler implements IMessageHandler<Message, IMessage>
    {
        @Override
        public IMessage onMessage(Message message, MessageContext ctx)
        {
            return null;
        }
    }
}
