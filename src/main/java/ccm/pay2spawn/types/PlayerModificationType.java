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

package ccm.pay2spawn.types;

import ccm.pay2spawn.permissions.Node;
import ccm.pay2spawn.types.guis.PlayerModificationTypeGui;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.FoodStats;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static ccm.pay2spawn.util.Constants.*;

public class PlayerModificationType extends TypeBase
{
    public static final String TYPE_KEY      = "type";
    public static final String OPERATION_KEY = "operation";
    public static final String AMOUNT_KEY    = "amount";

    public static final int SET      = 0;
    public static final int ADD      = 1;
    public static final int SUBTRACT = 2;
    public static final int ENABLE   = 3;
    public static final int DISABLE  = 4;

    public static final HashMap<String, String> typeMap = new HashMap<>();

    static
    {
        typeMap.put(TYPE_KEY, NBTTypes[INT]);
        typeMap.put(OPERATION_KEY, NBTTypes[INT]);
        typeMap.put(AMOUNT_KEY, NBTTypes[FLOAT]);
    }

    @Override
    public String getName()
    {
        return "playermodification";
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger(TYPE_KEY, Type.HUNGER.ordinal());
        data.setInteger(OPERATION_KEY, ADD);
        data.setFloat(AMOUNT_KEY, 20F);
        return data;
    }

    @Override
    public void spawnServerSide(EntityPlayer player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        Type.values()[dataFromClient.getInteger(TYPE_KEY)].doOnServer(player, dataFromClient);
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new PlayerModificationTypeGui(rewardID, getName(), data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();
        for (Type type : Type.values()) nodes.add(new Node(getName(), type.name().toLowerCase()));
        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        return new Node(getName(), Type.values()[dataFromClient.getInteger(TYPE_KEY)].name().toLowerCase());
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        switch (id)
        {
            case "type":
                int i = Integer.getInteger(jsonObject.get(TYPE_KEY).getAsString().replace("INT:", ""));
                return Type.values()[i].name().toLowerCase();
            case "operation":
                int j = Integer.getInteger(jsonObject.get(OPERATION_KEY).getAsString().replace("INT:", ""));
                switch (j)
                {
                    case ADD:
                        return "adding";
                    case SUBTRACT:
                        return "subtracting";
                    case SET:
                        return "setting";
                    case ENABLE:
                        return "enabling it" + (jsonObject.has(AMOUNT_KEY) ? " for" : "");
                    case DISABLE:
                        return "disabling it" + (jsonObject.has(AMOUNT_KEY) ? " for" : "");
                }
            case "amount":
                if (jsonObject.has(AMOUNT_KEY)) return NUMBER_FORMATTER.format(jsonObject.get(AMOUNT_KEY).getAsString().replace("FLOAT:", ""));
                else return "";
        }
        return id;
    }

    public static enum Type
    {
        HEALTH(false)
                {
                    @Override
                    public void doOnServer(EntityPlayer player, NBTTagCompound dataFromClient)
                    {
                        switch (dataFromClient.getInteger(OPERATION_KEY))
                        {
                            case ADD:
                                player.heal(dataFromClient.getFloat(AMOUNT_KEY));
                                break;
                            case SUBTRACT:
                                player.heal(-dataFromClient.getFloat(AMOUNT_KEY));
                                break;
                            case SET:
                                player.setHealth(dataFromClient.getFloat(AMOUNT_KEY));
                                break;
                        }
                    }
                },
        HUNGER(false)
                {
                    @Override
                    public void doOnServer(EntityPlayer player, NBTTagCompound dataFromClient)
                    {
                        FoodStats food = player.getFoodStats();
                        switch (dataFromClient.getInteger(OPERATION_KEY))
                        {
                            case ADD:
                                food.addStats((int) dataFromClient.getFloat(AMOUNT_KEY), 0);
                                break;
                            case SUBTRACT:
                                food.addStats((int) -dataFromClient.getFloat(AMOUNT_KEY), 0);
                                break;
                            case SET:
                                food.addStats(-food.getFoodLevel(), 0);
                                food.addStats((int) dataFromClient.getFloat(AMOUNT_KEY), 0);
                                break;
                        }
                    }
                },
        SATURATION(false)
                {
                    @Override
                    public void doOnServer(EntityPlayer player, NBTTagCompound dataFromClient)
                    {
                        FoodStats food = player.getFoodStats();
                        switch (dataFromClient.getInteger(OPERATION_KEY))
                        {
                            case ADD:
                                food.addStats(0, dataFromClient.getFloat(AMOUNT_KEY));
                                break;
                            case SUBTRACT:
                                food.addStats(0, -dataFromClient.getFloat(AMOUNT_KEY));
                                break;
                            case SET:
                                food.addStats(0, -food.getSaturationLevel());
                                food.addStats(0, dataFromClient.getFloat(AMOUNT_KEY));
                                break;
                        }
                    }
                },
        XP(false)
                {
                    @Override
                    public void doOnServer(EntityPlayer player, NBTTagCompound dataFromClient)
                    {
                        switch (dataFromClient.getInteger(OPERATION_KEY))
                        {
                            case ADD:
                                player.addExperience((int) dataFromClient.getFloat(AMOUNT_KEY));
                                break;
                            case SUBTRACT:
                                player.addExperience((int) -dataFromClient.getFloat(AMOUNT_KEY));
                                break;
                        }
                    }
                },
        XP_LEVEL(false)
                {
                    @Override
                    public void doOnServer(EntityPlayer player, NBTTagCompound dataFromClient)
                    {
                        switch (dataFromClient.getInteger(OPERATION_KEY))
                        {
                            case ADD:
                                player.addExperienceLevel((int) dataFromClient.getFloat(AMOUNT_KEY));
                                break;
                            case SUBTRACT:
                                player.addExperienceLevel((int) -dataFromClient.getFloat(AMOUNT_KEY));
                                break;
                        }
                    }
                },
        FLIGHT(true)
                {
                    @Override
                    public void doOnServer(EntityPlayer player, NBTTagCompound dataFromClient)
                    {
                        switch (dataFromClient.getInteger(OPERATION_KEY))
                        {
                            case ENABLE:
                                player.capabilities.allowFlying = true;
                                player.capabilities.isFlying = true;
                                player.sendPlayerAbilities();
                                if (dataFromClient.hasKey(AMOUNT_KEY))
                                {
                                    NBTTagCompound tagCompound = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getCompoundTag("P2S");
                                    tagCompound.setInteger(name(), (int) (dataFromClient.getFloat(AMOUNT_KEY) * 20));
                                    if (!player.getEntityData().hasKey(EntityPlayer.PERSISTED_NBT_TAG)) player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
                                    player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).setTag("P2S", tagCompound);
                                }
                                break;
                            case DISABLE:
                                player.capabilities.allowFlying = false;
                                player.capabilities.isFlying = false;
                                player.sendPlayerAbilities();
                                break;
                        }
                    }

                    @Override
                    public void undo(EntityPlayer player)
                    {
                        player.capabilities.allowFlying = player.capabilities.isCreativeMode;
                        player.capabilities.isFlying = player.capabilities.isCreativeMode;
                        player.sendPlayerAbilities();
                    }
                },
        INVULNERABILITY(true)
                {
                    @Override
                    public void doOnServer(EntityPlayer player, NBTTagCompound dataFromClient)
                    {
                        switch (dataFromClient.getInteger(OPERATION_KEY))
                        {
                            case ENABLE:
                                player.capabilities.disableDamage = true;
                                player.sendPlayerAbilities();
                                if (dataFromClient.hasKey(AMOUNT_KEY))
                                {
                                    NBTTagCompound tagCompound = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getCompoundTag("P2S");
                                    tagCompound.setInteger(name(), (int) (dataFromClient.getFloat(AMOUNT_KEY) * 20));
                                    if (!player.getEntityData().hasKey(EntityPlayer.PERSISTED_NBT_TAG)) player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
                                    player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).setTag("P2S", tagCompound);
                                }
                                break;
                            case DISABLE:
                                player.capabilities.disableDamage = false;
                                player.sendPlayerAbilities();
                                break;
                        }
                    }

                    @Override
                    public void undo(EntityPlayer player)
                    {
                        player.capabilities.disableDamage = player.capabilities.isCreativeMode;
                        player.sendPlayerAbilities();
                    }
                };
        private boolean timable;

        Type(boolean timable)
        {
            this.timable = timable;
        }

        public abstract void doOnServer(EntityPlayer player, NBTTagCompound dataFromClient);

        public boolean isTimable()
        {
            return timable;
        }

        public void undo(EntityPlayer player)
        {

        }
    }
}
