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
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

import java.util.Timer;
import java.util.TimerTask;

import static ccm.pay2spawn.util.Constants.NAME;

public class ConnectionHandler
{
    public static final ConnectionHandler INSTANCE = new ConnectionHandler();

    private ConnectionHandler()
    {
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.player instanceof EntityPlayerMP) // Cheap server detection
            StatusMessage.sendHandshakeToPlayer((EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public void connectionReceived(FMLNetworkEvent.ServerConnectionFromClientEvent event)
    {
        if (!event.isLocal && Pay2Spawn.getConfig().forceP2S)
        {
            final String username = ((NetHandlerPlayServer) event.handler).playerEntity.getCommandSenderName();
            new Timer().schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    if (!StatusMessage.doesPlayerHaveValidConfig(username)) MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(username).playerNetServerHandler.kickPlayerFromServer("Pay2Spawn is required on this server.\nIt needs to be configured properly.");
                }
            }, 5 * 1000);
        }
    }

    @SubscribeEvent
    public void disconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        Pay2Spawn.reloadDB();
        StatusMessage.resetServerStatus();
        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                if (!StatusMessage.doesServerHaveMod()) Helper.msg(EnumChatFormatting.RED + NAME + " isn't on the server. No rewards will spawn!");
            }
        }, 5 * 1000);
    }
}
