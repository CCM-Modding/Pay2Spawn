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
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;

import java.util.HashSet;

import static ccm.pay2spawn.util.Constants.CHANNEL_HANDSHAKE;

/**
 * Detect if the other side has this mod
 *
 * @author Dries007
 */
public class HandshakePacket
{
    private static final HashSet<String> playersWithHandshake = new HashSet<>();
    private static       boolean         serverHasMod         = false;

    private static final String HANDSHAKE_SERVER_TO_CLIENT = "HsS2C";
    private static final String HANDSHAKE_CLIENT_TO_SERVER = "HsC2S";
    private static final String HANDSHAKE_DEBUG = "debug";

    public static void sendDebugToPlayer(Player player)
    {
        PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(CHANNEL_HANDSHAKE, HANDSHAKE_DEBUG.getBytes()), player);
    }

    public static void sendHandshakeToPlayer(Player player)
    {
        playersWithHandshake.remove(((EntityPlayer) player).getEntityName());
        PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(CHANNEL_HANDSHAKE, HANDSHAKE_SERVER_TO_CLIENT.getBytes()), player);
    }

    public static void sendHandshakeToServer()
    {
        PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(CHANNEL_HANDSHAKE, HANDSHAKE_CLIENT_TO_SERVER.getBytes()));
    }

    public static void handel(Packet250CustomPayload packet, Player player)
    {
        String message = new String(packet.data);
        if (message.equals(HANDSHAKE_SERVER_TO_CLIENT))
        {
            sendHandshakeToServer();
            serverHasMod = true;
        }
        else if (message.equals(HANDSHAKE_CLIENT_TO_SERVER))
        {
            playersWithHandshake.add(((EntityPlayer) player).getEntityName());
            if (MinecraftServer.getServer().isDedicatedServer() && Pay2Spawn.getConfig().forceServerconfig) ConfigSyncPacket.sendToPlayer(player);
        }
        else if (message.equals(HANDSHAKE_DEBUG))
        {
            Pay2Spawn.debug = !Pay2Spawn.debug;
            ((EntityPlayer) player).sendChatToPlayer(ChatMessageComponent.createFromText("Debug now: " + Pay2Spawn.debug).setColor(EnumChatFormatting.RED));
        }
        else
        {
            Pay2Spawn.getLogger().severe("Invalid handshake received. Assuming no connection.");
        }
    }

    public static boolean doesServerHaveMod()
    {
        return serverHasMod;
    }

    public static boolean doesPlayerHaveMod(String username)
    {
        return playersWithHandshake.contains(username);
    }

    public static void resetServerStatus()
    {
        Pay2Spawn.debug = false;
        serverHasMod = false;
    }
}
