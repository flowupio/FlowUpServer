package controllers.view;

import models.Application;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicationViewModelMapper {

    public List<ApplicationViewModel> map(List<Application> applications) {
        return applications.stream().map(this::map).collect(Collectors.toList());
    }

    public ApplicationViewModel map(Application application) {
        return new ApplicationViewModel(
                application.getId().toString(),
                mapName(application)
        );
    }

    private String mapName(Application application) {
        if (application.getAppPackage().endsWith(Application.IOS_APPLICATION_SUFFIX)) {
            return application.getAppPackage();
        } else {
            return application.getAppPackage() + Application.ANDROID_APPLICATION_SUFFIX;
        }
    }
}
