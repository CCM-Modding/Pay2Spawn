package ccm.pay2spawn.checkers;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.hud.DonationsBasedHudEntry;
import ccm.pay2spawn.hud.Hud;
import ccm.pay2spawn.misc.Donation;
import ccm.pay2spawn.util.Helper;
import ccm.pay2spawn.util.JsonNBTHelper;
import ccm.pay2spawn.util.MetricsHelper;
import ccm.pay2spawn.util.Statistics;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Configuration;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import static ccm.pay2spawn.util.Constants.JSON_PARSER;
import static ccm.pay2spawn.util.Constants.MODID;

public class StreamtipChecker extends AbstractChecker implements Runnable
{
    public final static String NAME = "streamtip";
    public final static String CAT = MODID + '.' + NAME;
    public final static String URL = "https://streamtip.com/api/tips?";

    DonationsBasedHudEntry topDonationsBasedHudEntry, recentDonationsBasedHudEntry;

    HashSet<String> doneIDs  = new HashSet<>();
    HashSet<Donation> backlog  = new HashSet<>();
    String          ClientID = "", ClientAccessToken = "";
    boolean enabled  = true;
    int     interval = 3;

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public void fakeDonation(double amount)
    {
        Donation donation = new Donation(UUID.randomUUID().toString(), amount, new Date().getTime());
        Helper.msg(EnumChatFormatting.GOLD + "[P2S] Faking donation of " + amount + ".");
        Pay2Spawn.getRewardsDB().process(donation, false);
    }

    @Override
    public void init()
    {
        Hud.INSTANCE.set.add(topDonationsBasedHudEntry);
        Hud.INSTANCE.set.add(recentDonationsBasedHudEntry);

        new Thread(this, getName()).start();
    }

    @Override
    public boolean enabled()
    {
        return enabled && !ClientID.isEmpty() && !ClientAccessToken.isEmpty();
    }

    @Override
    public void doConfig(Configuration configuration)
    {
        configuration.addCustomCategoryComment(CAT, "This is the checker for streamtip.com");

        enabled = configuration.get(CAT, "enabled", enabled).getBoolean(enabled);
        ClientID = configuration.get(CAT, "ClientID", ClientID).getString();
        ClientAccessToken = configuration.get(CAT, "ClientAccessToken", ClientAccessToken).getString();
        interval = configuration.get(CAT, "interval", interval, "The time in between polls (in seconds).").getInt();

        recentDonationsBasedHudEntry = new DonationsBasedHudEntry(configuration, CAT + ".recentDonations", -1, 2, 5, "$name: $$amount", "-- Recent donations --", CheckerHandler.RECENT_DONATION_COMPARATOR);
        topDonationsBasedHudEntry = new DonationsBasedHudEntry(configuration, CAT + ".topDonations", -1, 1, 5, "$name: $$amount", "-- Top donations --", CheckerHandler.AMOUNT_DONATION_COMPARATOR);
    }

    @Override
    public void run()
    {
        try
        {
            JsonObject root = JSON_PARSER.parse(Helper.readUrl(new URL(URL + "client_id=" + ClientID + "&access_token=" + ClientAccessToken + "&sort_by=amount&limit=" + topDonationsBasedHudEntry.getAmount()))).getAsJsonObject();
            if (root.getAsJsonPrimitive("status").getAsInt() == 200)
            {
                JsonArray donations = root.getAsJsonArray("tips");
                for (JsonElement jsonElement : donations)
                {
                    Donation donation = getDonation(JsonNBTHelper.fixNulls(jsonElement.getAsJsonObject()));
                    topDonationsBasedHudEntry.add(donation);
                    doneIDs.add(donation.id);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        try
        {
            JsonObject root = JSON_PARSER.parse(Helper.readUrl(new URL(URL + "client_id=" + ClientID + "&access_token=" + ClientAccessToken + "&sort_by=date&limit=" + recentDonationsBasedHudEntry.getAmount()))).getAsJsonObject();
            if (root.getAsJsonPrimitive("status").getAsInt() == 200)
            {
                JsonArray donations = root.getAsJsonArray("tips");
                for (JsonElement jsonElement : donations)
                {
                    Donation donation = getDonation(JsonNBTHelper.fixNulls(jsonElement.getAsJsonObject()));
                    recentDonationsBasedHudEntry.add(donation);
                    doneIDs.add(donation.id);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        while (true)
        {
            doWait(interval);
            try
            {
                JsonObject root = JSON_PARSER.parse(Helper.readUrl(new URL(URL + "client_id=" + ClientID + "&access_token=" + ClientAccessToken + "&sort_by=date&limit=5"))).getAsJsonObject();
                if (root.getAsJsonPrimitive("status").getAsInt() == 200)
                {
                    JsonArray donations = root.getAsJsonArray("tips");
                    for (JsonElement jsonElement : donations)
                    {
                        process(getDonation(JsonNBTHelper.fixNulls(jsonElement.getAsJsonObject())), true);
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void process(Donation donation, boolean msg)
    {
        if (Minecraft.getMinecraft().thePlayer == null || !Pay2Spawn.enable)
        {
            if (!backlog.contains(donation)) backlog.add(donation);
            return;
        }

        if (!doneIDs.contains(donation.id))
        {
            doneIDs.add(donation.id);
            MetricsHelper.totalMoney += donation.amount;
            Statistics.addToDonationAmount(donation.amount);
            if (donation.amount < Pay2Spawn.getConfig().min_donation) return;
            try
            {
                topDonationsBasedHudEntry.add(donation);
                recentDonationsBasedHudEntry.add(donation);
                Pay2Spawn.getRewardsDB().process(donation, msg);
            }
            catch (Exception e)
            {
                Pay2Spawn.getLogger().warn("Error processing a donation.");
                e.printStackTrace();
            }
        }
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
    private Donation getDonation(JsonObject jsonObject)
    {
        try
        {
            return new Donation(jsonObject.get("_id").getAsString(), jsonObject.get("amount").getAsDouble(), sdf.parse(jsonObject.get("date").getAsString()).getTime(), jsonObject.get("username").getAsString(), jsonObject.get("note").getAsString());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return new Donation(jsonObject.get("_id").getAsString(), jsonObject.get("amount").getAsDouble(), new Date().getTime(), jsonObject.get("username").getAsString(), jsonObject.get("note").getAsString());
    }
}
