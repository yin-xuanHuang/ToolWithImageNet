package sample;

public class FileNameWithLabel {

    private final String fileName;
    private final byte label;

    public FileNameWithLabel(String fileName, byte label) {
        this.fileName = fileName;
        this.label = label;
    }

    public String getFileName() {
        return fileName;
    }

    public byte getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "FileNameWithLabel{" +
                "fileName='" + fileName + '\'' +
                ", label=" + label +
                '}';
    }
}
