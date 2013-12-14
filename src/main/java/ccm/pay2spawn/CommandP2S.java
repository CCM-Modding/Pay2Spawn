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

import ccm.pay2spawn.util.EventHandler;
import ccm.pay2spawn.util.JsonNBTHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

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
        if (args[0].equalsIgnoreCase("debug") && MinecraftServer.getServer().getConfigurationManager().isPlayerOpped(sender.getCommandSenderName()))
        {
            Pay2Spawn.debug = !Pay2Spawn.debug;
            sender.sendChatToPlayer(ChatMessageComponent.createFromText("Debug now: " + Pay2Spawn.debug).setColor(EnumChatFormatting.RED));
        }
        if (args[0].equalsIgnoreCase("getnbtofitem"))
        {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText("The nbt of the item you are holding:"));
            String text = JsonNBTHelper.parseNBT(player.inventory.getCurrentItem().writeToNBT(new NBTTagCompound())).toString();
            sender.sendChatToPlayer(ChatMessageComponent.createFromText(text));
        }
        if (args[0].equalsIgnoreCase("getnbtofentity"))
        {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText("You will get the nbt of the next entity you right click."));
            EventHandler.entitySet.add(player.getDisplayName());
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "debug", "getnbtofitem", "getnbtofentity");
        return null;
    }
}
