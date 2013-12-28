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
import ccm.pay2spawn.permissions.Group;
import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.permissions.PermissionsHandler;
import ccm.pay2spawn.permissions.Player;
import cpw.mods.fml.common.FMLCommonHandler;
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
        }
        switch (args[0])
        {
            case "debug":
                if (MinecraftServer.getServer().getConfigurationManager().isPlayerOpped(sender.getCommandSenderName())) HandshakePacket.sendDebugToPlayer(player);
                else sender.sendChatToPlayer(ChatMessageComponent.createFromText("You have to be OP to enable debug mode.").setColor(EnumChatFormatting.RED));
                break;
            case "reload":
                if (Pay2Spawn.getConfig().forceServerconfig && FMLCommonHandler.instance().getSide().isServer())
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText("[P2S] You can't do that with forced server configs. Reboot the server.").setColor(EnumChatFormatting.RED));
                else HandshakePacket.reload(player);
                break;
            case "configure":
                ConfiguratorManager.openConfigurator(player);
                break;
            case "getnbt":
                ConfiguratorManager.openNBTGrabber(player);
                break;
            case "off":
                HandshakePacket.toggle(player, false);
                break;
            case "on":
                HandshakePacket.toggle(player, true);
                break;
            case "donate":
                if (args.length == 1) player.sendChatToPlayer(ChatMessageComponent.createFromText("Use '/p2s donate <amount>'."));
                else
                {
                    double amount = CommandBase.parseDouble(sender, args[1]);
                    RedonatePacket.send(player, amount);
                }
                break;
            case "redonate":
                if (args.length == 1) player.sendChatToPlayer(ChatMessageComponent.createFromText("Use '/p2s redonate <1-5>' to redo one of the last 5 donations."));
                else
                {
                    int amount = CommandBase.parseIntBounded(sender, args[1], 1, 5) - 1;
                    RedonatePacket.send(player, amount);
                }
                break;
            case "permissions":
            case "permission":
            case "perm":
                if (!MinecraftServer.getServer().getConfigurationManager().isPlayerOpped(sender.getCommandSenderName()))
                {
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText("You need to be OP to change permissions.").setColor(EnumChatFormatting.RED));
                    break;
                }
                if (args.length == 1)
                {
                    player.sendChatToPlayer(ChatMessageComponent.createFromText("Use '/p2s perm group|groups|player' for more info."));
                    break;
                }
                switch (args[1])
                {
                    case "groups":
                        if (args.length < 4)
                        {
                            player.sendChatToPlayer(ChatMessageComponent.createFromText("Use '/p2s perm groups add|remove <name> [parent group]' to add or remove a group."));
                            break;
                        }
                        else
                        {
                            String name = args[3];
                            switch (args[2])
                            {
                                case "add":
                                    String parent = args.length == 5 ? args[4] : null;
                                    PermissionsHandler.getDB().newGroup(name, parent);
                                    player.sendChatToPlayer(ChatMessageComponent.createFromText("Added new group named '" + name + (parent != null ? "' with parent group '" + parent : "") + "'."));
                                    break;
                                case "remove":
                                    PermissionsHandler.getDB().remove(name);
                                    player.sendChatToPlayer(ChatMessageComponent.createFromText("Removed group named '" + name + "'"));
                                    break;
                            }
                        }
                        break;
                    case "group":
                        if (args.length < 5)
                        {
                            player.sendChatToPlayer(ChatMessageComponent.createFromText("Use '/p2s perm group <name> add|remove <node>' OR '<name> parent set|clear [name]'"));
                            break;
                        }
                        else
                        {
                            Group group = PermissionsHandler.getDB().getGroup(args[2]);
                            if (group == null)
                            {
                                player.sendChatToPlayer(ChatMessageComponent.createFromText("The group doesn't exitst."));
                                break;
                            }
                            switch (args[3])
                            {
                                case "parent":
                                    switch (args[4])
                                    {
                                        case "set":
                                            if (args.length != 6)
                                            {
                                                player.sendChatToPlayer(ChatMessageComponent.createFromText("Use 'parent set <name>."));
                                                return;
                                            }
                                            group.setParent(args[5]);
                                            player.sendChatToPlayer(ChatMessageComponent.createFromText("Set parent to: " + args[5]));
                                            break;
                                        case "clear":
                                            group.setParent(null);
                                            player.sendChatToPlayer(ChatMessageComponent.createFromText("Cleared parent group."));
                                            break;
                                    }
                                    break;
                                case "add":
                                    group.addNode(args[4]);
                                    player.sendChatToPlayer(ChatMessageComponent.createFromText("Added node: " + args[4]));
                                    break;
                                case "remove":
                                    if (group.removeNode(args[4])) player.sendChatToPlayer(ChatMessageComponent.createFromText("Removed node: " + args[4]));
                                    else player.sendChatToPlayer(ChatMessageComponent.createFromText("Node not removed, it wasn't there in the first place..."));
                                    break;
                            }
                        }
                        break;
                    case "player":
                        if (args.length < 6)
                        {
                            player.sendChatToPlayer(ChatMessageComponent.createFromText("Use '/p2s perm player <name> group add|remove <group>' OR '<name> perm add|remove <node>'"));
                            break;
                        }
                        else
                        {
                            Player playero = PermissionsHandler.getDB().getPlayer(args[2]);
                            if (playero == null)
                            {
                                player.sendChatToPlayer(ChatMessageComponent.createFromText("That player doesn't exist."));
                                break;
                            }
                            switch (args[3])
                            {
                                case "group":
                                    switch (args[4])
                                    {
                                        case "add":
                                            playero.addGroup(args[5]);
                                            break;
                                        case "remove":
                                            if (playero.removeGroup(args[5])) player.sendChatToPlayer(ChatMessageComponent.createFromText("Removed group: " + args[5]));
                                            else player.sendChatToPlayer(ChatMessageComponent.createFromText("Group not removed, it wasn't there in the first place..."));
                                            break;
                                    }
                                    break;
                                case "perm":
                                    switch (args[4])
                                    {
                                        case "add":
                                            playero.addNode(new Node(args[5]));
                                            break;
                                        case "remove":
                                            if (playero.removeNode(new Node(args[5]))) player.sendChatToPlayer(ChatMessageComponent.createFromText("Added per node: " + args[5]));
                                            else player.sendChatToPlayer(ChatMessageComponent.createFromText("Perm node not removed, it wasn't there in the first place..."));
                                            break;
                                    }
                                    break;
                            }
                        }
                        break;
                }
                PermissionsHandler.getDB().save();
                break;
            default:
                sender.sendChatToPlayer(ChatMessageComponent.createFromText("Unknown command. Protip: Use tab completion!"));
                break;
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        switch (args.length)
        {
            case 1:
                return getListOfStringsMatchingLastWord(args, "debug", "reload", "configure", "getnbt", "off", "on", "donate", "redonate", "permissions");
            case 2:
                switch (args[0])
                {
                    case "permissions":
                    case "permission":
                    case "perm":
                        return getListOfStringsMatchingLastWord(args, "groups", "group", "player");
                }
                break;
            case 3:
                switch (args[0])
                {
                    case "permissions":
                    case "permission":
                    case "perm":
                        switch (args[1])
                        {
                            case "groups":
                                return getListOfStringsMatchingLastWord(args, "add", "remove");
                            case "group":
                                return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroups());
                            case "player":
                                return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getPlayers());
                        }
                }
                break;
            case 4:
                switch (args[0])
                {
                    case "permissions":
                    case "permission":
                    case "perm":
                        switch (args[1])
                        {
                            case "groups":
                                if (args[2].equals("remove")) return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroups());
                                break;
                            case "group":
                                return getListOfStringsMatchingLastWord(args, "parent", "add", "remove");
                            case "player":
                                return getListOfStringsMatchingLastWord(args, "group", "perm");
                        }
                }
                break;
            case 5:
                switch (args[0])
                {
                    case "permissions":
                    case "permission":
                    case "perm":
                        switch (args[1])
                        {
                            case "groups":
                                if (args[2].equals("add")) return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroups());
                            case "group":
                                switch (args[3])
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
                                switch (args[3])
                                {
                                    case "group":
                                        return getListOfStringsMatchingLastWord(args, "add", "remove");
                                    case "perm":
                                        return getListOfStringsMatchingLastWord(args, "add", "remove");
                                }
                                break;
                        }
                }
                break;
            case 6:
                switch (args[0])
                {
                    case "permissions":
                    case "permission":
                    case "perm":
                        switch (args[1])
                        {
                            case "group":
                                if (args[3].equals("parent") && args[4].equals("set")) return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroups());
                                break;
                            case "player":
                                switch (args[3])
                                {
                                    case "group":
                                        switch (args[4])
                                        {
                                            case "add":
                                                return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroups());
                                            case "remove":
                                                return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getPlayer(args[2]).getGroups());
                                        }
                                        break;
                                    case "perm":
                                        switch (args[4])
                                        {
                                            case "add":
                                                return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getAllPermNodes());
                                            case "remove":
                                                return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getPlayer(args[2]).getNodes());
                                        }
                                        break;
                                }
                                break;
                        }
                }
                break;
        }
        return null;
    }
}
