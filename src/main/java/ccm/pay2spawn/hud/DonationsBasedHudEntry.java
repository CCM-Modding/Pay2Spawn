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

import ccm.pay2spawn.misc.Donation;
import ccm.pay2spawn.util.Helper;
import com.google.common.base.Strings;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DonationsBasedHudEntry implements IHudEntry
{
    final ArrayList<String> strings = new ArrayList<>();
    List<Donation> donations = new ArrayList<>();
    final int position, amount;
    final String header, format;
    final Comparator<Donation> comparator;

    public DonationsBasedHudEntry(Configuration config, String configCat, int maxAmount, int defaultPosition, int defaultAmount, String defaultFormat, String defaultHeader, Comparator<Donation> comparator)
    {
        position = config.get(configCat, "position", defaultPosition, "0 = off, 1 = left top, 2 = right top, 3 = left bottom, 4 = right bottom.").getInt(defaultPosition);
        int amount1 = config.get(configCat, "amount", defaultAmount, "maximum: " + maxAmount).getInt(defaultAmount);
        if (maxAmount != -1 && amount1 > maxAmount) amount1 = maxAmount;
        amount = amount1;

        format = Helper.formatColors(config.get(configCat, "format", defaultFormat).getString());
        header = Helper.formatColors(config.get(configCat, "header", defaultHeader, "Empty for no header. Use \\n for a blank line.").getString()).trim();
        this.comparator = comparator;
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

    public void add(Donation donation)
    {
        donations.add(donation);
        Collections.sort(donations, comparator);
        update();
    }

    private void update()
    {
        while (donations.size() > getAmount())
        {
            donations.remove(donations.size() - 1);
        }

        strings.clear();
        if (!Strings.isNullOrEmpty(this.getHeader())) Helper.addWithEmptyLines(this.strings, this.getHeader());
        for (Donation donation : donations)
        {
            strings.add(Helper.formatText(this.getFormat(), donation, null));
        }
    }
}

