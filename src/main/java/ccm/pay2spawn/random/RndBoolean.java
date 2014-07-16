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

import java.util.regex.Pattern;

import static ccm.pay2spawn.util.Constants.*;

/**
 * Makes a boolean random
 * Expected syntax: $random
 * Outcome: 0 or 1
 * Works with: BYTE, STRING
 *
 * @author Dries007
 */
public class RndBoolean implements IRandomResolver
{
    private static final Pattern PATTERN = Pattern.compile("\\$random\\(\\)");

    @Override
    public String getIdentifier()
    {
        return "$random";
    }

    @Override
    public String solverRandom(int type, String value)
    {
        return PATTERN.matcher(value).replaceFirst(RANDOM.nextBoolean() ? "1" : "0");
    }

    @Override
    public boolean matches(int type, String value)
    {
        return (type == BYTE || type == STRING) && PATTERN.matcher(value).find();
    }
}
