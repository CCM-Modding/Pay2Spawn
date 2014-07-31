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

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Handler for the event, keeps track of all active IHudEntry s
 *
 * @author Dries007
 */
public class Hud
{
    public static final Hud                INSTANCE = new Hud();
    public final        HashSet<IHudEntry> set      = new HashSet<>();

    private Hud() {}

    public void render(ArrayList<String> left, ArrayList<String> right, ArrayList<String> bottomLeft, ArrayList<String> bottomRight)
    {
        for (IHudEntry hudEntry : set)
        {
            switch (hudEntry.getPosition())
            {
                case 1:
                    hudEntry.addToList(left);
                    break;
                case 2:
                    hudEntry.addToList(right);
                    break;
                case 3:
                    hudEntry.addToList(bottomLeft);
                    break;
                case 4:
                    hudEntry.addToList(bottomRight);
                    break;
            }
        }
    }
}
