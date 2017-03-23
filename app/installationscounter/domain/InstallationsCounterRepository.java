package installationscounter.domain;

import installationscounter.api.InstallationsCounterApiClient;
import play.cache.CacheApi;

import javax.inject.Inject;

public class InstallationsCounterRepository {

    private final CacheApi cache;
    private final InstallationsCounterApiClient apiClient;

    @Inject
    public InstallationsCounterRepository(CacheApi cache, InstallationsCounterApiClient apiClient) {
        this.cache = cache;
        this.apiClient = apiClient;
    }
}
