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

package ccm.pay2spawn.hud;

import ccm.pay2spawn.Pay2Spawn;
import ccm.pay2spawn.misc.P2SConfig;
import ccm.pay2spawn.util.Helper;
import net.minecraftforge.common.Configuration;

import java.util.ArrayList;

public class StatisticsHudEntry implements IHudEntry
{
    public final ArrayList<String> strings = new ArrayList<>();
    final int position, amount;
    final String header, format;

    public StatisticsHudEntry(String configCat, int maxAmount, int defaultPosition, int defaultAmount, String defaultFormat, String defaultHeader)
    {
        int amount1;
        Configuration config = Pay2Spawn.getConfig().configuration;

        position = config.get(P2SConfig.HUD + "." + configCat, "position", defaultPosition, "0 = off, 1 = left top, 2 = right top, 3 = left bottom, 4 = right bottom.").getInt(defaultPosition);
        amount1 = config.get(P2SConfig.HUD + "." + configCat, "amount", defaultAmount).getInt(defaultAmount);
        if (maxAmount != -1 && amount1 > maxAmount) amount1 = maxAmount;
        amount = amount1;

        format = Helper.formatColors(config.get(P2SConfig.HUD + "." + configCat, "format", defaultFormat).getString());
        header = Helper.formatColors(config.get(P2SConfig.HUD + "." + configCat, "header", defaultHeader, "Empty for no header. Use \\n for a blank line.").getString()).trim();

        config.save();
    }

    @Override
    public int getPosition()
    {
        return position;
    }

    @Override
    public int getAmount()
    {
        return amount;
    }

    @Override
    public String getHeader()
    {
        return header;
    }

    @Override
    public String getFormat()
    {
        return format;
    }

    @Override
    public void addToList(ArrayList<String> list)
    {
        if (position != 0)
        {
            list.addAll(strings);
        }
    }
}
