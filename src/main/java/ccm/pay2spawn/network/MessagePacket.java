package ccm.pay2spawn.network;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.misc.Reward;
import ccm.pay2spawn.util.Helper;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import static ccm.pay2spawn.util.Constants.*;

public class MessagePacket extends AbstractPacket
{
    private JsonObject data;

    public MessagePacket()
    {

    }

    public MessagePacket(Reward reward, JsonObject donation)
    {
        data = donation;
        if (Minecraft.getMinecraft().thePlayer != null) data.addProperty("streamer", Minecraft.getMinecraft().thePlayer.getCommandSenderName());
        if (reward != null)
        {
            data.addProperty("reward_message", reward.getMessage());
            data.addProperty("reward_name", reward.getName());
            data.addProperty("reward_amount", reward.getAmount());
            data.addProperty("reward_countdown", reward.getCountdown());
        }
        else Pay2Spawn.getLogger().warn("Reward was null when sending message?! Please report how this happened.\n" + data.toString());
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, GSON.toJson(data));
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        data = JSON_PARSER.parse(ByteBufUtils.readUTF8String(buffer)).getAsJsonObject();
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

        data = Helper.filter(data);
        if (data.has(DONATION_USERNAME)) format = format.replace("$name", data.get(DONATION_USERNAME).getAsString());
        if (data.has(DONATION_AMOUNT)) format = format.replace("$amount", data.get(DONATION_AMOUNT).getAsString());
        if (data.has(DONATION_NOTE)) format = format.replace("$note", data.get(DONATION_NOTE).getAsString());
        if (data.has("streamer")) format = format.replace("$streamer", data.get("streamer").getAsString());
        if (data.has("reward_message")) format = format.replace("$reward_message", data.get("reward_message").getAsString());
        if (data.has("reward_name")) format = format.replace("$reward_name", data.get("reward_name").getAsString());
        if (data.has("reward_amount")) format = format.replace("$reward_amount", data.get("reward_amount").getAsString());
        if (data.has("reward_countdown")) format = format.replace("$reward_countdown", data.get("reward_countdown").getAsString());

        MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(format));
    }
}
