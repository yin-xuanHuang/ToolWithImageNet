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
        updateMessage("準備中。。。");

    }

    @Override
    protected Void call() throws Exception {

        totalFiles = getTotalFiles();
        if(totalFiles == -1){
            updateMessage("Can't find all image sub directories.");
            System.out.println("Can't find all image sub directories.");
            return null;
        }
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
    private long getTotalFiles(){

        long totalFiles = 0;
        String[] strings;

        Path imageSubDirPath = resource.getImageSubDirPath(0);
        if(!Files.exists(imageSubDirPath))
            return -1;
        try {
            strings = imageSubDirPath.toFile().list();
        } catch (NullPointerException e){
            e.printStackTrace();
            return -1;
        }
        totalFiles += strings.length;

        imageSubDirPath = resource.getImageSubDirPath(1);
        if(!Files.exists(imageSubDirPath))
            return -1;
        try {
            strings = imageSubDirPath.toFile().list();
        } catch (NullPointerException e){
            e.printStackTrace();
            return -1;
        }
        totalFiles += strings.length;

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
