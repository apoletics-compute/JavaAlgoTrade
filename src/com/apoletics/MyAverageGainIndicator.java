package com.apoletics;

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.indicators.CachedIndicator;
import eu.verdelhan.ta4j.indicators.helpers.CumulatedGainsIndicator;

/**
 * Average gain indicator.
 * <p>
 */
public class MyAverageGainIndicator extends CachedIndicator<Decimal> {

    private final CumulatedGainsIndicator cumulatedGains;

    private final int timeFrame;
    
    private final Indicator<Decimal> indicator;

    public MyAverageGainIndicator(Indicator<Decimal> indicator, int timeFrame) {
        super(indicator);
        this.cumulatedGains = new CumulatedGainsIndicator(indicator, timeFrame);
        this.timeFrame = timeFrame;
        this.indicator = indicator;
    }

    @Override
    protected Decimal calculate(int index) {
        Decimal sumOfGains = Decimal.ZERO;
        Decimal averageGain = Decimal.ZERO;
        Decimal thisGain = Decimal.ZERO;
        if (index <= timeFrame ) {
	        for (int i = 1; i <= index; i++) {        	
	            if (indicator.getValue(i).isGreaterThan(indicator.getValue(i - 1))) {
	                sumOfGains = sumOfGains.plus(indicator.getValue(i).minus(indicator.getValue(i - 1)));
	            }
	        }
            averageGain = sumOfGains.dividedBy(Decimal.valueOf(index));
        } else {
        	if (indicator.getValue(index).isGreaterThan(indicator.getValue(index - 1))) {
        		thisGain =indicator.getValue(index).minus(indicator.getValue(index - 1));
        	}
        	averageGain = this.getValue(index -1).multipliedBy(Decimal.valueOf(timeFrame-1)).plus(thisGain).dividedBy(Decimal.valueOf(timeFrame));
        }
        return averageGain;
    }
}
