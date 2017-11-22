package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.net.*;


import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Controller {

    private final String mainDirName = "machineLearningWithImageNetDir";
    private final String resourceDirName = "resourceDir";
    private final String wordsTextName = "words.txt";
    private final String projectWnidTextName = "wnid.txt";

    DoWork task;

    @FXML
    private TextField stepOneTextField;

    @FXML
    private Label stepOneLabel, progressBarLabel;

    @FXML
    private Button openDirectoryButton, setpOneCancelButton, stepOneAutoDownloadButton;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Hyperlink stepOneHyperlink;

    @FXML
    private Stage primaryStage;


    public void initialize() {
//      確認主、資源目錄有無存在，不存在就建立。
        try {
            Path mainDir = FileSystems.getDefault().getPath(mainDirName);
            Path resourceDir = FileSystems.getDefault().getPath(mainDirName, resourceDirName);
            if(!Files.exists(mainDir)) {
                Files.createDirectory(mainDir);
            }
            if(!Files.exists(resourceDir)) {
                Files.createDirectory(resourceDir);
            }

        } catch(IOException e) {
            e.printStackTrace();
            return;
        }


    }

    /*1.確認輸入有無合法並處理。
     *2.檢查words.txt文件檔是否存在並讀檔。
     *3.將輸入關鍵字與檔案內容進行比對。
     *4.建立專案以及結果資料儲存。         */
    @FXML
    public void stepOneSubmitAction() {
        String inputKeywords = stepOneTextField.getText();



//        step 1
        String valideTest = inputKeywords.replace(",", "")
                                         .replace("-", "")
                                         .replace(" ", "");

        Path projectDir = FileSystems.getDefault().getPath(mainDirName, valideTest);
        if(Files.exists(projectDir)) {
            System.out.println("the same project already exist.");//debug
            stepOneLabel.setText("The same project already exist.");
            return;
        }

        if(!isAlpha(valideTest) || valideTest.isEmpty()) {
//            input not valiate
            stepOneLabel.setText("輸入有問題，請再次輸入英文單字");
            return;
        } else {
            stepOneLabel.setText(inputKeywords);
        }

//        將字串打成字串陣列
        String[] keywordsArray = inputKeywords.toLowerCase().split(",");

//        step 2
//        確認words.txt有無存在。
        Path wordsTextFile = FileSystems.getDefault().getPath(mainDirName,
                                                              resourceDirName,
                                                              wordsTextName);
        if(!Files.exists(wordsTextFile)) {
            System.out.println("no words.txt");
            return;
        }

//        讀取words.txt檔案，將資料讀進ArrayList<String> 裡。
        ArrayList<String> data = new ArrayList<>();
        try(BufferedReader wordsTextReader = new BufferedReader(new FileReader(wordsTextFile.toString()))) {
            String input;
            while((input = wordsTextReader.readLine()) != null) {
                data.add(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

//        step 3
//        用 RegEx 配對使用者key-in的關鍵字與words.txt內容。
        ArrayList<String> valideWnid = new ArrayList<>();
        Pattern pattern = Pattern.compile(".*(\\t|, )("+
                                          concatWithCommas(Arrays.asList(keywordsArray), '|') +
                                          ")(, |).*");
        for(String s: data) {
            Matcher matcher = pattern.matcher(s);
            if(matcher.matches()){
                valideWnid.add(s.split("\\t")[0]);
            }
        }

        for(String s: valideWnid) {//debug
            System.out.println(s);
        }
        for(String s: keywordsArray) {//debug
            System.out.println(s);
        }

//        System.out.println(System.getProperty("user.dir"));//debug

        if(valideWnid.size() == 0) {
            System.out.println("no match avaliable.");
            return;
        }

//        step 4
//        建立專案資料夾以及wnid.txt
        Path projectWnidFile;
        try {
            Files.createDirectory(projectDir);
            projectWnidFile = FileSystems.getDefault().getPath(mainDirName,
                                                               valideTest,
                                                               projectWnidTextName);
            Files.createFile(projectWnidFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try(BufferedWriter wnidTextWriter = new BufferedWriter(new FileWriter(projectWnidFile.toString()))) {
            for(String s: valideWnid) {
                wnidTextWriter.write(s + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        stepOneLabel.setText("Project " + valideTest + "has created.");

    }

    @FXML
    public void openWebsiteAction() {
        Thread thread = new Thread() {
            @Override
            public void run(){
                Desktop d = Desktop.getDesktop();
                try {
                    d.browse(new URI("http://image-net.org/download-imageurls"));
                } catch(URISyntaxException e1) {
                    e1.printStackTrace();
                } catch(IOException e1) {
                    e1.printStackTrace();
                }
            }
        };
        thread.start();
    }

    @FXML
    public void stepOneDownloadAction(ActionEvent actionEvent) {

        if(((Button)actionEvent.getSource()).getId().equals("stepOneAutoDownloadButton")){
            System.out.println("auto-download be clicked.");//debug
            //        下載任務執行緒
            task = new DoWork();
            progressBar.progressProperty().bind(task.progressProperty());
            progressBarLabel.textProperty().bind(task.messageProperty());

            new Thread(task).start();
            System.out.println("auto-download thread start");//debug
            setpOneCancelButton.setDisable(false);
            stepOneAutoDownloadButton.setDisable(true);


        } else {
            task.cancel();
            setpOneCancelButton.setDisable(true);
            stepOneAutoDownloadButton.setDisable(false);
        }
    }

    @FXML
    public void openDirectoryAction() {

    }

//    字元辨識
    private boolean isAlpha(String name) {
        char[] chars = name.toCharArray();

        for (char c : chars) {
            if(!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Join a collection of strings and add commas as delimiters.
     * @require words.size() > 0 && words != null
     */
    public static String concatWithCommas(Collection<String> words, char delimiter) {
        StringBuilder wordList = new StringBuilder();
        for (String word : words) {
            wordList.append(word + delimiter);
        }
        return new String(wordList.deleteCharAt(wordList.length() - 1));
    }
}
