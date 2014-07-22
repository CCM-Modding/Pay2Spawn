/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Dries K. Aka Dries007 and the CCM modding crew.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ccm.pay2spawn.util;

import ccm.libs.org.mcstats.Metrics;
import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.checkers.AbstractChecker;
import ccm.pay2spawn.checkers.CheckerHandler;
import ccm.pay2spawn.checkers.TwitchChecker;
import cpw.mods.fml.common.FMLCommonHandler;

import java.io.IOException;

import static ccm.pay2spawn.util.Constants.NAME;

/**
 * Collect all of the data!
 *
 * @author Dries007
 */
public class MetricsHelper
{
    public static double  totalMoney;
    public static Metrics metrics;

    public static void init()
    {
        if (metrics != null) return;
        try
        {
            metrics = new Metrics(NAME + "2", Pay2Spawn.getVersion());
            if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            {
                metrics.createGraph("RewardCount").addPlotter(new Metrics.Plotter()
                {
                    @Override
                    public int getValue()
                    {
                        return Pay2Spawn.getRewardsDB().getRewards().size();
                    }
                });
                metrics.createGraph("MaxReward").addPlotter(new Metrics.Plotter()
                {
                    @Override
                    public int getValue()
                    {
                        return (int) (Helper.findMax(Pay2Spawn.getRewardsDB().getAmounts()));
                    }
                });
                metrics.createGraph("ChannelName").addPlotter(new Metrics.Plotter(TwitchChecker.INSTANCE.getChannel())
                {
                    @Override
                    public int getValue()
                    {
                        return 1;
                    }
                });
                Metrics.Graph graph = metrics.createGraph("Providers");
                for (final AbstractChecker abstractChecker : CheckerHandler.getAbstractCheckers())
                {
                    graph.addPlotter(new Metrics.Plotter(abstractChecker.getName())
                    {
                        @Override
                        public int getValue()
                        {
                            return abstractChecker.enabled() ? 1 : 0;
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
