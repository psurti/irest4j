package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IConfig;
import com.lotuslabs.rest.interfaces.IRestClient;
import com.lotuslabs.rest.model.NamedJsonPathExpression;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class RestAction {
    private final String path;
    private final String name;
    private final Object body;
    private final Map<String, NamedJsonPathExpression> namedJsonPathExpressions;
    private final boolean encodePath;
    private final boolean encode;
    private final String host;
    private static final Pattern templates = Pattern.compile("\\{\\{([A-Za-z0-9.]+)(:*)([A-Za-z0-9.]*)\\}\\}");

    public RestAction(String name, IConfig config) {
        super();
        this.name = name;
        host = config.getHost();
        path = config.getPath(name);
        encode = config.isEncodeUrl(name);
        encodePath = config.isEncodePath(name);
        body = config.getBody(name);
        namedJsonPathExpressions = config.getJsonExps(name);
    }

    private String substituteVariables(Map<String, Object> context, String data) {
        AtomicReference<String> substitutedData = new AtomicReference<>(data);
        //-- substitute template variables
        final Matcher matcher = templates.matcher(substitutedData.get());
        final List<String[]> variableCtx = new ArrayList<>();
        while (matcher.find()) {
            final String variableName = matcher.group(1);
            final String separator = matcher.group(2);
            final String value = matcher.group(3);
            final String defaultValue = (!separator.equals(":")) ? null : value;
            if (variableName != null) {
                String variableValue = String.valueOf(context.getOrDefault(variableName, defaultValue));
                variableCtx.add(new String[]{variableName, separator, value, variableValue});
            }
        }
        variableCtx.forEach(t -> {
            String val = substitutedData.get().replaceAll("\\{\\{" + t[0] + t[1] + t[2] + "\\}\\}", t[3]);
            log.info("===>{}", val);
            substitutedData.set(val);
        });

        return substitutedData.get();
    }


    URI getURI(Map<String, Object> context) {
        //-- Return the substituted and encoded URI
        String encodedPath = substituteVariables(context, path);
        if (encodePath) {
            encodedPath = applyCustomEncodedQuery(encodedPath);
        }
        String url = host + encodedPath;
        return UriComponentsBuilder.fromUriString(url).build(encode).toUri();
    }

    /*
    TODO: This custom logic is for a specific case so move it out in the future
     */
    private String applyCustomEncodedQuery(String queryPath) {
        String queryFragment = extractQuery(queryPath);
        log.info("extracted fragment: {} ", queryFragment);
        FreeTextQuery q = new FreeTextQuery(queryFragment);
        final String customEncoded = q.apply(new URIQueryGenerator());
        log.info("custom encoded fragment: {}", customEncoded);
        return queryPath.replace("q=" + queryFragment, customEncoded);
    }

    private String extractQuery(String queryPath) {
        int beginIndex = queryPath.indexOf("q=");
        int endIndex = queryPath.indexOf("&", beginIndex);
        if (endIndex == -1) {
            return queryPath.substring(beginIndex + 2);
        }
        return queryPath.substring(beginIndex + 2, endIndex);
    }

    public abstract Map<String,?> execute(Map<String, Object> context,
                                          IRestClient<Map<String,?>, String> restClient) throws IOException;

    public Object getBody() {
        return body;
    }

    public String getBodyString(Map<String, Object> ctx) {
        if (getBody() == null)
            return null;
        final String val = String.valueOf(getBody());
        return substituteVariables(ctx, val);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .toString();
    }

    protected NamedJsonPathExpression[] getAccessTokenJsonPathExpression() {
        return new NamedJsonPathExpression[] {NamedJsonPathExpression.valueOf("bearer", "$.access_token")};
    }

    public NamedJsonPathExpression[] getNamedJsonPathExpression() {
        return namedJsonPathExpressions.values().toArray(new NamedJsonPathExpression[0]);
    }

    private interface QueryVisitor<T> {
        T visit(FreeTextQuery query);
    }
    private static class FreeTextQuery implements Function<QueryVisitor<String>, String> {
        private final String text;
        public FreeTextQuery(String text) {
            this.text = text;
        }

        @Override
        public String apply(QueryVisitor<String> tQueryVisitor) {
            return tQueryVisitor.visit(this);
        }

        public boolean isEmpty() {
            return text == null || text.isEmpty();
        }

        public String getText() {
            return text;
        }
    }

    private static final class URIQueryGenerator implements QueryVisitor<String> {
        private static final boolean encode = true;

        @Override
        public String visit(FreeTextQuery query) {
            if (query.isEmpty()) {
                return null;
            }

            StringBuilder builder = new StringBuilder("q=");
            String data = replaceQueryText(query);
            if (encode) {
                data = URLParamEncoder.encode(data);
            }
            builder.append(data);
            builder.append( "&");
            return builder.toString(); //I18NOK:LSM
        }

        private String replaceQueryText(FreeTextQuery query)
        {
            String ret = query.getText();
            ret = (ret == null || ret.isEmpty() ) ? "*:*" : ret;//can this be in the query
            return ret;
        }

        static class URLParamEncoder {

            public static String encode(String input) {
                StringBuilder resultStr = new StringBuilder();
                for (char ch : input.toCharArray()) {
                    if (isUnsafe(ch)) {
                        resultStr.append('%');
                        resultStr.append(toHex(ch / 16));
                        resultStr.append(toHex(ch % 16));
                    } else {
                        resultStr.append(ch);
                    }
                }
                return resultStr.toString(); //I18NOK:LSM
            }

            private static char toHex(int ch) {
                return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
            }

            private static boolean isUnsafe(char ch) {
                if (ch > 128 || ch < 0) {
                    return true;
                }
                return " %$&+,/:;=?@<>#%\"{}\\".indexOf(ch) >= 0;
            }

            private URLParamEncoder() {}
        }
    }

}
