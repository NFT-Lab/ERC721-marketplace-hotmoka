package io.nfteam.nftlab.nftlabmarketplace;

import io.takamaka.code.lang.PayableContract;
import io.takamaka.code.math.UnsignedBigInteger;

public class Trade {
    public PayableContract poster;
    public UnsignedBigInteger item;
    public UnsignedBigInteger price;
    public Status status;

    public Trade(
            PayableContract poster,
            UnsignedBigInteger item,
            UnsignedBigInteger price,
            Status status) {
        this.poster = poster;
        this.item = item;
        this.price = price;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return poster.equals(trade.poster)
                && item.equals(trade.item)
                && price.equals(trade.price)
                && status.equals(trade.status);
    }
}
