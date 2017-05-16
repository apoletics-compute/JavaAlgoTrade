package com.apoletics;



import eu.verdelhan.ta4j.AnalysisCriterion;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Order.OrderType;
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
import eu.verdelhan.ta4j.indicators.trackers.RSIIndicator;
import eu.verdelhan.ta4j.indicators.trackers.MACDIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import eu.verdelhan.ta4j.trading.rules.CrossedDownIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.CrossedUpIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.StopGainRule;
import eu.verdelhan.ta4j.trading.rules.StopLossRule;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;

import java.awt.Color;
import java.awt.Dimension;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * This class is an example of a dummy trading bot using ta4j.
 * <p>
 */
public class AlgoTradeV2 {

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
    private static Strategy buildStrategy(TimeSeries series,int rsi1, int rsi2) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        
//        SMAIndicator sma10 = new SMAIndicator(closePrice, 10);
//        SMAIndicator sma50 = new SMAIndicator(closePrice, 50);
//        
//        // The bias is bullish when the shorter-moving average moves above the longer moving average.
//        // The bias is bearish when the shorter-moving average moves below the longer moving average.
//        EMAIndicator shortEma = new EMAIndicator(closePrice, 9);
//        EMAIndicator longEma = new EMAIndicator(closePrice, 26);
//
//        StochasticOscillatorKIndicator stochasticOscillK = new StochasticOscillatorKIndicator(series, 14);

//        MACDIndicator macd = new MACDIndicator(closePrice, 9, 26);
//        EMAIndicator emaMacd = new EMAIndicator(macd, 18);
//        MyRSIIndicator rsi14 = new MyRSIIndicator(closePrice, 14);
        MyAverageGainIndicator ag = new MyAverageGainIndicator(closePrice, 14);
        MyAverageLossIndicator al = new MyAverageLossIndicator(closePrice, 14);
        MyRSIIndicator rsi14 = new MyRSIIndicator(closePrice,ag,al,14);
        
        
        // Entry rule
//        Rule entryRule = new OverIndicatorRule(shortEma, longEma) // Trend
//                .and(new CrossedDownIndicatorRule(stochasticOscillK, Decimal.valueOf(20))) // Signal 1
//                .and(new OverIndicatorRule(macd, emaMacd)); // Signal 2
        Rule entryRule = new CrossedDownIndicatorRule(rsi14,Decimal.valueOf(rsi1));
//        Rule entryRule = new eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule(rsi14,Decimal.valueOf(30));
        
        // Exit rule
//        Rule exitRule = new UnderIndicatorRule(shortEma, longEma) // Trend
//                .and(new CrossedUpIndicatorRule(stochasticOscillK, Decimal.valueOf(80))) // Signal 1
//                .and(new UnderIndicatorRule(macd, emaMacd)); // Signal 2
//        Rule exitRule = new CrossedUpIndicatorRule(rsi14,Decimal.valueOf(70)).or(new StopGainRule(closePrice,Decimal.valueOf(10)).or(new StopLossRule(closePrice,Decimal.valueOf(5))));
        Rule exitRule = new CrossedUpIndicatorRule(rsi14,Decimal.valueOf(rsi2)).and(new SureGainRule(closePrice));
        
//        Rule exitRule = new eu.verdelhan.ta4j.trading.rules.OverIndicatorRule(rsi14,Decimal.valueOf(70));
        
//        
        // Signals
        // Buy when SMA goes over close price
        // Sell when close price goes over SMA
        
        Strategy buySellSignals = new Strategy(entryRule,exitRule);
        buySellSignals.setUnstablePeriod(20);
        
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

    public static void main(String[] args) throws InterruptedException {

        //System.err.println("********************** Initialization **********************");
        // Getting the time series
        //TimeSeries series = initMovingTimeSeries(20);
        //TimeSeries series = new TimeSeries();
        // Building the trading strategy
        
        
        // Initializing the trading history
        TradingRecord tradingRecord = new TradingRecord();
        String filename= args[0];
        int rsi1 = 30; //Integer.parseInt(args[1]);
        int rsi2 = 70; //Integer.parseInt(args[2]);
        TimeSeries series = CsvTicksLoader.loadAppleIncSeries(filename);
//        TimeSeries allSeries = CsvTicksLoader.loadAppleIncSeries(filename);
//        TimeSeries series = allSeries.subseries(20, allSeries.getEnd());
        Strategy strategy =  buildStrategy(series,rsi1,rsi2);        
//        TimeSeries ts = series.subseries(series.getTickCount()-300, series.getTickCount()-1);
//        ts.
//        	System.err.println("peroid date: START: "+ts.getTick(ts.getBegin()).getDateName());
//        	System.err.println("peroid date: END: "+ts.getTick(ts.getEnd()).getDateName());
//        	System.err.println("ts max:: "+(new eu.verdelhan.ta4j.indicators.simple.MaxPriceIndicator(ts)).getValue(ts.getEnd()-1));
//        	System.err.println("ts min : "+(new eu.verdelhan.ta4j.indicators.simple.MinPriceIndicator(ts)).getValue(ts.getEnd()-1));
//        	System.err.println("");
//        
//        if (ts.getTickCount() > 0) return;
        tradingRecord= series.run(strategy);
        tradingRecord= series.run(strategy, OrderType.BUY, Decimal.valueOf(1000));
        
        TotalProfitCriterion totalProfit = new TotalProfitCriterion();
        System.err.println("Total profit: " + totalProfit.calculate(series, tradingRecord));
        // Number of ticks
        System.err.println("Number of ticks: " + new NumberOfTicksCriterion().calculate(series, tradingRecord));
        // Average profit (per tick)
        System.err.println("Average profit (per tick): " + new AverageProfitCriterion().calculate(series, tradingRecord));
        // Number of trades
        System.err.println("Number of trades: " + new NumberOfTradesCriterion().calculate(series, tradingRecord));
        // Profitable trades ratio
        System.err.println("Profitable trades ratio: " + new AverageProfitableTradesCriterion().calculate(series, tradingRecord));
        // Maximum drawdown
        System.err.println("Maximum drawdown: " + new MaximumDrawdownCriterion().calculate(series, tradingRecord));
        // Reward-risk ratio
        System.err.println("Reward-risk ratio: " + new RewardRiskRatioCriterion().calculate(series, tradingRecord));
        // Total transaction cost
        System.err.println("Total transaction cost (from $1000): " + new LinearTransactionCostCriterion(1000, 0.005).calculate(series, tradingRecord));
        // Buy-and-hold
        System.err.println("Buy-and-hold: " + new BuyAndHoldCriterion().calculate(series, tradingRecord));
        // Total profit vs buy-and-hold
        System.err.println("Custom strategy profit vs buy-and-hold strategy profit: " + new VersusBuyAndHoldCriterion(totalProfit).calculate(series, tradingRecord));
        System.err.println("Trades:"+tradingRecord.getTrades());  
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        MyAverageGainIndicator ag = new MyAverageGainIndicator(closePrice, 14);
        MyAverageLossIndicator al = new MyAverageLossIndicator(closePrice, 14);
        MyRSIIndicator rsi14 = new MyRSIIndicator(closePrice,ag,al,14);                
        
        for (Trade iTrade : tradingRecord.getTrades()) {
        	System.err.println("Entry Type: ");
        	System.err.println("Date: "+series.getTick(iTrade.getEntry().getIndex()).getDateName());
        	System.err.println("price: "+iTrade.getEntry().getPrice().toDouble());
        	System.err.println("Entry RSI: "+rsi14.getValue(iTrade.getEntry().getIndex())); 
        	System.err.println("Exit Type:");
        	System.err.println("Date: "+series.getTick(iTrade.getExit().getIndex()).getDateName());
        	System.err.println("price: "+iTrade.getExit().getPrice().toDouble());
        	System.err.println("Exit RSI: "+rsi14.getValue(iTrade.getExit().getIndex())); 
        }
        int lastIndex = series.getEnd();
    	System.err.println("Last order: "+tradingRecord.getLastOrder().getType()+" price: "+tradingRecord.getLastOrder().getPrice() +" at: "+series.getTick(tradingRecord.getLastOrder().getIndex()).getDateName());
    	System.err.println("Last RSI: "+rsi14.getValue(lastIndex));
    	double predictedPrice70=(al.getValue(lastIndex).toDouble()*91- ag.getValue(lastIndex).toDouble()*39)/3 +closePrice.getValue(lastIndex).toDouble();
    	double predictedPrice30=closePrice.getValue(lastIndex).toDouble()- ( ag.getValue(lastIndex).toDouble()*91 - al.getValue(lastIndex).toDouble()*39 )/3;
    	System.err.println("Predict Price for RSI > 70: "+(rsi14.getValue(lastIndex).toDouble()>70?"NA":predictedPrice70));
    	System.err.println("Predict Price for RSI < 30: "+(rsi14.getValue(lastIndex).toDouble()<30?"NA":predictedPrice30));
    	System.err.println("last Close Price for RSI < 30: "+closePrice.getValue(lastIndex).toDouble());
        System.err.println("Should Enter?"+strategy.shouldEnter(lastIndex));
        System.err.println("Should Exit?"+strategy.shouldExit(lastIndex));
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
              
        Path p = Paths.get(filename);
        System.err.println("name:" +p.getFileName());
        String thisFilename = p.getFileName().toString();
        System.err.println("args.length: "+args.length);
        System.out.format("%s,%.2f,%.2f, %.2f, %.2f \n",thisFilename.substring(0,thisFilename.lastIndexOf(".")),closePrice.getValue(lastIndex).toDouble(),
        		rsi14.getValue(lastIndex).toDouble(),(rsi14.getValue(lastIndex).toDouble()<30?Double.NaN:predictedPrice30),(rsi14.getValue(lastIndex).toDouble()>70?Double.NaN:predictedPrice70) );
        if (args.length >1) {
	        int ncount = rsi14.getTimeSeries().getEnd();
	        System.err.println("Ncount:"+ncount);
	        Decimal rsiToday = rsi14.getValue(ncount);
	        System.err.println("START AT:"+rsi14.getTimeSeries().getTick(0).getDateName() );
	        System.err.println("RSI: at "+rsi14.getTimeSeries().getTick(rsi14.getTimeSeries().getEnd()).getDateName()+"is: "+rsiToday);
	        
	        
	        TimeSeriesCollection dataset = new TimeSeriesCollection();
	        dataset.addSeries(buildChartTimeSeries(series, rsi14, "883"));
	        
	        JFreeChart chart = ChartFactory.createTimeSeriesChart(
	                "RSI Trade", // title
	                "Date", // x-axis label
	                "RSI14", // y-axis label
	                dataset, // data
	                true, // create legend?
	                true, // generate tooltips?
	                false // generate URLs?
	                );
	        XYPlot plot = (XYPlot) chart.getPlot();
	        DateAxis axis = (DateAxis) plot.getDomainAxis();
	        axis.setDateFormatOverride(new SimpleDateFormat("MM-dd HH:mm"));
	        
	        addBuySellSignals(series, strategy, plot);
	        
	        displayChart(chart);
        }
    }
    
    private static org.jfree.data.time.TimeSeries buildChartTimeSeries(TimeSeries tickSeries, Indicator<Decimal> indicator, String name) {
        org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(name);
        for (int i = 0; i < tickSeries.getTickCount(); i++) {
            Tick tick = tickSeries.getTick(i);
            chartTimeSeries.add(new Minute(tick.getEndTime().toDate()), indicator.getValue(i).toDouble());
        }
        return chartTimeSeries;
    }
    
    private static void addBuySellSignals(TimeSeries series, Strategy strategy, XYPlot plot) {
        // Running the strategy
        List<Trade> trades = series.run(strategy).getTrades();
        // Adding markers to plot
        for (Trade trade : trades) {
            // Buy signal
            double buySignalTickTime = new Minute(series.getTick(trade.getEntry().getIndex()).getEndTime().toDate()).getFirstMillisecond();
            Marker buyMarker = new ValueMarker(buySignalTickTime);
            buyMarker.setPaint(Color.GREEN);
            buyMarker.setLabel("B");
            plot.addDomainMarker(buyMarker);
            // Sell signal
            double sellSignalTickTime = new Minute(series.getTick(trade.getExit().getIndex()).getEndTime().toDate()).getFirstMillisecond();
            Marker sellMarker = new ValueMarker(sellSignalTickTime);
            sellMarker.setPaint(Color.RED);
            sellMarker.setLabel("S");
            plot.addDomainMarker(sellMarker);
        }
    }
    
    /**
     * Displays a chart in a frame.
     * @param chart the chart to be displayed
     */
    private static void displayChart(JFreeChart chart) {
        // Chart panel
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new Dimension(1024, 400));
        // Application frame
        ApplicationFrame frame = new ApplicationFrame("Ta4j example - Buy and sell signals to chart");
        frame.setContentPane(panel);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }
}
