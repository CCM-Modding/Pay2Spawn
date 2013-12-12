package ccm.pay2spawn.util;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.types.TypeBase;
import ccm.pay2spawn.types.TypeRegistry;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

public class Reward
{
    private String         name;
    private TypeBase       type;
    private Double         amount;
    private NBTTagCompound data;

    public Reward(JsonObject json)
    {
        name = json.get("name").getAsString();
        type = TypeRegistry.getByName(json.get("type").getAsString().toUpperCase());
        amount = json.get("amount").getAsDouble();
        data = JsonNBTHelper.parseJSON(json.get("data").getAsJsonObject());
    }

    public TypeBase getType()
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
        type.sendToServer(name, Pay2Spawn.getConfig().currency + amount, data);
    }
}
