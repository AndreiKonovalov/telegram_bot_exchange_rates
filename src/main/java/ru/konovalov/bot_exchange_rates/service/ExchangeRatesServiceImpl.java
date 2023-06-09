package ru.konovalov.bot_exchange_rates.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import ru.konovalov.bot_exchange_rates.client.ClientCbr;
import ru.konovalov.bot_exchange_rates.exception.ServiceException;

import javax.swing.text.Document;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

@Service
public class ExchangeRatesServiceImpl implements ExchangeRatesService{

    private static final String USD_XPATH = "/ValCurs//Valute[@ID='R01235']/Value";
    private static final String EUR_XPATH = "/ValCurs//Valute[@ID='R01239']/Value";
    private static final String CNY_XPATH = "/ValCurs//Valute[@ID='R01375']/Value";

    @Autowired
    private ClientCbr client;

    @Override
    public String getUsdExchangeRate() throws ServiceException {
        String xml = client.getCurrencyRatesXml();
        return extractCurrencyValueFromXml(xml, USD_XPATH);
    }

    @Override
    public String getEurExchangeRate() throws ServiceException {
        String xml = client.getCurrencyRatesXml();
        return extractCurrencyValueFromXml(xml, EUR_XPATH);
    }

    @Override
    public String getCnyExchangeRate() throws ServiceException {
        String xml = client.getCurrencyRatesXml();
        return extractCurrencyValueFromXml(xml, CNY_XPATH);
    }

    private static String extractCurrencyValueFromXml(String xml, String xPathExpression) throws ServiceException {
        InputSource source = new InputSource(new StringReader(xml));
        try{
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document document = (Document) xPath.evaluate("/", source, XPathConstants.NODE);
            return xPath.evaluate(xPathExpression,document);
        } catch (XPathExpressionException ex){
            throw new ServiceException("Не удалось распарсить xml", ex);
        }
    }
}
