package controllers;

import controllers.view.ApplicationViewModel;
import controllers.view.ApplicationViewModelMapper;
import models.Application;
import models.Platform;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationViewModelMapperTest {

    private ApplicationViewModelMapper mapper;

    @Before
    public void setUp() {
        mapper = new ApplicationViewModelMapper();
    }

    @Test
    public void mapIOSApplicationNameIsLeftIntact() {
        Application application = givenAnApplication(Platform.IOS);

        ApplicationViewModel viewModel = mapper.map(application);

        Assert.assertEquals(application.getAppPackage(), viewModel.getName());
    }

    @Test
    public void mapAndroidApplicationNameAddsAndroidSuffix() {
        Application application = givenAnApplication(Platform.ANDROID);

        ApplicationViewModel viewModel = mapper.map(application);

        Assert.assertEquals(application.getAppPackage() + Application.ANDROID_APPLICATION_SUFFIX, viewModel.getName());
    }

    private Application givenAnApplication(Platform platform) {
        Application application = new Application();
        application.setAppPackage("io.flowup.demo");
        application.setId(UUID.randomUUID());
        if (platform == Platform.IOS) {
            application.setAppPackage(application.getAppPackage() + Application.IOS_APPLICATION_SUFFIX);
        }
        return application;
    }
}
