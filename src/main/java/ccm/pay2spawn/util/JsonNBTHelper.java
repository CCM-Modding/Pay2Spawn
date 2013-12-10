/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Dries K. Aka Dries007
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

import com.google.gson.*;
import net.minecraft.nbt.*;

import java.util.Map;

/**
 * This is nearly full Json (gson) to NBT converter.
 * Not working:
 * NBT IntArrays and ByteArrays to Json.
 * Json null to NBT
 *
 * @author Dries007
 */
public class JsonNBTHelper
{
    public static JsonElement parseNBT(NBTBase element)
    {
        switch (element.getId())
        {
            // 0 = END
            case 1:
                return new JsonPrimitive(((NBTTagByte) element).data);
            case 2:
                return new JsonPrimitive(((NBTTagShort) element).data);
            case 3:
                return new JsonPrimitive(((NBTTagInt) element).data);
            case 4:
                return new JsonPrimitive(((NBTTagLong) element).data);
            case 5:
                return new JsonPrimitive(((NBTTagFloat) element).data);
            case 6:
                return new JsonPrimitive(((NBTTagDouble) element).data);
            // 7 = BYTE[]
            case 8:
                return new JsonPrimitive(((NBTTagString) element).data);
            case 9:
                return parseNBT((NBTTagList) element);
            case 10:
                return parseNBT((NBTTagCompound) element);
            // 11 = INT[]
            default:
                return null;
        }
    }

    public static JsonArray parseNBT(NBTTagList nbtArray)
    {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < nbtArray.tagCount(); i++)
        {
            jsonArray.add(parseNBT(nbtArray.tagAt(i)));
        }
        return jsonArray;
    }

    public static JsonObject parseNBT(NBTTagCompound compound)
    {
        JsonObject jsonObject = new JsonObject();
        for (Object object : compound.getTags())
        {
            NBTBase tag = (NBTBase) object;
            jsonObject.add(tag.getName(), parseNBT(tag));
        }
        return jsonObject;
    }

    public static NBTBase parseJSON(JsonElement element)
    {
        if (element.isJsonObject())
            return parseJSON(element.getAsJsonObject());
        else if (element.isJsonArray())
            return parseJSON(element.getAsJsonArray());
        else if (element.isJsonPrimitive())
            return parseJSON(element.getAsJsonPrimitive());

        return null;
    }

    public static NBTBase parseJSON(JsonPrimitive element)
    {
        if (element.isString()) return new NBTTagString("", element.getAsString());
        else if (element.isBoolean()) return new NBTTagByte("", (byte) (element.getAsBoolean() ? 1 : 0));
        else if (element.isNumber())
        {
            if (element.getAsNumber() instanceof Byte) return new NBTTagByte("", element.getAsByte());
            else if (element.getAsNumber() instanceof Short) return new NBTTagShort("", element.getAsShort());
            else if (element.getAsNumber() instanceof Integer) return new NBTTagInt("", element.getAsInt());
            else if (element.getAsNumber() instanceof Long) return new NBTTagLong("", element.getAsLong());
            else if (element.getAsNumber() instanceof Float) return new NBTTagFloat("", element.getAsFloat());
            else if (element.getAsNumber() instanceof Double) return new NBTTagDouble("", element.getAsDouble());
            else if (element.getAsNumber() instanceof Integer) return new NBTTagInt("", element.getAsInt());
        }

        return null;
    }

    public static NBTTagCompound parseJSON(JsonObject data)
    {
        NBTTagCompound root = new NBTTagCompound();
        for (Map.Entry<String, JsonElement> entry : data.entrySet()) root.setTag(entry.getKey(), parseJSON(entry.getValue()));
        return root;
    }

    public static NBTTagList parseJSON(JsonArray data)
    {
        NBTTagList list = new NBTTagList();
        for (JsonElement element : data) list.appendTag(parseJSON(element));
        return list;
    }
}
