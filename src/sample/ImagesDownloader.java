package sample;

import javafx.concurrent.Task;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;

public class ImagesDownloader extends Task<Void> {

    private final Queue<String> urlQueue;
    private final Queue<String> massageQueue;
    private final String mainDirName;
    private final String projectDirName;
    private final String imageDirName;
    private final String imageSubDirName;

    private File file;
    private URL url;
    private HttpURLConnection conn;
    private BufferedInputStream is;
    private FileOutputStream fos;

    public ImagesDownloader(Queue<String> urlQueue,
                            Queue<String> massageQueue,
                            String mainDirName,
                            String projectDirName,
                            String imageDirName,
                            String imageSubDirName) {

        this.urlQueue = urlQueue;
        this.massageQueue = massageQueue;

        this.mainDirName = mainDirName;
        this.projectDirName = projectDirName;
        this.imageDirName = imageDirName;
        this.imageSubDirName = imageSubDirName;
    }

    @Override
    protected Void call() throws Exception {

        pollAndDownload();

        return null;
    }

    private void pollAndDownload() {
        String line;
        String[] nameAndUrl;

        while (true) {
            synchronized (urlQueue) {
                if (!urlQueue.isEmpty()) {
                    line = urlQueue.poll();
                } else {
                    System.out.println("imageDownloader gone.");
                    break;
                }
            }
//            parse line
            nameAndUrl = line.split("\t");

            downloadFiles(nameAndUrl);
        }
    }

    private void downloadFiles(String[] nameAndUrl) {

        try {
            Path wantToStoreFilePath = FileSystems.getDefault().getPath(mainDirName,
                                                                        projectDirName,
                                                                        imageDirName,
                                                                        imageSubDirName,
                                                                        nameAndUrl[0]);

            file = new File(wantToStoreFilePath.toString());
            url = new URL(nameAndUrl[1]);
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            is = new BufferedInputStream(conn.getInputStream());
            fos = new FileOutputStream(file);

            byte[] b = new byte[8192];
            int l;
            while ((l = is.read(b)) != -1) {
                fos.write(b, 0, l);
                if (isCancelled()) {
                    Files.delete(wantToStoreFilePath);
                    return;
                }
            }

            fos.close();
            is.close();
            conn.disconnect();
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
            this.massageQueue.add("9527");
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            this.massageQueue.add("9527");
        }
        this.massageQueue.add(nameAndUrl[0]);
    }
}
