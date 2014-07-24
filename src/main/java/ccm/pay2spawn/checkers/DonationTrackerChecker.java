package ccm.pay2spawn.checkers;

import ccm.pay2spawn.hud.DonationsBasedHudEntry;
import ccm.pay2spawn.hud.Hud;
import ccm.pay2spawn.util.Donation;
import ccm.pay2spawn.util.Helper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.common.config.Configuration;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ccm.pay2spawn.util.Constants.BASECAT_TRACKERS;
import static ccm.pay2spawn.util.Constants.JSON_PARSER;

public class DonationTrackerChecker extends AbstractChecker implements Runnable
{
    public static final DonationTrackerChecker INSTANCE     = new DonationTrackerChecker();
    public final static String                 NAME         = "donation-tracker";
    public final static String                 CAT          = BASECAT_TRACKERS + '.' + NAME;
    public final static String                 URL          = "https://www.donation-tracker.com/customapi/?";
    public final static Pattern                HTML_REGEX   = Pattern.compile("<td.*?>(.+?)<\\/td.*?>");
    public final static Pattern                AMOUNT_REGEX = Pattern.compile(".?(\\d+(?:\\.|,)\\d\\d)\\w?.?");

    //public final static Pattern                HTML_REGEX    = Pattern.compile("From: (.+?) - .(\\d+\\.\\d\\d) - (.+?) \\| ");

    DonationsBasedHudEntry topDonationsBasedHudEntry, recentDonationsBasedHudEntry;

    String Channel = "", APIKey = "";
    boolean          enabled  = true;
    int              interval = 3;
    SimpleDateFormat sdf      = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");

    private DonationTrackerChecker()
    {
        super();
    }

    @Override
    public String getName()
    {
        return NAME;
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
        return enabled && !Channel.isEmpty() && !APIKey.isEmpty();
    }

    @Override
    public void doConfig(Configuration configuration)
    {
        configuration.addCustomCategoryComment(CAT, "This is the checker for donation-tracker.com");

        enabled = configuration.get(CAT, "enabled", enabled).getBoolean(enabled);
        Channel = configuration.get(CAT, "Channel", Channel).getString();
        APIKey = configuration.get(CAT, "APIKey", APIKey).getString();
        interval = configuration.get(CAT, "interval", interval, "The time in between polls (in seconds).").getInt();

        recentDonationsBasedHudEntry = new DonationsBasedHudEntry(configuration, CAT + ".recentDonations", -1, 2, 5, "$name: $$amount", "-- Recent donations --", CheckerHandler.RECENT_DONATION_COMPARATOR);
        topDonationsBasedHudEntry = new DonationsBasedHudEntry(configuration, CAT + ".topDonations", -1, 1, 5, "$name: $$amount", "-- Top donations --", CheckerHandler.AMOUNT_DONATION_COMPARATOR);
    }

    @Override
    public DonationsBasedHudEntry[] getDonationsBasedHudEntries()
    {
        return new DonationsBasedHudEntry[] {topDonationsBasedHudEntry, recentDonationsBasedHudEntry};
    }

    @Override
    public void run()
    {
        try
        {
            JsonObject root = JSON_PARSER.parse(Helper.readUrl(new URL(URL + "channel=" + Channel + "&api_key=" + APIKey + "&custom=1"))).getAsJsonObject();
            if (root.getAsJsonPrimitive("api_check").getAsInt() == 1)
            {
                JsonArray donations = root.getAsJsonArray("donation_list");
                for (JsonElement jsonElement : donations)
                {
                    Donation donation = getDonation(jsonElement.getAsString());
                    topDonationsBasedHudEntry.add(donation);
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
                JsonObject root = JSON_PARSER.parse(Helper.readUrl(new URL(URL + "channel=" + Channel + "&api_key=" + APIKey + "&custom=1"))).getAsJsonObject();
                if (root.getAsJsonPrimitive("api_check").getAsInt() == 1)
                {
                    JsonArray donations = root.getAsJsonArray("donation_list");
                    for (JsonElement jsonElement : donations)
                    {
                        process(getDonation(jsonElement.getAsString()), true);
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private Donation getDonation(String html)
    {
        ArrayList<String> htmlMatches = new ArrayList<>();
        Matcher htmlMatcher = HTML_REGEX.matcher(html);
        while (htmlMatcher.find()) htmlMatches.add(htmlMatcher.group(1));
        String[] data = htmlMatches.toArray(new String[htmlMatches.size()]);

        Matcher amountMatcher = AMOUNT_REGEX.matcher(data[3]);
        amountMatcher.find();
        double amount = Double.parseDouble(amountMatcher.group(1).replace(',', '.'));

        long time = new Date().getTime();
        try
        {
            time = sdf.parse(data[2]).getTime();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return new Donation(data[2], amount, time, data[0], data[1]);
    }
}
