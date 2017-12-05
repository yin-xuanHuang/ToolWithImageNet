package sample;

import javafx.concurrent.Task;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;

public class WnidMatcher extends Task<Void> {

    private ArrayList<String> wnidList;
    private ArrayList<String> urlFilesName;
    private ArrayList<Integer> urlFilesLines;
    private ThisResource resource;
//    private final int chooseUrl0Max = 1000;


    public WnidMatcher(ThisResource resource,
                       ArrayList<String> wnidList) {

        this.resource = resource;
        this.wnidList = wnidList;

        urlFilesName = new ArrayList<>();
        urlFilesName.add("winter11_urls.txt");
        urlFilesName.add("fall11_urls.txt");
        urlFilesName.add("spring10_urls.txt");
        urlFilesName.add("urls.txt");

        urlFilesLines = new ArrayList<>();

        updateMessage("準備中。。。");

    }

    /**
     * Step 1: count every file's line to set progressBar total number in the resource directory.
     *
     * Step 2: create url directory and url files in the project's directory.
     *
     * Step -: reading url files in resource to compare to the project's wnid file.
     *
     * Step -: store both matched or un-matched url line list to project's url files.
     *
     *
     * @return
     * @throws Exception
     */

    @Override
    protected Void call() throws Exception {

        calculateUrlFilesLines();

//        判斷資料是否已經存在，並建立資料夾
        Path urlDirPath = resource.getUrlDirPath();
        if(Files.exists(urlDirPath)){
            updateMessage(urlDirPath.toString() + " already exist!");
            return null;
        } else {
            Files.createDirectory(urlDirPath);
        }

//        暫存處理好的url lines 的容器
        ArrayList<String> matchedUrl;
        ArrayList<String> unmatchedUrl;

        for(int i=0; i<urlFilesName.size(); i++) {

//            建立url庫的路徑

            Path urlFilePath = resource.getResourceDirPath().resolve(urlFilesName.get(i));
            matchedUrl = new ArrayList<>();
            unmatchedUrl = new ArrayList<>();

//            以"列"作為進度條的單位
//            long totalLines = Files.lines(urlFilePath).count();// 執行時間比較長
            long totalLines = urlFilesLines.get(i);
            long lineCount = 0;
            String message = "(" + (i+1) + " / " + urlFilesName.size() + ")Matching " + urlFilesName.get(i) + " : ";
            Random random = new Random();

//            讀取url庫
            try(BufferedReader urlFile = new BufferedReader(
                                         new FileReader(urlFilePath.toString()))) {

                String input;
                int countStoreListSize = 0;
                while((input = urlFile.readLine()) != null){

//                    每列判斷是否吻合wnidList
                    if(isContainsWnidList(input)) {
//                        吻合存入matchedUrl
                        matchedUrl.add(input);
                        countStoreListSize++;
                    } else {
//                        非吻合"有條件"存入unmatchedUrl
                        if(random.nextInt(33) == 11) {
                            unmatchedUrl.add(input);
                            countStoreListSize++;
                        }
                    }

//                    怕拖垮記憶體，一定的大小執行寫入檔案的操作
//                    one input line =~ 170 bytes
//                    500000 lines =~ 100MB
                    if(countStoreListSize >= 500000){
                        writeUrlToFile(matchedUrl, 1);
                        writeUrlToFile(unmatchedUrl, 0);
                        matchedUrl = new ArrayList<>();
                        unmatchedUrl = new ArrayList<>();
                        countStoreListSize = 0;
                    }

                    updateMessage(message + lineCount + "/" + totalLines);
                    updateProgress(lineCount, totalLines);
                    lineCount++;

                    if(isCancelled()){
                        Path url1Path = resource.getUrlSubFilePath(1);
                        Files.deleteIfExists(url1Path);
                        Path url0Path = resource.getUrlSubFilePath(0);
                        Files.deleteIfExists(url0Path);
                        Files.delete(urlDirPath);
                        return null;
                    }
                }
                if(countStoreListSize > 0){
                    writeUrlToFile(matchedUrl, 1);
                    writeUrlToFile(unmatchedUrl, 0);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Writing(append) chosen data to a file
     *
     * @param urlList the data list be chosen to write to a file
     * @param isMatched writing to matching file or writing to un-matching file
     */

    private void writeUrlToFile(ArrayList<String> urlList, int isMatched) {

        Path whichUrlPath = resource.getUrlSubFilePath(isMatched);

        try(BufferedWriter urlFile = new BufferedWriter(
                                     new FileWriter(whichUrlPath.toString(), true))) {
            for(String s: urlList) {
                urlFile.write(s + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.out.println(isMatched + " is done.");//debug

    }

    /**
     * Checking the wnid whether in wnid list or not.
     *
     * @param wnid the wnid which want to be checked.
     * @return whether the string is contained in the wnid list.
     */
    private boolean isContainsWnidList(String wnid) {
        for(String s: wnidList) {
            if(wnid.contains(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Count(use countLines() function) every file's total lines in the resource directory's url files
     */
    private void calculateUrlFilesLines() {
        for(String urlFileName: urlFilesName) {
            Path urlFilePath = resource.getResourceDirPath().resolve(urlFileName);

            try {
                urlFilesLines.add(countLines(urlFilePath.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Counting file's lines by new line character
     *
     * @param filename the file to be counted
     * @return the number of total lines
     * @throws IOException
     */
    private static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
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
