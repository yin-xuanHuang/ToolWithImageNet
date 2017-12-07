package sample;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Queue;


public class WalkingFileTree extends SimpleFileVisitor<Path> {

    private Queue<String> shareQueue;

    private ThisResource resource;

    private ArrayList<String> wrongImageSignatures;

    public WalkingFileTree(Queue<String> shareQueue, ThisResource resource) {

        this.shareQueue = shareQueue;
        this.resource = resource;

        wrongImageSignatures = new ArrayList<>();

        try(BufferedReader file = new BufferedReader(new FileReader(resource.getWrongImageSignaturePath().toString()))) {
            String input;
            while((input = file.readLine()) != null) {
                wrongImageSignatures.add(input);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

//        非jpg檔轉檔成jpg並加附檔名，jpg檔則增加副檔名
//        原檔刪除
        Path cleanDirPath = resource.getCleanDirPath();
        Path out = cleanDirPath.resolve(file.getParent().getFileName().toString() +
                "_" + file.getFileName().toString() + ".jpg");

        String type = file.toFile().toURI().toURL().openConnection().getContentType();
        if(!type.split("/")[1].equals("jpeg")){
            if(!image2jpeg(file, out)) {
                Files.deleteIfExists(out);
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }
        } else {
            Files.copy(file, out, StandardCopyOption.REPLACE_EXISTING);
        }
//        Files.delete(file);


        Boolean isWrongImage = false;
        if(wrongImageSignatures.size() > 0) {
            isWrongImage = isMissingImage(out);
        }else {
            System.out.println("No wrong image signature in " + resource.getWrongImageSignaturePath().toString() + " file!");
        }

        if(isWrongImage) {
            Files.delete(out);
            System.out.println("Delete a wrong image.");//debug
            shareQueue.add("delete");
        }

        shareQueue.add("total");

        return FileVisitResult.CONTINUE;
    }

    //    debug
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
    private boolean image2jpeg(Path in, Path out) {
        try (InputStream is = new FileInputStream(in.toFile())) {
            ImageInputStream iis = ImageIO.createImageInputStream(is);
            BufferedImage image = ImageIO.read(iis);
            try (OutputStream os = new FileOutputStream(out.toFile())) {
                ImageOutputStream ios = ImageIO.createImageOutputStream(os);
                ImageIO.write(image, "jpg", ios);
            } catch (Exception exp) {
                exp.printStackTrace();
                return false;
            }
        } catch (Exception exp) {
            exp.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Looping all wrong image's signatures to compare the beCheckedPath image's signature.
     *
     * @param beCheckedPath be checked image path.
     * @return whether the image is matching with wrong images or not.
     */
    private boolean isMissingImage(Path beCheckedPath) {

        //in linux ubuntu
        String command = "identify -format \"%#\" " + beCheckedPath.toString();

        String signature = executeCommand(command);

        for(String wrongImageSignature: wrongImageSignatures) {
            if(wrongImageSignature.equals(signature)){
                return true;
            }
        }
        return false;
    }

    /**
     * Execute command line, and return result message
     *
     * @param command command line want to be executed
     * @return result message
     */
    private String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();
    }
}
