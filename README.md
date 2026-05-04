# QMoney Stock Portfolio Analyzer

![Java](https://img.shields.io/badge/Java-11%2B-blue?logo=java)
![Gradle](https://img.shields.io/badge/Build-Gradle-02303A?logo=gradle)
![License](https://img.shields.io/badge/License-MIT-green)

QMoney is a stock portfolio analyzer that helps portfolio managers make data-driven trade recommendations for their clients. It fetches real-time and historical stock quotes from external REST APIs, computes annualized returns, and ranks holdings to guide buy/sell decisions.

---
   
## Features

- **Stock Quote Fetching**: Retrieves historical OHLC candle data from [Tiingo](https://api.tiingo.com) and [Alpha Vantage](https://www.alphavantage.co) REST APIs.
- **Annualized Return Calculation**: Computes annualized returns based on purchase price, current price, and holding period.
- **Portfolio Ranking**: Sorts holdings by annualized return to surface the best and worst performers.
- **Backup Quote Service**: Falls back to Alpha Vantage automatically when Tiingo is unavailable, improving service availability.
- **Multithreaded Processing**: Processes multiple stock quotes in parallel using a configurable thread pool for faster results.
- **Publishable Library**: The portfolio manager core is packaged as a JAR (`annual-return-1.0.0.jar`) for easy versioning and reuse.

---

## Project Structure

```
qmoney/src/main/java/com/crio/warmup/stock/
├── PortfolioManagerApplication.java   # Main entry point
├── dto/                               # Data Transfer Objects (PortfolioTrade, AnnualizedReturn, Candle, etc.)
├── portfolio/                         # Core portfolio logic
│   ├── PortfolioManager.java          # Interface contract
│   ├── PortfolioManagerImpl.java      # Sequential & parallel implementations
│   ├── PortfolioManagerFactory.java   # Factory for creating PortfolioManager instances
│   └── AnnualizedReturnTask.java      # Callable task for parallel execution
├── quotes/                            # Stock quote service integrations
│   ├── StockQuotesService.java        # Interface for quote services
│   ├── TiingoService.java             # Tiingo REST API implementation
│   ├── AlphavantageService.java       # Alpha Vantage REST API implementation
│   └── StockQuoteServiceFactory.java  # Factory for selecting the active quote service
├── exception/                         # Custom exception types
└── log/                               # Logging utilities (UncaughtExceptionHandler)
```

---

## Getting Started

### Prerequisites

| Tool | Version                                                 |
|------|---------------------------------------------------------|
| JDK  | 11 or later                                             |
| Gradle | Included via Gradle Wrapper, no separate install needed |

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/RohitKapare/QMoney-Stock-Portfolio-Analyzer.git
cd qmoney

# 2. Build the project
cd qmoney
./gradlew build
```

On Windows use `gradlew.bat` instead of `./gradlew`.

### Configuration

QMoney requires API tokens for the stock quote services.

**Tiingo**
1. Sign up at [https://api.tiingo.com](https://api.tiingo.com) and copy your API token.
2. Replace the token value in `PortfolioManagerApplication.java` and `TiingoService.java`:
```java
   private static String getToken() {
       return "<YOUR_TIINGO_TOKEN>";
   }
```

**Alpha Vantage** *(backup service)*
1. Get a free API key at [https://www.alphavantage.co/support/#api-key](https://www.alphavantage.co/support/#api-key).
2. Set the key in `AlphavantageService.java`.

```java
  private String getToken(){
    return "<YOUR_ALPHAVANTAGE_KEY>";
  }
```

> ****Use of your own keys is preferred****, but since these are free, you can use them for testing purpose.
> 
> Note: Usually ****API keys**** are kept in environment variables or configuration files for security, but for simplicity, they are hardcoded here. In production, consider using a secure vault or environment variable management.

---

## Usage

### Trades File Format

Create a JSON file listing your portfolio trades. A sample is provided at [`qmoney/src/main/resources/trades.json`](qmoney/src/main/resources/trades.json):

```json
[
  {
    "symbol": "AAPL",
    "quantity": 100,
    "tradeType": "BUY",
    "purchaseDate": "2019-01-02"
  },
  {
    "symbol": "MSFT",
    "quantity": 10,
    "tradeType": "BUY",
    "purchaseDate": "2019-01-02"
  },
  {
    "symbol": "GOOGL",
    "quantity": 50,
    "tradeType": "BUY",
    "purchaseDate": "2019-01-02"
  }
]
```

| Field | Type | Description |
|-------|------|-------------|
| `symbol` | String | Stock ticker symbol (e.g., `AAPL`) |
| `quantity` | Integer | Number of shares |
| `tradeType` | String | `BUY` or `SELL` |
| `purchaseDate` | String | Date of purchase in `YYYY-MM-DD` format |

### Running the Application

To compute annualized returns for a portfolio up to a specific end date, uncomment the relevant line in `main()` inside `PortfolioManagerApplication.java` and run:

```bash
# From the qmoney/ directory
./gradlew run --args="trades.json 2021-12-31"
```

Output is a JSON array of stocks ranked by annualized return (highest first).

### Programmatic Usage

```java
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.util.List;

// Sequential calculation
PortfolioManager manager = PortfolioManagerFactory.getPortfolioManager(new RestTemplate());
List<AnnualizedReturn> returns = manager.calculateAnnualizedReturn(portfolioTrades, LocalDate.of(2021, 12, 31));

// Parallel calculation (5 threads)
List<AnnualizedReturn> returnsParallel = manager.calculateAnnualizedReturnParallel(portfolioTrades, LocalDate.of(2021, 12, 31), 5);
```

---

## Publishing the Library

The portfolio manager can be packaged as a reusable JAR and published to a local Maven repository:

```bash
./gradlew publishToMavenLocal
```

**Maven coordinates:**

```xml
<dependency>
    <groupId>com.crio.warmup</groupId>
    <artifactId>annual-return</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**

```groovy
implementation 'com.crio.warmup:annual-return:1.0.0'
```

---

## Running Tests

```bash
# Run all tests
./gradlew test

# View the HTML test report
open qmoney/tmp/external_build/reports/tests/test/index.html
```

Tests cover:
- Module 1: File reading & symbol extraction
- Module 2: Quote fetching & sorting by closing price
- Module 3: Single and refactored annualized return calculation
- Module 4 & 5: Portfolio manager interface, factory, and performance benchmarks

---
