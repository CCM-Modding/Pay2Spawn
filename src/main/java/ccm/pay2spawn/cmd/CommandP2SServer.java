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
import ccm.pay2spawn.network.StatusPacket;
import ccm.pay2spawn.util.Constants;
import ccm.pay2spawn.util.Helper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
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

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if (args.length == 0)
        {
            Helper.msg(EnumChatFormatting.AQUA + HELP);
            Helper.msg(EnumChatFormatting.AQUA + "Protip: Use tab completion!");
            return;
        }
        switch (args[0])
        {
            case "butcher":
            {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText("Removing all spawned entities...").setColor(EnumChatFormatting.YELLOW));
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
                sender.sendChatToPlayer(ChatMessageComponent.createFromText("Removed " + count + " entities.").setColor(EnumChatFormatting.GREEN));
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
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText("RELOAD FAILED.").setColor(EnumChatFormatting.RED));
                        e.printStackTrace();
                    }
                }
                break;
            case "hasmod":
                if (args.length == 1) sender.sendChatToPlayer(ChatMessageComponent.createFromText("Use '/p2sserver hasmod <player>'.").setColor(EnumChatFormatting.RED));
                else sender.sendChatToPlayer(ChatMessageComponent.createFromText(args[2] + (StatusPacket.doesPlayerHaveValidConfig(args[2]) ? " does " : " doesn't ") + "have P2S.").setColor(EnumChatFormatting.AQUA));
                break;
            default:
                Helper.msg(EnumChatFormatting.RED + "Unknown command. Protip: Use tab completion!");
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
