package {{PACKAGE}}.frontend;

import ca.weblite.teavmreact.core.*;
import ca.weblite.teavmreact.hooks.*;
import static ca.weblite.teavmreact.html.Html.*;
import ca.weblite.teavmreact.html.DomBuilder.*;

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

        return Div.create().className("app")
                .child(Nav.create().className("navbar")
                        .child(H1.create().text("{{APP_NAME_CAMEL}}").className("app-title").build())
                        .child(Div.create().className("nav-links")
                                .child(button("Home").onClick(e -> currentPage.set("home")).className("nav-btn").build())
                                .child(button("About").onClick(e -> currentPage.set("about")).className("nav-btn").build())
{{#AUTH}}
                                .child(loggedIn.get() != null && loggedIn.getBool()
                                        ? button("Logout").onClick(e -> loggedIn.setBool(false)).className("nav-btn").build()
                                        : button("Login").onClick(e -> loggedIn.setBool(true)).className("nav-btn").build())
{{/AUTH}}
                                .build())
                        .build())
                .child(Div.create().className("content")
                        .child(renderPage(currentPage.get()))
                        .build())
                .build();
    }

    static ReactElement renderPage(String page) {
        return switch (page) {
            case "about" -> Div.create().className("page")
                    .child(H1.create().text("About").build())
                    .child(P.create().text("{{DESCRIPTION}}").build())
                    .build();
            default -> Div.create().className("page")
                    .child(H1.create().text("Welcome to {{APP_NAME_CAMEL}}").build())
                    .child(P.create().text("Your application is running!").build())
                    .build();
        };
    }
}
