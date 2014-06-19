package ccm.pay2spawn.misc;

import ccm.pay2spawn.Pay2Spawn;

import java.util.regex.Pattern;

import static ccm.pay2spawn.util.Constants.ANONYMOUS;

public class Donation
{
    public String id;
    public double amount;
    public String username;
    public String note;
    public long time;

    public Donation(String id, double amount, long time)
    {
        this.id = id;
        this.amount = amount;
        this.time = time;

        this.username = "Anonymous";
        this.note = "";
    }

    public Donation(String id, double amount, long time, String username)
    {
        this.id = id;
        this.amount = amount;
        this.time = time;

        for (Pattern p : Pay2Spawn.getConfig().blacklist_Name_p)
        {
            if (p.matcher(username).matches())
            {
                username = ANONYMOUS;
                break;
            }
        }
        this.username = username;
        this.note = "";
    }

    public Donation(String id, double amount, long time, String username, String note)
    {
        this.id = id;
        this.amount = amount;
        this.time = time;

        for (Pattern p : Pay2Spawn.getConfig().blacklist_Name_p)
        {
            if (p.matcher(username).matches())
            {
                username = ANONYMOUS;
                break;
            }
        }
        this.username = username;
        for (Pattern p : Pay2Spawn.getConfig().blacklist_Note_p)
        {
            if (p.matcher(note).matches())
            {
                note = "";
                break;
            }
        }
        this.note = note;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Donation)) return false;

        Donation donation = (Donation) o;

        if (Double.compare(donation.amount, amount) != 0) return false;
        if (time != donation.time) return false;
        if (!id.equals(donation.id)) return false;
        if (!note.equals(donation.note)) return false;
        if (!username.equals(donation.username)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = id.hashCode();
        temp = Double.doubleToLongBits(amount);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + username.hashCode();
        result = 31 * result + note.hashCode();
        result = 31 * result + (int) (time ^ (time >>> 32));
        return result;
    }
}
