package ru.konovalov.bot_exchange_rates.configuration;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.konovalov.bot_exchange_rates.bot.BotExchangeRates;

@Configuration
public class BotExchangeRatesConfiguration {

    @Bean
    public TelegramBotsApi telegramBotsApi(BotExchangeRates botExchangeRates){
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(botExchangeRates);
            return api;
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    @Bean
    public OkHttpClient okHttpClient(){
        return new OkHttpClient();
    }
}
