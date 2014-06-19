package ccm.pay2spawn.network;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.misc.Donation;
import ccm.pay2spawn.misc.Reward;
import ccm.pay2spawn.util.Helper;
import com.google.common.base.Strings;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import static ccm.pay2spawn.util.Constants.GSON;

public class MessagePacket extends AbstractPacket
{
    private Reward   reward;
    private Donation donation;

    public MessagePacket()
    {

    }

    public MessagePacket(Reward reward, Donation donation)
    {
        this.reward = reward;
        this.donation = donation;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, GSON.toJson(reward));
        ByteBufUtils.writeUTF8String(buffer, GSON.toJson(donation));
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        reward = GSON.fromJson(ByteBufUtils.readUTF8String(buffer), Reward.class);
        donation = GSON.fromJson(ByteBufUtils.readUTF8String(buffer), Donation.class);
    }

    @Override
    public void handleClientSide(EntityPlayer player)
    {

    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
        String format = Helper.formatColors(Pay2Spawn.getConfig().serverMessage);
        if (Strings.isNullOrEmpty(format)) return;


        format = format.replace("$name", donation.username);
        format = format.replace("$amount", donation.amount + "");
        format = format.replace("$note", donation.note);
        format = format.replace("$streamer", player.getDisplayName());
        format = format.replace("$reward_message", reward.getMessage());
        format = format.replace("$reward_name", reward.getName());
        format = format.replace("$reward_amount", reward.getAmount() + "");
        format = format.replace("$reward_countdown", reward.getCountdown() + "");

        MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(format));
    }
}
