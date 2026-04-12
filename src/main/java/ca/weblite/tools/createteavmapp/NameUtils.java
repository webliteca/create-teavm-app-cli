package ca.weblite.tools.createteavmapp;

public final class NameUtils {

    private NameUtils() {}

    public static String toPascalCase(String kebab) {
        StringBuilder sb = new StringBuilder();
        for (String part : kebab.split("-")) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    sb.append(part.substring(1).toLowerCase());
                }
            }
        }
        return sb.toString();
    }

    public static String toUnderscore(String kebab) {
        return kebab.replace('-', '_');
    }

    public static String toPackagePath(String pkg) {
        return pkg.replace('.', '/');
    }

    public static String deriveGroupId(String packageName) {
        int lastDot = packageName.lastIndexOf('.');
        return lastDot > 0 ? packageName.substring(0, lastDot) : packageName;
    }

    public static String lastSegment(String packageName) {
        int lastDot = packageName.lastIndexOf('.');
        return lastDot > 0 ? packageName.substring(lastDot + 1) : packageName;
    }
}
