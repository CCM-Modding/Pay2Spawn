package ccm.pay2spawn.util;

import ccm.pay2spawn.P2SConfig;
import ccm.pay2spawn.Pay2Spawn;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

public class Reward
{
    private String         name;
    private EnumSpawnType  type;
    private Double         amount;
    private NBTTagCompound data;

    public Reward(JsonObject json)
    {
        name = json.get("name").getAsString();
        type = EnumSpawnType.valueOf(json.get("type").getAsString().toUpperCase());
        amount = json.get("amount").getAsDouble();
        data = JsonNBTHelper.parseJSON(json.get("data").getAsJsonObject());
    }

    public EnumSpawnType getType()
    {
        return type;
    }

    public NBTTagCompound getData()
    {
        return data;
    }

    public String getName()
    {
        return name;
    }

    public Double getAmount()
    {
        return amount;
    }

    public void use(String name)
    {
        type.createAndSend(name, Pay2Spawn.getConfig().currency + amount, data);
    }
}
