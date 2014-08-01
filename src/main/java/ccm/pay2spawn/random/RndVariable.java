package ccm.pay2spawn.random;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * I'm insane for doing this but oh well.
 *
 * Use case: You want the same random value in 2 or more places.
 * You need to use the full expression everywhere.
 * The first time the solver comes across the tag with a new name, it will solve the random.
 * The second (or more) time if just fills in the value from memory.
 *
 * How to use: $var(name, somerandomthing)
 *
 * Example: $var(1, random(1, 10))
 *
 * @author Dries007
 */
public class RndVariable implements IRandomResolver
{
    private static final Pattern PATTERN = Pattern.compile("\\$var\\((.*?), ?([^$]*)\\)");
    private static final HashMap<String, String> VARMAP = new HashMap<>();

    @Override
    public String getIdentifier()
    {
        return "$var(";
    }

    @Override
    public String solverRandom(int type, String value)
    {
        Matcher matcher = PATTERN.matcher(value);
        if (matcher.find())
        {
            String var = matcher.group(1);
            if (!VARMAP.containsKey(var)) VARMAP.put(var, RandomRegistry.solveRandom(type, "$" + matcher.group(2)));
            return matcher.replaceFirst(VARMAP.get(var));
        }

        return value;
    }

    @Override
    public boolean matches(int type, String value)
    {
        return PATTERN.matcher(value).find();
    }

    public static void reset()
    {
        VARMAP.clear();
    }
}
