package controller;

import exception.ControllerNotFoundException;
import exception.ResourceTypeNotFoundException;
import http.request.HttpRequest;
import http.request.HttpUri;
import http.request.RequestLine;
import http.response.HttpResponse;
import http.response.HttpResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ControllerHandler {
    private static final Logger logger = LoggerFactory.getLogger(ControllerHandler.class);

    private static List<Controller> controllers = new ArrayList<>();

    static {
        controllers.add(new UserController());
    }

    public static HttpResponse handle(HttpRequest httpRequest) throws Exception {
        try {
            Controller controller = findController(httpRequest);
            return controller.doService(httpRequest);
        } catch (ResourceTypeNotFoundException | ControllerNotFoundException e) {
            logger.error(e.getMessage());
            return HttpResponseFactory.NOT_FOUND(e.getMessage());
        }
    }

    public static Controller findController(HttpRequest httpRequest) {
        RequestLine requestLine = httpRequest.getRequestLine();
        HttpUri httpUri = requestLine.getHttpUri();
        Controller controller = controllers
                .stream()
                .filter(con -> con.isMatch(httpRequest))
                .findFirst()
                .orElse(null);
        if (Objects.nonNull(controller)) {
            return controller;
        }
        if (httpUri.isEndWithResourceType()) {
            controller = new StaticController();
        }
        if(Objects.isNull(controller)) {
            throw new ControllerNotFoundException("Not Found Controller");
        }

        return controller;
    }
}
