package sample;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;

public class ThisResource {
    ArrayList<String> resourceUrlFiles;
    private static final String mainDirName = "DataCleanWithImageNet";
    private static final String wrongImageSignatureFileName = "wrongImageSignature.txt";

    private static final String resourceDirName = "resourceDir";
    private static final String wordsTextName = "words.txt";


    private String projectDirName;
    private static final String projectWnidTextName = "wnid.txt";

    private static final String urlDirName = "urlCollection";
    private static final String urlMatchFileName = "url1.txt";
    private static final String urlUnmatchFileName = "url0.txt";

    private static final String imageDirName = "imageCollection";
    private static final String imageMatchedDirName = "image1";
    private static final String imageUnmatchedDirName = "image0";

    private static final String cleanDirName = "cleanData";

    private static final String hdf5DirName = "hdf5";

    public ThisResource() {
        this.projectDirName = "";
        resourceUrlFiles = new ArrayList<>();
        resourceUrlFiles.add("winter11_urls.txt");
        resourceUrlFiles.add("fall11_urls.txt");
        resourceUrlFiles.add("spring10_urls.txt");
        resourceUrlFiles.add("urls.txt");
    }

    public void setProjectDirName(String projectDirName) {
        this.projectDirName = projectDirName;
    }

    public String getProjectDirName() {
        return projectDirName;
    }

    public Path getMainDirPath() {
        return FileSystems.getDefault().getPath(mainDirName);
    }

    public Path getResourceDirPath() {
        return FileSystems.getDefault().getPath(mainDirName, resourceDirName);
    }

    public Path getResourceWordsTextPath() {
        return FileSystems.getDefault().getPath(mainDirName, resourceDirName, wordsTextName);
    }

    public Path resolveResourcePath(String filename) {
        return FileSystems.getDefault().getPath(mainDirName, resourceDirName, filename);
    }

    public Path resolveProjectPath(String filename) {
        this.setProjectDirName(filename);
        return FileSystems.getDefault().getPath(mainDirName, filename);
    }

    public Path getProjectWnidText() {
        return FileSystems.getDefault().getPath(mainDirName,
                projectDirName,
                projectWnidTextName);
    }

    public Path getUrlDirPath() {
        return FileSystems.getDefault().getPath(mainDirName,
                projectDirName,
                urlDirName);
    }

    public Path getImageDirPath() {
        return FileSystems.getDefault().getPath(mainDirName,
                projectDirName,
                imageDirName);
    }

    public Path getCleanDirPath() {
        return FileSystems.getDefault().getPath(mainDirName,
                projectDirName,
                cleanDirName);
    }

    public Path getHdf5DirPath() {
        return FileSystems.getDefault().getPath(mainDirName,
                projectDirName,
                hdf5DirName);
    }

    public Path getImageSubDirPath(int whichSubDir){
        if(whichSubDir == 1){
            return FileSystems.getDefault().getPath(mainDirName,
                    projectDirName,
                    imageDirName,
                    imageMatchedDirName);
        } else {
            return FileSystems.getDefault().getPath(mainDirName,
                    projectDirName,
                    imageDirName,
                    imageUnmatchedDirName);
        }
    }

    public Path getUrlSubFilePath(int whichSubDir){
        if(whichSubDir == 1){
            return FileSystems.getDefault().getPath(mainDirName,
                    projectDirName,
                    urlDirName,
                    urlMatchFileName);
        } else {
            return FileSystems.getDefault().getPath(mainDirName,
                    projectDirName,
                    urlDirName,
                    urlUnmatchFileName);
        }
    }

    public ArrayList<String> getResourceUrlFiles() {
        return resourceUrlFiles;
    }

    public Path getWrongImageSignaturePath() {
        return FileSystems.getDefault().getPath(mainDirName,
                resourceDirName,
                wrongImageSignatureFileName);
    }

    public ArrayList<String> getWrongImageSignatureData() {
        ArrayList<String> wrongImageSignatures = new ArrayList<>();
        wrongImageSignatures.add("\"f6d18cb7d3278352c657edd791e7467003d6b14bc2f56a97c9a944e84c564558\"");
        wrongImageSignatures.add("\"0a3713ad3928e3e6ec6c5a6dccd04297d637165a92a64d5aa54b3a68ba0f0fd3\"");
        wrongImageSignatures.add("\"2c2ab95e8049304ed0da185484320cdb87fb02e8ebfb26b6f39696d63fb78425\"");
        wrongImageSignatures.add("\"d5732e799919a7171409b53db26bcda3ed4dd09d0c20c20a0e967aa6f9dbd08f\"");
        wrongImageSignatures.add("\"1d05d573d9f044baad25f1c8b2ca6b15b03f532860dbb003042d55b40295aaf8\"");
        wrongImageSignatures.add("\"941e9faacf87dbe7991201319031f78ea19c3ffec84104610583b60a8c38d33f\"");
        wrongImageSignatures.add("\"81508168746b45588a0048d5ce8050527661583f9048866c571e46951a0982ce\"");
        wrongImageSignatures.add("\"7a6845f1f85318d5a54f06bad72c94aa3657f12e370a34e95750e83107db4e74\"");
        wrongImageSignatures.add("\"517f512ba1a8a58f380f4da6c2acb829f728dd5fdedbb47367d54a14ce25135b\"");
        wrongImageSignatures.add("\"f041a454914ef7438e1b35f3ae8e363a67329c85a7cc34a2b4e8ed096a92448c\"");
        wrongImageSignatures.add("\"6e03c75e69b8ca17cfcbaab4256f2ea44c519669201fcab38f0d054e00ceec76\"");
        wrongImageSignatures.add("\"b32a77873e3c29a8bef173a80217b91eae8bc4e679407fdafead226e5437448f\"");
        wrongImageSignatures.add("\"0ed3687aea0d2cdda34a1e126337fab2cd6720e1f21941816e37c2523f74d47f\"");
        wrongImageSignatures.add("\"602bf5518bf4f8c1acfd6e7d692bac40c347aeae8df94020a4bedb81a8187675\"");
        wrongImageSignatures.add("\"6063b581031cee0e0ae77f1a3f881a4ea0d22efdcbd56213c5aca0f374d33c27\"");
        wrongImageSignatures.add("\"ee4a1600fcd55ecf0cc2e40d24bb42365ef12b01741e5973a5dff295bb023d16\"");
        wrongImageSignatures.add("\"e2cd67fd55d09a1f4acbaf91c7e11f55056018f56f3099c8a1103b92521ae800\"");
        wrongImageSignatures.add("\"40295b615b2396b77c1aa2f1f87ba4f9258e6b589b239678716baeabd4aec528\"");
        wrongImageSignatures.add("\"91f80c9893c8225c48a3413439fe29baeb6b396e594cf0aead3f26cea0a6b484\"");
        wrongImageSignatures.add("\"d4a8e981330a2075aa5ca050f2fe41feb1968f8f3e1aff6246c2ca2abad946e5\"");
        wrongImageSignatures.add("\"d1835b986a82c66b2b3c53de30812b25552f20c832afd670627cbcb8636bcb78\"");
        wrongImageSignatures.add("\"b5c73ffd8726fe2dfb749e63edb5dc9f71e6c33f620ef4a9f6991ba891efb13b\"");
        wrongImageSignatures.add("\"6e5dfc6dbaf2eadebbaa39375d3fae6824c4323f75ff476af0831bf644579a4b\"");
        wrongImageSignatures.add("\"00a795a0b39d5660197c37355a924afba45d5264cd2e27879672afe2cf21a703\"");
        wrongImageSignatures.add("\"4043df6c0a8bb1c6ba02a0183f4d7894dd721f5e77d490c8a926fb10059fac97\"");
        wrongImageSignatures.add("\"8210c104521936d3a937add49e99f70e3e7f48bff01e54760c13ffce86d0f831\"");
        wrongImageSignatures.add("\"80cbe63642ee775d6abcc85cfd384ae70713570021e7c78936049d01a86e9e60\"");
        wrongImageSignatures.add("\"0d5435c09c5e12a433e4636271cf2b9d95778d59dac07a8d1db218cfe114a763\"");
        wrongImageSignatures.add("\"b01215a646a4a8b673317b3762d9a3f7b70dae26518f25b71db5c24587ba5c81\"");
        wrongImageSignatures.add("\"d4a8e981330a2075aa5ca050f2fe41feb1968f8f3e1aff6246c2ca2abad946e5\"");
        wrongImageSignatures.add("\"4a6f5cabfabc04581b387554af4b978e4f7b913a149b6abb81297a86dbf31ef4\"");
        wrongImageSignatures.add("\"008699c548c2a594b298c7d073e12c0db74755a1352495d5b87a9488c7992333\"");
        wrongImageSignatures.add("\"641d861e91e3b9ae0ed25535dd31a23e4d87d0470c99cfc8fce42c1af15aa803\"");
        wrongImageSignatures.add("\"fbb4d45d44531a677a44a84fa9661a162407a1b6dac61e6779aa70376f71aa0e\"");
        wrongImageSignatures.add("\"24351ff6d93a4c5d73f551ab9178c15e941aafa55f8ea1ca47ec9c2acec182b3\"");
        wrongImageSignatures.add("\"abbca7e810a3629fce0fc0b50daba9e3094ce56486fb65e9fc24dc6351711c64\"");
        wrongImageSignatures.add("\"21bd8cef75b8317a1c659f8d5882ec4b385b6fa280d5298cb0abdc153e771d94\"");
        wrongImageSignatures.add("\"8ef20b2857300b7dd52862da9a4b39dfc944a125a6334c54f8c95e21d68a015f\"");
        wrongImageSignatures.add("\"58d69302c3bd2c5a213d4b6e51fc22ba5fd7ef1c7e256b17393bbd91a6f83a36\"");
        wrongImageSignatures.add("\"3be78494a76030e36ecf1f4bcbd0507bd8dea9546dbaab9c7a71fcfbb9c79ab7\"");
        wrongImageSignatures.add("\"6130ef21e32dc88ff94b7f7b55eddea00e3057b0cc074a2bec2ab33d76e4f1b3\"");
        wrongImageSignatures.add("\"5c76bf8db200e8123be6a615327b961cd5e64092aeb9c91c3ddbf29f1ef0dcdf\"");
        wrongImageSignatures.add("\"d994ec156af568814eb5ea0e2cdaa2ee2692bed25bb1e0a13d97ae9938466a96\"");
        wrongImageSignatures.add("\"986a97af02cf170514aeb6e9f95706e6d586ec0eb65ae425b95b2dfbbee8653e\"");
        wrongImageSignatures.add("\"e0fdf87fb537421c2be8253fd2c8b429cb1f724affaab9d23e1394ff68977614\"");
        wrongImageSignatures.add("\"9f1898269883cb94ad12f313b1f8f4624e2617fb57fe42952da5e9b6dff58233\"");
        wrongImageSignatures.add("\"53b30cc9122369263fcc31a5eaf136b0ece99e3b3d53b13e824fb45af2bd316a\"");
        wrongImageSignatures.add("\"e61070fb2041d2c06582cd7ec9358d85c23fe180e10beac018bb3add959fe3a1\"");
        wrongImageSignatures.add("\"a3ff2fbcf3a8dd6d6066b8ba0fd98f1b4d19fa943c8fbb615513e69abc73e01b\"");
        wrongImageSignatures.add("\"f3d7ba06070c020f658c6614fdaed576dd9b4c89e0abf03a202a75523103786c\"");
        wrongImageSignatures.add("\"1e701cb41e884cf4042c4137f7eca4025d18484e147c768c5a908562c9cc38bb\"");
        wrongImageSignatures.add("\"a1bed26ee065f456c8773463663506d067af5ff8f9061030c4d937b7d9da233a\"");

        return wrongImageSignatures;
    }

}
