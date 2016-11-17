package usecases.models;


import lombok.Data;

import java.util.List;

@Data
public class AndroidSDKVersion {

    private MavenRepositoryResponse response;

    public String getLatestVersion() {
        if(response.getDocs().isEmpty()){
            return null;
        }
        return response.getDocs().get(0).getLatestVersion();
    }


}

@Data
class MavenRepositoryResponse {

    private List<MavenRepositoryDocument> docs;

}

@Data
class MavenRepositoryDocument {

    private String latestVersion;

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }
}
