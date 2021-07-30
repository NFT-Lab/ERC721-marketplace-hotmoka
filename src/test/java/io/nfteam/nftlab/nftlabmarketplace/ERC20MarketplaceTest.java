package io.nfteam.nftlab.nftlabmarketplace;

import static io.hotmoka.beans.Coin.*;
import static org.junit.jupiter.api.Assertions.*;

import io.hotmoka.beans.CodeExecutionException;
import io.hotmoka.beans.TransactionException;
import io.hotmoka.beans.TransactionRejectedException;
import io.hotmoka.beans.references.TransactionReference;
import io.hotmoka.beans.signatures.CodeSignature;
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

class ERC20MarketplaceTest extends TakamakaTest {

    private static final ClassType Marketplace =
            new ClassType("io.nfteam.nftlab.nftlabmarketplace.ERC20Marketplace");

    private static final ClassType ERC20 =
            new ClassType("io.nfteam.nftlab.nftlabmarketplace.FakeToken");

    private static final ClassType NFTLabStore =
            new ClassType("io.nfteam.nftlab.nftlabmarketplace.NFTLabStore");

    private static final ConstructorSignature CONSTRUCTOR_ERC20_STR_STR =
            new ConstructorSignature(ERC20);

    private static final ConstructorSignature CONSTRUCTOR_MARKETPLACE_IERC20_STR_STR =
            new ConstructorSignature(
                    Marketplace,
                    new ClassType("io.takamaka.code.tokens.IERC20"),
                    ClassType.STRING,
                    ClassType.STRING);

    private static final ConstructorSignature CONSTRUCTOR_UNSIGNEDBIGINTEGER =
            new ConstructorSignature(ClassType.UNSIGNED_BIG_INTEGER, BasicTypes.INT);

    private TransactionReference classpath;

    private StorageReference creator;
    private PrivateKey creator_prv_key;

    private StorageReference marketplace;
    private StorageReference store;
    private StorageReference erc20;

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

        StringValue name = new StringValue("NFTLab");
        StringValue symbol = new StringValue("NFTL");

        erc20 =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_ERC20_STR_STR);

        marketplace =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_MARKETPLACE_IERC20_STR_STR,
                        erc20,
                        name,
                        symbol);

        store =
                (StorageReference)
                        addInstanceMethodCallTransaction(
                                creator_prv_key,
                                creator,
                                _10_000_000,
                                panarea(1),
                                classpath,
                                new NonVoidMethodSignature(
                                        Marketplace, "getStorage", ClassType.CONTRACT),
                                marketplace);

        addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new VoidMethodSignature(ERC20, "juice"),
                erc20);

        addInstanceMethodCallTransaction(
                privateKey(2),
                account(2),
                _10_000_000,
                panarea(1),
                classpath,
                new VoidMethodSignature(ERC20, "juice"),
                erc20);
    }

    @Test
    void constructor()
            throws SignatureException, TransactionException, CodeExecutionException,
                    InvalidKeyException, TransactionRejectedException {
        StringValue expectedName = new StringValue("NFTLab");
        StringValue expectedSymbol = new StringValue("NFTL");

        StorageReference marketplace =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_MARKETPLACE_IERC20_STR_STR,
                        erc20,
                        expectedName,
                        expectedSymbol);

        StorageValue store =
                addInstanceMethodCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        new NonVoidMethodSignature(Marketplace, "getStorage", ClassType.CONTRACT),
                        marketplace);

        StorageValue actualName =
                addInstanceMethodCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        new NonVoidMethodSignature(NFTLabStore, "name", ClassType.STRING),
                        (StorageReference) store);

        StorageValue actualSymbol =
                addInstanceMethodCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        new NonVoidMethodSignature(NFTLabStore, "symbol", ClassType.STRING),
                        (StorageReference) store);

        assertEquals(expectedName, actualName);
        assertEquals(expectedSymbol, actualSymbol);
    }

    /* OPEN TRADE TESTS */
    @Test
    void openTrade_happyPath()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {
        StringValue cid = new StringValue("cid");
        StringValue metadataCid = new StringValue("metadataCid");
        BooleanValue isImage = new BooleanValue(true);
        BooleanValue isMusic = new BooleanValue(true);
        BooleanValue isVideo = new BooleanValue(true);

        StorageValue mintedID =
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
                        store,
                        creator,
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
                new VoidMethodSignature(
                        NFTLabStore, "approve", ClassType.CONTRACT, ClassType.BIG_INTEGER),
                store,
                marketplace,
                mintedID);

        StorageValue openTrade =
                addInstanceMethodCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        new NonVoidMethodSignature(
                                Marketplace,
                                "openTrade",
                                ClassType.BIG_INTEGER,
                                ClassType.BIG_INTEGER,
                                ClassType.BIG_INTEGER),
                        marketplace,
                        mintedID,
                        new BigIntegerValue(panarea(1)));

        assertDoesNotThrow(
                () ->
                        addInstanceMethodCallTransaction(
                                creator_prv_key,
                                creator,
                                _10_000_000,
                                panarea(1),
                                classpath,
                                new NonVoidMethodSignature(
                                        Marketplace,
                                        "getTrade",
                                        new ClassType("io.nfteam.nftlab.nftlabmarketplace.Trade"),
                                        ClassType.BIG_INTEGER),
                                marketplace,
                                openTrade));
    }

    /* EXECUTE TRADE TESTS */
    @Test
    void executeTrade_happyPath()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {
        StringValue cid = new StringValue("cid");
        StringValue metadataCid = new StringValue("metadataCid");
        BooleanValue isImage = new BooleanValue(true);
        BooleanValue isMusic = new BooleanValue(true);
        BooleanValue isVideo = new BooleanValue(true);

        StorageValue mintedID =
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
                        store,
                        creator,
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
                new VoidMethodSignature(
                        NFTLabStore, "approve", ClassType.CONTRACT, ClassType.BIG_INTEGER),
                store,
                marketplace,
                mintedID);

        StorageValue openTrade =
                addInstanceMethodCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        new NonVoidMethodSignature(
                                Marketplace,
                                "openTrade",
                                ClassType.BIG_INTEGER,
                                ClassType.BIG_INTEGER,
                                ClassType.BIG_INTEGER),
                        marketplace,
                        mintedID,
                        new BigIntegerValue(panarea(1)));

        addInstanceMethodCallTransaction(
                privateKey(2),
                account(2),
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        ERC20,
                        "approve",
                        BasicTypes.BOOLEAN,
                        ClassType.CONTRACT,
                        ClassType.BIG_INTEGER),
                erc20,
                marketplace,
                new BigIntegerValue(BigInteger.TEN));

        assertDoesNotThrow(
                () ->
                        addInstanceMethodCallTransaction(
                                privateKey(2),
                                account(2),
                                _10_000_000,
                                panarea(1),
                                classpath,
                                new VoidMethodSignature(
                                        Marketplace,
                                        "executeTrade",
                                        ClassType.BIG_INTEGER,
                                        ClassType.BIG_INTEGER),
                                marketplace,
                                new BigIntegerValue(BigInteger.TEN),
                                openTrade));
    }

    @Test
    void executeTrade_invalidAmount()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {
        StringValue cid = new StringValue("cid");
        StringValue metadataCid = new StringValue("metadataCid");
        BooleanValue isImage = new BooleanValue(true);
        BooleanValue isMusic = new BooleanValue(true);
        BooleanValue isVideo = new BooleanValue(true);

        StorageValue mintedID =
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
                        store,
                        creator,
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
                new VoidMethodSignature(
                        NFTLabStore, "approve", ClassType.CONTRACT, ClassType.BIG_INTEGER),
                store,
                marketplace,
                mintedID);

        StorageValue openTrade =
                addInstanceMethodCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        new NonVoidMethodSignature(
                                Marketplace,
                                "openTrade",
                                ClassType.BIG_INTEGER,
                                ClassType.BIG_INTEGER,
                                ClassType.BIG_INTEGER),
                        marketplace,
                        mintedID,
                        new BigIntegerValue(panarea(100)));

        BigInteger balanceBefore =
                ((BigIntegerValue)
                                runInstanceMethodCallTransaction(
                                        account(2),
                                        _50_000,
                                        classpath,
                                        CodeSignature.BALANCE,
                                        account(2)))
                        .value;

        StorageReference value =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_UNSIGNEDBIGINTEGER,
                        new IntValue(10));

        addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new NonVoidMethodSignature(
                        ERC20,
                        "approve",
                        BasicTypes.BOOLEAN,
                        ClassType.CONTRACT,
                        ClassType.UNSIGNED_BIG_INTEGER),
                erc20,
                marketplace,
                value);

        throwsTransactionExceptionWithCause(
                Constants.REQUIREMENT_VIOLATION_EXCEPTION_NAME,
                () ->
                        addInstanceMethodCallTransaction(
                                privateKey(2),
                                account(2),
                                _10_000_000,
                                panarea(1),
                                classpath,
                                new VoidMethodSignature(
                                        Marketplace,
                                        "executeTrade",
                                        ClassType.BIG_INTEGER,
                                        ClassType.BIG_INTEGER),
                                marketplace,
                                new BigIntegerValue(BigInteger.TEN),
                                openTrade));

        BigInteger balanceAfter =
                ((BigIntegerValue)
                                runInstanceMethodCallTransaction(
                                        account(2),
                                        _50_000,
                                        classpath,
                                        CodeSignature.BALANCE,
                                        account(2)))
                        .value;

        assertEquals(1, balanceBefore.compareTo(balanceAfter));
    }

    @Test
    void executeTrade_executingACloseTrade()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {
        StringValue cid = new StringValue("cid");
        StringValue metadataCid = new StringValue("metadataCid");
        BooleanValue isImage = new BooleanValue(true);
        BooleanValue isMusic = new BooleanValue(true);
        BooleanValue isVideo = new BooleanValue(true);

        StorageValue mintedID =
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
                        store,
                        creator,
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
                new VoidMethodSignature(
                        NFTLabStore, "approve", ClassType.CONTRACT, ClassType.BIG_INTEGER),
                store,
                marketplace,
                mintedID);

        StorageValue openTrade =
                addInstanceMethodCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        new NonVoidMethodSignature(
                                Marketplace,
                                "openTrade",
                                ClassType.BIG_INTEGER,
                                ClassType.BIG_INTEGER,
                                ClassType.BIG_INTEGER),
                        marketplace,
                        mintedID,
                        new BigIntegerValue(panarea(3)));

        addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new VoidMethodSignature(Marketplace, "cancelTrade", ClassType.BIG_INTEGER),
                marketplace,
                mintedID);

        BigInteger balanceBefore =
                ((BigIntegerValue)
                                runInstanceMethodCallTransaction(
                                        account(2),
                                        _50_000,
                                        classpath,
                                        CodeSignature.BALANCE,
                                        account(2)))
                        .value;

        throwsTransactionExceptionWithCause(
                Constants.REQUIREMENT_VIOLATION_EXCEPTION_NAME,
                () ->
                        addInstanceMethodCallTransaction(
                                privateKey(2),
                                account(2),
                                _10_000_000,
                                panarea(1),
                                classpath,
                                new VoidMethodSignature(
                                        Marketplace,
                                        "executeTrade",
                                        ClassType.BIG_INTEGER,
                                        ClassType.BIG_INTEGER),
                                marketplace,
                                new BigIntegerValue(BigInteger.TEN),
                                openTrade));

        BigInteger balanceAfter =
                ((BigIntegerValue)
                                runInstanceMethodCallTransaction(
                                        account(2),
                                        _50_000,
                                        classpath,
                                        CodeSignature.BALANCE,
                                        account(2)))
                        .value;

        // the caller should have payed for the transaction anyway
        assertEquals(1, balanceBefore.compareTo(balanceAfter));
    }

    /* CANCEL TRADE TESTS */
    @Test
    void cancelTrade_happyPath()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {
        StringValue cid = new StringValue("cid");
        StringValue metadataCid = new StringValue("metadataCid");
        BooleanValue isImage = new BooleanValue(true);
        BooleanValue isMusic = new BooleanValue(true);
        BooleanValue isVideo = new BooleanValue(true);

        StorageValue mintedID =
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
                        store,
                        creator,
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
                new VoidMethodSignature(
                        NFTLabStore, "approve", ClassType.CONTRACT, ClassType.BIG_INTEGER),
                store,
                marketplace,
                mintedID);

        StorageValue openTrade =
                addInstanceMethodCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        new NonVoidMethodSignature(
                                Marketplace,
                                "openTrade",
                                ClassType.BIG_INTEGER,
                                ClassType.BIG_INTEGER,
                                ClassType.BIG_INTEGER),
                        marketplace,
                        mintedID,
                        new BigIntegerValue(panarea(3)));

        addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new VoidMethodSignature(Marketplace, "cancelTrade", ClassType.BIG_INTEGER),
                marketplace,
                mintedID);

        assertEquals(
                NullValue.class,
                addInstanceMethodCallTransaction(
                                privateKey(2),
                                account(2),
                                _10_000_000,
                                panarea(1),
                                classpath,
                                new NonVoidMethodSignature(
                                        Marketplace,
                                        "getTradeOfNFT",
                                        ClassType.BIG_INTEGER,
                                        ClassType.BIG_INTEGER),
                                marketplace,
                                mintedID)
                        .getClass());
    }

    /* getTradesOfAddress TESTS */
    @Test
    void getTradesOfAddress_keepsListOfTrades()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {
        StringValue cid = new StringValue("cid");
        StringValue metadataCid = new StringValue("metadataCid");
        BooleanValue isImage = new BooleanValue(true);
        BooleanValue isMusic = new BooleanValue(true);
        BooleanValue isVideo = new BooleanValue(true);

        StorageValue mintedID =
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
                        store,
                        creator,
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
                new VoidMethodSignature(
                        NFTLabStore, "approve", ClassType.CONTRACT, ClassType.BIG_INTEGER),
                store,
                marketplace,
                mintedID);

        StorageValue openTrade =
                addInstanceMethodCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        new NonVoidMethodSignature(
                                Marketplace,
                                "openTrade",
                                ClassType.BIG_INTEGER,
                                ClassType.BIG_INTEGER,
                                ClassType.BIG_INTEGER),
                        marketplace,
                        mintedID,
                        new BigIntegerValue(panarea(3)));

        StorageValue tradesBefore =
                addInstanceMethodCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        new NonVoidMethodSignature(
                                Marketplace,
                                "getTradesOfAddress",
                                ClassType.STORAGE_LIST,
                                ClassType.CONTRACT),
                        marketplace,
                        creator);

        addInstanceMethodCallTransaction(
                creator_prv_key,
                creator,
                _10_000_000,
                panarea(1),
                classpath,
                new VoidMethodSignature(Marketplace, "cancelTrade", ClassType.BIG_INTEGER),
                marketplace,
                mintedID);

        StorageValue tradesAfter =
                addInstanceMethodCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        new NonVoidMethodSignature(
                                Marketplace,
                                "getTradesOfAddress",
                                ClassType.STORAGE_LIST,
                                ClassType.CONTRACT),
                        marketplace,
                        creator);

        assertEquals(0, tradesBefore.compareTo(tradesAfter));
    }
}
