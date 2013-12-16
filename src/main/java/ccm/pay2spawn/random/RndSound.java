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

import ccm.pay2spawn.types.SoundType;

import java.util.regex.Pattern;

/**
 * Picks a random entity name (see SoundsList.txt)
 * Expected syntax: $randomSound or $randomSound(all) or $randomSound(music) or $randomSound(sounds) or $randomSound(streaming)
 * Outcome: The name (id) of 1 random sound (if a list is specified, it picks from that list only.
 * Works with: STRING
 *
 * @author Dries007
 */
public class RndSound implements IRandomResolver
{
    private final static Pattern PATTERN_ALL       = Pattern.compile("^\\$randomSound(\\(all\\))?$");
    private final static Pattern PATTERN_MUSIC     = Pattern.compile("^\\$randomSound\\(music\\)$");
    private final static Pattern PATTERN_SOUNDS    = Pattern.compile("^\\$randomSound\\(sounds\\)$");
    private final static Pattern PATTERN_STREAMING = Pattern.compile("^\\$randomSound\\(streaming\\)$");

    @Override
    public String solverRandom(int type, String value)
    {
        if (PATTERN_ALL.matcher(value).matches()) return RandomRegistry.getRandomFromSet(SoundType.all);
        if (PATTERN_MUSIC.matcher(value).matches()) return RandomRegistry.getRandomFromSet(SoundType.music);
        if (PATTERN_SOUNDS.matcher(value).matches()) return RandomRegistry.getRandomFromSet(SoundType.sounds);
        if (PATTERN_STREAMING.matcher(value).matches()) return RandomRegistry.getRandomFromSet(SoundType.streaming);

        return "";
    }

    @Override
    public boolean matches(int type, String value)
    {
        return type == 8 && (PATTERN_ALL.matcher(value).matches() || PATTERN_MUSIC.matcher(value).matches() || PATTERN_SOUNDS.matcher(value).matches() || PATTERN_STREAMING.matcher(value).matches());
    }
}
