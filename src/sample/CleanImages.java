package sample;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class CleanImages extends Task<Void> {
//    TODO 效能問題
//    算出共有幾張圖，以此為progressBar total。

    private Queue<String> shareQueue;
    private final String mainDirName;
    private final String projectDirName;

    private final String imageDirName;
    private final ArrayList<String> imageSubDirsName;

    private long countFiles = 0;
    private long totalFiles;
    private long countDeleteFiles = 0;

    public CleanImages(String mainDirName,
                       String projectDirName,
                       String imageDirName,
                       String imageMatchedDirName,
                       String imageUnmatchedDirName) {

        this.mainDirName = mainDirName;
        this.projectDirName = projectDirName;
        this.imageDirName = imageDirName;

        imageSubDirsName = new ArrayList<>();
        imageSubDirsName.add(imageMatchedDirName);
        imageSubDirsName.add(imageUnmatchedDirName);

        shareQueue = new ArrayDeque<>();
    }

    @Override
    protected Void call() throws Exception {

        totalFiles = getTotalFiles();

        Path imagePath = FileSystems.getDefault().getPath(mainDirName,
                                                        projectDirName,
                                                        imageDirName);


        new Thread(() -> {
            try {
//                這個如何stop?
                Files.walkFileTree(imagePath, new WalkingFileTree(shareQueue));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        while(true){
            Thread.sleep(500);
            if(updateUIMassage())
                break;
            if(isCancelled()){
                break;
            }
        }
        return null;
    }

    /**
     * Count the project's images amount in its image directory
     *
     * @return total files in the project's image directory
     */
    private long getTotalFiles() {

        long totalFiles = 0;

        Path imageDirPath = FileSystems.getDefault().getPath(mainDirName, projectDirName, imageDirName);

        for(File imageSubDir: imageDirPath.toFile().listFiles()) {
            totalFiles += imageSubDir.list().length;
        }

        return totalFiles;
    }

    /**
     * Update progressBar and label messages.
     *
     * @return whether walkingFileTree is done or not.
     */
    private boolean updateUIMassage() {

        if(!shareQueue.isEmpty()){
            String massage = shareQueue.poll();
            if(massage.equals("delete")){
                countDeleteFiles++;
            } else {
                updateProgress(this.countFiles++, this.totalFiles);
            }
            updateMessage( " " + countFiles + " / " + totalFiles + " [ " + countDeleteFiles + " ]");
        }

        if(this.countFiles == this.totalFiles)
            return true;
        else
            return false;

    }
}
