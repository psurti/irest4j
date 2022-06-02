package com.lotuslabs.rest.domain;

import com.jayway.jsonpath.JsonPath;
import com.lotuslabs.rest.domain.variables.Variable;
import com.lotuslabs.rest.domain.variables.VariableContext;
import com.lotuslabs.rest.domain.variables.VariableSet;
import net.minidev.json.JSONValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

class ExecuteActionTest {
    private final ExecuteAction action = new ExecuteAction();

    @Test
    @SuppressWarnings("unchecked")
    void evaluate_asMap_many_values() throws IOException, URISyntaxException {
        VariableContext ctx = new VariableContext();
        final String body = readFile("store.json");
        VariableSet responseVariables = VariableSet.create(Collections.emptyMap());
        VariableSet expectationVariables = VariableSet.create(Collections.emptyMap());
        VariableSet headerVariables = VariableSet.create(Collections.emptyMap());
        responseVariables.addVariable(Variable.create("thirdBook", "$.store.books[2]")); // get 3rd book record
        action.evaluate(ctx, headerVariables, body, "OK", 200, responseVariables, expectationVariables);
        final Object result = ctx.getOrDefault("thirdBook.json", "");
        System.out.println("json="+result);
        final long actualSum = getCRC32Checksum(result.toString().getBytes(StandardCharsets.UTF_8));
        Assertions.assertEquals(921285187, actualSum);
        final Map<String,?> mapData = (Map<String, ?>) JSONValue.parseKeepingOrder(result.toString());
        System.out.println("map="+mapData);
        Assertions.assertEquals(mapData.get("price"), 8.99);
    }

    @Test
    @SuppressWarnings("unchecked")
    void evaluate_asArray_many_values() throws IOException, URISyntaxException {
        VariableContext ctx = new VariableContext();
        final String body = readFile("store.json");
        VariableSet responseVariables = VariableSet.create(Collections.emptyMap());
        VariableSet expectationVariables = VariableSet.create(Collections.emptyMap());
        VariableSet headerVariables = VariableSet.create(Collections.emptyMap());
        responseVariables.addVariable(Variable.create("firstBookSection", "$.store.books[0].sections")); // get 3rd book record
        action.evaluate(ctx, headerVariables, body, "OK", 200, responseVariables, expectationVariables);
        final Object result = ctx.getOrDefault("firstBookSection", "");
        System.out.println("json="+result);
        final List<Object> actual = (List<Object>) JSONValue.parseKeepingOrder(result.toString());
        System.out.println("list="+actual);
        final List<String> expected = Arrays.asList("s1", "s2", "s3");
        Assertions.assertIterableEquals(expected, actual );
    }

    @Test
    void evaluate_regexJsonPath() throws IOException, URISyntaxException {
        final String body = readFile("store.json");
        Object val;
        // Using Regular Expression: get all books where author attribute contains 'sam' or 'bob'
        // Learn more about regular expressions here : https://zappysys.com/blog/using-regular-expressions-in-ssis/
        val = JsonPath.read(body, "$.store.books[?(@.author=~ /sam|bob/)]");
        System.out.println(val);
        // Using Regular Expression: get all books where author name is exactly 'sam' or 'bob'
        // Learn more about regular expressions here : https://zappysys.com/blog/using-regular-expressions-in-ssis/
        val = JsonPath.read(body,"$.store.books[?(@.author=~ /^sam|bob$/)]");
        System.out.println(val);
        // Using Regular Expression: get all books where author name starts with 'sam'
        // Learn more about regular expressions here : https://zappysys.com/blog/using-regular-expressions-in-ssis/
        val = JsonPath.read(body,"$.store.books[?(@.author=~ /^sam/)]");
        System.out.println(val);
        // Using Regular Expression: get all books where author name ends with 'sam'
        // Learn more about regular expressions here : https://zappysys.com/blog/using-regular-expressions-in-ssis/
        val = JsonPath.read(body,"$.store.books[?(@.author=~ /sam$/)]");
        System.out.println(val);
    }

    @Test
    void evaluate_plainJsonPath() throws IOException, URISyntaxException {
        final String body = readFile("store.json");
        List<String> authors = JsonPath.read(body, "$.store.books[*].author");
        System.out.println(authors);
        final List<String> expected = Arrays.asList("Nigel Rees","Evelyn Waugh","Herman Melville","J. R. R. Tolkien");
        Assertions.assertIterableEquals(expected, authors);
        // get all books for store
        Object val;
        val = JsonPath.read(body, "$.store.books[*]");
        System.out.println(val);
        // get all sections from all books
        val = JsonPath.read(body, "$.store.books[*].sections[*]");
        System.out.println(val);
        // get all authors of all books for store
        val = JsonPath.read(body, "$.store.books[*].author");
        System.out.println(val);
        // get 3rd book record
        val = JsonPath.read(body, "$.store.books[2]");
        System.out.println(val);
        // get first 2 books from the top
        val = JsonPath.read(body, "$.store.books[:2]");
        System.out.println(val);
        // get last 2 books
        val = JsonPath.read(body, "$.store.books[-2:]");
        System.out.println(val);
        // get all books where author attribute equals to 'sam'
        val = JsonPath.read(body, "$.store.books[?(@.author=='sam')]");
        System.out.println(val);
        // get all documents / sub documents (any level) where author attribute equals to 'sam'
        val = JsonPath.read(body, "$..[?(@.author=='sam')]");
        System.out.println(val);
        // get books where price is less than 10
        val = JsonPath.read(body, "$.store.books[?(@.price<10)]");
        System.out.println(val);
        // filter all books with tag
        val = JsonPath.read(body, "$.store.books[?(@.tag)]");
        System.out.println(val);
        // find all books which contains section s1 or s2 (Use of Logical operator OR ( || )
        val = JsonPath.read(body, "$.store.books[?(@.sections[*]=='s1' || @.sections[*]=='s2' )]");
        System.out.println(val);
        // find all books where first section is s1 and second section is s2 (Logical operator OR ( && )
        val = JsonPath.read(body, "$.store.books[?(@.sections[0]=='s1' && @.sections[1]=='s2' )]");
        System.out.println(val);
    }

    private String readFile(String fileName) throws IOException, URISyntaxException {
        final URL resourceURL = getClass().getClassLoader().getResource(fileName);
        assert resourceURL != null;
        final List<String> bodyLines = Files.readAllLines(Paths.get(resourceURL.toURI()));
        final StringBuilder bodyBuilder = new StringBuilder();
        bodyLines.forEach(bodyBuilder::append);
        System.out.printf("Read %d bytes of data%n", bodyBuilder.length());
        return bodyBuilder.toString();
    }

    private long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }
}