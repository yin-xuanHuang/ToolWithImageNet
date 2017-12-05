package sample;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
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

    private final ThisResource resource = new ThisResource();

    private GetResources task;
    private WnidMatcher wnidMatcher;
    private ImageDownloadForeman imageDownloadForeman;
    private CleanImages cleanImages;

    @FXML
    private TextField stepOneTextField;

    @FXML
    private Label stepOneLabel, directoryLabel, progressBarLabel;

    @FXML
    private Button openDirectoryButton;

    @FXML
    private Button stepOneButton, stepOneAutoDownloadButton, stepOneCancel,
                   stepTwoRun,    stepTwoRemove,             stepTwoCancel,
                   stepThreeRun,  stepThreeRemove,           stepThreeCancel,
                   stepFourRun,   stepFourRemove,            stepFourCancel,
                   stepFiveRun,   stepFiveRemove,            stepFiveCancel,
                   stepSixRun,    stepSixRemove,             stepSixCancel;

    @FXML
    private ProgressBar progressBar;


    @FXML
    private Stage primaryStage;


    public void initialize() {
//      確認主、資源目錄有無存在，不存在就建立。
        try {
            Path mainDirPath = resource.getMainDirPath();
            Path resourceDirPath = resource.getResourceDirPath();
            if(!Files.exists(mainDirPath)) {
                Files.createDirectory(mainDirPath);
            }
            if(!Files.exists(resourceDirPath)) {
                Files.createDirectory(resourceDirPath);
            } else {

                if(Files.exists(resource.getResourceWordsTextPath()))
                {
                    stepOneButton.setDisable(false);
                }

                ArrayList<String> resourceFiles = new ArrayList<>();
                resourceFiles.add("winter11_urls.txt");
                resourceFiles.add("fall11_urls.txt");
                resourceFiles.add("spring10_urls.txt");
                resourceFiles.add("urls.txt");

                for(String s:resourceFiles) {
                    Path resourceFilePath = resource.resolveResourcePath(s);
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
        String validTest = inputKeywords.replace(",", "")
                                         .replace("-", "")
                                         .replace(" ", "");

        Path projectDir = resource.resolveProjectPath(validTest);
        if(Files.exists(projectDir)) {
            stepOneLabel.setText("The same project already exist.");
            return;
        }

        if(!isAlpha(validTest) || validTest.isEmpty()) {
//            input not valid
            stepOneLabel.setText("輸入有問題，請再次輸入英文單字");
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


        if(valideWnid.size() == 0) {
            System.out.println("No matching available.");
            return;
        }

//        step 4
//        建立專案資料夾以及wnid.txt
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

        stepOneLabel.setText("Project " + validTest + "has created.(" + valideWnid.size() + " wnid(s) counted)");

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
            task = new GetResources();
            progressBar.progressProperty().bind(task.progressProperty());
            progressBarLabel.textProperty().bind(task.messageProperty());

            task.setOnSucceeded(e -> {
                stepOneButton.setDisable(false);
                stepOneCancel.setDisable(true);
                openDirectoryButton.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("Download finished.");
            });

            new Thread(task).start();
            System.out.println("auto-download thread start");//debug
            stepOneCancel.setDisable(false);
            stepOneAutoDownloadButton.setDisable(true);

        } else {
            task.cancel();
            stepOneCancel.setDisable(true);
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
            if (!Files.exists(resource.resolveProjectPath(project.getName()))) {
                directoryLabel.setText("Not a project in machineLearningWithImageNet directory.");
                return;
            }
        } catch(NullPointerException e){
            e.printStackTrace();
            directoryLabel.setText("Not a project in machineLearningWithImageNet directory.");
            return;
        }

//      確認子專案狀態，以便開啟所需功能

        ArrayList<Path> projectSubDirPath = new ArrayList<>();
        projectSubDirPath.add(resource.getUrlDirPath());
        projectSubDirPath.add(resource.getImageDirPath());
        projectSubDirPath.add(resource.getCleanDirPath());
        projectSubDirPath.add(resource.getHdf5DirPath());
        projectSubDirPath.add(resource.getParameterDirPath());

        ArrayList<Button> subProjectsRunButton = new ArrayList<>();
        subProjectsRunButton.add(stepTwoRun);
        subProjectsRunButton.add(stepThreeRun);
        subProjectsRunButton.add(stepFourRun);
        subProjectsRunButton.add(stepFiveRun);
        subProjectsRunButton.add(stepSixRun);

        for(int i=0;i < projectSubDirPath.size(); i++) {
            if(!Files.exists(projectSubDirPath.get(i))){
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
            Path projectPath = resource.getProjectWnidText();

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

            wnidMatcher = new WnidMatcher(resource, wnidList);

            wnidMatcher.setOnSucceeded(e -> {
                stepTwoCancel.setDisable(true);
                stepTwoRemove.setDisable(false);
                stepThreeRun.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("SubProject(create url files) get done.");
                progressBar.setProgress(-1);
            });

            wnidMatcher.setOnCancelled(e -> {
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                stepTwoRun.setDisable(false);
                stepTwoCancel.setDisable(true);
                stepTwoRemove.setDisable(true);
                progressBarLabel.setText("Cancel the action.");
                progressBar.setProgress(-1);
            });

            progressBar.progressProperty().bind(wnidMatcher.progressProperty());
            progressBarLabel.textProperty().bind(wnidMatcher.messageProperty());

            new Thread(wnidMatcher).start();
            System.out.println("urlMatcher thread start");//debug
            stepTwoRun.setDisable(true);
            stepTwoCancel.setDisable(false);

        } else if(((Button)actionEvent.getSource()).getId().equals("stepTwoCancel")){
            wnidMatcher.cancel();

        } else {
            Path projectUrlDirPath = resource.getUrlDirPath();
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
            imageDownloadForeman = new ImageDownloadForeman(resource);
            imageDownloadForeman.setOnSucceeded(e -> {
                stepThreeCancel.setDisable(true);
                stepThreeRemove.setDisable(false);
                stepFourRun.setDisable(false);
                stepOneButton.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("SubProject(download image files) get done.");
                progressBar.setProgress(-1);
            });

            imageDownloadForeman.setOnCancelled(e -> {
                stepThreeRun.setDisable(false);
                stepThreeCancel.setDisable(true);
                stepThreeRemove.setDisable(true);
                stepOneButton.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBarLabel.textProperty().unbind();
                progressBarLabel.setText("Cancel finished.");
                progressBar.setProgress(-1);
            });


            progressBar.progressProperty().bind(imageDownloadForeman.progressProperty());
            progressBarLabel.textProperty().bind(imageDownloadForeman.messageProperty());

            new Thread(imageDownloadForeman).start();
            System.out.println("ImageForeman thread start");//debug
            stepThreeRun.setDisable(true);
            stepThreeCancel.setDisable(false);

        } else if(((Button)actionEvent.getSource()).getId().equals("stepThreeCancel")){
            imageDownloadForeman.cancel();
        } else {
            Path projectImageDirPath = resource.getImageDirPath();
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
            cleanImages = new CleanImages(resource);

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
            Path projectImageDirPath = resource.getImageDirPath();
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
        stepOneCancel.setDisable(true);
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
        stepFiveRemove.setDisable(true);
        stepFiveCancel.setDisable(true);
        stepSixRun.setDisable(true);
        stepSixRemove.setDisable(true);
        stepSixCancel.setDisable(true);

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
