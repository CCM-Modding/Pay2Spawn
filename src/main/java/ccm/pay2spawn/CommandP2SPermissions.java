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

import ccm.pay2spawn.permissions.Group;
import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.permissions.PermissionsHandler;
import ccm.pay2spawn.permissions.Player;
import ccm.pay2spawn.util.JsonNBTHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

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
    public void processCommand(ICommandSender sender, String[] args)
    {
        EntityPlayer player = (EntityPlayer) sender;
        if (!MinecraftServer.getServer().getConfigurationManager().isPlayerOpped(sender.getCommandSenderName()))
        {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText("You need to be OP to change permissions.").setColor(EnumChatFormatting.RED));
            return;
        }
        if (args.length == 0)
        {
            player.sendChatToPlayer(ChatMessageComponent.createFromText("Use '/p2s perm group|groups|player' for more info.").setColor(EnumChatFormatting.RED));
            return;
        }
        switch (args[0])
        {
            case "test":
                double X = player.posX, Y = player.posY, Z = player.posZ, spread = 25;
                AxisAlignedBB AABB = AxisAlignedBB.getAABBPool().getAABB(X - spread, Y - spread, Z - spread, X + spread, Y + spread, Z + spread);
                Entity entity = player.getEntityWorld().findNearestEntityWithinAABB(Entity.class, AABB, player);

                NBTTagCompound nbt = new NBTTagCompound();
                entity.writeToNBT(nbt);
                entity.writeToNBTOptional(nbt);
                System.out.println(JsonNBTHelper.parseNBT(nbt).toString());

                player.mountEntity(entity);
                player.addChatMessage("test");
                return;
            case "groups":
                if (args.length < 3)
                {
                    player.sendChatToPlayer(ChatMessageComponent.createFromText("Use '/p2s perm groups add|remove <name> [parent group]' to add or remove a group.").setColor(EnumChatFormatting.RED));
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
                            player.sendChatToPlayer(ChatMessageComponent.createFromText("Added new group named '" + name + (parent != null ? "' with parent group '" + parent : "") + "'.").setColor(EnumChatFormatting.GOLD));
                            break;
                        case "remove":
                            PermissionsHandler.getDB().remove(name);
                            player.sendChatToPlayer(ChatMessageComponent.createFromText("Removed group named '" + name + "'").setColor(EnumChatFormatting.GOLD));
                            break;
                    }
                }
                break;
            case "group":
                if (args.length < 4)
                {
                    player.sendChatToPlayer(ChatMessageComponent.createFromText("Use '/p2s perm group <name> add|remove <node>' OR '<name> parent set|clear [name]'").setColor(EnumChatFormatting.RED));
                    break;
                }
                else
                {
                    Group group = PermissionsHandler.getDB().getGroup(args[1]);
                    if (group == null)
                    {
                        player.sendChatToPlayer(ChatMessageComponent.createFromText("The group doesn't exitst.").setColor(EnumChatFormatting.RED));
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
                                        player.sendChatToPlayer(ChatMessageComponent.createFromText("Use 'parent set <name>.").setColor(EnumChatFormatting.RED));
                                        return;
                                    }
                                    group.setParent(args[4]);
                                    player.sendChatToPlayer(ChatMessageComponent.createFromText("Set parent to: " + args[4]).setColor(EnumChatFormatting.GOLD));
                                    break;
                                case "clear":
                                    group.setParent(null);
                                    player.sendChatToPlayer(ChatMessageComponent.createFromText("Cleared parent group.").setColor(EnumChatFormatting.GOLD));
                                    break;
                            }
                            break;
                        case "add":
                            group.addNode(args[3]);
                            player.sendChatToPlayer(ChatMessageComponent.createFromText("Added node: " + args[3]).setColor(EnumChatFormatting.GOLD));
                            break;
                        case "remove":
                            if (group.removeNode(args[3])) player.sendChatToPlayer(ChatMessageComponent.createFromText("Removed node: " + args[3]).setColor(EnumChatFormatting.GOLD));
                            else player.sendChatToPlayer(ChatMessageComponent.createFromText("Node not removed, it wasn't there in the first place...").setColor(EnumChatFormatting.RED));
                            break;
                    }
                }
                break;
            case "player":
                if (args.length < 5)
                {
                    player.sendChatToPlayer(ChatMessageComponent.createFromText("Use '/p2s perm player <name> group add|remove <group>' OR '<name> perm add|remove <node>'").setColor(EnumChatFormatting.RED));
                    break;
                }
                else
                {
                    Player playero = PermissionsHandler.getDB().getPlayer(args[1]);
                    if (playero == null)
                    {
                        player.sendChatToPlayer(ChatMessageComponent.createFromText("That player doesn't exist.").setColor(EnumChatFormatting.RED));
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
                                    if (playero.removeGroup(args[4])) player.sendChatToPlayer(ChatMessageComponent.createFromText("Removed group: " + args[4]).setColor(EnumChatFormatting.GOLD));
                                    else player.sendChatToPlayer(ChatMessageComponent.createFromText("Group not removed, it wasn't there in the first place...").setColor(EnumChatFormatting.RED));
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
                                    if (playero.removeNode(new Node(args[4]))) player.sendChatToPlayer(ChatMessageComponent.createFromText("Added per node: " + args[4]).setColor(EnumChatFormatting.GOLD));
                                    else player.sendChatToPlayer(ChatMessageComponent.createFromText("Perm node not removed, it wasn't there in the first place...").setColor(EnumChatFormatting.RED));
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
