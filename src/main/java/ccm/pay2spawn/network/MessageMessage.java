package ccm.pay2spawn.network;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.util.Donation;
import ccm.pay2spawn.util.Reward;
import ccm.pay2spawn.util.Helper;
import com.google.common.base.Strings;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import static ccm.pay2spawn.util.Constants.GSON;

public class MessageMessage implements IMessage
{
    private Reward   reward;
    private Donation donation;

    public MessageMessage(Reward reward, Donation donation)
    {
        this.reward = reward;
        this.donation = donation;
    }

    public MessageMessage()
    {

    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        reward = GSON.fromJson(ByteBufUtils.readUTF8String(buf), Reward.class);
        donation = GSON.fromJson(ByteBufUtils.readUTF8String(buf), Donation.class);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, GSON.toJson(reward));
        ByteBufUtils.writeUTF8String(buf, GSON.toJson(donation));
    }

    public static class Handler implements IMessageHandler<MessageMessage, IMessage>
    {
        @Override
        public IMessage onMessage(MessageMessage message, MessageContext ctx)
        {
            if (ctx.side.isServer())
            {
                String format = Helper.formatColors(Pay2Spawn.getConfig().serverMessage);
                if (Strings.isNullOrEmpty(format)) return null;


                format = format.replace("$name", message.donation.username);
                format = format.replace("$amount", message.donation.amount + "");
                format = format.replace("$note", message.donation.note);
                format = format.replace("$streamer", ctx.getServerHandler().playerEntity.getDisplayName());
                format = format.replace("$reward_message", message.reward.getMessage());
                format = format.replace("$reward_name", message.reward.getName());
                format = format.replace("$reward_amount", message.reward.getAmount() + "");
                format = format.replace("$reward_countdown", message.reward.getCountdown() + "");

                MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(format));
            }

            return null;
        }
    }
}
