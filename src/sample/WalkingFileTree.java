package sample;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Queue;


public class WalkingFileTree extends SimpleFileVisitor<Path> {

//    轉檔成一致的圖檔(ex. jpg)
//    identify(remove missing image)

    private Queue<String> shareQueue;
    private final String wrongImageDirName = "wrong_images";
    private Path wrongImageDirPath;
    private File[] wrongImageList;

    public WalkingFileTree(Queue<String> shareQueue) {
        wrongImageDirPath = FileSystems.getDefault().getPath(wrongImageDirName);
        wrongImageList = wrongImageDirPath.toFile().listFiles();
        this.shareQueue = shareQueue;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

//        非jpg檔轉檔成jpg並加附檔名，jpg檔則增加副檔名
//        原檔刪除
        Path out = FileSystems.getDefault().getPath(file.getParent().toString(),
                                            file.getFileName() + ".jpg");

        String type = file.toFile().toURI().toURL().openConnection().getContentType();
        if(!type.split("/")[1].equals("jpeg")){
            image2jpeg(file, out);
        } else {
            Files.copy(file, out, StandardCopyOption.REPLACE_EXISTING);
        }
        Files.delete(file);


//        check isMissingImage()
//        list wrong image directory's file and compare to out jpg file.

        Boolean isWrongImage = false;

        if(wrongImageList != null) {
            isWrongImage = isMissingImage(out);
        } else {
            System.out.println("No wrong image in " + wrongImageDirName + " directory!");
        }

        if(isWrongImage) {
            Files.delete(file);
            System.out.println("Delete a wrong image.");//debug
            shareQueue.add("delete");
        }

        shareQueue.add("total");

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        System.out.println(dir.toAbsolutePath());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.out.println("Error accessing file: " + file.toAbsolutePath() + " " + exc.getMessage());
        System.out.println("===================================");
        return FileVisitResult.CONTINUE;
    }

    /**
     * Convert every image types to jpg type.
     *
     * @param in the origin image path.
     * @param out want to store the converted image path.
     */
    private void image2jpeg(Path in, Path out) {
        try (InputStream is = new FileInputStream(in.toFile())) {
            ImageInputStream iis = ImageIO.createImageInputStream(is);
            BufferedImage image = ImageIO.read(iis);
            try (OutputStream os = new FileOutputStream(out.toFile())) {
                ImageOutputStream ios = ImageIO.createImageOutputStream(os);
                ImageIO.write(image, "jpg", ios);
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    /**
     * Compares two images pixel by pixel.
     *
     * @param imgA the first image.
     * @param imgB the second image.
     * @return whether the images are both the same or not.
     */
    private boolean compareImages(BufferedImage imgA, BufferedImage imgB) {
        // The images must be the same size.
        if (imgA.getWidth() == imgB.getWidth() && imgA.getHeight() == imgB.getHeight()) {
            int width = imgA.getWidth();
            int height = imgA.getHeight();

            // Loop over every pixel.
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Compare the pixels for equality.
                    if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }

        return true;
    }

    /**
     * Looping all wrong images to compareImages() the beCheckedPath image.
     *
     * TODO 效能有點低
     *
     * @param beCheckedPath be checked image path.
     * @return whether the image is matching with wrong images or not.
     */
    private boolean isMissingImage(Path beCheckedPath) {

        try (InputStream beCheckedIS = new FileInputStream(beCheckedPath.toFile())) {
            ImageInputStream beCheckedIIS = ImageIO.createImageInputStream(beCheckedIS);
            BufferedImage beCheckedBI = ImageIO.read(beCheckedIIS);

            for(File wrongImage: wrongImageList) {
                try (InputStream wrongImageIS = new FileInputStream(wrongImage)) {
                    ImageInputStream wrongImageIIS  = ImageIO.createImageInputStream(wrongImageIS);
                    BufferedImage wrongImageBI = ImageIO.read(wrongImageIIS);

                    if(compareImages(beCheckedBI, wrongImageBI)) {
                        return true;
                    }

                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        } catch (Exception exp) {
            exp.printStackTrace();
            try {
                Files.delete(beCheckedPath);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return false;
    }
}
