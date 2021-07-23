module erc721Marketplace {
    exports io.nfteam.nftlab.nftlabmarketplace;

    requires io.takamaka.code;
    requires io.nfteam.nftlab.hotmoka.erc721;
    requires io.hotmoka.beans;
    requires io.hotmoka.nodes;
    requires io.hotmoka.views;
    requires io.hotmoka.local;
    requires io.hotmoka.crypto;
    requires io.hotmoka.memory;
    requires io.hotmoka.constants;
    requires io.hotmoka.instrumentation;
    requires io.hotmoka.verification;
    requires org.junit.jupiter.api;
    requires org.slf4j;

    opens io.nfteam.nftlab.nftlabmarketplace;
}
