package io.nfteam.nftlab.nftlabmarketplace;

import static io.hotmoka.beans.Coin.*;
import static org.junit.jupiter.api.Assertions.*;

import io.hotmoka.beans.CodeExecutionException;
import io.hotmoka.beans.TransactionException;
import io.hotmoka.beans.TransactionRejectedException;
import io.hotmoka.beans.references.TransactionReference;
import io.hotmoka.beans.signatures.ConstructorSignature;
import io.hotmoka.beans.signatures.NonVoidMethodSignature;
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

class TradeTest extends TakamakaTest {
    private static final ClassType Trade =
            new ClassType("io.nfteam.nftlab.nftlabmarketplace.Trade");

    private static final ClassType Status =
            new ClassType("io.nfteam.nftlab.nftlabmarketplace.Status");

    private static final ConstructorSignature CONSTRUCTOR_TRADE_PYBL_BI_BI_STAUTS =
            new ConstructorSignature(
                    Trade,
                    ClassType.PAYABLE_CONTRACT,
                    ClassType.BIG_INTEGER,
                    ClassType.BIG_INTEGER,
                    new ClassType("io.nfteam.nftlab.nftlabmarketplace.Status"));

    private TransactionReference classpath;

    private StorageReference creator;
    private PrivateKey creator_prv_key;

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

    @Test
    void constructor()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {

        assertDoesNotThrow(
                () ->
                        addConstructorCallTransaction(
                                creator_prv_key,
                                creator,
                                _500_000,
                                panarea(1),
                                classpath,
                                CONSTRUCTOR_TRADE_PYBL_BI_BI_STAUTS,
                                account(1),
                                BigIntegerValue.of("1", ClassType.BIG_INTEGER),
                                BigIntegerValue.of(panarea(1000).toString(), ClassType.BIG_INTEGER),
                                new EnumValue(
                                        "io.nfteam.nftlab.nftlabmarketplace.Status", "OPEN")));
    }

    @Test
    void equals()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {

        StorageReference first =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _500_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_TRADE_PYBL_BI_BI_STAUTS,
                        account(1),
                        BigIntegerValue.of("1", ClassType.BIG_INTEGER),
                        BigIntegerValue.of(panarea(1000).toString(), ClassType.BIG_INTEGER),
                        new EnumValue("io.nfteam.nftlab.nftlabmarketplace.Status", "OPEN"));

        StorageReference second =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _500_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_TRADE_PYBL_BI_BI_STAUTS,
                        account(1),
                        BigIntegerValue.of("1", ClassType.BIG_INTEGER),
                        BigIntegerValue.of(panarea(1000).toString(), ClassType.BIG_INTEGER),
                        new EnumValue("io.nfteam.nftlab.nftlabmarketplace.Status", "OPEN"));

        BooleanValue sameStatus =
                (BooleanValue)
                        runInstanceMethodCallTransaction(
                                creator,
                                _10_000_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        Trade, "equals", BasicTypes.BOOLEAN, ClassType.OBJECT),
                                second,
                                first);

        assertTrue(sameStatus.value);
    }
}
