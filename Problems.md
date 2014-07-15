# Problems?
***Please read through this before posting an issue.***
[Make sure you know what a stacktrace is.](http://www.dries007.net/downloads/stacktrace.png)
In all bugreports, include a stacktrace (or crash report) and if requested your json file.

**When the game actually crashes, please look at the entire "---- Minecraft Crash Report ----" section, and post that ONLY. We don't need the full log (wich is huge) unless we ask for it. Thanks. It gets saved in the "crash-reports" folder.**

If you are dealing with a crash report, look above the section from FML for helpfull [P2S] lines:
>    [SEVERE] [ForgeModLoader] Fatal errors were detected during the transition from ... to .... Loading cannot continue
>    [SEVERE] [ForgeModLoader] <br>
>    	mcp{8.09} [Minecraft Coder Pack] (minecraft.jar) Unloaded->...<br>
>    	FML{6.4.45.953} [Forge Mod Loader] (Pay2Spawn) Unloaded->...<br>
>    	Forge{9.11.1.953} [Minecraft Forge] (Pay2Spawn) Unloaded->...<br>
If that isn't anywhere in the log, copy the 5 to 10 lines above and below the stacktrace, and keep an eye out for [P2S] in the console.

## Type 0: Errors that you don't need to worry about
Ignore these errors:

* Anything with "*at ccm.libs...." in the stacktrace. (Its not us.)

## Type 1: Recurring errors
Errors that spam every time the mod tries to connect to Twitch or Streamdonations.net

* Make sure your config has up to date API keys and a correct (Case sensitive!) channel name.
* You can only have the twitch sub tracker if you are a twitch partner. (aka you have a sub button).

### Common errors:
* Wrong channel id or Streamdonations API-key:

>    [SEVERE] [P2S] ERROR TYPE 1: Error while contacting Streamdonations.
>    [INFO] [STDERR] java.lang.IllegalArgumentException: Not a valid channel and/or key

* Error in Twitch auth token:

>    [SEVERE] [P2S] ERROR TYPE 1: Error while contacting Twitch api.
>    [INFO] [STDERR] java.io.IOException: Server returned HTTP response code: 401 for URL: https://api.twitch.tv/kraken/...

* You aren't a twitch partner/you have no sub button:

>    [SEVERE] [P2S] ERROR TYPE 1: Error while contacting Twitch api.
>    [INFO] [STDERR] java.io.IOException: Server returned HTTP response code: 422 for URL: https://api.twitch.tv/kraken/...

## Type 2: Startup errors
We try to crash here if there is an issue with the configs. We try not to have it happen in a later stage where you might be live, but we can't guarantee that everything will work. (Especially with random tags, they only get solved once at the startup. Not all possibilities are tested.)

### Common errors:
* Your JSON is invalid. (Please use our build in GUI, or for experts an actual JSON editor.):

>    [INFO] [STDOUT] java.lang.IllegalStateException: This is not a JSON Array.
>    [INFO] [STDOUT] 	at com.google.gson.JsonElement.getAsJsonArray(JsonElement.java:100)
>    [INFO] [STDOUT] 	at ccm.pay2spawn.util.RewardsDB.<init>(RewardsDB.java:73)

* One of your rewards is invalid. (Please use our build in GUI, or for experts an actual JSON editor.):

>    [INFO] [STDOUT] java.lang.NullPointerException
>    [INFO] [STDOUT] 	at ccm.pay2spawn.util.Reward.<init>(Reward.java:59)
>    [INFO] [STDOUT] 	at ccm.pay2spawn.util.RewardsDB.<init>(RewardsDB.java:77)

* One of your rewards NBT data is invalid. (Please use our build in GUI, or for experts an actual JSON editor.) I know the NBT to JSON thing is weard but it is the best solution apart from saving our data as actual NBT. (Witch is not human readable without an editor):

>    [SEVERE] [P2S] ERROR TYPE 2: Error in reward ...'s NBT data.

* You forgot to add double quotes around a line in the black- or whitelist:

>    java.lang.RuntimeException: Unknown character '...' in '/config/Pay2Spawn/Pay2Spawn.cfg:...'

## Type 3: Errors on donation
Most likely, there is a corrupt NBT tag that is not getting found during the startup scan. We attempt at all cost to avoid this, but I recommend you make test donations for every possible configuration before you go live for the first time.

These errors will apear on the server so make sure to test SSP or on a server you can get the logs from. 
These errors might also crash the server or potentially (by spawning an invalid entity for example) crash other clients and make worlds unplayable.

### Common errors:
* There was an error with the spawning of a reward. Look at the stacktrace for more info. The type and data associalted with the reward will be printed, make sure to include them in your issue, if you think its our fault.

>    [SEVERE] [P2S] ERROR TYPE 3: Error spawning a reward on the server.

~~Dries007
