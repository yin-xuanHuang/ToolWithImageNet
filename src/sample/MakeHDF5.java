package sample;


import javafx.concurrent.Task;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MakeHDF5 extends Task<Void> {

    private ThisResource resource;
    private MyHDF5 myHDF5;
    private final String hdf5FileName;

    private List<FileNameWithLabel> fileNameWithLabels;

    private Size imageSize;
    private final int width;
    private final int imageChannels = 3;
    private final int cellsOfImage;

    private int numberOfImages;


    public MakeHDF5(ThisResource resource, int width) {
        this.width = width;
        this.imageSize = new Size(width, width);
        this.cellsOfImage = width * width * imageChannels;

        this.resource = resource;
        this.hdf5FileName = resource.getProjectDirName() + "_" + width + "x" + width + ".h5";
        this.myHDF5 = new MyHDF5(resource.getHdf5DirPath().resolve(this.hdf5FileName));
        this.fileNameWithLabels = new ArrayList<>();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        updateMessage("準備中。。。");
    }

    @Override
    protected Void call() throws Exception {

//        create HDF5 directory
        Path hdf5DirPath = resource.getHdf5DirPath();
        if(!Files.exists(hdf5DirPath))
            Files.createDirectory(hdf5DirPath);

        readFileNameAndLabels();

        if(!createHDF5File()){
            return null;
        }

        writeHDF5AndClose();

        return null;
    }

    /**
     * Create HDF5 file, and initial datasets.
     *
     * @return boolean whether is success or not
     * @throws Exception
     */
    private boolean createHDF5File() throws Exception{

        if(myHDF5.createNewDefaultFile()) {

            int trainDataLength = this.getTrainDataLength();
            int testDataLength = this.getTestDataLength();

            long[] trainLabelDim = {trainDataLength};
            long[] testLabelDim = {testDataLength};
            long[] trainDataDim = {trainDataLength, this.width, this.width, this.imageChannels};
            long[] testDataDim = {testDataLength, this.width, this.width, this.imageChannels};


            if(myHDF5.createNewDataSet(trainLabelDim, "train_labels") < 0){
                System.out.println("create dataset problem.");
            }
            if(myHDF5.createNewDataSet(testLabelDim, "test_labels") < 0){
                System.out.println("create dataset problem.");
            }
            if(myHDF5.createNewDataSet(trainDataDim, "train_img") < 0){
                System.out.println("create dataset problem.");
            }
            if(myHDF5.createNewDataSet(testDataDim, "test_img") < 0){
                System.out.println("create dataset problem.");
            }

            return true;

        } else {
            System.out.println("create hdf5 file problem.");
            return false;
        }

    }

    /**
     *  Create a list which contains all fileNames and labels.
     *  (simulating python's zip)
     */
    private void readFileNameAndLabels(){
        String[] listOfImageNames = getListOfImageNames();
        this.numberOfImages=listOfImageNames.length;

        byte label;
        for(int i=0; i<this.numberOfImages; i++){
            if(listOfImageNames[i].contains("image0")) {
                label = 0;
            } else {
                label = 1;
            }
            this.fileNameWithLabels.add(new FileNameWithLabel(listOfImageNames[i], label));
        }
    }

    /**
     *  Get all images in clean directory.
     *
     * @return String Array of all image fileNames
     */
    private String[] getListOfImageNames(){
        return this.resource.getCleanDirPath().toFile().list();
    }

    /**
     * The proportion of train data on separating images' amount
     *
     * TODO 0.8
     *
     * @return separating number of total images' amount
     */
    private int getTrainDataLength(){
        return (int)(this.fileNameWithLabels.size() * 0.8);
    }

    /**
     * The proportion of test data on separating images' amount
     *
     * TODO 0.2
     *
     * @return separating number of total images' amount
     */
    private int getTestDataLength(){
        return (this.fileNameWithLabels.size() - this.getTrainDataLength());
    }

    /**
     * Image processing(read, resize,and color convert) and transform to byte array
     *
     * @param index of the file in clean directory
     * @return byte array of the image
     */
    private byte[] imageProcessing(int index) throws Exception{

        Mat image = Imgcodecs.imread(this.resource.getCleanDirPath().resolve(this.fileNameWithLabels.get(index).getFileName()).toString());
        Mat dst = new Mat();

        Imgproc.resize(image, dst, this.imageSize, 0, 0, Imgproc.INTER_LINEAR);
        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_BGR2RGB);

        MatOfInt rgb = new MatOfInt(CvType.CV_8S);
        dst.convertTo(rgb, CvType.CV_8S);

        byte[] rgba = new byte[(int)(rgb.total() * rgb.channels())];
        rgb.get(0, 0, rgba);

        return rgba;
    }

    /**
     *
     * Step - : processing images by imageProcessing().
     *
     * Step - : write byte array of a image to the dataset(HDF5 file).
     *
     * @throws Exception
     */
    private void writeHDF5AndClose() throws Exception{

        int trainDataLength = this.getTrainDataLength();
        byte[] trainLabel = new byte[trainDataLength];
        byte[] testLabel = new byte[this.numberOfImages - trainDataLength];

        for(int i=0; i<this.numberOfImages; i++) {

            byte[] byteOfImage = imageProcessing(i);

            if(i < trainDataLength){
                if (!myHDF5.modifyPartOfTheDataspace("train_img", i, byteOfImage)) {
                    System.out.println("modify data problem");
                }
                trainLabel[i] = this.fileNameWithLabels.get(i).getLabel();
            } else {
                if (!myHDF5.modifyPartOfTheDataspace("test_img", i - trainDataLength, byteOfImage)) {
                    System.out.println("modify data problem");
                }
                testLabel[i-trainDataLength] = this.fileNameWithLabels.get(i).getLabel();
            }

            updateProgress(i, this.numberOfImages);
            updateMessage("ImageData: " + i + " / " + this.numberOfImages);

        }

        updateProgress(0, 2);
        updateMessage("ImageLabel dataset: 0 / 2");
        if (!myHDF5.writeDataset("train_labels", trainLabel)) {
            System.out.println("modify data problem");
        }
        updateProgress(1, 2);
        updateMessage("ImageLabel dataset: 1 / 2");
        if (!myHDF5.writeDataset("test_labels", testLabel)) {
            System.out.println("modify data problem");
        }
        updateProgress(2, 2);
        updateMessage("ImageLabel dataset: 2 / 2");


        // close file resource
        if(!myHDF5.close()){
            System.out.println("close problem.");
        }

    }
}
