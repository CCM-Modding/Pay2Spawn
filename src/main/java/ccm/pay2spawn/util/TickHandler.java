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

import ccm.pay2spawn.P2SConfig;
import ccm.pay2spawn.Pay2Spawn;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.event.Event;
import scala.tools.nsc.util.MultiHashMap;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class TickHandler implements IScheduledTickHandler
{
    HashSet<QueEntry> entries = new HashSet<>();

    public static final TickHandler INSTANCE = new TickHandler();

    @Override
    public int nextTickSpacing()
    {
        return 20;
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
        P2SConfig.HudSettings hudSettings = Pay2Spawn.getConfig().hud;
        EventHandler.COUNTDOWN.clear();
        if (hudSettings.countdown != 0)
        {
            String header = hudSettings.countdown_header.trim();
            if (!Strings.isNullOrEmpty(header)) EventHandler.COUNTDOWN.add(header);
        }
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
                if (hudSettings.countdown != 0) EventHandler.COUNTDOWN.add(hudSettings.countdown_format.replace("$name", queEntry.reward.getName()).replace("$time", queEntry.remaining + ""));
                queEntry.remaining --;
            }
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
        return Constants.MODID + "_Countdown";
    }

    public void add(Reward reward, JsonObject donation)
    {
        entries.add(new QueEntry(reward, donation));
    }

    public class QueEntry
    {
        int remaining;
        JsonObject donation;
        Reward reward;

        public QueEntry(Reward reward, JsonObject donation)
        {
            this.remaining = reward.getCountdown();
            this.donation = donation;
            this.reward = reward;
        }

        public void send()
        {
            reward.send(donation);
        }
    }
}
