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

            // Amount of rewards
            {
                Metrics.Graph graph = metrics.createGraph("Amount of rewards");
                for (final EnumSpawnType type : EnumSpawnType.values())
                {
                    graph.addPlotter(new Metrics.Plotter(type.name().toLowerCase())
                    {
                        @Override
                        public int getValue()
                        {
                            return Pay2Spawn.getRewardsDB().amountsPerType[type.ordinal()];
                        }
                    });
                }
            }
            // Avg price of rewards
            {
                Metrics.Graph graph = metrics.createGraph("Average price");
                for (final EnumSpawnType type : EnumSpawnType.values())
                {
                    graph.addPlotter(new Metrics.Plotter(type.name().toLowerCase())
                    {
                        @Override
                        public int getValue()
                        {
                            return (int) Pay2Spawn.getRewardsDB().avgPricePerType[type.ordinal()];
                        }
                    });
                }
            }
            // Max price of rewards
            {
                Metrics.Graph graph = metrics.createGraph("Maximum price");
                for (final EnumSpawnType type : EnumSpawnType.values())
                {
                    graph.addPlotter(new Metrics.Plotter(type.name().toLowerCase())
                    {
                        @Override
                        public int getValue()
                        {
                            return (int) Pay2Spawn.getRewardsDB().maxPricePerType[type.ordinal()];
                        }
                    });
                }
            }
            // Min price of rewards
            {
                Metrics.Graph graph = metrics.createGraph("Minimum price");
                for (final EnumSpawnType type : EnumSpawnType.values())
                {
                    graph.addPlotter(new Metrics.Plotter(type.name().toLowerCase())
                    {
                        @Override
                        public int getValue()
                        {
                            return (int) Pay2Spawn.getRewardsDB().minPricePerType[type.ordinal()];
                        }
                    });
                }
            }

            metrics.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
