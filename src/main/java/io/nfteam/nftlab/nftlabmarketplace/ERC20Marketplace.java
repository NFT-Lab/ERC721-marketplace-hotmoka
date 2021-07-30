package io.nfteam.nftlab.nftlabmarketplace;

import io.takamaka.code.lang.*;
import io.takamaka.code.math.UnsignedBigInteger;
import io.takamaka.code.tokens.IERC20;
import java.math.BigInteger;

public class ERC20Marketplace extends Marketplace {

    private final IERC20 tokenHandler;

    @FromContract
    public ERC20Marketplace(IERC20 tokenHandler, String _name, String _symbol) {
        super(_name, _symbol);
        this.tokenHandler = tokenHandler;
    }

    protected boolean _pay(Contract from, PayableContract to, BigInteger amount) {
        return tokenHandler.transferFrom(from, to, new UnsignedBigInteger(amount));
    }

    protected void _checkPayment(Contract from, BigInteger amount, BigInteger price) {
        Takamaka.require(
                tokenHandler.allowance(from, this).compareTo(new UnsignedBigInteger(price)) >= 0,
                "You should allow at least the price of the token to get it, current allowance "
                        + tokenHandler.allowance(from, this));
    }
}
