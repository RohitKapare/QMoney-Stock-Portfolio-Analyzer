
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {

  private RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private static String getToken(){
    return "207fa3fbbe534ddf1f526752982326dff1d98e9c";
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws StockQuoteServiceException {
    String tiingoURL = buildURL(symbol, from, to);
    String responseString=null;
    try {
      responseString = restTemplate.getForObject(tiingoURL, String.class);
    } catch (HttpClientErrorException e) {
        throw new StockQuoteServiceException("TooManyRequests: 429 Unknown Status Code");
    }
    
    TiingoCandle[] tiingoCandleArray;
    try {
      tiingoCandleArray = getObjectMapper().readValue(responseString, TiingoCandle[].class);
      if (tiingoCandleArray == null || responseString == null)
        throw new StockQuoteServiceException("Invalid Response Found");
    } catch (JsonProcessingException e) {
      throw new StockQuoteServiceException(e.getMessage());
    }
    return Arrays.stream(tiingoCandleArray).sorted(Comparator.comparing(Candle::getDate))
        .collect(Collectors.toList());
  }

  // @Override
  // public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonMappingException, JsonProcessingException {
  //   String tiingoURL = buildURL(symbol, from, to);

  //   String responseString = restTemplate.getForObject(tiingoURL, String.class);

  //   TiingoCandle[] tiingoCandleArray = getObjectMapper().readValue(responseString, TiingoCandle[].class);

  //   return Arrays.stream(tiingoCandleArray)
  //       .sorted(Comparator.comparing(Candle::getDate))
  //       .collect(Collectors.toList());
  // }

  private Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }

  private Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size() - 1).getClose();
  }
  protected String buildURL(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/"+ symbol + "/prices?startDate="
     + startDate + "&endDate=" + endDate + "&token=" + getToken();
    return uriTemplate;
  }

}
