package sample;

import javafx.concurrent.Task;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;


public class ImageForeman extends Task<Void> {

    private final Queue<String> urlQueue;
    private final Queue<String> massageQueue;
    private final String mainDirName;
    private final String projectDirName;

    private final String imageDirName;
    private final ArrayList<String> imageSubDirsName;

    private final String urlDirName;
    private final ArrayList<String> urlFilesName;

    private final int MAX_URLS_IN_QUEUE = 100;
    private final int MAX_CURRENT_THREADS = 32;
    private long countRealDownloads = 0;
    private long countLines = 0;
    private long totalLines;
    private int beforeCreateDownloadThreadCount;

    public ImageForeman(String mainDirName,
                        String projectDirName,
                        String imageDirName,
                        String imageMatchedDirName,
                        String imageUnMatchedDirName,
                        String urlDirName,
                        String urlMatchedFileName,
                        String urlUnmatchedFileName) {

        this.urlQueue = new ArrayDeque<>();
        this.massageQueue = new ArrayDeque<>();

        this.mainDirName = mainDirName;
        this.projectDirName = projectDirName;

        this.imageDirName = imageDirName;
        this.imageSubDirsName = new ArrayList<>();
        imageSubDirsName.add(imageMatchedDirName);
        imageSubDirsName.add(imageUnMatchedDirName);

        this.urlDirName = urlDirName;
        this.urlFilesName = new ArrayList<>();
        urlFilesName.add(urlMatchedFileName);
        urlFilesName.add(urlUnmatchedFileName);

        updateMessage("準備中。。。");

    }

    @Override
    protected Void call() throws Exception {

        this.totalLines = calculateUrlFilesLines();
        Path imageDirPath = FileSystems.getDefault().getPath(mainDirName,
                                                            projectDirName,
                                                            imageDirName);
        if(!Files.exists(imageDirPath))
            Files.createDirectory(imageDirPath);

        Path imageSubDirPath;

        for(int whichIndex=0; whichIndex<urlFilesName.size(); whichIndex++){

            imageSubDirPath = FileSystems.getDefault().getPath(mainDirName,
                                                                projectDirName,
                                                                imageDirName,
                                                                imageSubDirsName.get(whichIndex));
            if(!Files.exists(imageSubDirPath))
                Files.createDirectory(imageSubDirPath);

            this.beforeCreateDownloadThreadCount = Thread.activeCount();

            readAndPush(whichIndex);

            downloadThreadJoin(whichIndex);

            if(isCancelled()){
                Path projectImageDirPath = FileSystems.getDefault().getPath(mainDirName,
                                                                            projectDirName,
                                                                            imageDirName);
                deleteDirectory(projectImageDirPath.toFile());
                break;
            }
        }

        return null;
    }

    private void readAndPush(int whichIndex) {

        Path urlFilePath = FileSystems.getDefault().getPath(mainDirName,
                                                            projectDirName,
                                                            urlDirName,
                                                            urlFilesName.get(whichIndex));

        try(BufferedReader urlFile = new BufferedReader(
                                     new FileReader(urlFilePath.toString()))) {

            String input;
            while((input = urlFile.readLine()) != null){

                if(urlQueue.size() < MAX_URLS_IN_QUEUE) {
                    urlQueue.add(input);

                    updateUIMassage(whichIndex);

                } else {
//                    建立下載執行緒
                    createImageDownloader(whichIndex);

                    try {
                        //TODO
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }
                }
                if(isCancelled()){
                    updateMessage("Wait sub-threads to shutdown.");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateUIMassage(int whichIndex) {
//        synchronized?
        if(!massageQueue.isEmpty()){
            String massage = massageQueue.poll();
            updateProgress(this.countLines++, this.totalLines);
            if(!massage.equals("exception")){
                countRealDownloads++;
                updateMessage(massage + " download.( " + countLines + " / " + totalLines + " )[ " + countRealDownloads + " ]");
            }
        }

    }

    private void createImageDownloader(int whichIndex) {

        if(Thread.activeCount() < MAX_CURRENT_THREADS) {


            new Thread(new ImagesDownloader(urlQueue,
                                            massageQueue,
                                            mainDirName,
                                            projectDirName,
                                            imageDirName,
                                            imageSubDirsName.get(whichIndex))
            ).start();

        }
    }

    private void downloadThreadJoin(int whichIndex) {

        while(Thread.activeCount() > beforeCreateDownloadThreadCount){
            try{
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(isCancelled()){
                break;
            }
            updateUIMassage(whichIndex);
        }
    }

    private long calculateUrlFilesLines() {

        long totalLines = 0;

        for(String urlFileName: urlFilesName){
            Path urlFilePath = FileSystems.getDefault().getPath(mainDirName,
                    projectDirName,
                    urlDirName,
                    urlFileName);

            try {
                totalLines += countLines(urlFilePath.toString());
                System.out.println(urlFileName + " has " + countLines(urlFilePath.toString()) + " lines.");//debug
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return totalLines;
    }

    //    計算檔案內容有集個換行字元
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

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
