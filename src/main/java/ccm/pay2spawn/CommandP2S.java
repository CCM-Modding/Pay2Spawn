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

package ccm.pay2spawn;

import ccm.pay2spawn.configurator.ConfiguratorManager;
import ccm.pay2spawn.network.HandshakePacket;
import ccm.pay2spawn.network.RedonatePacket;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

/**
 * Useful command when dealing with setting up the JSON file
 * Can get an entities/items JSONified NBT
 * Can reload the JSON file
 *
 * @author Dries007
 */
public class CommandP2S extends CommandBase
{
    static final String HELP = "Use command to capture custom things.";

    @Override
    public String getCommandName()
    {
        return "pay2spawn";
    }

    @Override
    public List getCommandAliases()
    {
        return Arrays.asList("p2s");
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return sender instanceof EntityPlayer;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return HELP;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        EntityPlayer player = (EntityPlayer) sender;
        if (args.length == 0)
        {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText(HELP));
            sender.sendChatToPlayer(ChatMessageComponent.createFromText("Protip: Use tab completion!"));
            return;
        }
        else if (args[0].equalsIgnoreCase("debug"))
        {
            if (MinecraftServer.getServer().getConfigurationManager().isPlayerOpped(sender.getCommandSenderName())) HandshakePacket.sendDebugToPlayer((Player) player);
            else sender.sendChatToPlayer(ChatMessageComponent.createFromText("You have to be OP to enable debug mode.").setColor(EnumChatFormatting.RED));
        }
        else if (args[0].equalsIgnoreCase("reload"))
        {
            if (Pay2Spawn.getConfig().forceServerconfig && FMLCommonHandler.instance().getSide().isServer())
            {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText("[P2S] You can't do that with forced server configs. Reboot the server.").setColor(EnumChatFormatting.RED));
            }
            else
            {
                HandshakePacket.reload(player);
            }
        }
        else if (args[0].equalsIgnoreCase("configure"))
        {
            ConfiguratorManager.handleCommand(player);
        }
        else if (args[0].equalsIgnoreCase("off"))
        {
            HandshakePacket.toggle(player, false);
        }
        else if (args[0].equalsIgnoreCase("on"))
        {
            HandshakePacket.toggle(player, true);
        }
        else if (args[0].equalsIgnoreCase("donate"))
        {
            if (args.length == 1)
            {
                player.sendChatToPlayer(ChatMessageComponent.createFromText("Use '/p2s donate <amount>'."));
            }
            else
            {
                double amount = CommandBase.parseDouble(sender, args[1]);
                RedonatePacket.send((Player) player, amount);
            }
        }
        else if (args[0].equalsIgnoreCase("redonate"))
        {
            if (args.length == 1)
            {
                player.sendChatToPlayer(ChatMessageComponent.createFromText("Use '/p2s redonate <1-5>' to redo one of the last 5 donations."));
            }
            else
            {
                int amount = CommandBase.parseIntBounded(sender, args[1], 1, 5) - 1;
                RedonatePacket.send((Player) player, amount);
            }
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "debug", "getnbtofitem", "getnbtofentity", "reload", "configure", "off", "on", "donate", "redonate");
        if (args.length == 2 && args[0].equalsIgnoreCase("donate")) return getListOfStringsMatchingLastWord(args, "");
        return null;
    }
}
