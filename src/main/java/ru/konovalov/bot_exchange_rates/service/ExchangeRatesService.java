package ru.konovalov.bot_exchange_rates.service;

import ru.konovalov.bot_exchange_rates.exception.ServiceException;

public interface ExchangeRatesService {

    String getUsdExchangeRate() throws ServiceException;

    String getEurExchangeRate() throws ServiceException;

    String getCnyExchangeRate() throws ServiceException;
}
