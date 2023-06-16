package ru.konovalov.bot_exchange_rates.bot;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.konovalov.bot_exchange_rates.configuration.BotConfig;
import ru.konovalov.bot_exchange_rates.exception.ServiceException;
import ru.konovalov.bot_exchange_rates.model.UserEntity;
import ru.konovalov.bot_exchange_rates.repository.UserRepository;
import ru.konovalov.bot_exchange_rates.service.ExchangeRatesService;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class BotExchangeRates extends TelegramLongPollingBot {

    private static final String START = "/start";
    private static final String USD = "/usd";
    private static final String EUR = "/eur";
    private static final String CNY = "/cny";
    private static final String HELP = "/help";
    private static final String MYDATA = "/mydata";
    private static final String DELETEDATA = "/deletedata";

    private final List<BotCommand> botCommandList;

    private final BotConfig config;
    @Autowired
    private ExchangeRatesService exchangeRatesService;

    @Autowired
    private UserRepository repository;

    public BotExchangeRates(BotConfig config) {
        this.config = config;
        botCommandList = new ArrayList<>();
        addCommandToList();

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
            case START ->{
                    registerUser(update.getMessage());
                    startCommand(chatId, userName);}
            case USD -> usdCommand(chatId);
            case EUR -> eurCommand(chatId);
            case CNY -> cnyCommand(chatId);
            case HELP -> helpCommand(chatId);
//            case MYDATA -> myDataCommand(update.getMessage());
//            case DELETEDATA -> deleteDataCommand(update.getMessage());
            default -> unknownCommand(chatId);
        }
    }

    private void deleteDataCommand(Message message) {
        if (repository.findById(message.getChatId()).isPresent()){
            repository.deleteById(message.getChatId());
            sendMessage(message.getChatId(), "Ваши данные удалены!");
        }
        else {
            sendMessage(message.getChatId(), "У вас нет сохраненных данных!");
        }
    }

    private void myDataCommand(Message message) {
        if (repository.findById(message.getChatId()).isPresent()){
            UserEntity user = repository.findById(message.getChatId()).get();
            sendMessage(message.getChatId(), user.toString());
        }
        else {
            sendMessage(message.getChatId(), "У вас нет сохраненных данных!");
        }
    }

    private void registerUser(Message message) {
        if (repository.findById(message.getChatId()).isEmpty()){
            Long chatId = message.getChatId();
            Chat chat = message.getChat();
            saveUser(chatId, chat);
        }
    }

    private void saveUser(Long chatId, Chat chat) {
        UserEntity user = new UserEntity();
        user.setChatId(chatId);
        user.setFirstName(chat.getFirstName());
        user.setLastName(chat.getLastName());
        user.setUserName(chat.getUserName());
        user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

        repository.save(user);
        log.info("Пользователь добавлен: " + user);
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    private void addCommandToList(){
        botCommandList.add(new BotCommand("/start", "Начало работы"));
        botCommandList.add(new BotCommand("/usd", "Узнать курс доллара США"));
        botCommandList.add(new BotCommand("/eur", "Узнать курс ЕВРО"));
        botCommandList.add(new BotCommand("/cny", "Узнать курс ЮАНЯ"));
        botCommandList.add(new BotCommand("/help", "Помощь"));
        botCommandList.add(new BotCommand("/mydata", "Мои данные"));
        botCommandList.add(new BotCommand("/deletedata", "Удалить мои данные"));
        try{
            this.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), null));
        }
        catch (TelegramApiException ex){
            log.error("Ошибка настройки листа комманд бота: " + ex.getMessage());
        }
    }

    private void startCommand(Long chatId, String userName) {
        String text = EmojiParser.parseToUnicode("""
                Добро пожаловать в бот, %s! :blush:
                                
                Здесь Вы сможете узнать официальные курсы валют на сегодня, установленные ЦБ РФ.
                                
                Для этого используйте МЕНЮ в левом нижнем углу либо КНОПКИ.
              
                """);
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
                /mydata - мои данные
                /deletedata - удалить мои данные
                """;
        sendMessage(chatId, text);
    }

    private void unknownCommand(Long chatId) {
        String text = "Не удалось распознать команду! Воспользуйтесь меню для выбора комманды!";
        log.error("Команда не распознана! " + chatId);
        sendMessage(chatId, text);
    }

    private void sendMessage(Long chatId, String text) {
        String chatIdStr = String.valueOf(chatId);
        SendMessage sendMessage = new SendMessage(chatIdStr, text);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add("/usd");
        firstRow.add("/eur");
        firstRow.add("/cny");

        keyboardRows.add(firstRow);

        KeyboardRow secondRow = new KeyboardRow();

        secondRow.add("/help");
        secondRow.add("/mydata");
        secondRow.add("/deletedata");

        keyboardRows.add(secondRow);

        keyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(keyboardMarkup);
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
