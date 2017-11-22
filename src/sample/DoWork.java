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
import java.util.ArrayList;

public class DoWork extends Task<Void> {

    private final ArrayList<String> urllist = new ArrayList<>();
    private final ArrayList<String> fileNameList = new ArrayList<>();

    private final String mainDirName = "machineLearningWithImageNetDir";
    private final String resourceDirName = "resourceDir";

    private File file;
    private URL url;
    private HttpURLConnection conn;

    private long total;

    private BufferedInputStream is;
    private FileOutputStream fos;

    public DoWork() {
        updateMessage("Download thread is creating ...");

        urllist.add("http://image-net.org/archive/words.txt");
        urllist.add("http://image-net.org/imagenet_data/urls/imagenet_fall11_urls.tgz");
        urllist.add("http://image-net.org/imagenet_data/urls/imagenet_winter11_urls.tgz");
        urllist.add("http://image-net.org/imagenet_data/urls/imagenet_spring10_urls.tgz");
        urllist.add("http://image-net.org/imagenet_data/urls/imagenet_fall09_urls.tgz");

        fileNameList.add("words.txt");
        fileNameList.add("imagenet_fall11_urls.tgz");
        fileNameList.add("imagenet_winter11_urls.tgz");
        fileNameList.add("imagenet_spring10_urls.tgz");
        fileNameList.add("imagenet_fall09_urls.tgz");
    }

    @Override
    protected Void call() throws Exception {
        this.downloadFiles();
        return null;
    }

    private void decompressFiles() {

    }

    private void downloadFiles() {
        Path wantToStoreFile = null;
        int i;
        for(i=0; i<4; i++) {
            try {
                wantToStoreFile = FileSystems.getDefault().getPath(mainDirName,
                        resourceDirName,
                        fileNameList.get(i));
                if(Files.exists(wantToStoreFile))
                    continue;

                file = new File(wantToStoreFile.toString());
                url = new URL(urllist.get(i));
                conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                total = conn.getContentLength();

                is = new BufferedInputStream(conn.getInputStream());
                fos = new FileOutputStream(file);

                this.updateProgress(0, total);
                this.updateMessage((i + 1) + " / 4 : " + fileNameList.get(i) + " is downloading...");

                long count = 0;
                byte[] b = new byte[8192];
                int l = 0;
                while ((l = is.read(b)) != -1) {
                    this.updateProgress(count += l, total);
                    fos.write(b, 0, l);
                    if(isCancelled()) {
                        Files.delete(wantToStoreFile);
                        return;
                    }
                }

                fos.close();
                is.close();
                conn.disconnect();
            } catch (MalformedURLException e) {
                System.out.println("Malformed URL: " + e.getMessage());
                System.out.println(i);
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
                System.out.println(i);
            }
        }
        this.updateMessage("Download finished.");
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        updateMessage("Cancelled!");
        return super.cancel(mayInterruptIfRunning);
    }
}
