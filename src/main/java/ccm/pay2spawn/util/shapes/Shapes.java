package ccm.pay2spawn.util.shapes;

import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.HashMap;

public class Shapes
{
    public static final String                  SHAPE_KEY = "shape";
    public static final HashMap<String, IShape> MAP       = new HashMap<>();
    public static final ArrayList<String>       LIST      = new ArrayList<>();

    static
    {
        register(new Box());
        register(new Cylinder());
        register(new Pillar());
        register(new PointI());
        register(new Sphere());
    }

    private static void register(IShape shape)
    {
        LIST.add(shape.getClass().getSimpleName());
        MAP.put(shape.getClass().getSimpleName(), shape);
    }

    public static IShape loadShape(NBTTagCompound compound)
    {
        return MAP.get(compound.getString(SHAPE_KEY)).fromNBT(compound).cloneShape();
    }

    public static NBTTagCompound storeShape(IShape shape)
    {
        NBTTagCompound compound = shape.toNBT();
        compound.setString(SHAPE_KEY, shape.getClass().getSimpleName());
        return compound;
    }
}
