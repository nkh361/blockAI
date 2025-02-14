package edu.depaul.core;

import java.io.File;

public class FileSystemHelper {
    public boolean createDirectory(String path) {
        File dir = new File(path);
        return dir.exists() || dir.mkdirs();
    }
}
