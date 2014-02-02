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

import ccm.pay2spawn.hud.CountDownHudEntry;
import ccm.pay2spawn.hud.Hud;
import ccm.pay2spawn.misc.Reward;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;

import static ccm.pay2spawn.util.Constants.MODID;

public class ClientTickHandler implements IScheduledTickHandler
{
    HashSet<QueEntry> entries = new HashSet<>();

    public static final ClientTickHandler INSTANCE = new ClientTickHandler();

    private CountDownHudEntry countDownHudEntry;

    private ClientTickHandler() {}

    @Override
    public int nextTickSpacing()
    {
        return 20;
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
        Iterator<QueEntry> rewardIterator = entries.iterator();
        while (rewardIterator.hasNext())
        {
            QueEntry queEntry = rewardIterator.next();
            if (queEntry.remaining == 0)
            {
                queEntry.send();
                rewardIterator.remove();
            }
            else
            {
                if (countDownHudEntry.getPosition() != 0 && queEntry.addToHUD) countDownHudEntry.lines.add(countDownHudEntry.getFormat().replace("$name", queEntry.reward.getName()).replace("$time", queEntry.remaining + ""));
                queEntry.remaining--;
            }
        }
        if (countDownHudEntry.getPosition() != 0 && !countDownHudEntry.lines.isEmpty())
        {
            if (!Strings.isNullOrEmpty(countDownHudEntry.getHeader())) Helper.addWithEmptyLines(countDownHudEntry.lines, countDownHudEntry.getHeader());
        }
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {

    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.CLIENT);
    }

    @Override
    public String getLabel()
    {
        return MODID + "_ClientTicker";
    }

    public void add(Reward reward, JsonObject donation, boolean addToHUD, Reward actualReward)
    {
        entries.add(new QueEntry(reward, donation, addToHUD, actualReward));
    }

    public void init()
    {
        countDownHudEntry = new CountDownHudEntry("countdown", 1, "$name incoming in $time sec.", "-- Countdown --");
        Hud.INSTANCE.set.add(countDownHudEntry);
    }

    public class QueEntry
    {
        int        remaining;
        JsonObject donation;
        Reward     reward;
        Reward     actualReward;
        boolean    addToHUD;

        public QueEntry(Reward reward, JsonObject donation, boolean addToHUD, Reward actualReward)
        {
            this.remaining = reward.getCountdown();
            this.donation = donation;
            this.reward = reward;
            this.addToHUD = addToHUD;
            this.actualReward = actualReward;
        }

        public void send()
        {
            reward.send(donation, actualReward);
        }
    }
}
