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

package ccm.pay2spawn.types.guis;

import ccm.pay2spawn.configurator.Configurator;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.util.HashMap;

public abstract class HelperGuiBase
{
    public       JFrame                  frame;
    public final String                  name;
    public final int                     rewardID;
    public final HashMap<String, String> typeMap;
    public       JsonObject              data;

    public HelperGuiBase(final int rewardID, final String name, final JsonObject inputData, final HashMap<String, String> typeMap)
    {
        Configurator.instance.attachGui(this);
        this.rewardID = rewardID;
        this.name = name;
        this.typeMap = typeMap;
        this.data = inputData;
    }

    public abstract void readJson();

    public abstract void updateJson();

    public String readValue(String key, JsonObject jsonObject)
    {
        if (jsonObject == null || !jsonObject.has(key)) return "";
        String string = jsonObject.get(key).getAsString();
        return string.substring(string.indexOf(":") + 1);
    }

    public void storeValue(String key, JsonObject jsonObject, Object value)
    {
        jsonObject.addProperty(key, typeMap.containsKey(key) ? typeMap.get(key) + ":" + value.toString() : value.toString());
    }

    public void close()
    {
        frame.dispose();
    }
}
