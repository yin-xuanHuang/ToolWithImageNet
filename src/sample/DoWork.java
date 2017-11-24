package sample;

import javafx.concurrent.Task;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
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
        this.decompressFiles();
        this.updateMessage("Resources are ready.");
        return null;
    }

    private void decompressFiles() {
        for(int i=1;i<5;i++) {
            try {
                Path inPath = FileSystems.getDefault().getPath(mainDirName,
                        resourceDirName,
                        fileNameList.get(i));

                if(!Files.exists(inPath))
                    continue;

                this.updateMessage(i + " / 4 : " + fileNameList.get(i) + " is decompressing...");

                TarArchiveInputStream zis = new TarArchiveInputStream(new GzipCompressorInputStream(
                        new BufferedInputStream(new FileInputStream(inPath.toString()))));
                TarArchiveEntry ze;

                try {
                    while ((ze = zis.getNextTarEntry()) != null) {
                        if (ze.isDirectory()) {
                            continue;
                        }
                        Path outPath = FileSystems.getDefault().getPath(mainDirName,
                                                                        resourceDirName,
                                                                        ze.getName());
                        OutputStream fos = new BufferedOutputStream(new FileOutputStream(outPath.toString()));
                        try {
                            try {
                                final byte[] buf = new byte[8192];
                                int bytesRead;
                                long nread = 0L;
                                long length = ze.getSize();

                                while (-1 != (bytesRead = zis.read(buf))){
                                    fos.write(buf, 0, bytesRead);
                                    nread += bytesRead;
                                    updateMessage(nread + "/" + length);
                                    updateProgress(nread, length);
                                }
                            } finally {
                                fos.close();
                            }
                        } catch (final IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                } finally {
                    zis.close();
                }

                Files.delete(inPath);

                if(isCancelled())
                    break;

            }  catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadFiles() {
        Path wantToStoreFile;
        int i;
        for(i=0; i<5; i++) {
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
                this.updateMessage((i + 1) + " / 5 : " + fileNameList.get(i) + " is downloading...");

                long count = 0;
                byte[] b = new byte[8192];
                int l ;
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
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
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
