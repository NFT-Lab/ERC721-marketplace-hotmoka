package io.nfteam.nftlab.nftlabmarketplace;

import io.takamaka.code.lang.*;
import io.takamaka.code.util.*;
import java.math.BigInteger;

public class Marketplace extends Contract {

    private final NFTLabStore store;

    private final StorageMap<BigInteger, Trade> trades = new StorageTreeMap<>();
    private final StorageMap<Contract, StorageLinkedList<BigInteger>> _addressToTrades =
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
        Takamaka.require(
                price.compareTo(BigInteger.ZERO) >= 0,
                "You cannot sell an nft for a negativa price");
        this.store.safeTransferFrom(caller(), this, item);
        Trade newTrade = new Trade((PayableContract) caller(), item, price, Status.OPEN);
        trades.putIfAbsent(counter, newTrade);
        StorageLinkedList<BigInteger> trades =
                _addressToTrades.getOrDefault(caller(), new StorageLinkedList<>());
        trades.add(counter);
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
        Takamaka.require(trade.status.equals(Status.OPEN), "Trade is not open to execution");
        _checkPayment(caller(), amount, trade.price);
        Takamaka.require(_pay(caller(), trade.poster, trade.price), "Failed to pay the artist");
        this.store.safeTransferFrom(this, caller(), trade.item);
        nftToActiveTrade.remove(trade.item);
        trade.status = Status.EXECUTED;
        trades.put(tradeID, trade);
        Takamaka.event(new TradeStatusChange(trade.item, Status.EXECUTED));
    }

    protected boolean _pay(Contract from, PayableContract to, BigInteger amount) {
        to.receive(amount);
        return true;
    }

    protected void _checkPayment(Contract from, BigInteger amount, BigInteger price) {
        Takamaka.require(
                amount.compareTo(price) >= 0,
                "You should at least pay the price of the token to get it");
    }

    @FromContract
    public void cancelTrade(BigInteger tradeID) {
        Trade trade = trades.get(tradeID);
        Takamaka.require(trade.status.equals(Status.OPEN), "Trade is not open to execution");
        Takamaka.require(caller() == trade.poster, "Trade can only be cancelled by poster");
        this.store.safeTransferFrom(this, trade.poster, trade.item);
        nftToActiveTrade.remove(trade.item);
        trade.status = Status.CANCELLED;
        trades.put(tradeID, trade);
        Takamaka.event(new TradeStatusChange(trade.item, Status.CANCELLED));
    }

    @View
    public Contract getStorage() {
        return this.store;
    }

    @View
    public BigInteger getTradeOfNFT(BigInteger tokenID) {
        return nftToActiveTrade.get(tokenID);
    }

    @View
    public StorageListView<BigInteger> getTradesOfAddress(Contract address) {
        return _addressToTrades.get(address);
    }

    @View
    public Trade getTrade(BigInteger tradeID) {
        return trades.get(tradeID);
    }

    public static class TradeStatusChange extends Event {
        private final BigInteger item;
        private final Status status;

        @FromContract
        public TradeStatusChange(BigInteger item, Status status) {
            this.item = item;
            this.status = status;
        }
    }
}
