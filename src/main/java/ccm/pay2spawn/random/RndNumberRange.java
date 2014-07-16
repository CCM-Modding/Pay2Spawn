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

import static ccm.pay2spawn.util.Constants.*;

/**
 * Picks a random number in between 2 given numbers, int or double
 * Expected syntax: $random(x,y)
 * Format: A random number between x and y
 * Works with: BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, STRING
 *
 * @author Dries007
 */
public class RndNumberRange implements IRandomResolver
{
    private final static Pattern PATTERN = Pattern.compile("\\$random\\((-?\\w+), ?(-?\\w+)\\)");

    @Override
    public String getIdentifier()
    {
        return "$random(";
    }

    @Override
    public String solverRandom(int type, String value)
    {
        Matcher matcher = PATTERN.matcher(value);
        matcher.find();
        switch (type)
        {
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case STRING:
            {
                int begin = Integer.parseInt(matcher.group(1));
                int end = Integer.parseInt(matcher.group(2));
                return matcher.replaceFirst("" + (begin + RANDOM.nextInt(end - begin)));
            }
            case FLOAT:
            case DOUBLE:
            {
                double begin = Double.parseDouble(matcher.group(1));
                double end = Double.parseDouble(matcher.group(2));
                return matcher.replaceFirst("" + (begin + (end - begin) * RANDOM.nextDouble()));
            }
        }
        return value;
    }

    @Override
    public boolean matches(int type, String value)
    {
        return type != BYTE_ARRAY && type != LIST && type != INT_ARRAY && PATTERN.matcher(value).find();
    }
}
