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
import ccm.pay2spawn.util.Helper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

import java.util.Timer;
import java.util.TimerTask;

import static ccm.pay2spawn.util.Constants.NAME;

public class ConnectionHandler implements IConnectionHandler
{
    @Override
    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
    {
        StatusPacket.sendHandshakeToPlayer(player);
    }

    @Override
    public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
    {
        if (MinecraftServer.getServer().isDedicatedServer() && Pay2Spawn.getConfig().forceP2S)
        {
            final String username = netHandler.clientUsername;
            new Timer().schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    if (!StatusPacket.doesPlayerHaveValidConfig(username)) MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(username).playerNetServerHandler.kickPlayerFromServer("Pay2Spawn is required on this server.\nIt needs to be configured properly.");
                }
            }, 5 * 1000);
        }
        return null;
    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager)
    {

    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager)
    {

    }

    @Override
    public void connectionClosed(INetworkManager manager)
    {
        Pay2Spawn.getLogger().severe("connectionClosed " + FMLCommonHandler.instance().getEffectiveSide());
    }

    @Override
    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
    {
        Pay2Spawn.reloadDB();
        StatusPacket.resetServerStatus();
        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                if (!StatusPacket.doesServerHaveMod()) Helper.msg(EnumChatFormatting.RED + NAME + " isn't on the server. No rewards will spawn!");
            }
        }, 5 * 1000);
    }
}
