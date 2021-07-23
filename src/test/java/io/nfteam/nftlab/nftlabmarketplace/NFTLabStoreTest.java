package io.nfteam.nftlab.nftlabmarketplace;

import static io.hotmoka.beans.Coin.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.hotmoka.beans.CodeExecutionException;
import io.hotmoka.beans.TransactionException;
import io.hotmoka.beans.TransactionRejectedException;
import io.hotmoka.beans.references.TransactionReference;
import io.hotmoka.beans.signatures.ConstructorSignature;
import io.hotmoka.beans.signatures.NonVoidMethodSignature;
import io.hotmoka.beans.signatures.VoidMethodSignature;
import io.hotmoka.beans.types.BasicTypes;
import io.hotmoka.beans.types.ClassType;
import io.hotmoka.beans.values.*;
import io.hotmoka.constants.Constants;
import io.hotmoka.views.GasHelper;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NFTLabStoreTest extends TakamakaTest {

    private static final ClassType NFTLabStore =
            new ClassType("io.nfteam.nftlab.nftlabmarketplace.NFTLabStore");

    private static final ClassType NFTLab =
            new ClassType("io.nfteam.nftlab.nftlabmarketplace.NFTLab");

    private static final ClassType NFTTransaction =
            new ClassType("io.nfteam.nftlab.nftlabmarketplace.NFTTransaction");

    private static final ConstructorSignature CONSTRUCTOR_NFTLABSTORE_STR_STR =
            new ConstructorSignature(NFTLabStore, ClassType.STRING, ClassType.STRING);

    private static final ConstructorSignature CONSTRUCTOR_NFTLAB_STR_STR_BOOL_BOOL_BOOL =
            new ConstructorSignature(
                    NFTLab,
                    ClassType.STRING,
                    ClassType.STRING,
                    BasicTypes.BOOLEAN,
                    BasicTypes.BOOLEAN,
                    BasicTypes.BOOLEAN);

    private static final ConstructorSignature CONSTRUCTOR_NFTTRANSACTION_BI_CONTRACT_CONTRACT_STR =
            new ConstructorSignature(
                    NFTTransaction, ClassType.BIG_INTEGER, ClassType.CONTRACT, ClassType.CONTRACT);

    private TransactionReference classpath;

    private StorageReference creator;
    private PrivateKey creator_prv_key;

    private StorageReference nftLabStore;

    @BeforeEach
    void beforeEach() throws Exception {
        setAccounts(stromboli(1), filicudi(100), filicudi(100), filicudi(100));
        creator = account(1);
        creator_prv_key = privateKey(1);

        GasHelper gasHelper = new GasHelper(node);

        TransactionReference erc721Ref =
                addJarStoreTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        gasHelper.getSafeGasPrice(),
                        node.getTakamakaCode(),
                        Files.readAllBytes(erc721CodePath),
                        node.getTakamakaCode());

        classpath =
                addJarStoreTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        gasHelper.getSafeGasPrice(),
                        node.getTakamakaCode(),
                        Files.readAllBytes(smartContractCodePath),
                        erc721Ref);
    }

    @BeforeEach
    void createNFTLabStore()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {
        StringValue name = new StringValue("NFTLab");
        StringValue symbol = new StringValue("NFTL");

        nftLabStore =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_NFTLABSTORE_STR_STR,
                        name,
                        symbol);
    }

    @Test
    void constructor()
            throws SignatureException, TransactionException, CodeExecutionException,
                    InvalidKeyException, TransactionRejectedException {
        StringValue expectedName = new StringValue("NFTLab");
        StringValue expectedSymbol = new StringValue("NFTL");

        StorageReference nftLabStore =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_NFTLABSTORE_STR_STR,
                        expectedName,
                        expectedSymbol);

        StorageValue actualName =
                addInstanceMethodCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        new NonVoidMethodSignature(NFTLabStore, "name", ClassType.STRING),
                        nftLabStore);

        StorageValue actualSymbol =
                addInstanceMethodCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        new NonVoidMethodSignature(NFTLabStore, "symbol", ClassType.STRING),
                        nftLabStore);

        assertEquals(expectedName, actualName);
        assertEquals(expectedSymbol, actualSymbol);
    }

    @Test
    void mint_ValidNFT_NFTTokenId()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {
        BigIntegerValue expectedTokenId = new BigIntegerValue(BigInteger.ONE);

        StringValue cid = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue metadataCid = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");

        BooleanValue isImage = new BooleanValue(true);
        BooleanValue isMusic = new BooleanValue(false);
        BooleanValue isVideo = new BooleanValue(false);

        BigIntegerValue actualTokenId =
                (BigIntegerValue)
                        addInstanceMethodCallTransaction(
                                creator_prv_key,
                                creator,
                                _10_000_000,
                                panarea(1),
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLabStore,
                                        "mint",
                                        ClassType.BIG_INTEGER,
                                        ClassType.CONTRACT,
                                        ClassType.STRING,
                                        ClassType.STRING,
                                        BasicTypes.BOOLEAN,
                                        BasicTypes.BOOLEAN,
                                        BasicTypes.BOOLEAN),
                                nftLabStore,
                                account(2),
                                cid,
                                metadataCid,
                                isImage,
                                isMusic,
                                isVideo);

        assertEquals(expectedTokenId, actualTokenId);
    }

    @Test
    void mint_MintTwoTimesSameNFT_ThrowException()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {

        StorageValue cid = new StringValue("Cid");
        StorageValue metadataCid = new StringValue("metadataCid");

        BooleanValue isImage = new BooleanValue(true);
        BooleanValue isMusic = new BooleanValue(false);
        BooleanValue isVideo = new BooleanValue(false);

        addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "mint",
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.STRING,
                        ClassType.STRING,
                        BasicTypes.BOOLEAN,
                        BasicTypes.BOOLEAN,
                        BasicTypes.BOOLEAN),
                nftLabStore,
                account(2),
                cid,
                metadataCid,
                isImage,
                isMusic,
                isVideo);

        throwsTransactionExceptionWithCause(
                Constants.REQUIREMENT_VIOLATION_EXCEPTION_NAME,
                () ->
                        addInstanceMethodCallTransaction(
                                creator_prv_key,
                                creator,
                                _10_000_000,
                                panarea(1),
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLabStore,
                                        "mint",
                                        ClassType.BIG_INTEGER,
                                        ClassType.CONTRACT,
                                        ClassType.STRING,
                                        ClassType.STRING,
                                        BasicTypes.BOOLEAN,
                                        BasicTypes.BOOLEAN,
                                        BasicTypes.BOOLEAN),
                                nftLabStore,
                                account(2),
                                cid,
                                metadataCid,
                                isImage,
                                isMusic,
                                isVideo));
    }

    @Test
    void getHistory_ExistingNFT_ItsHistory()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {

        BigIntegerValue tokenId = new BigIntegerValue(BigInteger.ONE);

        StringValue cid = new StringValue("cid");

        StorageReference expectedTransaction =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _500_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_NFTTRANSACTION_BI_CONTRACT_CONTRACT_STR,
                        tokenId,
                        account(2),
                        account(3));

        BigIntegerValue mintedID =
                (BigIntegerValue)
                        addInstanceMethodCallTransaction(
                                privateKey(2),
                                account(2),
                                _10_000_000,
                                panarea(1),
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLabStore,
                                        "mint",
                                        ClassType.BIG_INTEGER,
                                        ClassType.CONTRACT,
                                        ClassType.STRING,
                                        ClassType.STRING,
                                        BasicTypes.BOOLEAN,
                                        BasicTypes.BOOLEAN,
                                        BasicTypes.BOOLEAN),
                                nftLabStore,
                                account(2),
                                cid,
                                new StringValue("metadataCid"),
                                new BooleanValue(true),
                                new BooleanValue(false),
                                new BooleanValue(false));

        addInstanceMethodCallTransaction(
                privateKey(2),
                account(2),
                _10_000_000,
                panarea(1),
                classpath,
                new VoidMethodSignature(
                        NFTLabStore,
                        "safeTransferFrom",
                        ClassType.CONTRACT,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER),
                nftLabStore,
                account(2),
                account(3),
                mintedID);

        StorageReference history =
                (StorageReference)
                        runInstanceMethodCallTransaction(
                                creator,
                                _10_000_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLabStore,
                                        "getHistory",
                                        ClassType.STORAGE_LINKED_LIST,
                                        ClassType.BIG_INTEGER),
                                nftLabStore,
                                tokenId);

        StorageReference actualTransaction =
                (StorageReference)
                        runInstanceMethodCallTransaction(
                                creator,
                                _10_000_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        ClassType.STORAGE_LINKED_LIST,
                                        "get",
                                        ClassType.OBJECT,
                                        BasicTypes.INT),
                                history,
                                new IntValue(1));

        BooleanValue sameTransaction =
                (BooleanValue)
                        runInstanceMethodCallTransaction(
                                creator,
                                _10_000_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTTransaction,
                                        "equals",
                                        BasicTypes.BOOLEAN,
                                        ClassType.OBJECT),
                                actualTransaction,
                                expectedTransaction);

        assertTrue(sameTransaction.value);
    }

    @Test
    void getHistory_NonExistingNFT_ThrowException()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {
        BigIntegerValue tokenId = new BigIntegerValue(BigInteger.ONE);

        throwsTransactionExceptionWithCause(
                Constants.REQUIREMENT_VIOLATION_EXCEPTION_NAME,
                () ->
                        addInstanceMethodCallTransaction(
                                creator_prv_key,
                                creator,
                                _10_000_000,
                                panarea(1),
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLabStore,
                                        "getHistory",
                                        ClassType.STORAGE_LINKED_LIST,
                                        ClassType.BIG_INTEGER),
                                nftLabStore,
                                tokenId));
    }

    @Test
    void getTokenId_ExistingNFT_ItsTokenId()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {

        StorageValue cid = new StringValue("Cid");
        StorageValue metadataCid = new StringValue("metadataCid");

        BooleanValue isImage = new BooleanValue(true);
        BooleanValue isMusic = new BooleanValue(false);
        BooleanValue isVideo = new BooleanValue(false);

        BigIntegerValue expectedTokenId = new BigIntegerValue(BigInteger.ONE);

        addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "mint",
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.STRING,
                        ClassType.STRING,
                        BasicTypes.BOOLEAN,
                        BasicTypes.BOOLEAN,
                        BasicTypes.BOOLEAN),
                nftLabStore,
                account(2),
                cid,
                metadataCid,
                isImage,
                isMusic,
                isVideo);

        BigIntegerValue actualTokenId =
                (BigIntegerValue)
                        addInstanceMethodCallTransaction(
                                creator_prv_key,
                                creator,
                                _10_000_000,
                                panarea(1),
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLabStore,
                                        "getTokenId",
                                        ClassType.BIG_INTEGER,
                                        ClassType.STRING),
                                nftLabStore,
                                cid);

        assertEquals(expectedTokenId, actualTokenId);
    }

    @Test
    void getTokenId_NotExistingNFT_ThrowException() {
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");

        throwsTransactionExceptionWithCause(
                Constants.REQUIREMENT_VIOLATION_EXCEPTION_NAME,
                () ->
                        addInstanceMethodCallTransaction(
                                creator_prv_key,
                                creator,
                                _10_000_000,
                                panarea(1),
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLabStore,
                                        "getTokenId",
                                        ClassType.BIG_INTEGER,
                                        ClassType.STRING),
                                nftLabStore,
                                hash));
    }

    @Test
    void getNFTByHash_ExistingNFT_NFT()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {
        StringValue cid = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue metadataCid = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");

        BooleanValue isImage = new BooleanValue(true);
        BooleanValue isMusic = new BooleanValue(false);
        BooleanValue isVideo = new BooleanValue(false);

        StorageReference expectedNFT =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _500_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_NFTLAB_STR_STR_BOOL_BOOL_BOOL,
                        cid,
                        metadataCid,
                        isImage,
                        isMusic,
                        isVideo);

        addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        NFTLabStore,
                        "mint",
                        ClassType.BIG_INTEGER,
                        ClassType.CONTRACT,
                        ClassType.STRING,
                        ClassType.STRING,
                        BasicTypes.BOOLEAN,
                        BasicTypes.BOOLEAN,
                        BasicTypes.BOOLEAN),
                nftLabStore,
                account(2),
                cid,
                metadataCid,
                isImage,
                isMusic,
                isVideo);

        StorageReference actualNFT =
                (StorageReference)
                        runInstanceMethodCallTransaction(
                                creator,
                                _10_000_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLabStore, "getNFTByHash", NFTLab, ClassType.STRING),
                                nftLabStore,
                                cid);

        BooleanValue sameNFT =
                (BooleanValue)
                        runInstanceMethodCallTransaction(
                                creator,
                                _10_000_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLab, "equals", BasicTypes.BOOLEAN, ClassType.OBJECT),
                                expectedNFT,
                                actualNFT);

        assertTrue(sameNFT.value);
    }

    @Test
    void getNFTByHash_NotExistingNFT_ThrowException() {
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");

        throwsTransactionExceptionWithCause(
                Constants.REQUIREMENT_VIOLATION_EXCEPTION_NAME,
                () ->
                        runInstanceMethodCallTransaction(
                                creator,
                                _10_000_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLabStore, "getNFTByHash", NFTLab, ClassType.STRING),
                                nftLabStore,
                                hash));
    }

    @Test
    void getNFTById_ExistingNFT_NFT()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {
        StringValue cid = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue metadataCid = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");

        BooleanValue isImage = new BooleanValue(true);
        BooleanValue isMusic = new BooleanValue(false);
        BooleanValue isVideo = new BooleanValue(false);

        StorageReference expectedNFT =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _500_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_NFTLAB_STR_STR_BOOL_BOOL_BOOL,
                        cid,
                        metadataCid,
                        isImage,
                        isMusic,
                        isVideo);

        BigIntegerValue tokenId =
                (BigIntegerValue)
                        addInstanceMethodCallTransaction(
                                creator_prv_key,
                                creator,
                                _10_000_000,
                                panarea(1),
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLabStore,
                                        "mint",
                                        ClassType.BIG_INTEGER,
                                        ClassType.CONTRACT,
                                        ClassType.STRING,
                                        ClassType.STRING,
                                        BasicTypes.BOOLEAN,
                                        BasicTypes.BOOLEAN,
                                        BasicTypes.BOOLEAN),
                                nftLabStore,
                                account(2),
                                cid,
                                metadataCid,
                                isImage,
                                isMusic,
                                isVideo);

        StorageReference actualNFT =
                (StorageReference)
                        runInstanceMethodCallTransaction(
                                creator,
                                _10_000_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLabStore, "getNFTById", NFTLab, ClassType.BIG_INTEGER),
                                nftLabStore,
                                tokenId);

        BooleanValue sameNFT =
                (BooleanValue)
                        runInstanceMethodCallTransaction(
                                creator,
                                _10_000_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLab, "equals", BasicTypes.BOOLEAN, ClassType.OBJECT),
                                expectedNFT,
                                actualNFT);

        assertTrue(sameNFT.value);
    }

    @Test
    void getNFTById_NotExistingNFT_ThrowException() {
        BigIntegerValue tokenId = new BigIntegerValue(BigInteger.ONE);

        throwsTransactionExceptionWithCause(
                Constants.REQUIREMENT_VIOLATION_EXCEPTION_NAME,
                () ->
                        runInstanceMethodCallTransaction(
                                creator,
                                _10_000_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLabStore, "getNFTById", NFTLab, ClassType.BIG_INTEGER),
                                nftLabStore,
                                tokenId));
    }

    @Test
    void tokenURI_ExistingNFT_NFTURI()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {
        StringValue cid = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue metadataCid = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");

        BooleanValue isImage = new BooleanValue(true);
        BooleanValue isMusic = new BooleanValue(false);
        BooleanValue isVideo = new BooleanValue(false);

        BigIntegerValue tokenId =
                (BigIntegerValue)
                        addInstanceMethodCallTransaction(
                                creator_prv_key,
                                creator,
                                _10_000_000,
                                panarea(1),
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLabStore,
                                        "mint",
                                        ClassType.BIG_INTEGER,
                                        ClassType.CONTRACT,
                                        ClassType.STRING,
                                        ClassType.STRING,
                                        BasicTypes.BOOLEAN,
                                        BasicTypes.BOOLEAN,
                                        BasicTypes.BOOLEAN),
                                nftLabStore,
                                account(2),
                                cid,
                                metadataCid,
                                isImage,
                                isMusic,
                                isVideo);

        StorageReference tokenIdUBI =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _500_000,
                        panarea(1),
                        classpath,
                        new ConstructorSignature(
                                ClassType.UNSIGNED_BIG_INTEGER, ClassType.BIG_INTEGER),
                        tokenId);

        StringValue actualURI =
                (StringValue)
                        runInstanceMethodCallTransaction(
                                creator,
                                _10_000_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLabStore,
                                        "tokenURI",
                                        ClassType.STRING,
                                        ClassType.UNSIGNED_BIG_INTEGER),
                                nftLabStore,
                                tokenIdUBI);

        assertEquals(("https://cloudflare-ipfs.com/ipfs/" + metadataCid), actualURI.toString());
    }
}
