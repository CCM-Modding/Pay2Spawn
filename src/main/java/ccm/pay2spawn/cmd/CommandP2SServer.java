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

package ccm.pay2spawn.cmd;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.network.StatusMessage;
import ccm.pay2spawn.util.Constants;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.Arrays;
import java.util.List;

public class CommandP2SServer extends CommandBase
{
    static final String HELP = "OP only command, Server side.";

    @Override
    public String getCommandName()
    {
        return "pay2spawnserver";
    }

    @Override
    public List getCommandAliases()
    {
        return Arrays.asList("p2sserver");
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return HELP;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return MinecraftServer.getServer().getConfigurationManager().isPlayerOpped(sender.getCommandSenderName());
    }

    public void sendChatToPlayer(ICommandSender sender, String message, EnumChatFormatting chatFormatting)
    {
        sender.addChatMessage(new ChatComponentText(message).setChatStyle(new ChatStyle().setColor(chatFormatting)));
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if (args.length == 0)
        {
            sendChatToPlayer(sender, HELP, EnumChatFormatting.AQUA);
            sendChatToPlayer(sender, "Protip: Use tab completion!", EnumChatFormatting.AQUA);
            return;
        }
        switch (args[0])
        {
            case "butcher":
            {
                sendChatToPlayer(sender, "Removing all spawned entities...", EnumChatFormatting.YELLOW);
                int count = 0;
                for (WorldServer world : DimensionManager.getWorlds())
                {
                    for (Entity entity : (List<Entity>) world.loadedEntityList)
                    {
                        if (entity.getEntityData().hasKey(Constants.NAME))
                        {
                            count++;
                            entity.setDead();
                        }
                    }
                }
                sendChatToPlayer(sender, "Removed " + count + " entities.", EnumChatFormatting.GREEN);
                break;
            }
            case "reload":
                if (MinecraftServer.getServer().isDedicatedServer())
                {
                    try
                    {
                        Pay2Spawn.reloadDB_Server();
                    }
                    catch (Exception e)
                    {
                        sendChatToPlayer(sender, "RELOAD FAILED.", EnumChatFormatting.RED);
                        e.printStackTrace();
                    }
                }
                break;
            case "hasmod":
                if (args.length == 1) sendChatToPlayer(sender, "Use '/p2sserver hasmod <player>'.", EnumChatFormatting.RED);
                else sendChatToPlayer(sender, args[1] + (StatusMessage.doesPlayerHaveValidConfig(args[1]) ? " does " : " doesn't ") + "have P2S.", EnumChatFormatting.AQUA);
                break;
            default:
                sendChatToPlayer(sender, "Unknown command. Protip: Use tab completion!", EnumChatFormatting.RED);
                break;
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        switch (args.length)
        {
            case 1:
                return getListOfStringsMatchingLastWord(args, "reload", "hasmod", "butcher");
            case 2:
                switch (args[1])
                {
                    case "hasmod":
                        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
                }
        }
        return null;
    }
}
