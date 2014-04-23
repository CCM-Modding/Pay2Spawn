package ccm.pay2spawn.network;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.configurator.ConfiguratorManager;
import ccm.pay2spawn.permissions.PermissionsHandler;
import ccm.pay2spawn.util.Helper;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

import java.util.HashSet;

public class StatusPacket extends AbstractPacket
{
    private static final HashSet<String> playersWithValidConfig = new HashSet<>();
    public static String serverConfig;
    private static boolean serverHasMod = false;
    private Type     type;
    private String[] extraData;

    public StatusPacket(Type type, String... extraData)
    {
        this.type = type;
        this.extraData = extraData;
    }

    public static boolean doesServerHaveMod()
    {
        return serverHasMod;
    }

    public static boolean doesPlayerHaveValidConfig(String username)
    {
        return playersWithValidConfig.contains(username);
    }

    public static void resetServerStatus()
    {
        Pay2Spawn.enable = true;
        Pay2Spawn.forceOn = false;
        serverHasMod = false;
    }

    public static void sendHandshakeToPlayer(EntityPlayerMP player)
    {
        PacketPipeline.PIPELINE.sendTo(new StatusPacket(Type.HANDSHAKE), player);
    }

    private static void sendForceToPlayer(EntityPlayerMP player)
    {
        PacketPipeline.PIPELINE.sendTo(new StatusPacket(Type.FORCE), player);
    }

    private static void sendConfigToPlayer(EntityPlayerMP player)
    {
        PacketPipeline.PIPELINE.sendTo(new StatusPacket(Type.CONFIGSYNC, serverConfig), player);
    }

    public static void sendConfigToAllPlayers()
    {
        PacketPipeline.PIPELINE.sendToAll(new StatusPacket(Type.CONFIGSYNC, serverConfig));
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        buffer.writeInt(type.ordinal());
        buffer.writeInt(extraData.length);
        for (String s : extraData) ByteBufUtils.writeUTF8String(buffer, s);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        type = Type.values()[buffer.readInt()];
        extraData = new String[buffer.readInt()];
        for (int i = 0; i < extraData.length; i++) extraData[i] = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void handleClientSide(EntityPlayer player)
    {
        switch (type)
        {
            case HANDSHAKE:
                sendHandshakeToServer();
                serverHasMod = true;
                break;
            case CONFIGSYNC:
                Pay2Spawn.reloadDBFromServer(extraData[0]);
                ConfiguratorManager.exit();
                Helper.msg(EnumChatFormatting.GOLD + "[P2S] Using config specified by the server.");
                break;
            case FORCE:
                Pay2Spawn.forceOn = true;
                break;
            case STATUS:
                sendStatusToServer();
                break;
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
        switch (type)
        {
            case HANDSHAKE:
                PermissionsHandler.getDB().newPlayer(player.getCommandSenderName());
                playersWithValidConfig.add(player.getCommandSenderName());
                if (MinecraftServer.getServer().isDedicatedServer() && Pay2Spawn.getConfig().forceServerconfig) StatusPacket.sendConfigToPlayer((EntityPlayerMP) player);
                if (MinecraftServer.getServer().isDedicatedServer() && Pay2Spawn.getConfig().forceP2S) StatusPacket.sendForceToPlayer((EntityPlayerMP) player);
                break;
            case CONFIGSYNC:
                // Noop
                break;
            case FORCE:
                // Noop
                break;
            case STATUS:
                EntityPlayer sender = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(extraData[0]);
                Helper.sendChatToPlayer(sender, player.getCommandSenderName() + " has Pay2Spawn " + (Boolean.parseBoolean(extraData[1]) ? "enabled." : "disabled."), EnumChatFormatting.AQUA);
                break;
        }
    }

    private void sendHandshakeToServer()
    {
        PacketPipeline.PIPELINE.sendToServer(new StatusPacket(Type.HANDSHAKE));
    }

    private void sendStatusToServer()
    {
        PacketPipeline.PIPELINE.sendToServer(new StatusPacket(Type.STATUS, extraData[0], Boolean.toString(Pay2Spawn.enable)));
    }

    public static enum Type
    {
        HANDSHAKE,
        CONFIGSYNC,
        FORCE,
        STATUS
    }
}
