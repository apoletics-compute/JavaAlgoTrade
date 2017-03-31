package com.apoletics;



import eu.verdelhan.ta4j.AnalysisCriterion;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.analysis.criteria.AverageProfitCriterion;
import eu.verdelhan.ta4j.analysis.criteria.AverageProfitableTradesCriterion;
import eu.verdelhan.ta4j.analysis.criteria.BuyAndHoldCriterion;
import eu.verdelhan.ta4j.analysis.criteria.LinearTransactionCostCriterion;
import eu.verdelhan.ta4j.analysis.criteria.MaximumDrawdownCriterion;
import eu.verdelhan.ta4j.analysis.criteria.NumberOfTicksCriterion;
import eu.verdelhan.ta4j.analysis.criteria.NumberOfTradesCriterion;
import eu.verdelhan.ta4j.analysis.criteria.RewardRiskRatioCriterion;
import eu.verdelhan.ta4j.analysis.criteria.TotalProfitCriterion;
import eu.verdelhan.ta4j.analysis.criteria.VersusBuyAndHoldCriterion;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.EMAIndicator;
import eu.verdelhan.ta4j.indicators.trackers.MACDIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import eu.verdelhan.ta4j.trading.rules.CrossedDownIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.CrossedUpIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;

import java.util.Iterator;

import org.joda.time.DateTime;

/**
 * This class is an example of a dummy trading bot using ta4j.
 * <p>
 */
public class AlgoTrade {

    /** Close price of the last tick */
    private static Decimal LAST_TICK_CLOSE_PRICE;
    private static int STARTINGNUMBER=50;

    /**
     * Builds a moving time series (i.e. keeping only the maxTickCount last ticks)
     * @param maxTickCount the number of ticks to keep in the time series (at maximum)
     * @return a moving time series
     */

    /**
     * @param series a time series
     * @return a dummy strategy
     */
    private static Strategy buildStrategy(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator sma10 = new SMAIndicator(closePrice, 10);
        SMAIndicator sma50 = new SMAIndicator(closePrice, 50);
        
        // The bias is bullish when the shorter-moving average moves above the longer moving average.
        // The bias is bearish when the shorter-moving average moves below the longer moving average.
        EMAIndicator shortEma = new EMAIndicator(closePrice, 9);
        EMAIndicator longEma = new EMAIndicator(closePrice, 26);

        StochasticOscillatorKIndicator stochasticOscillK = new StochasticOscillatorKIndicator(series, 14);

        MACDIndicator macd = new MACDIndicator(closePrice, 9, 26);
        EMAIndicator emaMacd = new EMAIndicator(macd, 18);
        
        // Entry rule
        Rule entryRule = new OverIndicatorRule(shortEma, longEma) // Trend
                .and(new CrossedDownIndicatorRule(stochasticOscillK, Decimal.valueOf(20))) // Signal 1
                .and(new OverIndicatorRule(macd, emaMacd)); // Signal 2
        
        // Exit rule
        Rule exitRule = new UnderIndicatorRule(shortEma, longEma) // Trend
                .and(new CrossedUpIndicatorRule(stochasticOscillK, Decimal.valueOf(80))) // Signal 1
                .and(new UnderIndicatorRule(macd, emaMacd)); // Signal 2
        
        // Signals
        // Buy when SMA goes over close price
        // Sell when close price goes over SMA
        Strategy buySellSignals = new Strategy(entryRule,exitRule);
        return buySellSignals;
    }

    /**
     * Generates a random decimal number between min and max.
     * @param min the minimum bound
     * @param max the maximum bound
     * @return a random decimal number between min and max
     */
    private static Decimal randDecimal(Decimal min, Decimal max) {
        Decimal randomDecimal = null;
        if (min != null && max != null && min.isLessThan(max)) {
            randomDecimal = max.minus(min).multipliedBy(Decimal.valueOf(Math.random())).plus(min);
        }
        return randomDecimal;
    }

    /**
     * Generates a random tick.
     * @return a random tick
     */
    private static Tick generateRandomTick() {
        final Decimal maxRange = Decimal.valueOf("0.03"); // 3.0%
        Decimal openPrice = LAST_TICK_CLOSE_PRICE;
        Decimal minPrice = openPrice.minus(openPrice.multipliedBy(maxRange.multipliedBy(Decimal.valueOf(Math.random()))));
        Decimal maxPrice = openPrice.plus(openPrice.multipliedBy(maxRange.multipliedBy(Decimal.valueOf(Math.random()))));
        Decimal closePrice = randDecimal(minPrice, maxPrice);
        LAST_TICK_CLOSE_PRICE = closePrice;
        return new Tick(DateTime.now(), openPrice, maxPrice, minPrice, closePrice, Decimal.ONE);
    }

    public static void main(String[] args) throws InterruptedException {

        //System.out.println("********************** Initialization **********************");
        // Getting the time series
        //TimeSeries series = initMovingTimeSeries(20);
        //TimeSeries series = new TimeSeries();
        // Building the trading strategy
        
        
        // Initializing the trading history
        TradingRecord tradingRecord = new TradingRecord();
        String filename= args[0];  
        TimeSeries series = CsvTicksLoader.loadAppleIncSeries(filename);
        Strategy strategy = buildStrategy(series);
        
        tradingRecord= series.run(strategy);
        TotalProfitCriterion totalProfit = new TotalProfitCriterion();
        System.out.println("Total profit: " + totalProfit.calculate(series, tradingRecord));
        // Number of ticks
        System.out.println("Number of ticks: " + new NumberOfTicksCriterion().calculate(series, tradingRecord));
        // Average profit (per tick)
        System.out.println("Average profit (per tick): " + new AverageProfitCriterion().calculate(series, tradingRecord));
        // Number of trades
        System.out.println("Number of trades: " + new NumberOfTradesCriterion().calculate(series, tradingRecord));
        // Profitable trades ratio
        System.out.println("Profitable trades ratio: " + new AverageProfitableTradesCriterion().calculate(series, tradingRecord));
        // Maximum drawdown
        System.out.println("Maximum drawdown: " + new MaximumDrawdownCriterion().calculate(series, tradingRecord));
        // Reward-risk ratio
        System.out.println("Reward-risk ratio: " + new RewardRiskRatioCriterion().calculate(series, tradingRecord));
        // Total transaction cost
        System.out.println("Total transaction cost (from $1000): " + new LinearTransactionCostCriterion(1000, 0.005).calculate(series, tradingRecord));
        // Buy-and-hold
        System.out.println("Buy-and-hold: " + new BuyAndHoldCriterion().calculate(series, tradingRecord));
        // Total profit vs buy-and-hold
        System.out.println("Custom strategy profit vs buy-and-hold strategy profit: " + new VersusBuyAndHoldCriterion(totalProfit).calculate(series, tradingRecord));
        System.out.println("Trades:"+tradingRecord.getTrades());        
        for (Trade iTrade : tradingRecord.getTrades()) {
        	System.out.println("Entry Type: ");
        	System.out.println("Date: "+series.getTick(iTrade.getEntry().getIndex()).getDateName());
        	System.out.println("price: "+iTrade.getEntry().getPrice().toDouble());
        	System.out.println("Exit Type:");
        	System.out.println("Date: "+series.getTick(iTrade.getExit().getIndex()).getDateName());
        	System.out.println("price: "+iTrade.getExit().getPrice().toDouble());        	
        }
        System.out.println("Should Enter?"+strategy.shouldEnter(series.getEnd()));
        System.out.println("Should Exit?"+strategy.shouldExit(series.getEnd()));
    }
}
