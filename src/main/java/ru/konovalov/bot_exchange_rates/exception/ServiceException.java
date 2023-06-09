package ru.konovalov.bot_exchange_rates.exception;

public class ServiceException extends Exception{

    public ServiceException(String message, Throwable cause){
        super(message, cause);
    }
}
