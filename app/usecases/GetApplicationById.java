package usecases;

import models.Application;
import usecases.repositories.ApplicationRepository;

import javax.inject.Inject;
import java.util.UUID;

public class GetApplicationById {
    private final ApplicationRepository applicationRepository;

    @Inject
    public GetApplicationById(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public Application execute(UUID id) {
        return applicationRepository.findById(id);
    }
}
