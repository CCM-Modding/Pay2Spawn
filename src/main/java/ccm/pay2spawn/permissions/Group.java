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

public class Group
{
    private HashSet<Node> nodes = new HashSet<>();
    private String name;
    private String parent;

    public Group(JsonObject jsonObject)
    {
        name = jsonObject.get("name").getAsString();
        if (jsonObject.has("parent")) parent = jsonObject.get("parent").getAsString();
        for (JsonElement node : jsonObject.getAsJsonArray("nodes")) nodes.add(new Node(node.getAsString()));
    }

    public Group(String name, String parent)
    {
        this.name = name;
        this.parent = parent;
    }

    public String getName()
    {
        return name;
    }

    public String getParent()
    {
        return parent;
    }

    public JsonElement toJson()
    {
        JsonObject root = new JsonObject();
        root.addProperty("name", getName());
        root.addProperty("parent", getParent());

        JsonArray nodes = new JsonArray();
        for (Node node : this.nodes) nodes.add(new JsonPrimitive(node.toString()));
        root.add("nodes", nodes);

        return root;
    }

    public boolean hasPermissionFor(Node requestNode)
    {
        if (parent != null && PermissionsHandler.getDB().checkGroup(parent, requestNode)) return true;
        for (Node hadNode : nodes) if (hadNode.matches(requestNode)) return true;
        return false;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        return name.equals(group.name) && nodes.equals(group.nodes) && parent.equals(group.parent);

    }

    @Override
    public int hashCode()
    {
        int result = nodes.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + parent.hashCode();
        return result;
    }

    public void setParent(String parent)
    {
        this.parent = parent;
    }

    public void addNode(String nodeString)
    {
        nodes.add(new Node(nodeString));
    }

    public boolean removeNode(String nodeString)
    {
        return nodes.remove(new Node(nodeString));
    }

    public HashSet<String> getNodes()
    {
        HashSet<String> strings = new HashSet<>();
        for (Node node : nodes) strings.add(node.toString());
        return strings;
    }
}
