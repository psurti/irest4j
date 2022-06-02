package com.lotuslabs.rest.adapters.spel;

import org.json.JSONObject;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class ExpTest {
    static final String[] vars = new String[] {
            "#json.glossary.title",
            "#json.glossary.GlossDiv.GlossList.GlossEntry.GlossDef.GlossSeeAlso",
            //"glossary.foo"
    };
    static final String[] expect = new String[] {
            "{{value0}}.startsWith('example')",
            "{{value1}}.contains('GML')",
            "{{value1}}.size() == 2",
            "{{value1}}[0].length <= 3",
            "{{value1}}[1].length <= 2"
    };


    public static String getExpString(String val) {
        return val.replaceAll("\\{\\{", "#").replaceAll("}}", "");
    }

    public static void main(String[] args) throws IOException {
        final ArrayList<String> allExpList = new ArrayList<>(Arrays.asList(vars));
        allExpList.addAll(Arrays.asList(expect));
        final String[] allExp = allExpList.toArray(new String[0]);
        final StandardEvaluationContext context = getData(args);
        context.addPropertyAccessor(new MapAccessor());
        final SpelExpressionParser parser = new SpelExpressionParser();
        for (int i = 0; i < allExp.length; i++) {
            String exp = getExpString(allExp[i]);
            final Expression expression = parser.parseExpression(exp);
            final Object value = expression.getValue(context, Object.class);
            System.out.println("value"+ i + ":" +  value + " processed:" + expression.getExpressionString());
            context.setVariable("value"+i, value);
        }
    }

    private static StandardEvaluationContext getData(String[] args) throws IOException {
        final String jsonFile = args[0];
        final byte[] json = Files.readAllBytes(Paths.get(jsonFile));
        final JSONObject jsonObject = new JSONObject(new String(json));
        final Map<String,Object> map = jsonObject.toMap();
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariable("json", map);
        return ctx;
    }
}
