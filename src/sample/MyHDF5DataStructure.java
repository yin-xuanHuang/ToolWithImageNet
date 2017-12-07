package sample;

public class MyHDF5DataStructure {
    private int dataspaceID;
    private int datasetID;
    private final String datasetName;
    private long[] dim;

    public MyHDF5DataStructure(String datasetName) {
        this.datasetName = datasetName;
    }

    public void setDataspaceID(int dataspaceID) {
        this.dataspaceID = dataspaceID;
    }
    public int getDataspaceID() {
        return dataspaceID;
    }

    public int getDatasetID() {
        return datasetID;
    }
    public void setDatasetID(int datasetID) {
        this.datasetID = datasetID;
    }

    public void setDim(long[] dim) {
        this.dim = dim;
    }
    public long[] getDim() {
        return dim;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyHDF5DataStructure myHDF5DataStructure = (MyHDF5DataStructure) o;

        return datasetName.equals(myHDF5DataStructure.datasetName);
    }

    @Override
    public int hashCode() {
        return datasetName.hashCode();
    }
}
