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

import ccm.pay2spawn.Pay2Spawn;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.HashMap;

import static ccm.pay2spawn.util.Constants.GSON;
import static ccm.pay2spawn.util.Constants.JSON_PARSER;

/**
 * Permission system stuff
 *
 * @author Dries007
 */
public class PermissionsDB
{
    private HashMap<String, Player> playerDB = new HashMap<>();
    private HashMap<String, Group>  groupDB  = new HashMap<>();

    public void save()
    {
        try
        {
            File file = getFile();
            if (!file.exists()) //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            JsonObject rootObject = new JsonObject();

            JsonArray players = new JsonArray();
            for (Player player : playerDB.values()) players.add(player.toJson());
            rootObject.add("players", players);

            JsonArray groups = new JsonArray();
            for (Group group : groupDB.values()) groups.add(group.toJson());
            rootObject.add("groups", groups);

            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(GSON.toJson(rootObject));
            bw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void load() throws IOException
    {
        File file = getFile();
        if (file.exists())
        {
            JsonObject rootObject = JSON_PARSER.parse(new FileReader(file)).getAsJsonObject();

            for (JsonElement element : rootObject.getAsJsonArray("players"))
            {
                Player player = new Player(element.getAsJsonObject());
                playerDB.put(player.getName(), player);
            }

            for (JsonElement element : rootObject.getAsJsonArray("groups"))
            {
                Group group = new Group(element.getAsJsonObject());
                groupDB.put(group.getName(), group);
            }
        }
        else
        {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            JsonObject rootObject = new JsonObject();
            rootObject.add("players", new JsonArray());
            rootObject.add("groups", new JsonArray());
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(GSON.toJson(rootObject));
            bw.close();
        }
    }

    private File getFile()
    {
        return new File(Pay2Spawn.getFolder(), "Permissions.json");
    }

    public boolean check(String entityName, Node node)
    {
        if (!playerDB.containsKey(entityName)) return false;
        Player player = playerDB.get(entityName);
        if (player.hasSpecificPermissionFor(node)) return true;
        for (String groupName : player.getGroups())
        {
            if (checkGroup(groupName, node)) return true;
        }
        return false;
    }

    public boolean checkGroup(String groupName, Node node)
    {
        return groupDB.containsKey(groupName) && groupDB.get(groupName).hasPermissionFor(node);
    }

    public void newGroup(String name, String parent)
    {
        groupDB.put(name, new Group(name, parent));
    }

    public void remove(String name)
    {
        groupDB.remove(name);
    }

    public Group getGroup(String name)
    {
        return groupDB.get(name);
    }

    public Player getPlayer(String name)
    {
        if (!playerDB.containsKey(name)) newPlayer(name);
        return playerDB.get(name);
    }

    public Iterable getGroups()
    {
        return groupDB.keySet();
    }

    public Iterable getPlayers()
    {
        return playerDB.keySet();
    }

    public void newPlayer(String name)
    {
        playerDB.put(name, new Player(name));
    }
}
