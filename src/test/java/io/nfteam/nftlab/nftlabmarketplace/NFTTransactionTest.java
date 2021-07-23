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
import io.hotmoka.beans.types.BasicTypes;
import io.hotmoka.beans.types.ClassType;
import io.hotmoka.beans.values.BigIntegerValue;
import io.hotmoka.beans.values.BooleanValue;
import io.hotmoka.beans.values.StorageReference;
import io.hotmoka.beans.values.StringValue;
import io.hotmoka.views.GasHelper;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NFTTransactionTest extends TakamakaTest {
    private static final ClassType NFTTransaction =
            new ClassType("io.nfteam.nftlab.nftlabmarketplace.NFTTransaction");
    private static final ConstructorSignature CONSTRUCTOR_NFTTRANSACTION_BI_CONTRACT_CONTRACT =
            new ConstructorSignature(
                    NFTTransaction, ClassType.BIG_INTEGER, ClassType.CONTRACT, ClassType.CONTRACT);

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

        BigIntegerValue expectedTokenId = new BigIntegerValue(BigInteger.ONE);

        StorageReference expectedSeller = account(2);

        StorageReference expectedBuyer = account(3);

        StorageReference transaction =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _500_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_NFTTRANSACTION_BI_CONTRACT_CONTRACT,
                        expectedTokenId,
                        expectedSeller,
                        expectedBuyer);

        BigIntegerValue actualTokenId =
                (BigIntegerValue)
                        runInstanceMethodCallTransaction(
                                creator,
                                _500_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTTransaction, "getTokenId", ClassType.BIG_INTEGER),
                                transaction);

        StorageReference actualSeller =
                (StorageReference)
                        runInstanceMethodCallTransaction(
                                creator,
                                _500_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTTransaction, "getSeller", ClassType.CONTRACT),
                                transaction);

        StorageReference actualBuyer =
                (StorageReference)
                        runInstanceMethodCallTransaction(
                                creator,
                                _500_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTTransaction, "getBuyer", ClassType.CONTRACT),
                                transaction);

        BooleanValue sameSeller =
                (BooleanValue)
                        addInstanceMethodCallTransaction(
                                creator_prv_key,
                                creator,
                                _500_000,
                                panarea(1),
                                takamakaCode(),
                                new NonVoidMethodSignature(
                                        ClassType.CONTRACT,
                                        "equals",
                                        BasicTypes.BOOLEAN,
                                        ClassType.OBJECT),
                                expectedSeller,
                                actualSeller);
        BooleanValue sameBuyer =
                (BooleanValue)
                        addInstanceMethodCallTransaction(
                                creator_prv_key,
                                creator,
                                _500_000,
                                panarea(1),
                                takamakaCode(),
                                new NonVoidMethodSignature(
                                        ClassType.CONTRACT,
                                        "equals",
                                        BasicTypes.BOOLEAN,
                                        ClassType.OBJECT),
                                expectedBuyer,
                                actualBuyer);

        assertEquals(expectedTokenId, actualTokenId);
        assertTrue(sameSeller.value);
        assertTrue(sameBuyer.value);
    }

    @Test
    void equals_ValidObject_True()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {
        StringValue hash = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");

        BigIntegerValue expectedTokenId = new BigIntegerValue(BigInteger.ONE);

        StorageReference expectedSeller = account(2);

        StorageReference expectedBuyer = account(3);

        StorageReference firstObject =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _500_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_NFTTRANSACTION_BI_CONTRACT_CONTRACT,
                        expectedTokenId,
                        expectedSeller,
                        expectedBuyer);

        StorageReference secondObject =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _500_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_NFTTRANSACTION_BI_CONTRACT_CONTRACT,
                        expectedTokenId,
                        expectedSeller,
                        expectedBuyer);

        BooleanValue equals =
                (BooleanValue)
                        runInstanceMethodCallTransaction(
                                creator,
                                _500_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTTransaction,
                                        "equals",
                                        BasicTypes.BOOLEAN,
                                        ClassType.OBJECT),
                                firstObject,
                                secondObject);

        assertTrue(equals.value);
    }
}
