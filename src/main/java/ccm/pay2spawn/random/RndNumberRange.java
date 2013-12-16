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

package ccm.pay2spawn.random;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ccm.pay2spawn.random.RandomRegistry.RANDOM;

/**
 * Picks a random number in between 2 given numbers, int or double
 * Expected syntax: $random(x,y)
 * Format: A random number between x and y
 * Works with: BYTE, SHORT, INT, LONG, FLOAT, DOUBLE
 *
 * @author Dries007
 */
public class RndNumberRange implements IRandomResolver
{
    private final static Pattern PATTERN = Pattern.compile("^\\$random\\((-?\\w+),(-?\\w+)\\)$");

    @Override
    public String solverRandom(int type, String value)
    {
        Matcher matcher = PATTERN.matcher(value);
        switch (type)
        {
            case 1:
            case 2:
            case 3:
            case 4:
            {
                int begin = Integer.parseInt(matcher.group(1));
                int end = Integer.parseInt(matcher.group(2));
                return "" + (begin + RANDOM.nextInt(end - begin));
            }
            case 5:
            case 6:
            {
                double begin = Double.parseDouble(matcher.group(1));
                double end = Double.parseDouble(matcher.group(2));
                return "" + (begin + (end - begin) * RANDOM.nextDouble());
            }
        }
        return value;
    }

    @Override
    public boolean matches(int type, String value)
    {
        return type != 7 && type != 8 && type != 9 && type != 11 && PATTERN.matcher(value).matches();
    }
}
