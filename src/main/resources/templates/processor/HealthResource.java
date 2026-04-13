package {{PACKAGE}}.{{PROCESSOR_PACKAGE_SEGMENT}};

import ca.weblite.teavmlambda.api.Response;
import ca.weblite.teavmlambda.api.annotation.*;

@Path("/health")
@Component
@Singleton
@PermitAll
public class HealthResource {

    @Inject
    public HealthResource() {
    }

    @GET
    public Response check() {
        return Response.ok("{\"status\":\"ok\"}")
                .header("Content-Type", "application/json");
    }
}
