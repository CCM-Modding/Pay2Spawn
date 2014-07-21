package ccm.pay2spawn.util.shapes;

import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractShape implements IShape
{
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
        center.fromNBT(compound.getCompoundTag("center"));
        hollow = compound.getBoolean("hollow");
        replaceableOnly = compound.getBoolean("replaceableOnly");
        return this;
    }

    @Override
    public NBTTagCompound toNBT()
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("center", center.toNBT());
        compound.setBoolean("hollow", hollow);
        compound.setBoolean("replaceableOnly", replaceableOnly);
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
