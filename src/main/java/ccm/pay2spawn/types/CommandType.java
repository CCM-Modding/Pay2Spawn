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

package ccm.pay2spawn.types;

import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.types.guis.CommandTypeGui;
import com.google.common.base.Throwables;
import com.google.gson.JsonObject;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.config.Configuration;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static ccm.pay2spawn.util.Constants.*;

public class CommandType extends TypeBase
{
    public static final  String                  COMMAND_KEY     = "command";
    public static final  HashMap<String, String> typeMap         = new HashMap<>();
    public static final  HashSet<String>         commands        = new HashSet<>();
    private static final String                  NAME            = "command";
    private static       Field                   commandSetField = getHackField();

    static
    {
        typeMap.put(COMMAND_KEY, NBTTypes[STRING]);
    }

    public boolean feedback = true;

    /**
     * Them cheaty ways...
     */
    private static Field getHackField()
    {
        try
        {
            Field f = CommandHandler.class.getDeclaredFields()[1];
            f.setAccessible(true);
            return f;
        }
        catch (Throwable t)
        {
            Throwables.propagate(t);
        }
        return null;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString(COMMAND_KEY, "weather clear");
        return nbt;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        MinecraftServer.getServer().getCommandManager().executeCommand(new cmdSender((EntityPlayerMP) player), dataFromClient.getString(COMMAND_KEY));
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new CommandTypeGui(rewardID, NAME, data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();
        MinecraftServer server = MinecraftServer.getServer();
        if (server != null)
        {
            try
            {
                Set set = (Set) commandSetField.get(server.getCommandManager());
                for (Object o : set)
                {
                    ICommand command = (ICommand) o;
                    commands.add(command.getCommandName());
                    nodes.add(new Node(NAME, command.getCommandName()));
                }
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            nodes.add(new Node(NAME));
        }

        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        return new Node(NAME, dataFromClient.getString(COMMAND_KEY).split(" ")[0]);
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        switch (id)
        {
            case "cmd":
                return jsonObject.get(COMMAND_KEY).getAsString().replace(typeMap.get(COMMAND_KEY) + ":", "");
        }
        return id;
    }

    @Override
    public void doConfig(Configuration configuration)
    {
        feedback = configuration.get(MODID + ".command", "feedback", feedback, "Disable command feedback. (server overrides client)").getBoolean(feedback);
    }

    public class cmdSender extends EntityPlayerMP
    {
        public cmdSender(EntityPlayerMP player)
        {
            super(player.mcServer, player.getServerForPlayer(), player.getGameProfile(), player.theItemInWorldManager);
            this.theItemInWorldManager.thisPlayerMP = player;
            this.playerNetServerHandler = player.playerNetServerHandler;
        }

        @Override
        public boolean canCommandSenderUseCommand(int par1, String cmd)
        {
            return true;
        }

        @Override
        public void addChatComponentMessage(IChatComponent p_146105_1_)
        {
            if (feedback) super.addChatComponentMessage(p_146105_1_);
        }
    }
}
