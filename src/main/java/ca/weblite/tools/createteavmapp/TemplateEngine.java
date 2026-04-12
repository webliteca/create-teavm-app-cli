package ca.weblite.tools.createteavmapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class TemplateEngine {

    private static final Pattern CONDITIONAL_PATTERN = Pattern.compile(
            "\\{\\{#(\\w+)}}(.*?)\\{\\{/\\1}}", Pattern.DOTALL
    );

    private static final Pattern NEGATED_PATTERN = Pattern.compile(
            "\\{\\{\\^(\\w+)}}(.*?)\\{\\{/\\1}}", Pattern.DOTALL
    );

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(
            "\\{\\{(\\w+)}}"
    );

    private TemplateEngine() {}

    public static String loadResource(String resourcePath) throws IOException {
        try (InputStream is = TemplateEngine.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Template not found: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }

    public static String process(String template, Map<String, String> variables, Map<String, Boolean> conditions) {
        String result = template;

        // Process conditionals iteratively until stable (handles nesting)
        String previous;
        do {
            previous = result;
            result = processConditionals(result, conditions);
            result = processNegated(result, conditions);
        } while (!result.equals(previous));

        // Replace placeholders
        result = replacePlaceholders(result, variables);

        // Clean up blank lines left by removed conditional blocks
        result = result.replaceAll("(?m)^\\s*\n(?=\\s*\n)", "");

        return result;
    }

    private static String processConditionals(String text, Map<String, Boolean> conditions) {
        Matcher matcher = CONDITIONAL_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String condName = matcher.group(1);
            String content = matcher.group(2);
            boolean include = conditions.getOrDefault(condName, false);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(include ? content : ""));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String processNegated(String text, Map<String, Boolean> conditions) {
        Matcher matcher = NEGATED_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String condName = matcher.group(1);
            String content = matcher.group(2);
            boolean include = !conditions.getOrDefault(condName, false);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(include ? content : ""));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String replacePlaceholders(String text, Map<String, String> variables) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = variables.getOrDefault(key, matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
