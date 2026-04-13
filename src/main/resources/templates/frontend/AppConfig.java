package {{PACKAGE}}.frontend;

import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.NodeList;

public class AppConfig {

    public static String apiBaseUrl() {
        return readMeta("api-base-url", "http://localhost:8080");
    }
{{#AUTH}}

    public static String firebaseApiKey() {
        return readMeta("firebase-api-key", "");
    }

    public static String firebaseAuthDomain() {
        return readMeta("firebase-auth-domain", "");
    }

    public static String firebaseProjectId() {
        return readMeta("firebase-project-id", "demo-project");
    }

    public static String firebaseStorageBucket() {
        return readMeta("firebase-storage-bucket", "");
    }

    public static String firebaseMessagingSenderId() {
        return readMeta("firebase-messaging-sender-id", "");
    }

    public static String firebaseAppId() {
        return readMeta("firebase-app-id", "");
    }
{{/AUTH}}

    private static String readMeta(String name, String defaultValue) {
        NodeList metas = HTMLDocument.current().getElementsByTagName("meta");
        for (int i = 0; i < metas.getLength(); i++) {
            HTMLElement el = (HTMLElement) metas.item(i);
            if (name.equals(el.getAttribute("name"))) {
                String content = el.getAttribute("content");
                return content != null && !content.isEmpty() ? content : defaultValue;
            }
        }
        return defaultValue;
    }
}
