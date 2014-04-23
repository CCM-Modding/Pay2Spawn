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

import ccm.pay2spawn.random.RandomRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.*;

import java.util.Map;

import static ccm.pay2spawn.util.Constants.*;

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
            case BYTE:
                return new JsonPrimitive(NBTTypes[element.getId()] + ":" + ((NBTTagByte) element).func_150290_f());
            case SHORT:
                return new JsonPrimitive(NBTTypes[element.getId()] + ":" + ((NBTTagShort) element).func_150289_e());
            case INT:
                return new JsonPrimitive(NBTTypes[element.getId()] + ":" + ((NBTTagInt) element).func_150287_d());
            case LONG:
                return new JsonPrimitive(NBTTypes[element.getId()] + ":" + ((NBTTagLong) element).func_150291_c());
            case FLOAT:
                return new JsonPrimitive(NBTTypes[element.getId()] + ":" + ((NBTTagFloat) element).func_150288_h());
            case DOUBLE:
                return new JsonPrimitive(NBTTypes[element.getId()] + ":" + ((NBTTagDouble) element).func_150286_g());
            case BYTE_ARRAY:
                return parseNBT((NBTTagByteArray) element);
            case STRING:
                return new JsonPrimitive(NBTTypes[element.getId()] + ":" + ((NBTTagString) element).func_150285_a_());
            case LIST:
                return parseNBT((NBTTagList) element);
            case COMPOUND:
                return parseNBT((NBTTagCompound) element);
            case INT_ARRAY:
                return parseNBT((NBTTagIntArray) element);
            default:
                return null;
        }
    }

    public static JsonPrimitive parseNBT(NBTTagIntArray nbtArray)
    {
        JsonArray jsonArray = new JsonArray();
        for (int i : nbtArray.func_150302_c()) jsonArray.add(new JsonPrimitive(i));
        return new JsonPrimitive(NBTTypes[nbtArray.getId()] + ":" + jsonArray.toString());
    }

    public static JsonPrimitive parseNBT(NBTTagByteArray nbtArray)
    {
        JsonArray jsonArray = new JsonArray();
        for (int i : nbtArray.func_150292_c()) jsonArray.add(new JsonPrimitive(i));
        return new JsonPrimitive(NBTTypes[nbtArray.getId()] + jsonArray.toString());
    }

    public static JsonArray parseNBT(NBTTagList nbtArray)
    {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < nbtArray.tagCount(); i++)
        {
            // TODO: Yell at Mojang for this...
            switch (nbtArray.func_150303_d())
            {
                case 5:
                    jsonArray.add(parseNBT(new NBTTagFloat(nbtArray.func_150308_e(i))));
                    break;
                case 6:
                    jsonArray.add(parseNBT(new NBTTagDouble(nbtArray.func_150309_d(i))));
                    break;
                case 8:
                    jsonArray.add(parseNBT(new NBTTagString(nbtArray.getStringTagAt(i))));
                    break;
                case 10:
                    jsonArray.add(parseNBT(nbtArray.getCompoundTagAt(i)));
                    break;
                case 11:
                    jsonArray.add(parseNBT(new NBTTagIntArray(nbtArray.func_150306_c(i))));
                    break;
            }

        }
        return jsonArray;
    }

    public static JsonObject parseNBT(NBTTagCompound compound)
    {
        JsonObject jsonObject = new JsonObject();
        for (Object object : compound.func_150296_c())
        {
            jsonObject.add(object.toString(), parseNBT(compound.getTag(object.toString())));
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
            for (int id = 0; id < NBTTypes.length; id++)
            {
                if (string.startsWith(NBTTypes[id] + ":"))
                {
                    String value = string.replace(NBTTypes[id] + ":", "");
                    value = RandomRegistry.solveRandom(id, value);
                    switch (id)
                    {
                        // 0 = END
                        case BYTE:
                            return new NBTTagByte(Byte.parseByte(value));
                        case SHORT:
                            return new NBTTagShort(Short.parseShort(value));
                        case INT:
                            return new NBTTagInt(Integer.parseInt(value));
                        case LONG:
                            return new NBTTagLong(Long.parseLong(value));
                        case FLOAT:
                            return new NBTTagFloat(Float.parseFloat(value));
                        case DOUBLE:
                            return new NBTTagDouble(Double.parseDouble(value));
                        case BYTE_ARRAY:
                            return parseJSONByteArray(value);
                        case STRING:
                            return new NBTTagString(value);
                        // 9 = LIST != JsonPrimitive
                        // 10 = COMPOUND != JsonPrimitive
                        case INT_ARRAY:
                            return parseJSONIntArray(value);
                    }
                }
            }
        }

        // Now it becomes guesswork.
        if (element.isString()) return new NBTTagString(string);
        if (element.isBoolean()) return new NBTTagByte((byte) (element.getAsBoolean() ? 1 : 0));

        Number n = element.getAsNumber();
        if (n instanceof Byte) return new NBTTagByte(n.byteValue());
        if (n instanceof Short) return new NBTTagShort(n.shortValue());
        if (n instanceof Integer) return new NBTTagInt(n.intValue());
        if (n instanceof Long) return new NBTTagLong(n.longValue());
        if (n instanceof Float) return new NBTTagFloat(n.floatValue());
        if (n instanceof Double) return new NBTTagDouble(n.doubleValue());

        throw new NumberFormatException(element.toString() + " is was not able to be parsed.");
    }

    public static NBTTagByteArray parseJSONByteArray(String value)
    {
        JsonArray in = JSON_PARSER.parse(value).getAsJsonArray();
        byte[] out = new byte[in.size()];
        for (int i = 0; i < in.size(); i++) out[i] = in.get(i).getAsByte();
        return new NBTTagByteArray(out);
    }

    public static NBTTagIntArray parseJSONIntArray(String value)
    {
        JsonArray in = JSON_PARSER.parse(value).getAsJsonArray();
        int[] out = new int[in.size()];
        for (int i = 0; i < in.size(); i++) out[i] = in.get(i).getAsInt();
        return new NBTTagIntArray(out);
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

    public static JsonElement cloneJSON(JsonElement toClone)
    {
        return JSON_PARSER.parse(toClone.toString());
    }

    public static JsonElement fixNulls(JsonElement element)
    {
        if (element.isJsonNull()) return new JsonPrimitive("");
        if (element.isJsonObject()) return fixNulls(element.getAsJsonObject());
        if (element.isJsonArray()) return fixNulls(element.getAsJsonArray());
        if (element.isJsonPrimitive()) return fixNulls(element.getAsJsonPrimitive());
        return null;
    }

    public static JsonPrimitive fixNulls(JsonPrimitive primitive)
    {
        if (primitive.isBoolean()) return new JsonPrimitive(primitive.getAsBoolean());
        if (primitive.isNumber()) return new JsonPrimitive(primitive.getAsNumber());
        if (primitive.isString()) return new JsonPrimitive(primitive.getAsString());
        return JSON_PARSER.parse(primitive.toString()).getAsJsonPrimitive();
    }

    public static JsonArray fixNulls(JsonArray array)
    {
        JsonArray newArray = new JsonArray();
        for (JsonElement element : array) newArray.add(fixNulls(element));
        return newArray;
    }

    public static JsonObject fixNulls(JsonObject object)
    {
        JsonObject newObject = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) newObject.add(entry.getKey(), fixNulls(entry.getValue()));
        return newObject;
    }
}
