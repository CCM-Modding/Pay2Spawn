package ccm.pay2spawn.types;

import ccm.pay2spawn.configurator.Configurator;
import ccm.pay2spawn.permissions.Node;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import java.util.Arrays;
import java.util.Collection;

public class DeleteWorldType extends TypeBase
{
    private static final String                  NAME            = "deleteworld";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        return new NBTTagCompound();
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        for (int i = 0; i < MinecraftServer.getServer().getConfigurationManager().playerEntityList.size(); ++i)
        {
            ((EntityPlayerMP)MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(i)).playerNetServerHandler.kickPlayerFromServer("A Pay2Spawn donation deleted the world.\nGoodbye!");
        }
        MinecraftServer.getServer().deleteWorldAndStopServer();
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        Configurator.instance.callback(rewardID, NAME, data);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        return Arrays.asList(new Node(NAME));
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        return new Node(NAME);
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        return id;
    }
}
