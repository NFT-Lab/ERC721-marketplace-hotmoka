package io.nfteam.nftlab.nftlabmarketplace;

import static io.hotmoka.beans.Coin.*;
import static org.junit.jupiter.api.Assertions.*;

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
import io.hotmoka.views.GasHelper;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MarketplaceTest extends TakamakaTest {

    private static final ClassType Marketplace =
            new ClassType("io.nfteam.nftlab.nftlabmarketplace.Marketplace");

    private static final ClassType NFTLabStore =
            new ClassType("io.nfteam.nftlab.nftlabmarketplace.NFTLabStore");

    private static final ClassType NFTLab =
            new ClassType("io.nfteam.nftlab.nftlabmarketplace.NFTLab");

    private static final ClassType NFTTransaction =
            new ClassType("io.nfteam.nftlab.nftlabmarketplace.NFTTransaction");

    private static final ClassType Trade =
            new ClassType("io.nfteam.nftlab.nftlabmarketplace.Trade");

    private static final ConstructorSignature CONSTRUCTOR_MARKETPLACE_STR_STR =
            new ConstructorSignature(Marketplace, ClassType.STRING, ClassType.STRING);

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

    private static final ConstructorSignature CONSTRUCTOR_TADE_PYBL_BI_BI_STATUS =
            new ConstructorSignature(
                    Trade,
                    ClassType.PAYABLE_CONTRACT,
                    ClassType.BIG_INTEGER,
                    ClassType.BIG_INTEGER,
                    new ClassType("io.nfteam.nftlab.nftlabmarketplace.Status"));

    private TransactionReference classpath;

    private StorageReference creator;
    private PrivateKey creator_prv_key;

    private StorageReference marketplace;
    private StorageReference store;

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
    void createMarketplace()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {
        StringValue name = new StringValue("NFTLab");
        StringValue symbol = new StringValue("NFTL");

        marketplace =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _10_000_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_MARKETPLACE_STR_STR,
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
                        CONSTRUCTOR_MARKETPLACE_STR_STR,
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
}
