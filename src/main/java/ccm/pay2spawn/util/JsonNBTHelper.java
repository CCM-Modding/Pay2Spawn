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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
    /**
     * To avoid idiocy later we need to store all things as a string with the type in the string. :(
     * Please tell your users about this!
     *
     * @see ccm.pay2spawn.util.JsonNBTHelper#parseJSON(com.google.gson.JsonPrimitive)
     */
    public static JsonElement parseNBT(NBTBase element)
    {
        switch (element.getId())
        {
            // 0 = END
            case 1:
                return new JsonPrimitive(NBTBase.NBTTypes[element.getId()] + ":" + ((NBTTagByte) element).data);
            case 2:
                return new JsonPrimitive(NBTBase.NBTTypes[element.getId()] + ":" + ((NBTTagShort) element).data);
            case 3:
                return new JsonPrimitive(NBTBase.NBTTypes[element.getId()] + ":" + ((NBTTagInt) element).data);
            case 4:
                return new JsonPrimitive(NBTBase.NBTTypes[element.getId()] + ":" + ((NBTTagLong) element).data);
            case 5:
                return new JsonPrimitive(NBTBase.NBTTypes[element.getId()] + ":" + ((NBTTagFloat) element).data);
            case 6:
                return new JsonPrimitive(NBTBase.NBTTypes[element.getId()] + ":" + ((NBTTagDouble) element).data);
            case 7:
                return parseNBT((NBTTagByteArray) element);
            case 8:
                return new JsonPrimitive(NBTBase.NBTTypes[element.getId()] + ":" + ((NBTTagString) element).data);
            case 9:
                return parseNBT((NBTTagList) element);
            case 10:
                return parseNBT((NBTTagCompound) element);
            case 11:
                return parseNBT((NBTTagIntArray) element);
            default:
                return null;
        }
    }

    public static JsonPrimitive parseNBT(NBTTagIntArray nbtArray)
    {
        JsonArray jsonArray = new JsonArray();
        for (int i : nbtArray.intArray)
            jsonArray.add(new JsonPrimitive(i));
        return new JsonPrimitive("INT[]:" + jsonArray.toString());
    }

    public static JsonPrimitive parseNBT(NBTTagByteArray nbtArray)
    {
        JsonArray jsonArray = new JsonArray();
        for (int i : nbtArray.byteArray)
            jsonArray.add(new JsonPrimitive(i));
        return new JsonPrimitive("BYTE[]:" + jsonArray.toString());
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
        if (element.isJsonObject()) return parseJSON(element.getAsJsonObject());
        else if (element.isJsonArray()) return parseJSON(element.getAsJsonArray());
        else if (element.isJsonPrimitive()) return parseJSON(element.getAsJsonPrimitive());

        return null;
    }

    /**
     * There is no way to detect number types and NBT is picky about this. Lets hope the original type id is there, otherwise we are royally screwed.
     */
    public static NBTBase parseJSON(JsonPrimitive element)
    {
        String string = element.getAsString();
        if (string.contains(":"))
        {
            for (int id = 0; id < NBTBase.NBTTypes.length; id++)
            {
                if (string.startsWith(NBTBase.NBTTypes[id] + ":"))
                {
                    String value = string.replace(NBTBase.NBTTypes[id] + ":", "");
                    switch (id)
                    {
                        // 0 = END
                        case 1:
                            return new NBTTagByte("", Byte.parseByte(value));
                        case 2:
                            return new NBTTagShort("", Short.parseShort(value));
                        case 3:
                            return new NBTTagInt("", Integer.parseInt(value));
                        case 4:
                            return new NBTTagLong("", Long.parseLong(value));
                        case 5:
                            return new NBTTagFloat("", Float.parseFloat(value));
                        case 6:
                            return new NBTTagDouble("", Double.parseDouble(value));
                        case 7:
                            return parseJSONByteArray(value);
                        case 8:
                            return new NBTTagString("", value);
                        // 9 = LIST != JsonPrimitive
                        // 10 = COMPOUND != JsonPrimitive
                        case 11:
                            return parseJSONIntArray(value);
                    }
                }
            }
        }

        // Now it becomes guesswork.
        if (element.isString()) return new NBTTagString("", string);
        if (element.isBoolean()) return new NBTTagByte("", (byte) (element.getAsBoolean() ? 1 : 0));

        Number n = element.getAsNumber();
        if (n instanceof Byte) return new NBTTagByte("", n.byteValue());
        if (n instanceof Short) return new NBTTagShort("", n.shortValue());
        if (n instanceof Integer) return new NBTTagInt("", n.intValue());
        if (n instanceof Long) return new NBTTagLong("", n.longValue());
        if (n instanceof Float) return new NBTTagFloat("", n.floatValue());
        if (n instanceof Double) return new NBTTagDouble("", n.doubleValue());

        throw new NumberFormatException(element.toString() + " is was not able to be parsed.");
    }

    public static NBTTagByteArray parseJSONByteArray(String value)
    {
        JsonArray in = Helper.PARSER.parse(value).getAsJsonArray();
        byte[] out = new byte[in.size()];
        for (int i = 0; i < in.size(); i++)
            out[i] = in.get(i).getAsByte();
        return new NBTTagByteArray("", out);
    }

    public static NBTTagIntArray parseJSONIntArray(String value)
    {
        JsonArray in = Helper.PARSER.parse(value).getAsJsonArray();
        int[] out = new int[in.size()];
        for (int i = 0; i < in.size(); i++)
            out[i] = in.get(i).getAsInt();
        return new NBTTagIntArray("", out);
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
