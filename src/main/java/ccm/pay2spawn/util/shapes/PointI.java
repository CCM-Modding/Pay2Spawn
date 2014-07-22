package ccm.pay2spawn.util.shapes;

import ccm.pay2spawn.types.guis.StructureTypeGui;
import ccm.pay2spawn.types.guis.shapes.PointIGui;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static ccm.pay2spawn.util.Constants.BYTE;
import static ccm.pay2spawn.util.Constants.INT;
import static ccm.pay2spawn.util.Constants.NBTTypes;

public class PointI implements IShape
{
    int x, y, z;
    boolean hollow, replaceableOnly;

    public static final HashMap<String, String> typeMap = new HashMap<>();
    public static final String HOLLOWCENTER_KEY = "hollow";
    public static final String REPLACEABLEONLY_KEY = "replaceableOnly";
    public static final String                  X_KEY   = "x";
    public static final String                  Y_KEY   = "y";
    public static final String                  Z_KEY   = "z";

    static
    {
        typeMap.put(HOLLOWCENTER_KEY, NBTTypes[BYTE]);
        typeMap.put(REPLACEABLEONLY_KEY, NBTTypes[BYTE]);

        typeMap.put(X_KEY, NBTTypes[INT]);
        typeMap.put(Y_KEY, NBTTypes[INT]);
        typeMap.put(Z_KEY, NBTTypes[INT]);
    }

    public PointI(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PointI()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public PointI(NBTTagCompound compound)
    {
        fromNBT(compound);
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public int getZ()
    {
        return z;
    }

    public void setZ(int z)
    {
        this.z = z;
    }

    public double distanceTo(PointI p)
    {
        return Math.sqrt((diffX(p) * diffX(p)) + (diffY(p) * diffY(p)) + (diffZ(p) * diffZ(p)));
    }

    public double diffX(PointI p)
    {
        return this.x - p.x;
    }

    public double diffY(PointI p)
    {
        return this.y - p.y;
    }

    public double diffZ(PointI p)
    {
        return this.z - p.z;
    }

    public boolean isValid()
    {
        return this.y >= 0;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof PointI)) return false;

        PointI pointI = (PointI) o;

        return x == pointI.x && y == pointI.y && z == pointI.z;
    }

    @Override
    public int hashCode()
    {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    @Override
    public String toString()
    {
        return "[" + x + ";" + y + ";" + z + "]";
    }

    @Override
    public NBTTagCompound toNBT()
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("x", x);
        compound.setInteger("y", y);
        compound.setInteger("z", z);
        return compound;
    }

    @Override
    public IShape fromNBT(NBTTagCompound compound)
    {
        x = compound.getInteger("x");
        y = compound.getInteger("y");
        z = compound.getInteger("z");
        return this;
    }

    @Override
    public IShape move(int x, int y, int z)
    {
        this.x += x;
        this.y += y;
        this.z += z;

        return this;
    }

    @Override
    public Collection<PointI> getPoints()
    {
        return Arrays.asList(this);
    }

    public PointI addX(int x)
    {
        this.x += x;
        return this;
    }

    public PointI addY(int y)
    {
        this.y += y;
        return this;
    }

    public PointI addZ(int z)
    {
        this.z += z;
        return this;
    }


    @Override
    public PointI getCenter()
    {
        return this;
    }

    @Override
    public IShape setCenter(PointI pointI)
    {
        x = pointI.x;
        y = pointI.y;
        z = pointI.z;
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
    public IShape setReplaceableOnly(boolean replaceableOnly)
    {
        this.replaceableOnly = replaceableOnly;
        return this;
    }

    @Override
    public boolean getReplaceableOnly()
    {
        return replaceableOnly;
    }

    @Override
    public void openGui(int i, JsonObject jsonObject, StructureTypeGui instance)
    {
        new PointIGui(i, jsonObject, instance, typeMap);
    }

    @Override
    public IShape clone()
    {
        return new PointI(x, y, z);
    }
}
