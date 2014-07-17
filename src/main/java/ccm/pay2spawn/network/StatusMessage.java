package ccm.pay2spawn.network;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.configurator.ConfiguratorManager;
import ccm.pay2spawn.permissions.PermissionsHandler;
import ccm.pay2spawn.util.Helper;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

import java.util.HashSet;

public class StatusMessage implements IMessage
{
    private static final HashSet<String> playersWithValidConfig = new HashSet<>();
    public static String serverConfig;
    private static boolean serverHasMod = false;
    private Type     type;
    private String[] extraData;

    public StatusMessage(Type type, String... extraData)
    {
        this.type = type;
        this.extraData = extraData;
    }

    public StatusMessage()
    {

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
        Pay2Spawn.getSnw().sendTo(new StatusMessage(Type.HANDSHAKE), player);
    }

    private static void sendForceToPlayer(EntityPlayerMP player)
    {
        Pay2Spawn.getSnw().sendTo(new StatusMessage(Type.FORCE), player);
    }

    private static void sendConfigToPlayer(EntityPlayerMP player)
    {
        Pay2Spawn.getSnw().sendTo(new StatusMessage(Type.CONFIGSYNC, serverConfig), player);
    }

    public static void sendConfigToAllPlayers()
    {
        Pay2Spawn.getSnw().sendToAll(new StatusMessage(Type.CONFIGSYNC, serverConfig));
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        type = Type.values()[buf.readInt()];
        extraData = new String[buf.readInt()];
        for (int i = 0; i < extraData.length; i++) extraData[i] = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(type.ordinal());
        buf.writeInt(extraData.length);
        for (String s : extraData) ByteBufUtils.writeUTF8String(buf, s);
    }

    public static enum Type
    {
        HANDSHAKE,
        CONFIGSYNC,
        FORCE,
        STATUS
    }

    public static class Handler implements IMessageHandler<StatusMessage, IMessage>
    {
        @Override
        public IMessage onMessage(StatusMessage message, MessageContext ctx)
        {
            if (ctx.side.isClient())
            {
                switch (message.type)
                {
                    case HANDSHAKE:
                        serverHasMod = true;
                        return new StatusMessage(Type.HANDSHAKE);
                    case CONFIGSYNC:
                        Pay2Spawn.reloadDBFromServer(message.extraData[0]);
                        ConfiguratorManager.exit();
                        Helper.msg(EnumChatFormatting.GOLD + "[P2S] Using config specified by the server.");
                        break;
                    case FORCE:
                        Pay2Spawn.forceOn = true;
                        break;
                    case STATUS:
                        return new StatusMessage(Type.STATUS, message.extraData[0], Boolean.toString(Pay2Spawn.enable));
                }
            }
            else
            {
                switch (message.type)
                {
                    case HANDSHAKE:
                        PermissionsHandler.getDB().newPlayer(ctx.getServerHandler().playerEntity.getCommandSenderName());
                        playersWithValidConfig.add(ctx.getServerHandler().playerEntity.getCommandSenderName());
                        // Can't use return statement here cause you can't return multiple packets
                        if (MinecraftServer.getServer().isDedicatedServer() && Pay2Spawn.getConfig().forceServerconfig) sendConfigToPlayer(ctx.getServerHandler().playerEntity);
                        if (MinecraftServer.getServer().isDedicatedServer() && Pay2Spawn.getConfig().forceP2S) sendForceToPlayer(ctx.getServerHandler().playerEntity);
                        break;
                    case CONFIGSYNC:
                        // Noop
                        break;
                    case FORCE:
                        // Noop
                        break;
                    case STATUS:
                        EntityPlayer sender = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(message.extraData[0]);
                        Helper.sendChatToPlayer(sender, ctx.getServerHandler().playerEntity.getCommandSenderName() + " has Pay2Spawn " + (Boolean.parseBoolean(message.extraData[1]) ? "enabled." : "disabled."), EnumChatFormatting.AQUA);
                        break;
                }
            }
            return null;
        }
    }
}
