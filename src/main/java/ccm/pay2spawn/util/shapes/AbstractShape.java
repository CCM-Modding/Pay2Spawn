package ccm.pay2spawn.util.shapes;

import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;

import static ccm.pay2spawn.util.Constants.BYTE;
import static ccm.pay2spawn.util.Constants.NBTTypes;

public abstract class AbstractShape implements IShape
{
    public static final HashMap<String, String> typeMap             = new HashMap<>();
    public static final String                  CENTER_KEY          = "center";
    public static final String                  HOLLOWCENTER_KEY    = "hollow";
    public static final String                  REPLACEABLEONLY_KEY = "replaceableOnly";

    static
    {
        typeMap.put(HOLLOWCENTER_KEY, NBTTypes[BYTE]);
        typeMap.put(REPLACEABLEONLY_KEY, NBTTypes[BYTE]);
    }

    PointI center = new PointI();
    boolean hollow, replaceableOnly;

    public AbstractShape(PointI center)
    {
        this.center = center;
    }

    public AbstractShape()
    {

    }

    @Override
    public IShape fromNBT(NBTTagCompound compound)
    {
        center.fromNBT(compound.getCompoundTag(CENTER_KEY));
        hollow = compound.getBoolean(HOLLOWCENTER_KEY);
        replaceableOnly = compound.getBoolean(REPLACEABLEONLY_KEY);
        return this;
    }

    @Override
    public NBTTagCompound toNBT()
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(CENTER_KEY, center.toNBT());
        compound.setBoolean(HOLLOWCENTER_KEY, hollow);
        compound.setBoolean(REPLACEABLEONLY_KEY, replaceableOnly);
        return compound;
    }

    @Override
    public IShape move(int x, int y, int z)
    {
        center.move(x, y, z);
        return this;
    }

    @Override
    public PointI getCenter()
    {
        return center;
    }

    @Override
    public IShape setCenter(PointI pointI)
    {
        center = pointI;
        return this;
    }

    @Override
    public IShape setHollow(boolean hollow)
    {
        this.hollow = hollow;
        return this;
    }

    @Override
    public boolean getHollow()
    {
        return hollow;
    }

    @Override
    public boolean getReplaceableOnly()
    {
        return replaceableOnly;
    }

    @Override
    public IShape setReplaceableOnly(boolean replaceableOnly)
    {
        this.replaceableOnly = replaceableOnly;
        return this;
    }
}
