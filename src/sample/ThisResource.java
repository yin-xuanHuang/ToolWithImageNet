package sample;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class ThisResource {
    private static final String mainDirName = "machineLearningWithImageNetDir";
    private final String wrongImageSignatureFileName = "wrongImageSignature.txt";

    private static final String resourceDirName = "resourceDir";
    private static final String wordsTextName = "words.txt";


    private String projectDirName;
    private static final String projectWnidTextName = "wnid.txt";

    private static final String urlDirName = "urlCollection";
    private static final String urlMatchFileName = "url1.txt";
    private static final String urlUnmatchFileName = "url0.txt";

    private static final String imageDirName = "imageCollection";
    private static final String imageMatchedDirName = "image1";
    private static final String imageUnmatchedDirName = "image0";

    private static final String cleanDirName = "cleanData";

    private static final String hdf5DirName = "hdf5";

    private static final String parameterDirName = "parameter";

    public ThisResource() {
        this.projectDirName = "";
    }

    public void setProjectDirName(String projectDirName) {
        this.projectDirName = projectDirName;
    }

    public String getProjectDirName() {
        return projectDirName;
    }

    public Path getMainDirPath() {
        return FileSystems.getDefault().getPath(mainDirName);
    }

    public Path getResourceDirPath() {
        return FileSystems.getDefault().getPath(mainDirName, resourceDirName);
    }

    public Path getResourceWordsTextPath() {
        return FileSystems.getDefault().getPath(mainDirName, resourceDirName, wordsTextName);
    }

    public Path resolveResourcePath(String filename) {
        return FileSystems.getDefault().getPath(mainDirName, resourceDirName, filename);
    }

    public Path resolveProjectPath(String filename) {
        this.setProjectDirName(filename);
        return FileSystems.getDefault().getPath(mainDirName, filename);
    }

    public Path getProjectWnidText() {
        return FileSystems.getDefault().getPath(mainDirName,
                                                projectDirName,
                                                projectWnidTextName);
    }

    public Path getUrlDirPath() {
        return FileSystems.getDefault().getPath(mainDirName,
                projectDirName,
                urlDirName);
    }

    public Path getImageDirPath() {
        return FileSystems.getDefault().getPath(mainDirName,
                projectDirName,
                imageDirName);
    }

    public Path getCleanDirPath() {
        return FileSystems.getDefault().getPath(mainDirName,
                projectDirName,
                cleanDirName);
    }

    public Path getHdf5DirPath() {
        return FileSystems.getDefault().getPath(mainDirName,
                projectDirName,
                hdf5DirName);
    }

    public Path getParameterDirPath() {
        return FileSystems.getDefault().getPath(mainDirName,
                projectDirName,
                parameterDirName);
    }

    public Path getImageSubDirPath(int whichSubDir){
        if(whichSubDir == 1){
            return FileSystems.getDefault().getPath(mainDirName,
                    projectDirName,
                    imageDirName,
                    imageMatchedDirName);
        } else {
            return FileSystems.getDefault().getPath(mainDirName,
                    projectDirName,
                    imageDirName,
                    imageUnmatchedDirName);
        }
    }

    public Path getUrlSubFilePath(int whichSubDir){
        if(whichSubDir == 1){
            return FileSystems.getDefault().getPath(mainDirName,
                    projectDirName,
                    urlDirName,
                    urlMatchFileName);
        } else {
            return FileSystems.getDefault().getPath(mainDirName,
                    projectDirName,
                    urlDirName,
                    urlUnmatchFileName);
        }
    }

    public Path getWrongImageSignaturePath() {
        return FileSystems.getDefault().getPath(wrongImageSignatureFileName);
    }
}
