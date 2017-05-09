package com.apoletics;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.trading.rules.AbstractRule;

/**
 * A stop-gain rule.
 * <p>
 * Satisfied when the close price reaches the gain threshold.
 */
public class SureGainRule extends AbstractRule {

    /** The close price indicator */
    private ClosePriceIndicator closePrice;
    
    /** The gain ratio threshold (e.g. 1.03 for 3%) */
    private Decimal gainRatioThreshold;

    /**
     * Constructor.
     * @param closePrice the close price indicator
     * @param gainPercentage the gain percentage
     */
    public SureGainRule(ClosePriceIndicator closePrice) {
        this.closePrice = closePrice;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;
        // No trading history or no trade opened, no gain
        if (tradingRecord != null) {
            Trade currentTrade = tradingRecord.getCurrentTrade();
            if (currentTrade.isOpened()) {
                Decimal entryPrice = currentTrade.getEntry().getPrice();
                Decimal currentPrice = closePrice.getValue(index);
                satisfied = currentPrice.isGreaterThanOrEqual(entryPrice);
            }
        }
        traceIsSatisfied(index, satisfied);
        return satisfied;
    }
}
