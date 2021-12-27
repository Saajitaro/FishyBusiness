# Fishy Business
This is a mod to provide a somewhat hacky fix to the config reset bug present in the [Mine Mine no Mi](https://pixelatedw.xyz/mine-mine-no-mi/home) mod.  
The bug: Seemingly randomly, the config would reset to default values fairly frequently.  
**Any Mine Mine no Mi server owner is free to use this solution.**

## How do I setup the mod?
- Put the mod in your mods folder,
- Start server,
- Stop server after fully loaded,
- Put your ideal mineminenomi-common.toml file in ```./config/fishybusiness/perfect config/```,
- Start server.

## What does the mod do?
The logic is as follows:
- On player join/leave,
- Compare ideal config (```./config/fishybusiness/perfect config/mineminenomi-common.toml```) with current config (```./config/mineminenomi-common.toml```),
- If not the same, replace current config with ideal config, and stop the server.
The mod will also log when the config was reset and which players were online, stored in ```./logs/fishybusiness.log```.

When the server is stopped, it needs to be started again manually.
It's a small price to pay, as it prevents nasty situations involving ```"One Devil Fruit per World Logic"```, ```"Keep Stats after Death"``` config options, and more.

Check out my own One Piece server at https://onepiecefactions.weebly.com/.
