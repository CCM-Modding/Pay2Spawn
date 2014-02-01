/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Dries K. Aka Dries007 and the CCM modding crew.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ccm.pay2spawn.network;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.configurator.ConfiguratorManager;
import ccm.pay2spawn.permissions.PermissionsHandler;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.Statistics;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;

import java.io.*;
import java.util.HashSet;

import static ccm.pay2spawn.util.Constants.CHANNEL_STATUS;

public class StatusPacket
{
    public static String serverConfig;
    private static final HashSet<String> playersWithValidConfig = new HashSet<>();
    private static       boolean         serverHasMod           = false;

    private static final byte HANDSHAKE  = 0;
    private static final byte CONFIGSYNC = 1;
    private static final byte FORCE      = 2;
    private static final byte STATUS     = 3;
    private static final byte DEATH      = 4;

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

    public static void reconstruct(Packet250CustomPayload packet, Player player)
    {
        ByteArrayInputStream streambyte = new ByteArrayInputStream(packet.data);
        DataInputStream stream = new DataInputStream(streambyte);

        try
        {
            switch (stream.readByte())
            {
                case HANDSHAKE:
                    if (FMLCommonHandler.instance().getEffectiveSide().isClient())
                    {
                        sendHandshakeToServer();
                        serverHasMod = true;
                    }
                    else
                    {
                        PermissionsHandler.getDB().newPlayer(((EntityPlayer) player).getEntityName());
                        if (stream.readBoolean()) playersWithValidConfig.add(((EntityPlayer) player).getEntityName());
                        if (MinecraftServer.getServer().isDedicatedServer() && Pay2Spawn.getConfig().forceServerconfig) StatusPacket.sendConfigToPlayer(player);
                        if (MinecraftServer.getServer().isDedicatedServer() && Pay2Spawn.getConfig().forceP2S) StatusPacket.sendForceToPlayer(player);
                    }
                    break;
                case CONFIGSYNC:
                    if (FMLCommonHandler.instance().getEffectiveSide().isClient())
                    {
                        Pay2Spawn.reloadDBFromServer(stream.readUTF());
                        ConfiguratorManager.exit();
                        Helper.msg(EnumChatFormatting.GOLD + "[P2S] Using config specified by the server.");
                    }
                    break;
                case FORCE:
                    if (FMLCommonHandler.instance().getEffectiveSide().isClient())
                    {
                        Pay2Spawn.forceOn = true;
                    }
                    break;
                case STATUS:
                    if (FMLCommonHandler.instance().getEffectiveSide().isClient()) sendStatusToServer(stream.readUTF());
                    else
                    {
                        EntityPlayer sender = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(stream.readUTF());
                        String message = ((EntityPlayer) player).getEntityName() + " has Pay2Spawn " + (stream.readBoolean() ? "enabled." : "disabled.");
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText(message).setColor(EnumChatFormatting.AQUA));
                    }
                    break;
                case DEATH:
                    Statistics.handleKill(Helper.readNBTTagCompound(stream));
                    break;
            }
            stream.close();
            streambyte.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void sendForceToPlayer(Player player)
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeByte(FORCE);
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(CHANNEL_STATUS, streambyte.toByteArray()), player);
    }

    private static void sendStatusToServer(String sender)
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeByte(STATUS);
            stream.writeBoolean(Pay2Spawn.enable);
            stream.writeUTF(sender);
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(CHANNEL_STATUS, streambyte.toByteArray()));
    }

    public static void sendHandshakeToPlayer(Player player)
    {
        playersWithValidConfig.remove(((EntityPlayer) player).getEntityName());
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeByte(HANDSHAKE);
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(CHANNEL_STATUS, streambyte.toByteArray()), player);
    }

    public static void sendHandshakeToServer()
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeByte(HANDSHAKE);
            stream.writeBoolean(Pay2Spawn.isConfiguredProperly());
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(CHANNEL_STATUS, streambyte.toByteArray()));
    }

    public static void sendConfigToAllPlayers()
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeByte(CONFIGSYNC);
            stream.writeUTF(serverConfig);
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        PacketDispatcher.sendPacketToAllPlayers(PacketDispatcher.getPacket(CHANNEL_STATUS, streambyte.toByteArray()));
    }

    public static void sendConfigToPlayer(Player player)
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeByte(CONFIGSYNC);
            stream.writeUTF(serverConfig);
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(CHANNEL_STATUS, streambyte.toByteArray()), player);
    }

    public static void sendKillDataToClient(EntityPlayer entity, NBTTagCompound data)
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeByte(DEATH);
            Helper.writeNBTTagCompound(data, stream);
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(CHANNEL_STATUS, streambyte.toByteArray()), (Player) entity);
    }
}
