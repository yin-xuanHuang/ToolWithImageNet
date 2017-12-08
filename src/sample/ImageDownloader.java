package sample;

import javafx.concurrent.Task;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;

public class ImageDownloader extends Task<Void> {

    private final Queue<String> urlQueue;
    private final Queue<String> massageQueue;

    private final Path imageSubDirPath;

    private File file;
    private URL url;
    private HttpURLConnection conn;
    private BufferedInputStream is;
    private FileOutputStream fos;

    public ImageDownloader(Queue<String> urlQueue,
                            Queue<String> massageQueue,
                            Path imageSubDirPath) {

        this.urlQueue = urlQueue;
        this.massageQueue = massageQueue;
        this.imageSubDirPath = imageSubDirPath;
    }

    @Override
    protected Void call() throws Exception {

        pollAndDownload();

        return null;
    }

    /**
     * Poll data from the queue, and parsing it, and deal with it by downloadFiles() function.
     *
     */
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

    /**
     * Connecting to image, and download it.
     *
     * @param nameAndUrl data of the file name and the url of the image
     */

    private void downloadFiles(String[] nameAndUrl) {

        try {
            this.massageQueue.add("start downloading");

            Path wantToStoreFilePath = this.imageSubDirPath.resolve(nameAndUrl[0]);

//            之前已經下載過得檔案
            if(Files.exists(wantToStoreFilePath))
                return;

            file = new File(wantToStoreFilePath.toString());
            url = new URL(nameAndUrl[1]);

            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(7000);
            conn.setReadTimeout(7000);

            // checking whether or not it is a image file type
            String type = conn.getContentType().split("/")[0];
            if(!type.equals("image")){
                conn.disconnect();
                return;
            }

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
            // downloading success
            this.massageQueue.add(nameAndUrl[0]);
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
