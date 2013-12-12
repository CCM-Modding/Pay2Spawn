package ccm.pay2spawn;

import ccm.pay2spawn.types.TypeBase;
import ccm.pay2spawn.types.TypeRegistry;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.JsonNBTHelper;
import ccm.pay2spawn.util.Reward;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

import java.io.*;
import java.util.HashMap;

public class RewardsDB
{
    private final HashMap<Double, Reward> amountMap = new HashMap<>();

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
                    Reward reward = new Reward(element.getAsJsonObject());

                    amountMap.put(reward.getAmount(), reward);
                    reward.getType().totalPrice += reward.getAmount();
                    reward.getType().amountOfRewards++;

                    if (reward.getAmount() < reward.getType().minPrice) reward.getType().minPrice = reward.getAmount();
                    if (reward.getAmount() > reward.getType().maxPrice) reward.getType().maxPrice = reward.getAmount();
                }

                for (TypeBase type : TypeRegistry.getAllTypes())
                {
                    type.avgPrice = type.totalPrice / type.amountOfRewards;
                }
            }
            else
            {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
                JsonArray rootArray = new JsonArray();
                for (TypeBase type : TypeRegistry.getAllTypes())
                {
                    JsonObject element = new JsonObject();

                    element.addProperty("name", "DEMO_" + type.getName());
                    element.addProperty("type", type.getName().toLowerCase());
                    element.addProperty("amount", (double) ((int) (Helper.RANDOM.nextDouble() * 10000) / 10) / 100);
                    element.add("data", JsonNBTHelper.parseNBT(type.convertToNBT(type.getExample())));

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

    public synchronized boolean process(String name, double amount)
    {
        if (!amountMap.containsKey(amount))
        {
            Minecraft.getMinecraft().thePlayer.addChatMessage(EnumChatFormatting.GREEN + "[" + name + " donated " + Pay2Spawn.getConfig().currency + amount + "] " + EnumChatFormatting.WHITE);
            return false;
        }
        else
        {
            amountMap.get(amount).use(name);
            return true;
        }
    }
}
