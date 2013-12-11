package ccm.pay2spawn;

import ccm.pay2spawn.util.EnumSpawnType;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.JsonNBTHelper;
import ccm.pay2spawn.util.Reward;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RewardsDB
{
    private final HashMap<String, Reward> nameMap = new HashMap<>();
    private final HashMap<Double, Reward> amountMap = new HashMap<>();

    public final int[] amountsPerType = new int[EnumSpawnType.values().length];
    public final double[] minPricePerType = new double[EnumSpawnType.values().length];
    public final double[] maxPricePerType = new double[EnumSpawnType.values().length];
    public final double[] avgPricePerType = new double[EnumSpawnType.values().length];

    RewardsDB(File file)
    {
        try
        {
            if (file.exists())
            {
                JsonParser parser = new JsonParser();
                JsonArray rootArray = parser.parse(new FileReader(file)).getAsJsonArray();

                double[] totalPricePerType = new double[EnumSpawnType.values().length];

                for (JsonElement element : rootArray)
                {
                    Reward reward = new Reward(element.getAsJsonObject());

                    nameMap.put(reward.getName(), reward);
                    amountMap.put(reward.getAmount(), reward);

                    totalPricePerType[reward.getType().ordinal()] += reward.getAmount();
                    amountsPerType[reward.getType().ordinal()] ++;
                    if (reward.getAmount() < minPricePerType[reward.getType().ordinal()]) minPricePerType[reward.getType().ordinal()] = reward.getAmount();
                    if (reward.getAmount() > maxPricePerType[reward.getType().ordinal()]) maxPricePerType[reward.getType().ordinal()] = reward.getAmount();
                }

                for (int i = 0; i < EnumSpawnType.values().length; i++)
                {
                    avgPricePerType[i] = totalPricePerType[i] / amountsPerType[i];
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
                    element.addProperty("type", type.name().toLowerCase());
                    element.addProperty("amount", (double) ((int) (Helper.RANDOM.nextDouble() * 10000) / 10) / 100);
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
}
