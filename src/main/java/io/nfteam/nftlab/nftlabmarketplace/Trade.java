package io.nfteam.nftlab.nftlabmarketplace;

import io.takamaka.code.lang.PayableContract;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.View;
import java.math.BigInteger;

public class Trade extends Storage {
    public PayableContract poster;
    public BigInteger item;
    public BigInteger price;
    public Status status;

    public Trade(PayableContract poster, BigInteger item, BigInteger price, Status status) {
        this.poster = poster;
        this.item = item;
        this.price = price;
        this.status = status;
    }

    @Override
    @View
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
