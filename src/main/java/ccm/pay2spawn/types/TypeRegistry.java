package ccm.pay2spawn.types;

import net.minecraftforge.common.Configuration;

import java.util.Collection;
import java.util.HashMap;

public class TypeRegistry
{
    private static HashMap<String, TypeBase> map = new HashMap<>();

    public static void register(TypeBase typeBase)
    {
        if (map.put(typeBase.getName().toLowerCase(), typeBase) != null) throw new IllegalArgumentException("Duplicate type!");
    }

    public static TypeBase getByName(String name)
    {
        return map.get(name.toLowerCase());
    }

    public static void doConfig(Configuration configuration)
    {
        for (TypeBase type : map.values())
        {
            type.doConfig(configuration);
        }
    }

    public static Collection<TypeBase> getAllTypes()
    {
        return map.values();
    }

    public static int getAmount()
    {
        return map.size();
    }
}
