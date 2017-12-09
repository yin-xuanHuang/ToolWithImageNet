package sample;

import javafx.concurrent.Task;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;


public class ImageDownloadForeman extends Task<Void> {

    private final Queue<String> url0Queue;
    private final Queue<String> url1Queue;
    private final ArrayList<Queue<String>> urlQueues;
    private final Queue<String> massageQueue;

    private ThisResource resource;

    private final int MAX_URLS_IN_QUEUE = 100;
    private final int MAX_CURRENT_THREADS = 32;
    private final int CLASSIFICATION = 2;
    private long countRealDownloads = 0;
    private long countLines = 0;
    private long totalLines;
    private int beforeCreateDownloadThreadCount;

    public ImageDownloadForeman(ThisResource resource) {

        this.url0Queue = new ArrayDeque<>();
        this.url1Queue = new ArrayDeque<>();
        this.urlQueues = new ArrayList<>();
        this.urlQueues.add(url0Queue);
        this.urlQueues.add(url1Queue);

        this.massageQueue = new ArrayDeque<>();

        this.resource = resource;

        updateMessage("準備中。。。");

    }

    /**
     * Step 1: count every file's line to set progressBar total number in the project's directory.
     *
     * Step -: read url line from files in the project url directory and push it to the queue.
     *
     * Step -: determining whether or not create downloading thread.
     *
     * Step -: waiting the short-term goal to finished
     *
     * @return
     * @throws Exception
     */

    @Override
    protected Void call() throws Exception {

        this.totalLines = calculateUrlFilesLines();
        Path imageDirPath = resource.getImageDirPath();
        if(!Files.exists(imageDirPath))
            Files.createDirectory(imageDirPath);

        Path imageSubDirPath;

        for(int whichIndex=0; whichIndex<CLASSIFICATION; whichIndex++){

            imageSubDirPath = resource.getImageSubDirPath(whichIndex);
            if(!Files.exists(imageSubDirPath))
                Files.createDirectory(imageSubDirPath);

            this.beforeCreateDownloadThreadCount = Thread.activeCount();

            readAndPush(whichIndex);

            downloadThreadJoin(whichIndex);

            if(isCancelled()){
                break;
            }
        }

        while(!updateUIMassage());

        return null;
    }

    /**
     * Read lines from the project's url file, and push them to queue,
     *
     * and determining whether to create a new downloading thread to help or not.
     *
     * @param whichIndex choose url file to read.
     */
    private void readAndPush(int whichIndex) {

        Path urlFilePath = resource.getUrlSubFilePath(whichIndex);

        try(BufferedReader urlFile = new BufferedReader(
                new FileReader(urlFilePath.toString()))) {

            String input;
            while((input = urlFile.readLine()) != null){

                urlQueues.get(whichIndex).add(input);
                if(!(urlQueues.get(whichIndex).size() < MAX_URLS_IN_QUEUE)) {
//                    建立下載執行緒
                    createImageDownloader(whichIndex);

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }
                }

                updateUIMassage();

                if(isCancelled()){
                    updateMessage("Wait sub-threads to shutdown.");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update progressBar and label messages.
     *
     */
    private boolean updateUIMassage() {

        if(!massageQueue.isEmpty()){
            String massage = massageQueue.poll();
            if(!massage.equals("start downloading")){
                countRealDownloads++;
                updateMessage(massage + " download. ( " + countLines + " / " + totalLines + " )[ " + countRealDownloads + " ]");
            } else {
                updateProgress(this.countLines++, this.totalLines);
            }
        }

        if(this.countLines == this.totalLines){
            return true;
        } else {
            return false;
        }

    }

    /**
     * determining whether or not to create a new downloading thread.
     *
     * @param whichIndex choose sub-directory where the downloading image to located.
     */
    private void createImageDownloader(int whichIndex) {

        if(Thread.activeCount() < MAX_CURRENT_THREADS) {


            new Thread(new ImageDownloader(urlQueues.get(whichIndex),
                    massageQueue,
                    resource.getImageSubDirPath(whichIndex))
            ).start();

        }
    }

    /**
     * Waiting the short-term goal(download images) to be done.
     *
     * @param whichIndex choose sub-directory where the downloading image to located.
     */
    private void downloadThreadJoin(int whichIndex) {

        while((!urlQueues.get(whichIndex).isEmpty()) || (Thread.activeCount() > beforeCreateDownloadThreadCount)){

            if(!urlQueues.get(whichIndex).isEmpty())
                createImageDownloader(whichIndex);

            if(isCancelled()){
                break;
            }
            updateUIMassage();
        }
    }

    /**
     * Count(use countLines() function) every file's total lines in the project's url directory
     *
     * @return the sum of total lines in every url file in project's url directory
     */
    private long calculateUrlFilesLines() {

        long totalLines = 0;

        for(int i=0; i<CLASSIFICATION; i++){
            Path urlFilePath = resource.getUrlSubFilePath(i);

            try {
                totalLines += countLines(urlFilePath.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return totalLines;
    }

    /**
     * Counting file's lines by new line character
     *
     * @param filename the file to be counted
     * @return the number of total lines
     * @throws IOException
     */
    private static long countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            long count = 0;
            int readChars;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }
}
