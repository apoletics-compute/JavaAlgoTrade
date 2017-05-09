package com.apoletics;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.CachedIndicator;


public class MyRSIIndicator extends CachedIndicator<Decimal> {

	private final int timeFrame;
	private final Indicator<Decimal> indicator;
    private MyAverageGainIndicator averageGainIndicator;

    private MyAverageLossIndicator averageLossIndicator;
    
    public MyRSIIndicator(Indicator<Decimal> indicator, int timeFrame) {
        super(indicator);
        this.indicator = indicator;
        this.timeFrame = timeFrame;
        averageGainIndicator = new MyAverageGainIndicator(indicator, timeFrame);
        averageLossIndicator = new MyAverageLossIndicator(indicator, timeFrame);
    }

	@Override
	protected Decimal calculate(int index) {
        return Decimal.HUNDRED
                .minus(Decimal.HUNDRED.dividedBy(Decimal.ONE.plus(relativeStrength(index))));
	}
	
    /**
     * @param index
     * @return the relative strength
     */
    private Decimal relativeStrength(int index) {
        if (index == 0) {
            return Decimal.ZERO;
        }
        Decimal averageGain = averageGainIndicator.getValue(index);
        Decimal averageLoss = averageLossIndicator.getValue(index);
        return averageGain.dividedBy(averageLoss);
    }

}
