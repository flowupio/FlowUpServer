package usecases.models;

import lombok.Data;

import java.util.List;

@Data
public class AndroidSDKVersion {

    private MavenRepositoryResponse response;

    public String getLatestVersion() {
        return response.getDocs().get(0).getLatestVersion();
    }

    @Data
    private class MavenRepositoryResponse {
        private List<MavenRepositoryDocument> docs;
    }

    @Data
    private class MavenRepositoryDocument {
        private String latestVersion;
    }

}
