package io.nfteam.nftlab.nftlabmarketplace;

import io.nfteam.nftlab.hotmoka.erc721.extensions.ERC721URIStorage;
import io.takamaka.code.lang.*;
import io.takamaka.code.math.UnsignedBigInteger;
import io.takamaka.code.util.*;
import java.math.BigInteger;

public class NFTLabStore extends ERC721URIStorage {
    private BigInteger tokenId = BigInteger.ONE;

    private StorageMap<Contract, StorageMap<UnsignedBigInteger, UnsignedBigInteger>> _ownedTokens =
            new StorageTreeMap<>();
    private StorageMap<UnsignedBigInteger, UnsignedBigInteger> _ownedTokensIndex =
            new StorageTreeMap<>();
    private StorageMap<UnsignedBigInteger, UnsignedBigInteger> _allTokensIndex =
            new StorageTreeMap<>();

    private final StorageMap<UnsignedBigInteger, NFTLab> nfts = new StorageTreeMap<>();
    private final StorageMap<String, UnsignedBigInteger> hashToId = new StorageTreeMap<>();
    private final StorageMap<UnsignedBigInteger, StorageLinkedList<NFTTransaction>> history =
            new StorageTreeMap<>();

    @FromContract
    public NFTLabStore(String name, String symbol) {
        super(name, symbol, true);
    }

    @FromContract
    public BigInteger mint(
            Contract to,
            String cid,
            String metadataCid,
            boolean isImage,
            boolean isMusic,
            boolean isVideo) {

        Takamaka.require(hashToId.get(cid) == null, "The token already exists.");

        BigInteger newTokenId = tokenId;
        UnsignedBigInteger newTokenIdUBI = new UnsignedBigInteger(newTokenId);

        _safeMint(to, newTokenIdUBI);
        _setTokenURI(newTokenIdUBI, cid);
        _recordHistory(null, to, newTokenId);

        nfts.put(newTokenIdUBI, new NFTLab(cid, metadataCid, isImage, isMusic, isVideo));
        hashToId.put(cid, newTokenIdUBI);

        tokenId = tokenId.add(BigInteger.ONE);

        Takamaka.event(new Minted(to, cid, metadataCid, isImage, isMusic, isVideo));

        return newTokenId;
    }

    @FromContract
    public void safeTransferFrom(Contract from, Contract to, BigInteger tokenId) {
        super.safeTransferFrom(from, to, new UnsignedBigInteger(tokenId));
        _recordHistory(from, to, tokenId);
    }

    @View
    public StorageLinkedList<NFTTransaction> getHistory(BigInteger tokenId) {
        UnsignedBigInteger tokenIdUBI = new UnsignedBigInteger(tokenId);
        Takamaka.require(_exists(tokenIdUBI), "Unable to get the history of a non-existent NFT.");

        return history.get(tokenIdUBI);
    }

    @View
    public BigInteger getTokenId(String hash) {
        Takamaka.require(hashToId.get(hash) != null, "Unable to get the ID of a non-existent NFT.");

        return hashToId.get(hash).toBigInteger();
    }

    @View
    public NFTLab getNFTByHash(String hash) {
        Takamaka.require(hashToId.get(hash) != null, "Unable to get a non-existent NFT.");

        return nfts.get(hashToId.get(hash));
    }

    @View
    public NFTLab getNFTById(BigInteger tokenId) {
        UnsignedBigInteger tokenIdUBI = new UnsignedBigInteger(tokenId);
        Takamaka.require(_exists(tokenIdUBI), "Unable to get a non-existent NFT.");

        return nfts.get(tokenIdUBI);
    }

    @Override
    protected String _baseURI() {
        return "https://cloudflare-ipfs.com/ipfs/";
    }

    public UnsignedBigInteger totalSupply() {
        return UnsignedBigInteger.valueOf(nfts.size());
    }

    public UnsignedBigInteger tokenOfOwnerByIndex(Contract owner, UnsignedBigInteger index) {
        return _ownedTokens
                .getOrDefault(owner, new StorageTreeMap<>())
                .getOrDefault(index, UnsignedBigInteger.valueOf(0));
    }

    public NFTLab tokenByIndex(UnsignedBigInteger index) {
        Takamaka.require(_exists(index), "Unable to get a non-existent NFT.");
        return nfts.get(index);
    }

    private void _recordHistory(Contract from, Contract to, BigInteger tokenID) {
        StorageLinkedList<NFTTransaction> historyOfToken =
                history.getOrDefault(new UnsignedBigInteger(tokenID), new StorageLinkedList<>());
        historyOfToken.add(new NFTTransaction(tokenID, from, to));
        history.put(new UnsignedBigInteger(tokenID), historyOfToken);
    }

    // Events

    class Minted extends Event {
        public final Contract to;
        public final String cid;
        public final String metadataCid;
        public final boolean isImage, isMusic, isVideo;

        @FromContract
        Minted(
                Contract to,
                String cid,
                String metadataCid,
                boolean isImage,
                boolean isMusic,
                boolean isVideo) {
            this.to = to;
            this.cid = cid;
            this.metadataCid = metadataCid;
            this.isImage = isImage;
            this.isMusic = isMusic;
            this.isVideo = isVideo;
        }
    }

    class Transferred extends Event {
        public final BigInteger tokenId;
        public final Contract seller;
        public final Contract buyer;
        public final String price;
        public final String timestamp;

        @FromContract
        public Transferred(
                BigInteger tokenId,
                Contract seller,
                Contract buyer,
                String price,
                String timestamp) {
            this.tokenId = tokenId;
            this.seller = seller;
            this.buyer = buyer;
            this.price = price;
            this.timestamp = timestamp;
        }
    }
}
