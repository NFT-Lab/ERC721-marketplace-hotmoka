package io.nfteam.nftlab.nftlabmarketplace;

import io.takamaka.code.lang.Contract;
import io.takamaka.code.lang.FromContract;
import io.takamaka.code.math.UnsignedBigInteger;
import io.takamaka.code.tokens.ERC20;
import java.math.BigInteger;

public class FakeToken extends ERC20 {

    public FakeToken() {
        super("FakeToken", "FKTK");
    }

    @FromContract
    public void juice() {
        _mint(caller(), new UnsignedBigInteger(100000));
    }

    @FromContract
    public boolean approve(Contract spender, BigInteger amount) {
        this._approve(caller(), spender, new UnsignedBigInteger(amount));
        return true;
    }
}
