package org.kaneeldias.utils;

public class PathUtils {

    public static String getOutputPath(String outputDirectory, String jobOutputPath) {
        return outputDirectory + "/" + jobOutputPath;
    }
}