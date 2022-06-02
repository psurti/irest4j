package com.lotuslabs.rest.adapters.xjunit;

import com.lotuslabs.rest.domain.xjunit.Description;
import com.lotuslabs.rest.domain.xjunit.Failure;
import com.lotuslabs.rest.domain.xjunit.OutputListener;
import com.lotuslabs.rest.domain.xjunit.Result;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class JUnitXmlListener implements OutputListener {
    private static final double ONE_SECOND = 1000.0;

    // XML constants

    /**
     * the failure element
     */
    private static final String FAILURE = "failure";

    /**
     * name attribute for property, testcase and testsuite elements
     */
    private static final String ATTR_NAME = "name";

    /**
     * time attribute for testcase and testsuite elements
     */
    private static final String ATTR_TIME = "time";

    private final OutputStream outputStream;

    /**
     * The XML document.
     */
    private Document document;
    /**
     * The wrapper for the whole testsuite.
     */
    private Element rootElement;

    private final Result.Listener listener;
    private final Map<Description, Failure> failedTests;
    private final Map<Description, Element> testElements;
    private final Map<Description, Long> testStarts;

    public JUnitXmlListener(Result result, OutputStream outputStream) {
        this.listener = result.createListener();
        this.failedTests = new LinkedHashMap<>();
        this.testElements = new LinkedHashMap<>();
        this.testStarts = new LinkedHashMap<>();
        this.outputStream = outputStream;
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        listener.testAssumptionFailure(failure);
        formatError(failure);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        listener.testFailure(failure);
        formatError(failure);
    }

    @Override
    public void testFinished(Description description) {
        listener.testFinished(description);
        if (!testStarts.containsKey(description)) {
            testStarted(description);
        }
        Element currentTest;
        if (!failedTests.containsKey(description)) {
            final String TESTCASE = "testcase";
            currentTest = document.createElement(TESTCASE);
            String n = description.getDisplayName();
            currentTest.setAttribute(ATTR_NAME, n == null ? "unknown" : n);
            final String ATTR_CLASSNAME = "classname";
            currentTest.setAttribute(ATTR_CLASSNAME, n);
            rootElement.appendChild(currentTest);
            testElements.put(description, currentTest);
        } else {
            currentTest = testElements.get(description);
        }

        Long l = testStarts.get(description);
        currentTest.setAttribute(ATTR_TIME, "" + ((System.currentTimeMillis() - l) / ONE_SECOND));
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        listener.testIgnored(description);
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        listener.testRunStarted(description);
        document = getDocumentBuilder().newDocument();
        final String TESTSUITE = "testsuite";
        rootElement = document.createElement(TESTSUITE);
        String n = description.getDisplayName();
        rootElement.setAttribute(ATTR_NAME, n == null ? "unknown" : n);
        final String TIMESTAMP = "timestamp";

        rootElement.setAttribute(TIMESTAMP, DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()));
        final String HOSTNAME = "hostname";
        rootElement.setAttribute(HOSTNAME, getHostname());
        final String PROPERTIES = "properties";
        Element element = document.createElement(PROPERTIES);
        rootElement.appendChild(element);
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        listener.testRunFinished(result);
        try {
            final String ATTR_TESTS = "tests";
            rootElement.setAttribute(ATTR_TESTS, "" + result.getRunCount());
            final String ATTR_FAILURES = "failures";
            rootElement.setAttribute(ATTR_FAILURES, "" + result.getFailureCount());
            final String ATTR_ERRORS = "errors";
            rootElement.setAttribute(ATTR_ERRORS, "" + 0);
            rootElement.setAttribute(ATTR_TIME, "" + (result.getRunTime() / ONE_SECOND));
            Writer wri = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            (new DOMElementWriter()).write(rootElement, wri, 0, "  ");
            wri.flush();
        } catch (IOException exc) {
            throw new RuntimeException("Unable to write log file", exc);
        } finally {
            outputStream.close();
        }
    }

    @Override
    public void testStarted(Description description) {
        listener.testStarted(description);
        testStarts.put(description, System.currentTimeMillis());
    }

    private void formatError(Failure f) {
        testFinished(f.getDescription());
        failedTests.put(f.getDescription(), f);

        Element nested = document.createElement(JUnitXmlListener.FAILURE);
        Element currentTest = testElements.get(f.getDescription());

        currentTest.appendChild(nested);

        String message = f.getMessage();
        if (message != null && message.length() > 0) {
            final String ATTR_MESSAGE = "message";
            nested.setAttribute(ATTR_MESSAGE, message);
        }
        final String ATTR_TYPE = "type";
        nested.setAttribute(ATTR_TYPE, f.getDescription().getDisplayName());


        String strace = f.getTrace();
        Text trace = document.createTextNode(strace);
        nested.appendChild(trace);
    }

    private static DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get the local hostname
     *
     * @return the name of the local host, or "localhost" if we cannot work it out
     */
    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }
}
