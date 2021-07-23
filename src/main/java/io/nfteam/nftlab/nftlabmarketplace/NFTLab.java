package io.nfteam.nftlab.nftlabmarketplace;

import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.View;

public final class NFTLab extends Storage {
    private final String cid;
    private final String metadataCid;
    private final boolean isImage;
    private final boolean isMusic;
    private final boolean isVideo;

    public NFTLab(
            String cid, String metadataCid, boolean isImage, boolean isMusic, boolean isVideo) {
        this.cid = cid;
        this.metadataCid = metadataCid;
        this.isImage = isImage;
        this.isMusic = isMusic;
        this.isVideo = isVideo;
    }

    @View
    public String getCid() {
        return cid;
    }

    @View
    public String getMetadataCid() {
        return metadataCid;
    }

    @View
    public boolean isImage() {
        return isImage;
    }

    @View
    public boolean isMusic() {
        return isMusic;
    }

    @View
    public boolean isVideo() {
        return isVideo;
    }

    @Override
    @View
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NFTLab nftLab = (NFTLab) o;
        return cid.equals(nftLab.cid)
                && metadataCid.equals(nftLab.metadataCid)
                && isImage == nftLab.isImage
                && isMusic == (nftLab.isMusic)
                && isVideo == (nftLab.isVideo);
    }
}
