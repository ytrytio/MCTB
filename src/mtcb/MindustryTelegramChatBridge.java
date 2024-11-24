package mtcb;

import arc.Core;
import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.io.*;
import java.util.Properties;
import mindustry.game.EventType.PlayerChatEvent;
import mindustry.gen.Call;
import mindustry.mod.Plugin;

public class MindustryTelegramChatBridge extends Plugin {

    private TelegramBot bot;
    private String botToken = "";
    private long chatId = 0;
    private boolean isConfigured = false;
    private boolean sendCommandMessages = false;
    private static final String CONFIG_FILE = "tgconfig.properties";

    @Override
    public void init() {
        loadConfig();
        if (isConfigured) {
            initializeBot();
        }
        Events.on(PlayerChatEvent.class, this::handleMindustryChat);
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<mindustry.gen.Player>register(
            "tgconfig",
            "<token> <chatId> <sendCommandMessages>",
            "Настроить Telegram бота",
            (args, player) -> {
                if (!player.admin) {
                    player.sendMessage(
                        "[scarlet]Только администраторы могут использовать эту команду."
                    );
                    return;
                }

                botToken = args[0];
                chatId = Long.parseLong(args[1]);
                sendCommandMessages = Boolean.parseBoolean(args[2]);

                saveConfig();
                initializeBot();

                player.sendMessage(
                    "[green]Конфигурация Telegram бота обновлена и сохранена."
                );
            }
        );
    }

    private void initializeBot() {
        if (bot != null) {
            bot.removeGetUpdatesListener();
        }

        bot = new TelegramBot(botToken);

        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (
                    update.message() != null &&
                    update.message().chat().id() == chatId
                ) {
                    if (
                        update.message().replyToMessage() != null &&
                        update.message().replyToMessage().from() != null &&
                        update.message().replyToMessage().from().isBot()
                    ) {
                        String telegramUsername = update
                            .message()
                            .from()
                            .firstName();
                        String messageText = update.message().text();
                        sendMessageToMindustry(telegramUsername, messageText);
                    }
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        isConfigured = true;
    }

    private void handleMindustryChat(PlayerChatEvent event) {
        if (!isConfigured) return;

        String playerName = event.player.name;
        String message = event.message;

        if (!sendCommandMessages && message.startsWith("/")) {
            return;
        }

        String cleanedPlayerName = removeColorTags(playerName);
        cleanedPlayerName = removeInvalidChars(cleanedPlayerName);

        message = removeAtSymbol(message);

        sendMessageToTelegram(
            cleanedPlayerName + ": " + escapeTelegramMarkdown(message)
        );
    }

    private void sendMessageToTelegram(String text) {
        if (isConfigured) {
            bot.execute(
                new SendMessage(chatId, text).parseMode(
                    com.pengrad.telegrambot.model.request.ParseMode.MarkdownV2
                )
            );
        }
    }

    private void sendMessageToMindustry(String username, String message) {
        String formattedMessage = "[TG] " + username + ": " + message;

        if (formattedMessage.length() > 150) {
            formattedMessage = formattedMessage.substring(0, 150);
        }

        Call.sendMessage(formattedMessage);
    }

    private String removeColorTags(String input) {
        return input.replaceAll("\\[.*?\\]", "");
    }

    private String removeInvalidChars(String input) {
        return input.replaceAll("[^a-zA-Z0-9 ]", "");
    }

    private String escapeTelegramMarkdown(String text) {
        return text.replaceAll("([_*\\[\\]()~`>#+\\-=|{}.!])", "\\\\$1");
    }

    private String removeAtSymbol(String input) {
        return input.replace("@", "");
    }

    private void loadConfig() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
            botToken = properties.getProperty("telegram.token");
            chatId = Long.parseLong(properties.getProperty("telegram.chat_id"));
            sendCommandMessages = Boolean.parseBoolean(
                properties.getProperty(
                    "telegram.send_command_messages",
                    "false"
                )
            );
            isConfigured = true;
            Log.info("Конфигурация Telegram бота загружена.");
        } catch (IOException | NumberFormatException e) {
            Log.warn(
                "Не удалось загрузить конфигурацию Telegram бота. Используйте команду /tgconfig для настройки."
            );
            isConfigured = false;
        }
    }

    private void saveConfig() {
        Properties properties = new Properties();
        properties.setProperty("telegram.token", botToken);
        properties.setProperty("telegram.chat_id", String.valueOf(chatId));
        properties.setProperty(
            "telegram.send_command_messages",
            String.valueOf(sendCommandMessages)
        );
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Telegram Bot Configuration");
            Log.info("Конфигурация Telegram бота сохранена.");
        } catch (IOException e) {
            Log.err("Не удалось сохранить конфигурацию Telegram бота.", e);
        }
    }
}
