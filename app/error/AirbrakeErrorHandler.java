package error;

import airbrake.AirbrakeNotice;
import airbrake.AirbrakeNoticeBuilder;
import airbrake.AirbrakeNotifier;
import airbrake.Backtrace;
import org.apache.commons.lang3.StringUtils;
import play.Configuration;
import play.api.UsefulException;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AirbrakeErrorHandler {
    private final Configuration configuration;

    @Inject
    public AirbrakeErrorHandler(@Named("airbrake") Configuration configuration) {
        this.configuration = configuration;
    }

    String getAirbrakeApiKey() {
        return configuration.getString("api_key");
    }

    public void logServerError(Http.RequestHeader request, UsefulException usefulException, play.Environment environment) {
        String apiKey = getAirbrakeApiKey();
        if (apiKey != null) {
            AirbrakeNotice notice = new AirbrakeNoticeBuilderForRequest(apiKey, usefulException.getCause(), environment.mode().name(), request).newNotice();
            AirbrakeNotifier notifier = new AirbrakeNotifier();
            notifier.notify(notice);
        }
    }
}

class AirbrakeNoticeBuilderForRequest extends AirbrakeNoticeBuilder {

    AirbrakeNoticeBuilderForRequest(String apiKey, Throwable throwable, String env, Http.RequestHeader request) {
        super(apiKey, new Backtrace(throwable), throwable, env);
        mapRequest(request);
        Map<String, Object> data = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.headers().entrySet()) {
            data.put(entry.getKey(), StringUtils.join(entry.getValue(), ";"));
        }

        super.request(data);
    }

    private void mapRequest(Http.RequestHeader request) {
        List<String> components = request.queryString().entrySet().stream()
                .map(entry -> {
                    return entry.getKey() + "=" + StringUtils.join(entry.getValue(), ",");
                })
                .collect(Collectors.toList());
        super.setRequest(request.path(), StringUtils.join(components, "&"));
    }
}