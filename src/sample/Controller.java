package sample;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    private Button openDirectoryButton;

    @FXML
    private Button stepOneButton, stepOneAutoDownloadButton,
            stepTwoRun, stepThreeRun, stepThreeCancel,
            stepFourRun, stepFourCancel,
            stepFiveRun, stepFiveCancel;

    @FXML
    private ProgressBar progressBar;


    @FXML
    private Stage primaryStage;

    @FXML
    private Hyperlink stepOneHyperlinkWord, stepOneHyperlinkUrls;

    public void initialize() {
//        先關所有功能
        stepOneHyperlinkWord.setDisable(true);
        stepOneHyperlinkUrls.setDisable(true);
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
            } else {
//                有資源wnid.txt 就開啟stepOne 的功能
                if(Files.exists(resource.getResourceWordsTextPath()))
                {
                    stepOneButton.setDisable(false);
                }
//                有全部資源檔才關閉自動下載資源檔按鈕
                ArrayList<String> resourceFiles = new ArrayList<>();
                resourceFiles.add("winter11_urls.txt");
                resourceFiles.add("fall11_urls.txt");
                resourceFiles.add("spring10_urls.txt");
                resourceFiles.add("urls.txt");

                stepOneAutoDownloadButton.setDisable(true);

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
            }

        } catch(IOException e) {
            e.printStackTrace();
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
     * @param actionEvent
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

        System.out.println("auto-download be clicked.");//debug
//        下載任務執行緒
        getResources = new GetResources(resource);
        progressBar.progressProperty().bind(getResources.progressProperty());
        progressBarLabel.textProperty().bind(getResources.messageProperty());

        getResources.setOnSucceeded(e -> {
            stepOneButton.setDisable(false);
            openDirectoryButton.setDisable(false);
            stepOneAutoDownloadButton.setDisable(true);
            progressBar.progressProperty().unbind();
            progressBarLabel.textProperty().unbind();
            progressBarLabel.setText("Download finished.");
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
        stepTwoRun.setDisable(true);
        stepThreeRun.setDisable(true);
        stepFourRun.setDisable(true);
        stepFiveRun.setDisable(true);

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
            return;
        }

        try {
            if (!Files.exists(resource.resolveProjectPath(project.getName()))) {
                directoryLabel.setText("您選的資料夾不在" + resource.getMainDirPath().toString() + "裡！");
                return;
            }
        } catch(NullPointerException e){
            e.printStackTrace();
            directoryLabel.setText("您選的資料夾不在" + resource.getMainDirPath().toString() + "裡！");
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
            progressBar.progressProperty().unbind();
            progressBarLabel.textProperty().unbind();
            progressBarLabel.setText("SubProject(create url files) get done.");
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
     * @param actionEvent
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
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("SubProject(download image files) get done.");
                progressBar.setProgress(-1);
            });

            imageDownloadForeman.setOnCancelled(e -> {
                stepThreeRun.setDisable(false);
                stepThreeCancel.setDisable(true);
                stepOneButton.setDisable(false);
                openDirectoryButton.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("Cancel finished.");
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
     * @param actionEvent
     */
    @FXML
    public void stepFourRunAction(ActionEvent actionEvent) {
        // walk file tree
        // FileVisitor interface
        if(((Button)actionEvent.getSource()).getId().equals("stepFourRun")) {
            disableAllButton();

            cleanImages = new CleanImages(resource);

            cleanImages.setOnSucceeded(e -> {
                stepFourCancel.setDisable(true);
                stepFiveRun.setDisable(false);
                stepOneButton.setDisable(false);
                openDirectoryButton.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("SubProject(clean image files) get done.");
                progressBar.setProgress(-1);
            });

            cleanImages.setOnCancelled(e -> {
                stepFourRun.setDisable(false);
                stepFourCancel.setDisable(true);
                stepOneButton.setDisable(false);
                openDirectoryButton.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("Cancel finished.");
                progressBar.setProgress(-1);
            });


            progressBar.progressProperty().bind(cleanImages.progressProperty());
            progressBarLabel.textProperty().bind(cleanImages.messageProperty());

            new Thread(cleanImages).start();
            System.out.println("CleanImage thread start");//debug
            stepFourCancel.setDisable(false);

        } else {
            cleanImages.cancel();
        }
    }

    /**
     * Make one HDF5 file from the project's images
     *
     * @param actionEvent
     */
    @FXML
    public void stepFiveRunAction(ActionEvent actionEvent) {

        if(((Button)actionEvent.getSource()).getId().equals("stepFiveRun")) {
            disableAllButton();

            int width = 64;
            makeHDF5 = new MakeHDF5(resource, width);

            makeHDF5.setOnSucceeded(e -> {
                stepFiveCancel.setDisable(true);
                stepOneButton.setDisable(false);
                openDirectoryButton.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("SubProject(make HDF5) get done.");
                progressBar.setProgress(-1);
            });

            makeHDF5.setOnCancelled(e -> {
                stepFiveRun.setDisable(false);
                stepFiveCancel.setDisable(true);
                stepOneButton.setDisable(false);
                openDirectoryButton.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("Cancel finished.");
                progressBar.setProgress(-1);
            });


            progressBar.progressProperty().bind(makeHDF5.progressProperty());
            progressBarLabel.textProperty().bind(makeHDF5.messageProperty());

            new Thread(makeHDF5).start();
            System.out.println("MakeHDF5 thread start");//debug
            stepFiveCancel.setDisable(false);

        } else {
            makeHDF5.cancel();
        }
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
        stepFourCancel.setDisable(true);
        stepFiveRun.setDisable(true);
        stepFiveCancel.setDisable(true);
    }

    /**
     * Delete the directory recursively.
     *
     * @param directoryToBeDeleted the directory wanted to delete.
     * @return whether it is success or not.
     */
    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

}
