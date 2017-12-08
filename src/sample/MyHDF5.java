package sample;

import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.exceptions.HDF5Exception;
import hdf.hdf5lib.exceptions.HDF5LibraryException;

import java.nio.file.Path;
import java.util.ArrayList;

public class MyHDF5 {

    private Path filePath;
    private int file_id;
    private ArrayList<MyHDF5DataStructure> dataSets;

    public MyHDF5(Path filePath) {
        this.filePath = filePath;
        this.file_id = -1;
        dataSets = new ArrayList<>();
    }

    /**
     * Create a new file using default properties.
     *
     * @return whether is success or not
     */
    public boolean createNewDefaultFile() {
        if(file_id >=0){
            System.out.println("File already created.");
            return false;
        }
        try {
            file_id = H5.H5Fcreate(filePath.toString(), HDF5Constants.H5F_ACC_TRUNC,
                    HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to create file:" + filePath.toString());
            return false;
        }
    }

    /**
     * Create a new dataset
     *
     * @param dims An array of the size of each dimension
     * @return if >= 0 : the index of data space
     *         else    : the error ojf status
     */
    public int createNewDataSet(long[] dims, String datasetName) {

        if(getIndexOfTheDatasetName(datasetName) >= 0){
            System.out.println(datasetName + "already exist.");
            return -1;
        }

        int dataspace_id = -1;

        // Create the data space for the dataset.
        try {
            dataspace_id = H5.H5Screate_simple(dims.length, dims, null);
        }
        catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        int dataset_id = -1;

        // create integer dataset
        try {
            if ((file_id >= 0) && (dataspace_id >= 0)) {
                dataset_id = H5.H5Dcreate(file_id, datasetName,
                        HDF5Constants.H5T_NATIVE_CHAR, dataspace_id,
                        HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return -2;
        }

        MyHDF5DataStructure newDataset = new MyHDF5DataStructure(datasetName);
        newDataset.setDataspaceID(dataspace_id);
        newDataset.setDatasetID(dataset_id);
        newDataset.setDim(dims);
        dataSets.add(newDataset);

        return dataspace_id;

    }

    /**
     * Close all dataspace_id and the file
     *
     * @return whether is success or not
     */
    public boolean close() {

        // Terminate access to the data space and data set.
        for(MyHDF5DataStructure dataset: dataSets) {
            try {
                if (dataset.getDataspaceID() >= 0)
                    H5.H5Sclose(dataset.getDataspaceID());
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (dataset.getDataspaceID() >= 0)
                    H5.H5Dclose(dataset.getDatasetID());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Close the file.
        try {
            if (file_id >= 0)
                H5.H5Fclose(file_id);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     *  Write data to a part of the dataset
     *
     * @param datasetName Name of the dataset
     * @param offset the position of address want to locate on dataset
     * @param data the data want be write in the dataset
     * @return
     */
    public boolean modifyPartOfTheDataspace(String datasetName, long offset, byte[] data) {

        int indexOfTheDataset = getIndexOfTheDatasetName(datasetName);

        if(indexOfTheDataset == -1){
            System.out.println("There is no dataset naming " + datasetName);
            return false;
        }

        MyHDF5DataStructure theDataset = dataSets.get(indexOfTheDataset);
        long[] dim = theDataset.getDim();

        long[] start = new long[dim.length];     // Offset of start of hyperslab
        long[] stride = new long[dim.length];    // Hyperslab stride
        long[] count = new long[dim.length];     // Number of blocks included in hyperslab
        long[] block = new long[dim.length];     // Size of block in hyperslab

//        initial
        start[0] = offset;
        count[0] = 1;
        stride[0] = 1;
        block[0] = 1;
        for(int len=1; len < dim.length; len++) {
            start[len] = 0;
            count[len] = dim[len];
            stride[len] = 1;
            block[len] = 1;

        }

        // 這裡很微妙，只是執行函式，沒有紀錄，接下來也能知道要處理那一區塊
        // Get the subset dataspace
        int status = -1;
        try {
            status = H5.H5Sselect_hyperslab(theDataset.getDataspaceID(),
                    HDF5Constants.H5S_SELECT_SET,
                    start,
                    stride,
                    count,
                    block);

        } catch (HDF5LibraryException e) {
            e.printStackTrace();
        }

        int memspace_id = -1;

        try {
            memspace_id = H5.H5Screate_simple(count.length, count, null);
        } catch (HDF5Exception e) {
            e.printStackTrace();
        }


        // Write the dataset.
        try {
            if (status >= 0 && memspace_id >=0)
                H5.H5Dwrite(theDataset.getDatasetID(),
                        HDF5Constants.H5T_NATIVE_CHAR,
                        memspace_id,
                        theDataset.getDataspaceID(),
                        HDF5Constants.H5P_DEFAULT,
                        data);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    public boolean writeDataset(String datasetName, byte[] data){
        int indexOfTheDataset = getIndexOfTheDatasetName(datasetName);

        if(indexOfTheDataset == -1){
            System.out.println("There is no dataset naming " + datasetName);
            return false;
        }

        MyHDF5DataStructure theDataset = dataSets.get(indexOfTheDataset);

        // Write the dataset.
        try{
            H5.H5Dwrite(theDataset.getDatasetID(),
                    HDF5Constants.H5T_NATIVE_CHAR,
                    theDataset.getDataspaceID(),
                    theDataset.getDataspaceID(),
                    HDF5Constants.H5P_DEFAULT,
                    data);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Get index of the dataset name
     *
     * @param datasetName the name of dataset
     * @return the index of the dataset id
     */
    private int getIndexOfTheDatasetName(String datasetName) {

        return dataSets.indexOf(new MyHDF5DataStructure(datasetName));
    }
}
