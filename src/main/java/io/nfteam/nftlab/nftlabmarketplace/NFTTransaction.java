package io.nfteam.nftlab.nftlabmarketplace;

import io.takamaka.code.lang.Contract;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.View;
import java.math.BigInteger;

public final class NFTTransaction extends Storage {
    private final BigInteger tokenId;
    private final Contract seller;
    private final Contract buyer;

    public NFTTransaction(BigInteger tokenId, Contract seller, Contract buyer) {
        this.tokenId = tokenId;
        this.seller = seller;
        this.buyer = buyer;
    }

    @View
    public BigInteger getTokenId() {
        return tokenId;
    }

    @View
    public Contract getSeller() {
        return seller;
    }

    @View
    public Contract getBuyer() {
        return buyer;
    }

    @Override
    @View
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NFTTransaction that = (NFTTransaction) o;
        return tokenId.equals(that.tokenId)
                && (seller != null ? seller.equals(that.seller) : that.seller == null)
                && (buyer != null ? buyer.equals(that.buyer) : that.buyer == null);
    }
}
