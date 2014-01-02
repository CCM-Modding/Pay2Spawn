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
import ccm.pay2spawn.util.Helper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
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
    static final String HELP = "Use command to control P2S.";

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
            sender.sendChatToPlayer(ChatMessageComponent.createFromText("Use serverside command 'P2SPermissions' for all things related to permissions."));
            return;
        }
        switch (args[0])
        {
            case "reload":
                if (Pay2Spawn.getRewardsDB().editable) Pay2Spawn.reloadDB();
                else Helper.msg(EnumChatFormatting.RED + "[P2S] You can't do that with a server side config.");
                break;
            case "configure":
                if (Pay2Spawn.getRewardsDB().editable) ConfiguratorManager.openCfg();
                else Helper.msg(EnumChatFormatting.RED + "[P2S] You can't do that with a server side config.");
                break;
            case "getnbt":
                ConfiguratorManager.openNbt();
                break;
            case "off":
                Pay2Spawn.enable = false;
                break;
            case "on":
                Pay2Spawn.enable = true;
                break;
            case "donate":
                if (args.length == 1) player.sendChatToPlayer(ChatMessageComponent.createFromText("Use '/p2s donate <amount>'."));
                else
                {
                    double amount = CommandBase.parseDouble(sender, args[1]);
                    DonationCheckerThread.fakeDonation(amount);
                }
                break;
            case "redonate":
                if (args.length == 1) player.sendChatToPlayer(ChatMessageComponent.createFromText("Use '/p2s redonate <1-5>' to redo one of the last 5 donations."));
                else
                {
                    int id = CommandBase.parseIntBounded(sender, args[1], 1, 5) - 1;
                    DonationCheckerThread.redonate(id);
                }
                break;

            default:
                sender.sendChatToPlayer(ChatMessageComponent.createFromText("Unknown command. Protip: Use tab completion!"));
                break;
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "reload", "configure", "getnbt", "off", "on", "donate", "redonate", "permissions");
        return null;


    }
}
