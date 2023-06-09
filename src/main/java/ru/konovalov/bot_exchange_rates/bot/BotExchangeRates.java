package ru.konovalov.bot_exchange_rates.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class BotExchangeRates extends TelegramLongPollingBot {

    public BotExchangeRates(@Value("${bot.token}") String botToken){
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {

    }

    @Override
    public String getBotUsername() {
        return "konovalov_andrey_er_bot";
    }
}
