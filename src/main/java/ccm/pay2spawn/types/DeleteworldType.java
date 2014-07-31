package ccm.pay2spawn.types;

import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.types.guis.DeleteworldTypeGui;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static ccm.pay2spawn.util.Constants.NBTTypes;
import static ccm.pay2spawn.util.Constants.STRING;

/**
 * This should be !FUN!
 *
 * @author Dries007
 */
public class DeleteworldType extends TypeBase
{
    public static final  String                  MESSAGE_KEY = "message";
    public static final  HashMap<String, String> typeMap     = new HashMap<>();
    private static final String                  NAME        = "deleteworld";

    static
    {
        typeMap.put(MESSAGE_KEY, NBTTypes[STRING]);
    }

    public static String DEFAULTMESSAGE = "A Pay2Spawn donation deleted the world.\\nGoodbye!";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagCompound.setString(MESSAGE_KEY, DEFAULTMESSAGE);
        return nbtTagCompound;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        for (int i = 0; i < MinecraftServer.getServer().getConfigurationManager().playerEntityList.size(); ++i)
        {
            ((EntityPlayerMP) MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(i)).playerNetServerHandler.kickPlayerFromServer(dataFromClient.getString(MESSAGE_KEY).replace("\\n", "\n"));
        }
        MinecraftServer.getServer().deleteWorldAndStopServer();
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new DeleteworldTypeGui(rewardID, NAME, data, typeMap);
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
