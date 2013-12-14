# Config options
These is an explanation of a simple json database file.
You can fully customise this file.

[Pro tip: Use this for all your json needs.](http://jsoneditoronline.org/)

## How rewards are selected:
A reward is selected based on the amount donated.
The name of the reward or the message in paypal DO NOTHING.

If a donation matches no reward, a default message shows. (See .cfg file)

## File structure
<table>
<tr>
<td>
<code>
    [<br>
      {<br>
        "name": "EXAMPLE",<br>
        "amount": 10,<br>
        "message": "\u0026a[$name donated $$amount]",<br>
        "rewards": [<br>
          {<br>
            "type": "xporbs",<br>
            "data": {<br>
              "amoutOfOrbs": "INT:100"<br>
            }<br>
          }<br>
        ]<br>
      }<br>
    ]
</code>
</td><td>
*Opening of a list (1)*<br>
*Opening of an object (2)*<br>
*The name of the reward*<br>
*The amount of $ this reward is worth*<br>
*The message shown when the reward is spawned (Vars usable)*<br>
*Opening of the list of things you actually get (3)*<br>
*Opening of the 1th thing (4)*<br>
*The type of this thing*<br>
*The data belonging to this thing (5) (Vars usable)*<br>
*An example of data associated with the type.*<br>
*end of (5)*<br>
*end of (4)*<br>
*end of (3). You can add a comma and go back to 3.*<br>
*end of (2)*<br>
*end of (1). You can add a comma and go back to 1.*<br>
</td>
</tr>
<table>

## Variables
If you actually read all the things up to this point, you will have seen (Vars usable).
This means you can use $name, $amount and $note to represent the donators name, the amount donated and the message left in PayPal.

###Example:
starting from (4) in previous example.
<table>
<tr>
<td>
<code>
    {<br>
      "type": "item",<br>
      "data": {<br>
        "id": "SHORT:322",<br>
        "Damage": "SHORT:0",<br>
        "Count": "BYTE:1",<br>
        "tag": {<br>
          "display": {<br>
            "Name": "STRING:$name"<br>
          }<br>
        }<br>
      }<br>
    }<br>
</code>
</td><td>
*(4)*<br>
*The type of this thing*<br>
*The data belonging to this thing (5) (Vars usable)*<br>
*Item ID*<br>
*Damage value (aka metadata)*<br>
*Count (aka stacksize)*<br>
*NBT data of the ItemStack (6)*<br>
*The display subtag (7)*<br>
*The item name. (example uses $name to give the item the name of the donator)*<br>
*end of (7)*<br>
*end of (6)*<br>
*end of (5)*<br>
*end of (4)*<br>
</td>
</tr>
<table>

## NBT tags in JSON
Make sure you **NEVER** remove the "BYTE:" or "STRING:" or any other 'CAPITALS:' part of a tag. This allows the json to be converted in the Minecraft NBT format automatically.
As you can see in your default config (specifically the fireworks one) the data tags can become quite complex. 

[My advice: Don't do this manually.](http://jsoneditoronline.org/)
