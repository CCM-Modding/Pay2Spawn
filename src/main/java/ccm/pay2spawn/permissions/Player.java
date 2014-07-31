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

package ccm.pay2spawn.permissions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.HashSet;

/**
 * Permission system stuff
 *
 * @author Dries007
 */
public class Player
{
    private HashSet<String> groups        = new HashSet<>();
    private HashSet<Node>   overrideNodes = new HashSet<>();
    private String name;

    public Player(JsonObject jsonObject)
    {
        name = jsonObject.get("name").getAsString();
        if (jsonObject.has("groups")) for (JsonElement groupName : jsonObject.getAsJsonArray("groups")) groups.add(groupName.getAsString());
        if (jsonObject.has("overrideNodes")) for (JsonElement node : jsonObject.getAsJsonArray("overrideNodes")) overrideNodes.add(new Node(node.getAsString()));
    }

    public Player(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public JsonElement toJson()
    {
        JsonObject root = new JsonObject();
        root.addProperty("name", getName());
        JsonArray groups = new JsonArray();
        for (String group : this.getGroups()) groups.add(new JsonPrimitive(group));
        root.add("groups", groups);

        JsonArray nodes = new JsonArray();
        for (Node node : this.overrideNodes) nodes.add(new JsonPrimitive(node.toString()));
        root.add("overrides", nodes);

        return root;
    }

    public boolean hasSpecificPermissionFor(Node requestNode)
    {
        for (Node hadNode : overrideNodes) if (hadNode.matches(requestNode)) return true;
        return false;
    }

    public Iterable<? extends String> getGroups()
    {
        return groups;
    }

    public boolean removeGroup(String group)
    {
        return groups.remove(group);
    }

    public void addGroup(String groupName)
    {
        groups.add(groupName);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return groups.equals(player.groups) && name.equals(player.name) && overrideNodes.equals(player.overrideNodes);

    }

    @Override
    public int hashCode()
    {
        int result = groups.hashCode();
        result = 31 * result + overrideNodes.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public void addNode(Node node)
    {
        overrideNodes.add(node);
    }

    public boolean removeNode(Node node)
    {
        return overrideNodes.remove(node);
    }

    public HashSet<String> getNodes()
    {
        HashSet<String> strings = new HashSet<>();
        for (Node node : overrideNodes) strings.add(node.toString());
        return strings;
    }
}
