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

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

/**
 * ModID and P2S and so much more...
 *
 * @author Dries007
 */
public class Constants
{
    public static final String   NAME                = "Pay2Spawn";
    public static final String   MODID               = "P2S";
    /**
     * Network related
     */
    public static final String   CHANNEL_STATUS      = MODID + "_s";
    public static final String   CHANNEL_MESSAGE     = MODID + "_mg";
    public static final String   CHANNEL_REWARD      = MODID + "_r";
    public static final String   CHANNEL_TEST        = MODID + "_t";
    public static final String   CHANNEL_NBT_REQUEST = MODID + "_req";
    public static final String   CHANNEL_MUSIC       = MODID + "_m";
    public static final String[] CHANNELS            = {CHANNEL_STATUS, CHANNEL_MESSAGE, CHANNEL_REWARD, CHANNEL_TEST, CHANNEL_NBT_REQUEST, CHANNEL_MUSIC};
    public static final String   BASECAT_TRACKERS    = "P2S_trackers";
    public static final String   CAPEURL             = "https://raw.github.com/CCM-Modding/Pay2Spawn/master/capes.txt";
    public static final String   MUSICURL            = "https://raw.github.com/CCM-Modding/Pay2Spawn/master/files/music.zip";
    public static final String[] NBTTypes            = new String[] {"END", "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BYTE[]", "STRING", "LIST", "COMPOUND", "INT[]"};
    /**
     * Donation data
     */
    public static final String   DONATION_USERNAME   = "twitchUsername";
    public static final String   DONATION_AMOUNT     = "amount";
    public static final String   DONATION_NOTE       = "note";
    public static final String   ANONYMOUS           = "Anonymous";

    public static final String CUSTOMHTML = "customHTML";

    /**
     * Global helpers
     */
    public static final Random RANDOM = new Random();

    public static final Joiner JOINER_COMMA_SPACE = Joiner.on(", ").skipNulls();
    public static final Joiner JOINER_DOT         = Joiner.on(".").skipNulls();

    public static final JsonParser JSON_PARSER = new JsonParser();
    public static final Gson       GSON        = new GsonBuilder().setPrettyPrinting().create();
    public static final Gson       GSON_NOPP   = new GsonBuilder().create();

    public static final NumberFormat NUMBER_FORMATTER   = new DecimalFormat("#.##");
    public static final NumberFormat CURRENCY_FORMATTER = new DecimalFormat("0.00");

    /**
     * NBT constants
     */
    public static final int END        = 0;
    public static final int BYTE       = 1;
    public static final int SHORT      = 2;
    public static final int INT        = 3;
    public static final int LONG       = 4;
    public static final int FLOAT      = 5;
    public static final int DOUBLE     = 6;
    public static final int BYTE_ARRAY = 7;
    public static final int STRING     = 8;
    public static final int LIST       = 9;
    public static final int COMPOUND   = 10;
    public static final int INT_ARRAY  = 11;
}
