package sample;

import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;

public class CleanImages extends Task<Void> {

    private Queue<String> shareQueue;
    private ThisResource resource;

    private long countFiles = 0;
    private long totalFiles;
    private long countDeleteFiles = 0;

    public CleanImages(ThisResource resource) {

        this.resource = resource;


        shareQueue = new ArrayDeque<>();

    }

    @Override
    protected Void call() throws Exception {

        totalFiles = getTotalFiles();

        Path imagePath = resource.getImageDirPath();

//        create cleanImageDir
        Path cleanDirPath = resource.getCleanDirPath();
        if(!Files.exists(cleanDirPath))
            Files.createDirectory(cleanDirPath);


        new Thread(() -> {
            try {
//                這個如何stop?
                Files.walkFileTree(imagePath, new WalkingFileTree(shareQueue, resource));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        while(true){
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


        Path imageSubDirPath = resource.getImageSubDirPath(0);
        totalFiles += imageSubDirPath.toFile().list().length;

        imageSubDirPath = resource.getImageSubDirPath(1);
        totalFiles += imageSubDirPath.toFile().list().length;

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
