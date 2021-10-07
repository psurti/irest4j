package com.lotuslabs.rest.model;

import com.lotuslabs.rest.interfaces.IConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class RestContext {
    private static final Pattern templates = Pattern.compile("\\{\\{([A-Za-z0-9._]+)(:*)([A-Za-z0-9.]*)\\}\\}");

    private final Map<String, Object> context = new LinkedHashMap<>();
    private final IConfig config;
    private static final String SEQ_ID = "ctx.seq.id";

    public RestContext(IConfig config) {
        final Map<String, String> collect = config.getContext().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, v -> substituteVariables(v.getValue())));
        context.putAll(collect);
        log.debug("configuration:" + new TreeMap<>(collect));
        this.config = config;
    }

    private String substituteVariables(String data) {
        if (data == null)
            return null;
        AtomicReference<String> substitutedData = new AtomicReference<>(data);
        //-- substitute template variables
        final Matcher matcher = templates.matcher(substitutedData.get());
        final List<String[]> variableCtx = new ArrayList<>();
        while (matcher.find()) {
            final String variableName = matcher.group(1);
            final String separator = matcher.group(2);
            final String value = matcher.group(3);
            final String defaultValue = (!separator.equals(":")) ? System.getenv(variableName) : value;
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

    public URI getURI(String name) {
        //-- Return the substituted and encoded URI
        String encodedPath = substituteVariables(config.getPath(name));
        if (config.isEncodePath(name)) {
            encodedPath = applyCustomEncodedQuery(encodedPath);
        }
        String url = substituteVariables(config.getHost(name)) + encodedPath;
        return UriComponentsBuilder.fromUriString(url).build(config.isEncodeUrl(name)).toUri();
    }

    public boolean ignoreFailure(String name) {
        return this.config.ignoreFailure(name);
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

    public Object getBody(String name) {
        return config.getBody(name);
    }

    public String getBodyString(String name) {
        if (getBody(name) == null)
            return null;
        final String val = String.valueOf(getBody(name));
        return substituteVariables(val);

    }

    public NamedJsonPathExpression[] getAccessTokenJsonPathExpression() {
        return new NamedJsonPathExpression[]{NamedJsonPathExpression.valueOf("bearer", "$.access_token")};
    }

    public NamedJsonPathExpression[] getNamedJsonPathExpression(String name) {
        Collection<NamedJsonPathExpression> ret = config.getJsonExps(name).values();
        ret.forEach(n -> n.setResolvedExpectedValue(substituteVariables(n.getResolvedExpectedValue())));
        return ret.toArray(new NamedJsonPathExpression[0]);
    }

    public void updateContext(Map<String, ?> ctx) {
        context.putAll(ctx);
    }

    @SuppressWarnings("unchecked")
    public List<Object> getEtag() {
        List<Object> val = (List<Object>) context.remove(HttpHeaders.ETAG);
        if (val == null) {
            //non-convention - Etag
            val = (List<Object>) context.remove("Etag");
            if (val == null) {
                val = new ArrayList<>(0);
            }
        }
        return val;
    }

    public String getEtagStrValue() {
        String eTag = null;
        final List<Object> valueList = getEtag();
        if (!valueList.isEmpty()) {
            final Object o = valueList.get(0);
            if (o != null) {
                eTag = o.toString();
                //eTag = eTag.substring(1, eTag.length() - 1); //hack
                log.warn("etag:{}", eTag);
            }
        }
        return eTag;
    }

    public void updateSequenceId() {
        context.put(SEQ_ID, System.currentTimeMillis());
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
                data = URIQueryGenerator.URLParamEncoder.encode(data);
            }
            builder.append(data);
            builder.append("&");
            return builder.toString(); //I18NOK:LSM
        }

        private String replaceQueryText(FreeTextQuery query) {
            String ret = query.getText();
            ret = (ret == null || ret.isEmpty()) ? "*:*" : ret;//can this be in the query
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

            private URLParamEncoder() {
            }
        }
    }
}
