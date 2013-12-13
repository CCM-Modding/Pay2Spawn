package ccm.pay2spawn;

import ccm.pay2spawn.types.TypeBase;
import ccm.pay2spawn.types.TypeRegistry;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.JsonNBTHelper;
import ccm.pay2spawn.util.Reward;
import com.google.gson.*;

import java.io.*;
import java.util.HashMap;

public class RewardsDB
{
    private final HashMap<Double, Reward> map = new HashMap<>();

    RewardsDB(File file)
    {
        try
        {
            if (file.exists())
            {
                JsonArray rootArray = Helper.PARSER.parse(new FileReader(file)).getAsJsonArray();

                for (JsonElement element : rootArray)
                {
                    Reward reward = new Reward(element.getAsJsonObject());
                    map.put(reward.getAmount(), reward);
                }
            }
            else
            {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
                JsonArray rootArray = new JsonArray();

                JsonObject group = new JsonObject();
                group.addProperty("name", "EXAMPLE");
                group.addProperty("amount", 10);
                group.addProperty("message", "&a[$name donated $$amount]");
                JsonArray rewards = new JsonArray();
                for (TypeBase type : TypeRegistry.getAllTypes())
                {
                    JsonObject element = new JsonObject();
                    element.addProperty("type", type.getName());
                    element.add("data", JsonNBTHelper.parseNBT(type.convertToNBT(type.getExample())));
                    rewards.add(element);
                }
                group.add("rewards", rewards);
                rootArray.add(group);

                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                bw.write(gson.toJson(rootArray));
                bw.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public synchronized boolean process(JsonObject donation)
    {
        if (!map.containsKey(donation.get("amount").getAsDouble()))
        {
            Helper.msg(Helper.formatText(Pay2Spawn.getConfig().messageNoReward, donation));
            return false;
        }
        else
        {
            map.get(donation.get("amount").getAsDouble()).sendToServer(donation);
            return true;
        }
    }
}
