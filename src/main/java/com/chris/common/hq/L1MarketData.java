package com.chris.common.hq;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
public class L1MarketData implements Serializable {

    @ToString.Exclude
    public static final int L1_SIZE = 5;

    //股票代码
    public int code;
    public long newPrice;

    @ToString.Exclude
    public transient int buySize;

    @ToString.Exclude
    public transient int sellSize;

    public long[] buyPrices;
    public long[] buyVolumes;
    public long[] sellPrices;
    public long[] sellVolumes;

    public long timestamp;

    public L1MarketData(long[] buyPrices, long[] buyVolumes,
                        long[] sellPrices, long[] sellVolumes) {
        this.buyPrices = buyPrices;
        this.buyVolumes = buyVolumes;
        this.sellPrices = sellPrices;
        this.sellVolumes = sellVolumes;

        this.buySize = buyPrices.length;
        this.sellSize = sellPrices.length;
    }

    public L1MarketData(int buySize, int sellSize) {
        this.buyPrices = new long[buySize];
        this.buyVolumes = new long[buySize];
        this.sellPrices = new long[sellSize];
        this.sellVolumes = new long[sellSize];
    }


}
