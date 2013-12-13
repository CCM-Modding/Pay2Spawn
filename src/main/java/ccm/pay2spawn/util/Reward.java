package ccm.pay2spawn.util;

import ccm.pay2spawn.types.TypeRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;

import java.io.*;

public class Reward
{
    private String    message;
    private String    name;
    private Double    amount;
    private JsonArray rewards;

    public Reward(JsonObject json)
    {
        name = json.get("name").getAsString();
        amount = json.get("amount").getAsDouble();
        message = Helper.formatColors(json.get("message").getAsString());
        rewards = json.getAsJsonArray("rewards");
    }

    public Reward(String name, Double amount, JsonArray rewards)
    {
        this.name = name;
        this.amount = amount;
        this.rewards = rewards;
    }

    public String getName()
    {
        return name;
    }

    public Double getAmount()
    {
        return amount;
    }

    public void sendToServer(JsonObject donation)
    {
        Helper.msg(Helper.formatText(message, donation));
        PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(Archive.MODID, toBytes(Helper.formatText(rewards, donation).toString())));
    }

    private byte[] toBytes(String formattedData)
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.writeUTF(name);
            stream.writeDouble(amount);
            stream.writeUTF(formattedData);
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return streambyte.toByteArray();
    }

    public static Reward reconstruct(Packet250CustomPayload packet) throws IOException
    {
        ByteArrayInputStream streambyte = new ByteArrayInputStream(packet.data);
        DataInputStream stream = new DataInputStream(streambyte);
        String name = stream.readUTF();
        Double amount = stream.readDouble();
        JsonArray rewards = Helper.PARSER.parse(stream.readUTF()).getAsJsonArray();

        return new Reward(name, amount, rewards);
    }

    public void spawnOnServer(EntityPlayer player)
    {
        for (JsonElement element : rewards)
        {
            JsonObject reward = element.getAsJsonObject();
            TypeRegistry.getByName(reward.get("type").getAsString().toLowerCase()).spawnServerSide(player, JsonNBTHelper.parseJSON(reward.getAsJsonObject("data")));
        }
    }
}
