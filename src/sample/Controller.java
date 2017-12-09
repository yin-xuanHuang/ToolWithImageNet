package sample;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.*;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Controller {

    private final ThisResource resource = new ThisResource();

    private GetResources getResources;
    private WnidMatcher wnidMatcher;
    private ImageDownloadForeman imageDownloadForeman;
    private CleanImages cleanImages;
    private MakeHDF5 makeHDF5;

    @FXML
    private TextField stepOneTextField;

    @FXML
    private Label stepOneLabel, directoryLabel, progressBarLabel;

    @FXML
    private Text selfDownloadtext2, selfDownloadtext3, selfDownloadtext4;

    @FXML
    private Button openDirectoryButton;

    @FXML
    private Button stepOneButton, stepOneAutoDownloadButton,
            stepTwoRun,
            stepThreeRun, stepThreeCancel,
            stepFourRun,
            stepFiveRun;

    @FXML
    private ProgressBar progressBar;


    @FXML
    private Stage primaryStage;

    @FXML
    private Hyperlink stepOneHyperlinkWord, stepOneHyperlinkUrls;

    public void initialize() {
//        先關所有功能
        disableAllButton();

//        確認主、資源目錄有無存在，不存在就建立。
        try {
            Path mainDirPath = resource.getMainDirPath();
            if(!Files.exists(mainDirPath)) {
                Files.createDirectory(mainDirPath);
            }
            Path resourceDirPath = resource.getResourceDirPath();
            if(!Files.exists(resourceDirPath)) {
                Files.createDirectory(resourceDirPath);
            }
            Path resourceWrongImageSignatureFilePath = resource.getWrongImageSignaturePath();
            if(!Files.exists(resourceWrongImageSignatureFilePath)){
                ArrayList<String> wrongImageSignatureData = resource.getWrongImageSignatureData();
                try(BufferedWriter wo = new BufferedWriter(new FileWriter(resourceWrongImageSignatureFilePath.toString()))){
                    for(String signature: wrongImageSignatureData){
                        wo.write(signature + "\n");
                    }
                }
            }

            selfDownloadtext2.setText("下載後，請將檔案解壓縮後放置在");
            selfDownloadtext3.setText(resource.getMainDirPath().toString() + "資料夾裡的");
            selfDownloadtext4.setText("resource資料夾裡(共6個txt檔)");

//            有資源wnid.txt 就開啟stepOne 的功能
            if(Files.exists(resource.getResourceWordsTextPath()))
            {
                stepOneButton.setDisable(false);
            } else {
                stepOneAutoDownloadButton.setDisable(false);
                stepOneHyperlinkWord.setDisable(false);
                stepOneHyperlinkUrls.setDisable(false);
            }
//            有全部資源檔才關閉自動下載資源檔按鈕
            ArrayList<String> resourceFiles = resource.getResourceUrlFiles();

            for(String s:resourceFiles) {
                Path resourceFilePath = resource.resolveResourcePath(s);
                if(!Files.exists(resourceFilePath)){
                    stepOneAutoDownloadButton.setDisable(false);
                    stepOneHyperlinkWord.setDisable(false);
                    stepOneHyperlinkUrls.setDisable(false);
                    break;
                }
            }
            if(stepOneAutoDownloadButton.isDisable())
                openDirectoryButton.setDisable(false);


        } catch(IOException e) {
            e.printStackTrace();
        }


    }

    /**
     *
     * 1.確認輸入有無合法並處理。
     * 2.檢查words.txt文件檔是否存在並讀檔。
     * 3.將輸入關鍵字與檔案內容進行比對。
     * 4.建立專案以及結果資料儲存。
     *
     */
    @FXML
    public void stepOneSubmitAction() {

        String inputKeywords = stepOneTextField.getText();
        if(inputKeywords.equals("")){
            stepOneLabel.setText("無輸入");
            return;
        }

//        step 1
//        移除掉允許的添加字元，進行判斷
        String validTest = inputKeywords.replace(",", "")
                .replace("-", "")
                .replace(" ", "");
//        判斷專案資料夾是否已經存在
        Path projectDir = resource.resolveProjectPath(validTest);
        if(Files.exists(projectDir)) {
            stepOneLabel.setText("相同名稱的專案已經存在。");
            return;
        }

//        判斷輸入的字串
        if(!isAlpha(validTest) || validTest.isEmpty()) {
            stepOneLabel.setText("輸入有問題，請輸入英文單字(-可)");
            return;
        } else {
            stepOneLabel.setText(inputKeywords);
        }

//        將字串打成字串陣列
        String[] keywordsArray = inputKeywords.toLowerCase().
                replace(" ", "").
                split(",");

//        step 2
//        確認words.txt有無存在。
        Path wordsTextFile = resource.getResourceWordsTextPath();
        if(!Files.exists(wordsTextFile)) {
            stepOneLabel.setText("資源檔有缺少(words.txt)");
            return;
        }

//        讀取資源words.txt檔案，將資料讀進ArrayList<String> 裡。
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
                concatWithCommas(Arrays.asList(keywordsArray)) +
                ")(, |).*");
        for(String s: data) {
            Matcher matcher = pattern.matcher(s);
            if(matcher.matches()){
                valideWnid.add(s.split("\\t")[0]);
            }
        }


        if(valideWnid.size() == 0) {
            stepOneLabel.setText("沒有任何媒合單詞。");
            return;
        }

//        step 4
//        建立專案資料夾以及建立專案wnid.txt檔
        Path projectWnidFile;
        try {
            Files.createDirectory(projectDir);
            projectWnidFile = resource.getProjectWnidText();
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

        stepOneLabel.setText("專案：" + validTest + "已經建立.(有" + valideWnid.size() + " wnid(s) 條目)");

    }

    /**
     * 用瀏覽器開啟自行下載的網頁
     *
     * @param actionEvent which hypertext to be clicked
     */
    @FXML
    public void openWebsiteAction(ActionEvent actionEvent) {

        String urlWord = "http://image-net.org/archive/words.txt";
        String urlUrls = "http://image-net.org/download-imageurls";
        final String url;

        Object source = actionEvent.getSource();

        if(source instanceof Hyperlink){
            if(((Hyperlink) source).getId().equals("stepOneHyperlinkWord")){
                url = urlWord;
            } else {
                url = urlUrls;
            }
        } else {
            System.out.println("Something wrong with openWebsiteAction");
            return;
        }

        Thread thread = new Thread(() ->{
            Desktop d = Desktop.getDesktop();
            try {
                d.browse(new URI(url));
            } catch(URISyntaxException e1) {
                e1.printStackTrace();
            } catch(IOException e1) {
                e1.printStackTrace();
            }
        });
        thread.start();
    }

    /**
     * 自動下載資料檔
     *
     * 需解壓縮會自動解壓縮，然後刪除不需要的壓縮檔
     *
     */
    @FXML
    public void stepOneDownloadAction() {
        disableAllButton();

        System.out.println("auto-download be clicked.");//debug
//        下載任務執行緒
        getResources = new GetResources(resource);
        progressBar.progressProperty().bind(getResources.progressProperty());
        progressBarLabel.textProperty().bind(getResources.messageProperty());

        getResources.setOnSucceeded(e -> {
            stepOneButton.setDisable(false);
            openDirectoryButton.setDisable(false);
            stepOneAutoDownloadButton.setDisable(false);
            progressBar.progressProperty().unbind();
            progressBarLabel.textProperty().unbind();
            progressBarLabel.setText("Download finished.");
            progressBar.setProgress(-1);
        });

        new Thread(getResources).start();
        System.out.println("auto-download thread start");//debug
        stepOneAutoDownloadButton.setDisable(true);
        stepOneButton.setDisable(true);
    }

    /**
     * 開啟專案資料夾
     */
    @FXML
    public void openDirectoryAction() {

        stepOneButton.setDisable(true);
        openDirectoryButton.setDisable(true);

//        開啟選擇資料夾視窗
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("選擇專案資料夾");

        File project = chooser.showDialog(primaryStage);
        if(project != null) {
            try {
                directoryLabel.setText(project.getName());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else {
            stepOneButton.setDisable(false);
            openDirectoryButton.setDisable(false);
            return;
        }

//        確認選擇的資料夾在指定範圍內
        try {
            if (!Files.exists(resource.resolveProjectPath(project.getName()))) {
                directoryLabel.setText("您選的資料夾不在" + resource.getMainDirPath().toString() + "裡！");
                stepOneButton.setDisable(false);
                openDirectoryButton.setDisable(false);
                return;
            }
        } catch(NullPointerException e){
            e.printStackTrace();
            directoryLabel.setText("您選的資料夾不在" + resource.getMainDirPath().toString() + "裡！");
            stepOneButton.setDisable(false);
            openDirectoryButton.setDisable(false);
            return;
        }

//        確認子專案狀態，以便開啟所需功能
        ArrayList<Path> projectSubDirPath = new ArrayList<>();
        projectSubDirPath.add(resource.getUrlDirPath());
        projectSubDirPath.add(resource.getImageDirPath());
        projectSubDirPath.add(resource.getCleanDirPath());
        projectSubDirPath.add(resource.getHdf5DirPath());

        ArrayList<Button> subProjectsRunButton = new ArrayList<>();
        subProjectsRunButton.add(stepTwoRun);
        subProjectsRunButton.add(stepThreeRun);
        subProjectsRunButton.add(stepFourRun);
        subProjectsRunButton.add(stepFiveRun);

        for(int i=0;i < projectSubDirPath.size(); i++) {
            if(!Files.exists(projectSubDirPath.get(i))){
                subProjectsRunButton.get(i).setDisable(false);
                break;

            }
        }

        stepOneButton.setDisable(false);
        openDirectoryButton.setDisable(false);


    }

    /**
     * Matching the project's wnid with resource wnid
     *
     */
    @FXML
    public void stepTwoRunAction() {

        disableAllButton();

        wnidMatcher = new WnidMatcher(resource);

        wnidMatcher.setOnSucceeded(e -> {
            stepThreeRun.setDisable(false);
            stepOneButton.setDisable(false);
            openDirectoryButton.setDisable(false);
            stepOneAutoDownloadButton.setDisable(false);
            stepOneHyperlinkWord.setDisable(false);
            stepOneHyperlinkUrls.setDisable(false);
            progressBar.progressProperty().unbind();
            progressBarLabel.textProperty().unbind();
            progressBarLabel.setText("URL檔案比對以及建立完成");
            progressBar.setProgress(-1);
        });

        progressBar.progressProperty().bind(wnidMatcher.progressProperty());
        progressBarLabel.textProperty().bind(wnidMatcher.messageProperty());

        new Thread(wnidMatcher).start();
        System.out.println("urlMatcher thread start");//debug

    }

    /**
     * Downloading images by the project's url files
     *
     * @param actionEvent run or cancel
     */
    @FXML
    public void stepThreeRunAction(ActionEvent actionEvent) {

        if(((Button)actionEvent.getSource()).getId().equals("stepThreeRun")) {
            disableAllButton();
            imageDownloadForeman = new ImageDownloadForeman(resource);

            imageDownloadForeman.setOnSucceeded(e -> {
                stepThreeCancel.setDisable(true);
                stepFourRun.setDisable(false);
                stepOneButton.setDisable(false);
                openDirectoryButton.setDisable(false);
                stepOneAutoDownloadButton.setDisable(false);
                stepOneHyperlinkWord.setDisable(false);
                stepOneHyperlinkUrls.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("圖片檔案下載完成");
                progressBar.setProgress(-1);
            });

            imageDownloadForeman.setOnCancelled(e -> {
                stepThreeRun.setDisable(false);
                stepThreeCancel.setDisable(true);
                stepOneButton.setDisable(false);
                stepOneAutoDownloadButton.setDisable(false);
                stepOneHyperlinkWord.setDisable(false);
                stepOneHyperlinkUrls.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("停止發出下載工作。（可能還有一些執行緒在下載之前發出的工作）");
                progressBar.setProgress(-1);
            });


            progressBar.progressProperty().bind(imageDownloadForeman.progressProperty());
            progressBarLabel.textProperty().bind(imageDownloadForeman.messageProperty());

            new Thread(imageDownloadForeman).start();
            System.out.println("ImageForeman thread start");//debug
            stepThreeCancel.setDisable(false);

        } else{
            imageDownloadForeman.cancel();
        }
    }

    /**
     * Cleaning the project's images by checking each whether is image file type
     *
     */
    @FXML
    public void stepFourRunAction() {
        // walk file tree
        // FileVisitor interface
        disableAllButton();

        cleanImages = new CleanImages(resource);

        cleanImages.setOnSucceeded(e -> {
            stepFiveRun.setDisable(false);
            stepOneButton.setDisable(false);
            openDirectoryButton.setDisable(false);
            stepOneAutoDownloadButton.setDisable(false);
            stepOneHyperlinkWord.setDisable(false);
            stepOneHyperlinkUrls.setDisable(false);
            progressBar.progressProperty().unbind();
            progressBarLabel.textProperty().unbind();
            progressBarLabel.setText("圖片清理完成");
            progressBar.setProgress(-1);
        });

        progressBar.progressProperty().bind(cleanImages.progressProperty());
        progressBarLabel.textProperty().bind(cleanImages.messageProperty());

        new Thread(cleanImages).start();
        System.out.println("CleanImage thread start");//debug

    }

    /**
     * Make one HDF5 file from the project's images
     *
     */
    @FXML
    public void stepFiveRunAction() {
        disableAllButton();
        int width = 64;
        makeHDF5 = new MakeHDF5(resource, width);

        makeHDF5.setOnSucceeded(e -> {
            stepOneButton.setDisable(false);
            openDirectoryButton.setDisable(false);
            stepOneAutoDownloadButton.setDisable(false);
            stepOneHyperlinkWord.setDisable(false);
            stepOneHyperlinkUrls.setDisable(false);
            progressBar.progressProperty().unbind();
            progressBarLabel.textProperty().unbind();
            progressBarLabel.setText("SubProject(make HDF5) get done.");
            progressBar.setProgress(-1);
        });

        progressBar.progressProperty().bind(makeHDF5.progressProperty());
        progressBarLabel.textProperty().bind(makeHDF5.messageProperty());

        new Thread(makeHDF5).start();
        System.out.println("MakeHDF5 thread start");//debug
    }


    /**
     * Check whether or not every character is alpha in the string
     *
     * @param name be checked by each character
     * @return whether or not the string is all alpha.
     */
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
    private static String concatWithCommas(Collection<String> words) {
        StringBuilder wordList = new StringBuilder();
        for (String word : words) {
            wordList.append(word + '|');
        }
        return new String(wordList.deleteCharAt(wordList.length() - 1));
    }

    /**
     * When processing, disable all button.
     */
    @FXML
    private void disableAllButton() {
        openDirectoryButton.setDisable(true);
        stepOneButton.setDisable(true);
        stepTwoRun.setDisable(true);
        stepThreeRun.setDisable(true);
        stepThreeCancel.setDisable(true);
        stepFourRun.setDisable(true);
        stepFiveRun.setDisable(true);
        stepOneAutoDownloadButton.setDisable(true);
        stepOneHyperlinkUrls.setDisable(true);
        stepOneHyperlinkWord.setDisable(true);
    }

}
