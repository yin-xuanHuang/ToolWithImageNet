package sample;

import javafx.concurrent.Task;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;

public class UrlMatcher extends Task<Void> {

    private ArrayList<String> wnidList;
    private ArrayList<String> urlFilesName;
    private ArrayList<Integer> urlFilesLines;
    private final String mainDirName;
    private final String resourceDirName;
    private final String projectDirName;
    private final String urlDirName;
    private final String urlMatchFileName;
    private final String urlUnmatchFileName;
//    private final int chooseUrl0Max = 1000;


    public UrlMatcher(String mainDirName,
                      String resourceDirName,
                      String projectDirName,
                      String urlDirName,
                      String urlMatchFileName,
                      String urlUnmatchFileName,
                      ArrayList<String> wnidList) {

        this.mainDirName = mainDirName;
        this.resourceDirName = resourceDirName;
        this.projectDirName = projectDirName;
        this.urlDirName = urlDirName;
        this.urlMatchFileName = urlMatchFileName;
        this.urlUnmatchFileName = urlUnmatchFileName;
        this.wnidList = wnidList;

        urlFilesName = new ArrayList<>();
        urlFilesName.add("winter11_urls.txt");
        urlFilesName.add("fall11_urls.txt");
        urlFilesName.add("spring10_urls.txt");
        urlFilesName.add("urls.txt");

        urlFilesLines = new ArrayList<>();

        updateMessage("準備中。。。");

    }


    @Override
    protected Void call() throws Exception {

        calculateUrlFilesLines();

//        判斷資料捷是否已經存在，並建立資料夾
        Path urlDirPath = FileSystems.getDefault().getPath(mainDirName,
                                                           projectDirName,
                                                           urlDirName);
        if(Files.exists(urlDirPath)){
            updateMessage(urlDirName + " already exist!");
            return null;
        } else {
            Files.createDirectory(urlDirPath);
        }

//        暫存處理好的url lines 的容器
        ArrayList<String> matchedUrl;
        ArrayList<String> unmatchedUrl;

        for(int i=0; i<urlFilesName.size(); i++) {

//            建立url庫的路徑
            Path urlFilePath = FileSystems.getDefault().getPath(mainDirName,
                                                                resourceDirName,
                                                                urlFilesName.get(i));
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
                        writeUrlToFile(matchedUrl, true);
                        writeUrlToFile(unmatchedUrl, false);
                        matchedUrl = new ArrayList<>();
                        unmatchedUrl = new ArrayList<>();
                        countStoreListSize = 0;
                    }

                    updateMessage(message + lineCount + "/" + totalLines);
                    updateProgress(lineCount, totalLines);
                    lineCount++;

                    if(isCancelled()){
                        Path url1Path = FileSystems.getDefault().getPath(mainDirName,
                                                                        projectDirName,
                                                                        urlDirName,
                                                                        urlMatchFileName);
                        Files.deleteIfExists(url1Path);
                        Path url0Path = FileSystems.getDefault().getPath(mainDirName,
                                                                        projectDirName,
                                                                        urlDirName,
                                                                        urlUnmatchFileName);
                        Files.deleteIfExists(url0Path);
                        Files.delete(urlDirPath);
                        return null;
                    }
                }
                if(countStoreListSize > 0){
                    writeUrlToFile(matchedUrl, true);
                    writeUrlToFile(unmatchedUrl, false);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void writeUrlToFile(ArrayList<String> urlList, boolean isMatched) {

        String whichUrlFileName;

        if(isMatched) {
            whichUrlFileName = urlMatchFileName;
        } else {
//            亂數取1000個(chooseUrl0Max)
            whichUrlFileName = urlUnmatchFileName;
//            if(urlList.size() > chooseUrl0Max){
//                urlList = (ArrayList<String>) urlList.subList(0, chooseUrl0Max);// 執行時間 很長
//            }

        }

        Path whichUrlPath = FileSystems.getDefault().getPath(mainDirName,
                                                             projectDirName,
                                                             urlDirName,
                                                             whichUrlFileName);

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


    private boolean isContainsWnidList(String string) {
        for(String s: wnidList) {
            if(string.contains(s)) {
                return true;
            }
        }
        return false;
    }

    private void calculateUrlFilesLines() {
        for(String urlFileName: urlFilesName) {
            Path urlFilePath = FileSystems.getDefault().getPath(mainDirName,
                                                                resourceDirName,
                                                                urlFileName);

            try {
                urlFilesLines.add(countLines(urlFilePath.toString()));
                System.out.println(urlFileName + " has " + countLines(urlFilePath.toString()) + " lines.");//debug
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //    計算檔案內容有集個換行字元
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
