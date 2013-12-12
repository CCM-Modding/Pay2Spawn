package ccm.pay2spawn;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;

public class DonationCheckerThread extends Thread
{
    final JsonParser parser = new JsonParser();
    final int    interval;
    final String channel;
    final String API_Key;
    final String URL;
    String lastKnownDonation;

    public DonationCheckerThread(int interval, String channel, String API_Key)
    {
        super(DonationCheckerThread.class.getSimpleName());
        this.interval = interval;
        this.channel = channel;
        this.API_Key = API_Key;
        this.URL = "http://donationtrack.nightdev.com/api/poll?channel=" + channel + "&key=" + API_Key;
    }

    @Override
    public void run()
    {
        String message;
        while (true)
        {
            try
            {

                String input = readUrl(URL);
                JsonObject root = parser.parse(input).getAsJsonObject();

                if (root.get("status").getAsString().equals("success"))
                {
                    go(root.getAsJsonArray("mostRecent"));
                }
                else
                {
                    message = root.get("error").getAsString();
                    break;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    synchronized (this)
                    {
                        this.wait(interval * 1000);
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        throw new IllegalArgumentException(message);
    }

    private void go(JsonArray mostRecent)
    {
        Iterator<JsonElement> i = mostRecent.iterator();
        while (i.hasNext())
        {
            JsonObject donation = i.next().getAsJsonObject();

            if (lastKnownDonation == null || lastKnownDonation.equals(donation.get("transactionID").getAsString())) break;

            Pay2Spawn.getRewardsDB().process(donation.get("twitchUsername").getAsString(), donation.get("amount").getAsDouble());
        }
        lastKnownDonation = mostRecent.get(0).getAsJsonObject().get("transactionID").getAsString();
    }

    private String readUrl(String urlString) throws Exception
    {
        BufferedReader reader = null;
        try
        {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) buffer.append(chars, 0, read);

            return buffer.toString();
        }
        finally
        {
            if (reader != null) reader.close();
        }
    }
}
