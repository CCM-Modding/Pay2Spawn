package ccm.pay2spawn.util;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.types.TypeBase;
import ccm.pay2spawn.types.TypeRegistry;
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
                for (final TypeBase type : TypeRegistry.getAllTypes())
                {
                    graph.addPlotter(new Metrics.Plotter(type.getName().toLowerCase())
                    {
                        @Override
                        public int getValue()
                        {
                            return type.getAmountOfRewards();
                        }
                    });
                }
            }
            // Avg price of rewards
            {
                Metrics.Graph graph = metrics.createGraph("Average price");
                for (final TypeBase type : TypeRegistry.getAllTypes())
                {
                    graph.addPlotter(new Metrics.Plotter(type.getName().toLowerCase())
                    {
                        @Override
                        public int getValue()
                        {
                            return type.getAvgPrice();
                        }
                    });
                }
            }
            // Max price of rewards
            {
                Metrics.Graph graph = metrics.createGraph("Maximum price");
                for (final TypeBase type : TypeRegistry.getAllTypes())
                {
                    graph.addPlotter(new Metrics.Plotter(type.getName().toLowerCase())
                    {
                        @Override
                        public int getValue()
                        {
                            return (int) type.getMaxPrice();
                        }
                    });
                }
            }
            // Min price of rewards
            {
                Metrics.Graph graph = metrics.createGraph("Minimum price");
                for (final TypeBase type : TypeRegistry.getAllTypes())
                {
                    graph.addPlotter(new Metrics.Plotter(type.getName().toLowerCase())
                    {
                        @Override
                        public int getValue()
                        {
                            return (int) type.getMinPrice();
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
