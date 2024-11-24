# MTCB
Mindustry-Telegram Chat Bridge plugin enables seamless communication between Mindustry in-game chat and Telegram. With this bridge, players and administrators can exchange messages between the game and a designated Telegram group or channel in real-time.

## Setup

Clone this repository first.
To edit the plugin display name and other data, take a look at `plugin.json`.
Edit the name of the project itself by going into `settings.gradle`.

## Basic Usage

See `src/mtcb/MindustryTelegramChatBridge.java` for some basic commands and event handlers.  
Every main plugin class must extend `Plugin`. Make sure that `plugin.json` points to the correct main plugin class.

Please note that the plugin system is in beta, and as such is subject to changes.

## Building a Jar

`gradlew jar` / `./gradlew jar`

Output jar should be in `build/libs`.


## Installing

Simply place the output jar from the step above in your server's `config/mods` directory and restart the server.
List your currently installed plugins/mods by running the `mods` command.


## Configurating

Command: `/tgconfig <TOKEN> <ID> <transfer_commands>`
- **TOKEN** - this parameter requires a bot token, through which the plugin will send messages.
- **ID** - ID of the chat in which the bot will send and receive messages; in other chats the bot simply will not work.
- **transfer_commands** - bool variable that determines whether to send commands (/) to Mindustry in Telegram.

The command must be executed from the Mindustry client, it can only be executed by administrators, when setting parameters, they will be automatically saved and upon subsequent server starts, they will be automatically unloaded/

## Using

Once the plugin is set up, it works as follows:

- **From Mindustry to Telegram**: Messages sent in the Mindustry chat are instantly forwarded to the linked Telegram chat. No additional steps are required.
- **From Telegram to Mindustry**: To send a message from Telegram to the Mindustry chat, reply to a message from the bot in the Telegram group with your desired text. The plugin will then forward your reply to the in-game chat.

This system ensures smooth communication between both platforms while keeping the process simple and intuitive.


