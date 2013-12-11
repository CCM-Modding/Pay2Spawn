package ccm.pay2spawn.util;

import ccm.pay2spawn.network.P2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;

public enum EnumSpawnType
{
    ITEM
            {
                @Override
                public void spawnFromData(EntityPlayer player, NBTTagCompound data)
                {
                    ItemStack itemStack = ItemStack.loadItemStackFromNBT(data);
                    System.out.println(Archive.MODID + ": Giving " + player.getDisplayName() + " item " + itemStack); //TODO: debug line
                    EntityItem entityItem = player.dropPlayerItem(itemStack);
                    entityItem.delayBeforeCanPickup = 0;
                }

                @Override
                public void createAndSend(NBTTagCompound nbt, Object data)
                {
                    ItemStack itemStack = (ItemStack) data;
                    Minecraft.getMinecraft().thePlayer.addChatMessage(EnumChatFormatting.GREEN + "[" + nbt.getString("donator") + " donated " + nbt.getString(
                            "amount") + "] " + EnumChatFormatting.WHITE + itemStack.getDisplayName() + " given!");
                    send(this, getNBTfromData(data));
                }

                @Override
                public Object makeRandomData()
                {
                    return new ItemStack(Item.appleGold);
                }

                @Override
                public NBTTagCompound getNBTfromData(Object data)
                {
                    return ((ItemStack) data).writeToNBT(new NBTTagCompound());
                }
            },
    EFFECT
            {
                @Override
                public void spawnFromData(EntityPlayer player, NBTTagCompound data)
                {
                    PotionEffect effect = PotionEffect.readCustomPotionEffectFromNBT(data);
                    System.out.println(Archive.MODID + ": Giving " + player.getDisplayName() + " effect " + effect); //TODO: debug line
                    player.addPotionEffect(effect);
                }

                @Override
                public void createAndSend(NBTTagCompound nbt, Object data)
                {
                    PotionEffect effect = (PotionEffect) data;
                    Minecraft.getMinecraft().thePlayer.addChatMessage(EnumChatFormatting.GREEN + "[" + nbt.getString("donator") + " donated " + nbt.getString(
                            "amount") + "] " + EnumChatFormatting.WHITE + StatCollector.translateToLocal(effect.getEffectName()) + " applied!");
                    send(this, getNBTfromData(data));
                }

                @Override
                public Object makeRandomData()
                {
                    Potion potion = null;
                    while (potion == null)
                    {
                        potion = Potion.potionTypes[Helper.RANDOM.nextInt(Potion.potionTypes.length)];
                    }
                    return new PotionEffect(potion.getId(), (int) (Helper.RANDOM.nextDouble() * 1000));
                }

                @Override
                public NBTTagCompound getNBTfromData(Object data)
                {
                    return ((PotionEffect) data).writeCustomPotionEffectToNBT(new NBTTagCompound());
                }
            },
    ENTITY
            {
                @Override
                public void spawnFromData(EntityPlayer player, NBTTagCompound data)
                {
                    System.out.println(Archive.MODID + ": Spawning " + data.getString("name") + " near " + player.getDisplayName()); //TODO: debug line
                    double x, y, z;

                    y = player.posY + 1;

                    x = player.posX + (RADIUS - Helper.RANDOM.nextInt(RADIUS));
                    z = player.posZ + (RADIUS - Helper.RANDOM.nextInt(RADIUS));

                    Entity entity = EntityList.createEntityByName(data.getString("name"), player.getEntityWorld());
                    if (entity != null && entity instanceof EntityLivingBase)
                    {
                        EntityLiving entityliving = (EntityLiving) entity;
                        entity.setLocationAndAngles(x, y, z, MathHelper.wrapAngleTo180_float(player.getEntityWorld().rand.nextFloat() * 360.0F), 0.0F);
                        entityliving.rotationYawHead = entityliving.rotationYaw;
                        entityliving.renderYawOffset = entityliving.rotationYaw;
                        entityliving.onSpawnWithEgg((EntityLivingData) null);
                        player.getEntityWorld().spawnEntityInWorld(entity);
                        entityliving.playLivingSound();
                    }
                }

                @Override
                public void createAndSend(NBTTagCompound nbt, Object data)
                {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(EnumChatFormatting.GREEN + "[" + nbt.getString("donator") + " donated " + nbt.getString(
                            "amount") + "] " + EnumChatFormatting.WHITE + data + " spawned!");
                    send(this, getNBTfromData(data));
                }

                @Override
                public Object makeRandomData()
                {
                    return Helper.getRndEntity();
                }

                @Override
                public NBTTagCompound getNBTfromData(Object data)
                {
                    NBTTagCompound dataTag = new NBTTagCompound();
                    dataTag.setString("name", (String) data);
                    return dataTag;
                }
            };

    public static final int RADIUS = 15;

    public abstract void spawnFromData(EntityPlayer player, NBTTagCompound data);

    public abstract void createAndSend(NBTTagCompound nbt, Object data);

    public abstract Object makeRandomData();

    public abstract NBTTagCompound getNBTfromData(Object data);

    private static void send(EnumSpawnType type, NBTTagCompound data)
    {
        new P2SPacket(type, data).sendToServer();
    }
}
