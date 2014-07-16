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

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ccm.pay2spawn.util.Constants.*;

/**
 * Produces a random color array
 * Expected syntax: $randomRGB(x)
 * Format: An int array with x amount of random colors
 * Works with: INT[], INT, STRING
 *
 * @author Dries007
 */
public class RndColors implements IRandomResolver
{
    private static final Pattern PATTERN = Pattern.compile("\\$randomRGB\\((\\w+)\\)");

    @Override
    public String getIdentifier()
    {
        return "$randomRGB";
    }

    @Override
    public String solverRandom(int type, String value)
    {
        Matcher mRGB = PATTERN.matcher(value);
        if (type == INT_ARRAY)
        {
            JsonArray colors = new JsonArray();
            mRGB.find();
            for (int i = 0; i < Integer.parseInt(mRGB.group(1)); i++) colors.add(new JsonPrimitive((RANDOM.nextInt(200) << 16) + (RANDOM.nextInt(200) << 8) + RANDOM.nextInt(200)));
            return mRGB.replaceFirst(colors.toString());
        }
        else
        {
            return mRGB.replaceFirst("" + ((RANDOM.nextInt(200) << 16) + (RANDOM.nextInt(200) << 8) + RANDOM.nextInt(200)));
        }
    }

    @Override
    public boolean matches(int type, String value)
    {
        return (type == INT_ARRAY || type == INT || type == STRING) && PATTERN.matcher(value).find();
    }
}
