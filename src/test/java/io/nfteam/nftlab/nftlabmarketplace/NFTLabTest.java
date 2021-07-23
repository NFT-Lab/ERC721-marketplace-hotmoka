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
import io.hotmoka.beans.values.BooleanValue;
import io.hotmoka.beans.values.StorageReference;
import io.hotmoka.beans.values.StringValue;
import io.hotmoka.views.GasHelper;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NFTLabTest extends TakamakaTest {
    private static final ClassType NFTLab =
            new ClassType("io.nfteam.nftlab.nftlabmarketplace.NFTLab");
    private static final ConstructorSignature CONSTRUCTOR_NFTLAB_STR_STR_OBJ_OBJ_OBJ =
            new ConstructorSignature(
                    NFTLab,
                    ClassType.STRING,
                    ClassType.STRING,
                    BasicTypes.BOOLEAN,
                    BasicTypes.BOOLEAN,
                    BasicTypes.BOOLEAN);

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
        StringValue expectedCid = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue expectedMetadataCid =
                new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        BooleanValue isImage = new BooleanValue(true);
        BooleanValue isMusic = new BooleanValue(false);
        BooleanValue isVideo = new BooleanValue(false);

        StorageReference nftlab =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _500_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_NFTLAB_STR_STR_OBJ_OBJ_OBJ,
                        expectedCid,
                        expectedMetadataCid,
                        isImage,
                        isMusic,
                        isVideo);

        StringValue actualCid =
                (StringValue)
                        runInstanceMethodCallTransaction(
                                creator,
                                _500_000,
                                classpath,
                                new NonVoidMethodSignature(NFTLab, "getCid", ClassType.STRING),
                                nftlab);

        StringValue actualMetadataCid =
                (StringValue)
                        runInstanceMethodCallTransaction(
                                creator,
                                _500_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLab, "getMetadataCid", ClassType.STRING),
                                nftlab);

        BooleanValue actuallyIsImage =
                (BooleanValue)
                        runInstanceMethodCallTransaction(
                                creator,
                                _500_000,
                                classpath,
                                new NonVoidMethodSignature(NFTLab, "isImage", BasicTypes.BOOLEAN),
                                nftlab);

        BooleanValue actuallyIsMusic =
                (BooleanValue)
                        runInstanceMethodCallTransaction(
                                creator,
                                _500_000,
                                classpath,
                                new NonVoidMethodSignature(NFTLab, "isMusic", BasicTypes.BOOLEAN),
                                nftlab);

        BooleanValue actuallyIsVideo =
                (BooleanValue)
                        runInstanceMethodCallTransaction(
                                creator,
                                _500_000,
                                classpath,
                                new NonVoidMethodSignature(NFTLab, "isVideo", BasicTypes.BOOLEAN),
                                nftlab);

        assertEquals(expectedCid, actualCid);
        assertEquals(expectedMetadataCid, actualMetadataCid);
        assertEquals(isImage, actuallyIsImage);
        assertEquals(isMusic, actuallyIsMusic);
        assertEquals(isVideo, actuallyIsVideo);
    }

    @Test
    void equals_ValidObject_True()
            throws TransactionException, TransactionRejectedException, CodeExecutionException,
                    SignatureException, InvalidKeyException {
        StringValue expectedCid = new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        StringValue expectedMetadataCid =
                new StringValue("QmeK3GCfbMzRp3FW3tWZCg5WVZKM52XZrk6WCTLXWwALbq");
        BooleanValue isImage = new BooleanValue(true);
        BooleanValue isMusic = new BooleanValue(false);
        BooleanValue isVideo = new BooleanValue(false);

        StorageReference firstObject =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _500_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_NFTLAB_STR_STR_OBJ_OBJ_OBJ,
                        expectedCid,
                        expectedMetadataCid,
                        isImage,
                        isMusic,
                        isVideo);

        StorageReference secondObject =
                addConstructorCallTransaction(
                        creator_prv_key,
                        creator,
                        _500_000,
                        panarea(1),
                        classpath,
                        CONSTRUCTOR_NFTLAB_STR_STR_OBJ_OBJ_OBJ,
                        expectedCid,
                        expectedMetadataCid,
                        isImage,
                        isMusic,
                        isVideo);

        BooleanValue equals =
                (BooleanValue)
                        runInstanceMethodCallTransaction(
                                creator,
                                _500_000,
                                classpath,
                                new NonVoidMethodSignature(
                                        NFTLab, "equals", BasicTypes.BOOLEAN, ClassType.OBJECT),
                                firstObject,
                                secondObject);

        assertTrue(equals.value);
    }
}
