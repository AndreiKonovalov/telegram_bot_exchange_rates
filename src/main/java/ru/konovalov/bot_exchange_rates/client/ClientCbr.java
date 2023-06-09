package ru.konovalov.bot_exchange_rates.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.konovalov.bot_exchange_rates.exception.ServiceException;

import java.io.IOException;

@Component
public class ClientCbr {

    @Autowired
    private OkHttpClient client;

    @Value("${cbr.currency.rates.xml.url}")
    private String url;

    public String getCurrencyRatesXml() throws ServiceException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()){
            ResponseBody body = response.body();
            return body == null ? null : body.string();
        } catch (IOException ex){
            throw new ServiceException("Ошибка получения курсов валют!", ex);
        }
    }
}
