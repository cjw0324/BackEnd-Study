package hello.upload.domain;

import lombok.Data;

@Data
public class UploadFile {
    private String uploadFileName;
    private String storeFileName;

    public UploadFile(String uploadFileName, String storeFileName) {
        this.uploadFileName = uploadFileName; //유저가 업로드한 파일명
        this.storeFileName = storeFileName;  //서버 내부에서 관리하는 파일명
    }
}
