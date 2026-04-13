package {{PACKAGE}}.frontend;

import ca.weblite.teavmreact.core.*;
import ca.weblite.teavmreact.hooks.*;
import static ca.weblite.teavmreact.html.Html.*;

import org.teavm.jso.JSObject;
import org.teavm.jso.dom.html.HTMLDocument;

public class App {
    public static void main(String[] args) {
        var root = ReactDOM.createRoot(HTMLDocument.current().getElementById("root"));
        JSObject app = React.wrapComponent(App::renderApp, "App");
        root.render(React.createElement(app, null));
    }

    static ReactElement renderApp(JSObject props) {
{{#AUTH}}
        StateHandle<Boolean> loggedIn = Hooks.useState(false);
{{/AUTH}}
        StateHandle<String> currentPage = Hooks.useState("home");

        return div(
                nav(
                        h1("{{APP_NAME_CAMEL}}").className("app-title").build(),
                        div(
                                button("Home").onClick(e -> currentPage.set("home")).className("nav-btn").build(),
                                button("About").onClick(e -> currentPage.set("about")).className("nav-btn").build()
{{#AUTH}}
                                , loggedIn.get() != null && loggedIn.getBool()
                                        ? button("Logout").onClick(e -> loggedIn.setBool(false)).className("nav-btn").build()
                                        : button("Login").onClick(e -> loggedIn.setBool(true)).className("nav-btn").build()
{{/AUTH}}
                        ).className("nav-links").build()
                ).className("navbar").build(),
                div(
                        renderPage(currentPage.get())
                ).className("content").build()
        ).className("app").build();
    }

    static ReactElement renderPage(String page) {
        return switch (page) {
            case "about" -> div(
                    h1("About").build(),
                    p("{{DESCRIPTION}}").build()
            ).className("page").build();
            default -> div(
                    h1("Welcome to {{APP_NAME_CAMEL}}").build(),
                    p("Your application is running!").build()
            ).className("page").build();
        };
    }
}
