package ru.konovalov.bot_exchange_rates.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.konovalov.bot_exchange_rates.bot.BotExchangeRates;
@Slf4j
@Component
public class BotInitializer {

    @Autowired
    BotExchangeRates bot;

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException{
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        try {
            api.registerBot(bot);
        }
        catch (TelegramApiException ex){
            log.error("Ошибка: " + ex.getMessage());
        }
    }
}
