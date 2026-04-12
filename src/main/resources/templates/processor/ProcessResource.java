package {{PACKAGE}}.{{PROCESSOR_PACKAGE_SEGMENT}};

import ca.weblite.teavmlambda.api.Request;
import ca.weblite.teavmlambda.api.Response;
import ca.weblite.teavmlambda.api.annotation.*;

@Path("/process")
@Component
@Singleton
@PermitAll
public class ProcessResource {

    @Inject
    public ProcessResource() {
    }

    @POST
    public Response process(Request request) {
        String body = request.body();
        // TODO: Implement {{PROCESSOR_NAME}} processing logic

        return Response.ok("{\"status\":\"processed\"}")
                .header("Content-Type", "application/json");
    }
}
