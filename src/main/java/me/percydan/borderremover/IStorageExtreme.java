package me.percydan.borderremover;

public interface IStorageExtreme {
    int getStoredLevelExtreme(long sectionPos, int offset);
    void setStoredLevelExtreme(long sectionPos, int offset, int lightLevel);
}
