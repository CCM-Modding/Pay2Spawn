package ccm.pay2spawn.random;

import java.util.regex.Pattern;

/**
 * I'm insane for doing this but oh well.
 *
 * @author Dries007
 */
public class RndVariable implements IRandomResolver
{
    private static final Pattern PATTERN = Pattern.compile("\\$var\\(.*\\)");

    @Override
    public String getIdentifier()
    {
        return "$var(";
    }

    @Override
    public String solverRandom(int type, String value)
    {
        //TODO: implement
        return value;
    }

    @Override
    public boolean matches(int type, String value)
    {
        return PATTERN.matcher(value).matches();
    }
}
