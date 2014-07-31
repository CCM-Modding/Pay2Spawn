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

import ccm.pay2spawn.permissions.Group;
import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.permissions.PermissionsHandler;
import ccm.pay2spawn.permissions.Player;
import ccm.pay2spawn.util.Helper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

/**
 * The main permission system command
 *
 * @author Dries007
 */
public class CommandP2SPermissions extends CommandBase
{
    static final String HELP = "Use client side command 'p2s' for non permissions stuff.";

    @Override
    public String getCommandName()
    {
        return "p2spermissions";
    }

    @Override
    public List getCommandAliases()
    {
        return Arrays.asList("p2sperm");
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
            Helper.sendChatToPlayer(sender, "Use '/p2s perm group|groups|player' for more info.", EnumChatFormatting.RED);
            return;
        }
        switch (args[0])
        {
            case "groups":
                if (args.length < 3)
                {
                    Helper.sendChatToPlayer(sender, "Use '/p2s perm groups add|remove <name> [parent group]' to add or remove a group.", EnumChatFormatting.RED);
                    break;
                }
                else
                {
                    String name = args[2];
                    switch (args[1])
                    {
                        case "add":
                            String parent = args.length == 4 ? args[3] : null;
                            PermissionsHandler.getDB().newGroup(name, parent);
                            Helper.sendChatToPlayer(sender, "Added new group named '" + name + (parent != null ? "' with parent group '" + parent : "") + "'.", EnumChatFormatting.GOLD);
                            break;
                        case "remove":
                            PermissionsHandler.getDB().remove(name);
                            Helper.sendChatToPlayer(sender, "Removed group named '" + name + "'", EnumChatFormatting.GOLD);
                            break;
                    }
                }
                break;
            case "group":
                if (args.length < 4)
                {
                    Helper.sendChatToPlayer(sender, "Use '/p2s perm group <name> add|remove <node>' OR '<name> parent set|clear [name]'", EnumChatFormatting.RED);
                    break;
                }
                else
                {
                    Group group = PermissionsHandler.getDB().getGroup(args[1]);
                    if (group == null)
                    {
                        Helper.sendChatToPlayer(sender, "The group doesn't exist.", EnumChatFormatting.RED);
                        break;
                    }
                    switch (args[2])
                    {
                        case "parent":
                            switch (args[3])
                            {
                                case "set":
                                    if (args.length != 5)
                                    {
                                        Helper.sendChatToPlayer(sender, "Use 'parent set <name>.", EnumChatFormatting.RED);
                                        return;
                                    }
                                    group.setParent(args[4]);
                                    Helper.sendChatToPlayer(sender, "Set parent to: " + args[4], EnumChatFormatting.GOLD);
                                    break;
                                case "clear":
                                    group.setParent(null);
                                    Helper.sendChatToPlayer(sender, "Cleared parent group.", EnumChatFormatting.GOLD);
                                    break;
                            }
                            break;
                        case "add":
                            group.addNode(args[3]);
                            Helper.sendChatToPlayer(sender, "Added node: " + args[3], EnumChatFormatting.GOLD);
                            break;
                        case "remove":
                            if (group.removeNode(args[3])) Helper.sendChatToPlayer(sender, "Removed node: " + args[3], EnumChatFormatting.GOLD);
                            else Helper.sendChatToPlayer(sender, "Node not removed, it wasn't there in the first place...", EnumChatFormatting.RED);
                            break;
                    }
                }
                break;
            case "player":
                if (args.length < 5)
                {
                    Helper.sendChatToPlayer(sender, "Use '/p2s perm player <name> group add|remove <group>' OR '<name> perm add|remove <node>'", EnumChatFormatting.RED);
                    break;
                }
                else
                {
                    Player playero = PermissionsHandler.getDB().getPlayer(args[1]);
                    if (playero == null)
                    {
                        Helper.sendChatToPlayer(sender, "That player doesn't exist.", EnumChatFormatting.RED);
                        break;
                    }
                    switch (args[2])
                    {
                        case "group":
                            switch (args[3])
                            {
                                case "add":
                                    playero.addGroup(args[4]);
                                    break;
                                case "remove":
                                    if (playero.removeGroup(args[4])) Helper.sendChatToPlayer(sender, "Removed group: " + args[4], EnumChatFormatting.GOLD);
                                    else Helper.sendChatToPlayer(sender, "Group not removed, it wasn't there in the first place...", EnumChatFormatting.RED);
                                    break;
                            }
                            break;
                        case "perm":
                            switch (args[3])
                            {
                                case "add":
                                    playero.addNode(new Node(args[4]));
                                    break;
                                case "remove":
                                    if (playero.removeNode(new Node(args[4]))) Helper.sendChatToPlayer(sender, "Added per node: " + args[4], EnumChatFormatting.GOLD);
                                    else Helper.sendChatToPlayer(sender, "Perm node not removed, it wasn't there in the first place...", EnumChatFormatting.RED);
                                    break;
                            }
                            break;
                    }
                }
                break;
        }
        PermissionsHandler.getDB().save();
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        switch (args.length)
        {
            case 1:
                return getListOfStringsMatchingLastWord(args, "groups", "group", "player");
            case 2:
                switch (args[0])
                {
                    case "groups":
                        return getListOfStringsMatchingLastWord(args, "add", "remove");
                    case "group":
                        return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroups());
                    case "player":
                        return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getPlayers());
                }
                break;
            case 3:
                switch (args[0])
                {
                    case "groups":
                        if (args[1].equals("remove")) return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroups());
                        break;
                    case "group":
                        return getListOfStringsMatchingLastWord(args, "parent", "add", "remove");
                    case "player":
                        return getListOfStringsMatchingLastWord(args, "group", "perm");
                }
                break;
            case 4:
                switch (args[0])
                {
                    case "groups":
                        if (args[1].equals("add")) return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroups());
                    case "group":
                        switch (args[2])
                        {
                            case "parent":
                                return getListOfStringsMatchingLastWord(args, "set", "clear");
                            case "add":
                                return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getAllPermNodes());
                            case "remove":
                                return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroup(args[2]).getNodes());
                        }
                        break;
                    case "player":
                        switch (args[2])
                        {
                            case "group":
                                return getListOfStringsMatchingLastWord(args, "add", "remove");
                            case "perm":
                                return getListOfStringsMatchingLastWord(args, "add", "remove");
                        }
                        break;
                }
                break;
            case 5:
                switch (args[0])
                {
                    case "group":
                        if (args[2].equals("parent") && args[3].equals("set")) return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroups());
                        break;
                    case "player":
                        switch (args[2])
                        {
                            case "group":
                                switch (args[3])
                                {
                                    case "add":
                                        return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroups());
                                    case "remove":
                                        return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getPlayer(args[1]).getGroups());
                                }
                                break;
                            case "perm":
                                switch (args[3])
                                {
                                    case "add":
                                        return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getAllPermNodes());
                                    case "remove":
                                        return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getPlayer(args[1]).getNodes());
                                }
                                break;
                        }
                        break;
                }
                break;
        }
        return null;
    }
}
