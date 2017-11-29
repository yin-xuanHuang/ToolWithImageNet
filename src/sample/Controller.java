package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.activation.MimeType;
import javax.activation.MimetypesFileTypeMap;
import java.awt.Desktop;
import java.net.*;


import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Controller {

    private final String mainDirName = "machineLearningWithImageNetDir";
    private final String resourceDirName = "resourceDir";
    private final String wordsTextName = "words.txt";
    private final String projectWnidTextName = "wnid.txt";

    private final String urlDirName = "urlCollection";
    private final String urlMatchFileName = "url1.txt";
    private final String urlUnmatchFileName = "url0.txt";

    private final String imageDirName = "imageCollection";
    private final String imageMatchedDirName = "image1";
    private final String imageUnmatchedDirName = "image0";

    private DoWork task;
    private UrlMatcher urlMatcher;
    private ImageForeman imageForeman;
    private CleanImages cleanImages;

    @FXML
    private TextField stepOneTextField;

    @FXML
    private Label stepOneLabel, directoryLabel, progressBarLabel;

    @FXML
    private Button openDirectoryButton,
            setpOneCancelButton,
            stepOneAutoDownloadButton;

    @FXML
    private Button stepOneButton,
            stepTwoRun, stepTwoRemove,stepTwoCancel,
            stepThreeRun, stepThreeRemove, stepThreeCancel,
            stepFourRun, stepFourRemove, stepFourCancel,
            stepFiveRun, stepFiveRerun,
            stepSixRun, stepSixRerun;

    @FXML
    private ProgressBar progressBar;

//    @FXML
//    private Hyperlink stepOneHyperlinkWord, stepOneHyperlinkUrls;

    @FXML
    private Stage primaryStage;


    public void initialize() {
//      確認主、資源目錄有無存在，不存在就建立。
        try {
            Path mainDirPath = FileSystems.getDefault().getPath(mainDirName);
            Path resourceDirPath = FileSystems.getDefault().getPath(mainDirName, resourceDirName);
            if(!Files.exists(mainDirPath)) {
                Files.createDirectory(mainDirPath);
            }
            if(!Files.exists(resourceDirPath)) {
                Files.createDirectory(resourceDirPath);
            } else {

                if(Files.exists(FileSystems.getDefault().getPath(mainDirName, resourceDirName, "words.txt")))
                {
                    stepOneButton.setDisable(false);
                }

                ArrayList<String> resourceFiles = new ArrayList<>();
                resourceFiles.add("winter11_urls.txt");
                resourceFiles.add("fall11_urls.txt");
                resourceFiles.add("spring10_urls.txt");
                resourceFiles.add("urls.txt");
                Path resourceFilePath;

                for(String s:resourceFiles) {
                    resourceFilePath = FileSystems.getDefault().getPath(mainDirName, resourceDirName, s);
                    if(!Files.exists(resourceFilePath)){
                        stepOneAutoDownloadButton.setDisable(false);
                        break;
                    }
                    if(s.equals("urls.txt")){
                        openDirectoryButton.setDisable(false);
                    }
                }
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
            System.out.println("No matching available.");
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

        stepOneLabel.setText("Project " + valideTest + "has created.(" + valideWnid.size() + " wnid(s) counted)");

    }

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

    @FXML
    public void stepOneDownloadAction(ActionEvent actionEvent) {

        if(((Button)actionEvent.getSource()).getId().equals("stepOneAutoDownloadButton")){
            System.out.println("auto-download be clicked.");//debug
//            下載任務執行緒
            task = new DoWork();
            progressBar.progressProperty().bind(task.progressProperty());
            progressBarLabel.textProperty().bind(task.messageProperty());

            task.setOnSucceeded(e -> {
                stepOneButton.setDisable(false);
                setpOneCancelButton.setDisable(true);
                openDirectoryButton.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("Download finished.");
            });

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

        stepTwoRun.setDisable(true);
        stepThreeRun.setDisable(true);
        stepFourRun.setDisable(true);
        stepFiveRun.setDisable(true);
        stepSixRun.setDisable(true);

//        開啟選擇資料夾視窗
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose project directory");

        File project = chooser.showDialog(primaryStage);
        if(project != null) {
            try {
                directoryLabel.setText(project.getName());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            System.out.println(project.getName());// debug
        }

        try {
            if (!Files.exists(FileSystems.getDefault().getPath(mainDirName, project.getName()))) {
                directoryLabel.setText("Not a project in machineLearningWithImageNet directory.");
                return;
            }
        } catch(NullPointerException e){
            e.printStackTrace();
            directoryLabel.setText("Not a project in machineLearningWithImageNet directory.");
            return;
        }

//      確認子專案狀態，以便開啟所需功能

        ArrayList<String> subProjectsName = new ArrayList<>();
        subProjectsName.add(urlDirName);
        subProjectsName.add(imageDirName);
        subProjectsName.add("cleanImage");
        subProjectsName.add("hdf5");
        subProjectsName.add("parameters");

        ArrayList<Button> subProjectsRunButton = new ArrayList<>();
        subProjectsRunButton.add(stepTwoRun);
        subProjectsRunButton.add(stepThreeRun);
        subProjectsRunButton.add(stepFourRun);
        subProjectsRunButton.add(stepFiveRun);
        subProjectsRunButton.add(stepSixRun);

        Path subProjectPath;

        for(int i=0;i < subProjectsName.size(); i++) {
            subProjectPath = FileSystems.getDefault().getPath(mainDirName,
                                                              project.getName(),
                                                              subProjectsName.get(i));
            if(!Files.exists(subProjectPath)){
                subProjectsRunButton.get(i).setDisable(false);
                break;

            }
        }


    }

    @FXML
    public void stepTwoRunAction(ActionEvent actionEvent) {

        if(((Button)actionEvent.getSource()).getId().equals("stepTwoRun")) {

            System.out.println("stepTwo thread start");//debug

//            確認檔案存在
            Path projectPath = FileSystems.getDefault().getPath(mainDirName,
                                                                directoryLabel.getText(),
                                                                projectWnidTextName);

            if (!Files.exists(projectPath)) {
                System.out.println(directoryLabel.getText() + " can't find.");
                return;
            }

//            讀wnid檔
            ArrayList<String> wnidList = new ArrayList<>();
            try (BufferedReader wnidTextReader = new BufferedReader(new FileReader(projectPath.toString()))) {
                String input;
                while ((input = wnidTextReader.readLine()) != null) {
                    wnidList.add(input);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

    /*        for(String s:wnidList) {//debug
                System.out.println(s);
            }*/

            urlMatcher = new UrlMatcher(mainDirName,
                                                resourceDirName,
                                                directoryLabel.getText(),
                                                urlDirName,
                                                urlMatchFileName,
                                                urlUnmatchFileName,
                                                wnidList);

            urlMatcher.setOnSucceeded(e -> {
                stepTwoCancel.setDisable(true);
                stepTwoRemove.setDisable(false);
                stepThreeRun.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("SubProject(create url files) get done.");
                progressBar.setProgress(-1);
            });

            urlMatcher.setOnCancelled(e -> {
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                stepTwoRun.setDisable(false);
                stepTwoCancel.setDisable(true);
                stepTwoRemove.setDisable(true);
                progressBarLabel.setText("Cancel the action.");
                progressBar.setProgress(-1);
            });

            progressBar.progressProperty().bind(urlMatcher.progressProperty());
            progressBarLabel.textProperty().bind(urlMatcher.messageProperty());

            new Thread(urlMatcher).start();
            System.out.println("urlMatcher thread start");//debug
            stepTwoRun.setDisable(true);
            stepTwoCancel.setDisable(false);

        } else if(((Button)actionEvent.getSource()).getId().equals("stepTwoCancel")){
            urlMatcher.cancel();

        } else {
            Path projectUrlDirPath = FileSystems.getDefault().getPath(mainDirName,
                                                                directoryLabel.getText(),
                                                                urlDirName);
            try{
                Files.deleteIfExists(projectUrlDirPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            stepTwoRun.setDisable(false);
            stepTwoRemove.setDisable(true);
        }
    }

    @FXML
    public void stepThreeRunAction(ActionEvent actionEvent) {

        if(((Button)actionEvent.getSource()).getId().equals("stepThreeRun")) {
            disableAllButton();
            imageForeman = new ImageForeman(mainDirName,
                                            directoryLabel.getText(),
                                            imageDirName,
                                            imageMatchedDirName,
                                            imageUnmatchedDirName,
                                            urlDirName,
                                            urlMatchFileName,
                                            urlUnmatchFileName);
            imageForeman.setOnSucceeded(e -> {
                stepThreeCancel.setDisable(true);
                stepThreeRemove.setDisable(false);
                stepFourRun.setDisable(false);
                stepOneButton.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("SubProject(download image files) get done.");
                progressBar.setProgress(-1);
            });

            imageForeman.setOnCancelled(e -> {
                stepThreeRun.setDisable(false);
                stepThreeCancel.setDisable(true);
                stepThreeRemove.setDisable(true);
                stepOneButton.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("Cancel finished.");
                progressBar.setProgress(-1);
            });


            progressBar.progressProperty().bind(imageForeman.progressProperty());
            progressBarLabel.textProperty().bind(imageForeman.messageProperty());

            new Thread(imageForeman).start();
            System.out.println("ImageForeman thread start");//debug
            stepThreeRun.setDisable(true);
            stepThreeCancel.setDisable(false);

        } else if(((Button)actionEvent.getSource()).getId().equals("stepThreeCancel")){
            imageForeman.cancel();
        } else {
            Path projectImageDirPath = FileSystems.getDefault().getPath(mainDirName,
                                                                    directoryLabel.getText(),
                                                                    imageDirName);
            deleteDirectory(projectImageDirPath.toFile());
            stepThreeRun.setDisable(false);
            stepThreeRemove.setDisable(true);
            stepOneButton.setDisable(false);
        }

    }

    @FXML
    public void stepFourRunAction(ActionEvent actionEvent) {
        // walk file tree
        // FileVisitor interface
        if(((Button)actionEvent.getSource()).getId().equals("stepFourRun")) {
            disableAllButton();
            cleanImages = new CleanImages(mainDirName,
                                        directoryLabel.getText(),
                                        imageDirName,
                                        imageMatchedDirName,
                                        imageUnmatchedDirName);

            cleanImages.setOnSucceeded(e -> {
                stepFourCancel.setDisable(true);
                stepFourRemove.setDisable(false);
                stepFiveRun.setDisable(false);
                stepOneButton.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("SubProject(clean image files) get done.");
                progressBar.setProgress(-1);
            });

            cleanImages.setOnCancelled(e -> {
                stepFourRun.setDisable(false);
                stepFourCancel.setDisable(true);
                stepFourRemove.setDisable(true);
                stepOneButton.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("Cancel finished.");
                progressBar.setProgress(-1);
            });


            progressBar.progressProperty().bind(cleanImages.progressProperty());
            progressBarLabel.textProperty().bind(cleanImages.messageProperty());

            new Thread(cleanImages).start();
            System.out.println("CleanImage thread start");//debug
            stepFourRun.setDisable(true);
            stepFourCancel.setDisable(false);

        }else if(((Button)actionEvent.getSource()).getId().equals("stepFourCancel")){
            cleanImages.cancel();
        } else {
            Path projectImageDirPath = FileSystems.getDefault().getPath(mainDirName,
                                                                        directoryLabel.getText(),
                                                                        imageDirName);
            deleteDirectory(projectImageDirPath.toFile());
            stepFourRun.setDisable(false);
            stepFourRemove.setDisable(true);
        }

    }

    @FXML
    public void stepFiveRunAction(ActionEvent actionEvent) {

    }

    @FXML
    public void stepSixRunAction(ActionEvent actionEvent) {

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
    private static String concatWithCommas(Collection<String> words, char delimiter) {
        StringBuilder wordList = new StringBuilder();
        for (String word : words) {
            wordList.append(word + delimiter);
        }
        return new String(wordList.deleteCharAt(wordList.length() - 1));
    }

    @FXML
    private void disableAllButton() {
        openDirectoryButton.setDisable(true);
        setpOneCancelButton.setDisable(true);
        stepOneAutoDownloadButton.setDisable(true);
        stepOneButton.setDisable(true);
        stepTwoRun.setDisable(true);
        stepTwoRemove.setDisable(true);
        stepTwoCancel.setDisable(true);
        stepThreeRun.setDisable(true);
        stepThreeRemove.setDisable(true);
        stepThreeCancel.setDisable(true);
        stepFourRun.setDisable(true);
        stepFourRemove.setDisable(true);
        stepFourCancel.setDisable(true);

        stepFiveRun.setDisable(true);

        stepSixRun.setDisable(true);

    }

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
