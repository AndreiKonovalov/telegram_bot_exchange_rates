package ru.konovalov.bot_exchange_rates.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.konovalov.bot_exchange_rates.configuration.BotConfig;
import ru.konovalov.bot_exchange_rates.exception.ServiceException;
import ru.konovalov.bot_exchange_rates.service.ExchangeRatesService;

import java.time.LocalDate;
@Slf4j
@Component
public class BotExchangeRates extends TelegramLongPollingBot {

    private static final String START = "/start";
    private static final String USD = "/usd";
    private static final String EUR = "/eur";
    private static final String CNY = "/cny";
    private static final String HELP = "/help";

    private final BotConfig config;
    @Autowired
    private ExchangeRatesService exchangeRatesService;

    public BotExchangeRates(BotConfig config) {
        this.config = config;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getChat().getUserName();
        switch (message) {
            case START -> startCommand(chatId, userName);
            case USD -> usdCommand(chatId);
            case EUR -> eurCommand(chatId);
            case CNY -> cnyCommand(chatId);
            case HELP -> helpCommand(chatId);
            default -> unknownCommand(chatId);
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    private void startCommand(Long chatId, String userName) {
        String text = """
                Добро пожаловать в бот, %s!
                                
                Здесь Вы сможете узнать официальные курсы валют на сегодня, установленные ЦБ РФ.
                                
                Для этого воспользуйтесь командами:
                /usd - курс доллара
                /eur - курс евро
                /cny - курс юаня
                                
                Дополнительные команды:
                /help - получение справки
                """;
        String formattedText = String.format(text, userName);
        log.info("Написал юзер: " + userName);
        sendMessage(chatId, formattedText);
    }

    private void usdCommand(Long chatId) {
        String formattedText;
        try {
            String usd = exchangeRatesService.getUsdExchangeRate();
            String text = "Курс доллара на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), usd);
        } catch (ServiceException ex) {
            log.error("Ошибка получения курса доллара: " + ex.getMessage());
            formattedText = "Не удалось получить текущий курс доллара. Попробуйте позже.";
        }
        sendMessage(chatId, formattedText);
    }

    private void eurCommand(Long chatId) {
        String formattedText;
        try {
            String eur = exchangeRatesService.getEurExchangeRate();
            String text = "Курс евро на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), eur);
        } catch (ServiceException ex) {
            log.error("Ошибка получения курса евро: " + ex.getMessage());
            formattedText = "Не удалось получить текущий курс евро. Попробуйте позже.";
        }
        sendMessage(chatId, formattedText);
    }

    private void cnyCommand(Long chatId) {
        String formattedText;
        try {
            String cny = exchangeRatesService.getCnyExchangeRate();
            String text = "Курс юаня на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), cny);
        } catch (ServiceException ex) {
            log.error("Ошибка получения курса юаня: " + ex.getMessage());
            formattedText = "Не удалось получить текущий курс юаня. Попробуйте позже.";
        }
        sendMessage(chatId, formattedText);
    }

    private void helpCommand(Long chatId) {
        String text = """
                Справочная информация по боту
                                
                Для получения текущих курсов валют воспользуйтесь командами:
                /usd - курс доллара
                /eur - курс евро
                /cny - курс юаня
                """;
        sendMessage(chatId, text);
    }

    private void unknownCommand(Long chatId) {
        String text = "Не удалось распознать команду!";
        log.error("Команда не распознана! " + chatId);
        sendMessage(chatId, text);
    }

    private void sendMessage(Long chatId, String text) {
        String chatIdStr = String.valueOf(chatId);
        SendMessage sendMessage = new SendMessage(chatIdStr, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException ex) {
            log.error("Ошибка отправки сообщения: " + ex.getMessage());
        }
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
}
