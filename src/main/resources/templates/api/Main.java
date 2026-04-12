package {{PACKAGE}}.api;

import ca.weblite.teavmlambda.api.Container;
import ca.weblite.teavmlambda.api.Platform;
import ca.weblite.teavmlambda.api.Router;
import ca.weblite.teavmlambda.api.MiddlewareRouter;
import ca.weblite.teavmlambda.api.middleware.CorsMiddleware;
{{#DATABASE}}
import ca.weblite.teavmlambda.api.db.Database;
import ca.weblite.teavmlambda.api.db.DatabaseFactory;
{{/DATABASE}}
{{#AUTH}}
import ca.weblite.teavmlambda.api.auth.JwtValidator;
import ca.weblite.teavmlambda.api.auth.JwtValidatorFactory;
{{/AUTH}}
import ca.weblite.teavmlambda.generated.GeneratedContainer;
import ca.weblite.teavmlambda.generated.GeneratedRouter;

public class Main {
    public static void main(String[] args) {
{{#DATABASE}}
        String dbUrl = Platform.env("DATABASE_URL", "postgresql://postgres:postgres@localhost:5432/{{APP_NAME_UNDERSCORE}}");
{{/DATABASE}}

        Container container = new GeneratedContainer();
{{#DATABASE}}
        container.register(Database.class, DatabaseFactory.create(dbUrl));
{{/DATABASE}}
{{#AUTH}}
        container.register(JwtValidator.class, JwtValidatorFactory.create());
{{/AUTH}}

        Router router = new MiddlewareRouter(new GeneratedRouter(container))
                .use(CorsMiddleware.builder()
                        .allowCredentials(true)
                        .build());

        Platform.start(router);
    }
}
