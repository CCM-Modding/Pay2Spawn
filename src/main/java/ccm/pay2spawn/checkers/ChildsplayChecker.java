package ccm.pay2spawn.checkers;

import ccm.pay2spawn.hud.DonationsBasedHudEntry;
import ccm.pay2spawn.hud.Hud;
import ccm.pay2spawn.misc.Donation;
import ccm.pay2spawn.util.Base64;
import ccm.pay2spawn.util.JsonNBTHelper;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.common.config.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static ccm.pay2spawn.util.Constants.ANONYMOUS;
import static ccm.pay2spawn.util.Constants.JSON_PARSER;
import static ccm.pay2spawn.util.Constants.MODID;

public class ChildsplayChecker extends AbstractChecker implements Runnable
{
    public static final ChildsplayChecker INSTANCE = new ChildsplayChecker();
    public final static String NAME = "childsplay";
    public final static String CAT = MODID + '.' + NAME;
    public final static String ENDPOINT = "donate.childsplaycharity.org";
    public final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM YYYY HH:mm:ss zzz", Locale.US);

    static
    {
        SIMPLE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    DonationsBasedHudEntry recentDonationsBasedHudEntry;

    String APIKey = "", APIsecret = "";
    boolean enabled  = true;
    int     interval = 3;

    private ChildsplayChecker()
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
        Hud.INSTANCE.set.add(recentDonationsBasedHudEntry);

        new Thread(this, getName()).start();
    }

    @Override
    public boolean enabled()
    {
        return enabled && !APIKey.isEmpty() && !APIsecret.isEmpty();
    }

    @Override
    public void doConfig(Configuration configuration)
    {
        configuration.addCustomCategoryComment(CAT, "This is the checker for ChildsPlay Charity\nYou need to get your API key from them.");

        enabled = configuration.get(CAT, "enabled", enabled).getBoolean(enabled);
        APIKey = configuration.get(CAT, "APIKey", APIKey).getString();
        APIsecret = configuration.get(CAT, "APIsecret", APIsecret).getString();
        interval = configuration.get(CAT, "interval", interval, "The time in between polls (in seconds).").getInt();

        recentDonationsBasedHudEntry = new DonationsBasedHudEntry(configuration, CAT + ".recentDonations", -1, 2, 5, "$name: $$amount", "-- Recent donations --", CheckerHandler.RECENT_DONATION_COMPARATOR);
    }

    @Override
    public DonationsBasedHudEntry[] getDonationsBasedHudEntries()
    {
        return new DonationsBasedHudEntry[] {recentDonationsBasedHudEntry};
    }

    @Override
    public void run()
    {
        try
        {
            JsonObject root = get(recentDonationsBasedHudEntry.getAmount());
            if (root.get("ack").getAsString().equalsIgnoreCase("Success"))
            {
                JsonArray donations = root.getAsJsonArray("donations");
                for (JsonElement jsonElement : donations)
                {
                    Donation donation = getDonation(JsonNBTHelper.fixNulls(jsonElement.getAsJsonObject()));
                    recentDonationsBasedHudEntry.add(donation);
                    doneIDs.add(donation.id);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        while (true)
        {
            doWait(interval);
            try
            {
                JsonObject root = get(5);
                if (root.get("ack").getAsString().equalsIgnoreCase("Success"))
                {
                    JsonArray donations = root.getAsJsonArray("donations");
                    for (JsonElement jsonElement : donations)
                    {
                        process(getDonation(JsonNBTHelper.fixNulls(jsonElement.getAsJsonObject())), true);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Donation getDonation(JsonObject jsonObject)
    {
        long time = new Date().getTime();
        try
        {
            time = sdf.parse(jsonObject.get("date").getAsString()).getTime();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        String name = jsonObject.get("donor_name").getAsString();
        if (Strings.isNullOrEmpty(name)) name = ANONYMOUS;
        return new Donation(jsonObject.get("id").toString(), jsonObject.get("amount").getAsDouble(), time, name, jsonObject.get("custom").getAsString());
    }

    private JsonObject get(int amount) throws Exception
    {
        String uri = "/api/donations/" + recentDonationsBasedHudEntry.getAmount() + "/json";
        String date = SIMPLE_DATE_FORMAT.format(new Date());
        String sig = getSignature("GET\n\n\n" + date + "\n" + uri);

        URL url = new URL("https://" + ENDPOINT + uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "CP/pay2spawn");

        con.setRequestProperty("Host", ENDPOINT);
        con.setRequestProperty("Date", date);
        con.setRequestProperty("Authorization", sig);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        return JSON_PARSER.parse(in).getAsJsonObject();
    }

    private String getSignature(String s) throws Exception
    {
        return "CP " + APIKey + ":" + URLEncoder.encode(encode(APIsecret, s), "UTF-8");
    }

    public static String encode(String key, String data) throws Exception
    {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        return Base64.encodeToString(sha256_HMAC.doFinal(data.getBytes()), false);
    }
}
