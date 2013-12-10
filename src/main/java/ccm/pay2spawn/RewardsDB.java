package ccm.pay2spawn;

import ccm.pay2spawn.util.Reward;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

public class RewardsDB
{
    //TODO: Pick the right map, start using it.
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
        }
        catch (FileNotFoundException e)
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
