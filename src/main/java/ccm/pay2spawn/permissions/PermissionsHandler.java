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

package ccm.pay2spawn.permissions;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

public class PermissionsHandler
{
    private static PermissionsDB   permissionsDB = new PermissionsDB();
    private static HashSet<String> nodes         = new HashSet<>();

    public static boolean hasPermissionNode(EntityPlayer player, Node node)
    {
        return permissionsDB.check(player.getCommandSenderName(), node);
    }

    public static void init() throws IOException
    {
        permissionsDB.load();
        BanHelper.init();
    }

    public static void register(Collection<Node> nodesToAdd)
    {
        for (Node node : nodesToAdd)
            nodes.add(node.toString());
    }

    public static boolean needPermCheck(EntityPlayer player)
    {
        MinecraftServer mcs = MinecraftServer.getServer();
        return !(mcs.isSinglePlayer() || mcs.getConfigurationManager().isPlayerOpped(player.getCommandSenderName()));
    }

    public static PermissionsDB getDB()
    {
        return permissionsDB;
    }

    public static Iterable<String> getAllPermNodes()
    {
        return nodes;
    }
}
