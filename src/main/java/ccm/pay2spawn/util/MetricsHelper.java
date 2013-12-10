package ccm.pay2spawn.util;

import ccm.pay2spawn.Pay2Spawn;
import org.mcstats.Metrics;

import java.io.IOException;

public class MetricsHelper
{
    private static Metrics metrics;

    public static void init()
    {
        if (metrics != null) return;
        try
        {
            metrics = new Metrics(Archive.NAME, Pay2Spawn.getVersion());



            metrics.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
