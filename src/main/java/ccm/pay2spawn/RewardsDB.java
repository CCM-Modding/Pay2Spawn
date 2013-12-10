package ccm.pay2spawn;

import ccm.pay2spawn.util.EnumSpawnType;
import ccm.pay2spawn.util.JsonNBTHelper;
import ccm.pay2spawn.util.Reward;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.util.HashMap;

public class RewardsDB
{
    HashMap<CheatyBiKey<String, Double>, Reward> map = new HashMap<>();

    RewardsDB(File file)
    {
        try
        {
            if (file.exists())
            {
                JsonParser parser = new JsonParser();
                JsonArray rootArray = parser.parse(new FileReader(file)).getAsJsonArray();

                for (JsonElement element : rootArray)
                {
                    add(new Reward(element.getAsJsonObject()));
                }
            }
            else
            {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
                JsonArray rootArray = new JsonArray();
                for (EnumSpawnType type : EnumSpawnType.values())
                {
                    JsonObject element = new JsonObject();

                    element.addProperty("name", "DEMO_" + type.name());
                    element.addProperty("type", type.name());
                    element.addProperty("amount", 45.01);
                    element.add("data", JsonNBTHelper.parseNBT(type.getNBTfromData(type.makeRandomData())));

                    rootArray.add(element);
                }

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

    private void add(Reward reward)
    {
        map.put(new CheatyBiKey<String, Double>(reward.getName(), reward.getAmount()), reward);
    }

    static class CheatyBiKey<A, B>
    {
        A key1;
        B key2;

        public CheatyBiKey(A key1, B key2)
        {
            this.key1 = key1;
            this.key2 = key2;
        }

        @Override
        public boolean equals(Object o)
        {
            return o instanceof CheatyBiKey && (((CheatyBiKey) o).key1.equals(key1) || ((CheatyBiKey) o).key2.equals(key2));
        }
    }
}
