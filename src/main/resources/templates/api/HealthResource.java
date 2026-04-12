package {{PACKAGE}}.api;

import ca.weblite.teavmlambda.api.Response;
import ca.weblite.teavmlambda.api.annotation.*;
{{#DATABASE}}
import ca.weblite.teavmlambda.api.db.Database;
{{/DATABASE}}

@Path("/health")
@Component
@Singleton
@PermitAll
public class HealthResource {
{{#DATABASE}}
    private final Database database;

    @Inject
    public HealthResource(Database database) {
        this.database = database;
    }

    @GET
    public Response check() {
        database.query("SELECT 1");
        return Response.ok("{\"status\":\"ok\"}")
                .header("Content-Type", "application/json");
    }
{{/DATABASE}}
{{^DATABASE}}
    @Inject
    public HealthResource() {
    }

    @GET
    public Response check() {
        return Response.ok("{\"status\":\"ok\"}")
                .header("Content-Type", "application/json");
    }
{{/DATABASE}}
}
