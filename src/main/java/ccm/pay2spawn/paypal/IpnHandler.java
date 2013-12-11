package ccm.pay2spawn.paypal;

import ccm.pay2spawn.Pay2Spawn;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

public class IpnHandler extends ServerResource
{
    public static void init(int port) throws Exception
    {
        Server server = new Server(Protocol.HTTP, port, IpnHandler.class);
        server.start();
    }

    @Post
    public Representation acceptPost(Representation input) throws IOException
    {
        String requestString = input.getText();
        Pay2Spawn.getLogger().info(requestString);

        URL u = new URL("https://www.sandbox.paypal.com/cgi-bin/webscr"); //TODO: SANDBOX !!
        HttpsURLConnection uc = (HttpsURLConnection) u.openConnection();
        uc.setDoOutput(true);
        uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        uc.setRequestProperty("Host", "www.paypal.com");
        PrintWriter pw = new PrintWriter(uc.getOutputStream());
        pw.println("cmd=_notify-validate&" + requestString);
        pw.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String res = in.readLine();
        in.close();

        Pay2Spawn.getLogger().info(res); // TODO: Catch data, find amount and name, do spawning crap

        return new EmptyRepresentation();
    }
}
