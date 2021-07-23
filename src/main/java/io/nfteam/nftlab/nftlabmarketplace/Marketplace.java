package io.nfteam.nftlab.nftlabmarketplace;

import io.takamaka.code.lang.*;
import io.takamaka.code.math.UnsignedBigInteger;
import io.takamaka.code.util.*;
import java.math.BigInteger;

public class Marketplace extends Contract {

    private final NFTLabStore store;

    private final StorageMap<BigInteger, Trade> trades = new StorageTreeMap<>();
    private final StorageMap<Contract, StorageLinkedList<Trade>> _addressToTrades =
            new StorageTreeMap<>();
    private final StorageMap<BigInteger, BigInteger> nftToActiveTrade = new StorageTreeMap<>();

    private BigInteger tradeCounter = BigInteger.ONE;

    @FromContract
    public Marketplace(String _name, String _symbol) {
        this.store = new NFTLabStore(_name, _symbol);
    }

    @FromContract
    public BigInteger openTrade(BigInteger item, BigInteger price) {
        BigInteger counter = tradeCounter;
        this.store.safeTransferFrom(caller(), this, item);
        Trade newTrade = new Trade((PayableContract) caller(), item, price, Status.OPEN);
        trades.putIfAbsent(counter, newTrade);
        StorageLinkedList<Trade> trades =
                _addressToTrades.getOrDefault(caller(), new StorageLinkedList<>());
        trades.add(newTrade);
        _addressToTrades.put(caller(), trades);
        nftToActiveTrade.put(item, counter);
        tradeCounter = tradeCounter.add(BigInteger.ONE);
        Takamaka.event(new TradeStatusChange(counter, Status.OPEN));
        return counter;
    }

    @FromContract
    @Payable
    public void executeTrade(BigInteger amount, BigInteger tradeID) {
        Trade trade = trades.get(tradeID);
        Takamaka.require(
                amount.compareTo(trade.price) >= 0,
                "You should at least pay the price of the token to get it");
        Takamaka.require(trade.status.equals(Status.OPEN), "Trade is not open to execution");
        trade.poster.receive(trade.price);
        nftToActiveTrade.remove(trade.item);
        this.store.safeTransferFrom(this, caller(), trade.item);
        trade.status = Status.EXECUTED;
        trades.put(tradeID, trade);
        Takamaka.event(new TradeStatusChange(trade.item, Status.EXECUTED));
    }

    @FromContract
    public void cancelTrade(BigInteger tradeID) {
        Trade trade = trades.get(tradeID);
        Takamaka.require(trade.status.equals(Status.OPEN), "Trade is not open to execution");
        Takamaka.require(caller() == trade.poster, "Trade can only be cancelled by poster");
        nftToActiveTrade.remove(trade.item);
        this.store.safeTransferFrom(this, trade.poster, trade.item);
        trade.status = Status.CANCELLED;
        trades.put(tradeID, trade);
        Takamaka.event(new TradeStatusChange(trade.item, Status.CANCELLED));
    }

    public Contract getStorage() {
        return this.store;
    }

    public BigInteger getTradeOfNFT(UnsignedBigInteger tokenID) {
        return nftToActiveTrade.get(tokenID);
    }

    public StorageList<Trade> getTradesOfAddress(Contract address) {
        return _addressToTrades.get(address);
    }

    public Trade getTrade(BigInteger tradeID) {
        return trades.get(tradeID);
    }

    public class TradeStatusChange extends Event {
        private final BigInteger item;
        private final Status status;

        @FromContract
        public TradeStatusChange(BigInteger item, Status status) {
            this.item = item;
            this.status = status;
        }
    }
}
