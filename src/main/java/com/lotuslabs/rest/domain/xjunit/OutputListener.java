package com.lotuslabs.rest.domain.xjunit;


public interface OutputListener {
    void testFailure(Failure failure) throws Exception;

    void testAssumptionFailure(Failure failure);

    void testIgnored(Description description) throws Exception;

    //TestFinished
    void testFinished(Description description) throws Exception;

    //TestStarted
    void testStarted(Description description);

    //TestSuiteStarted
    void testRunStarted(Description description) throws Exception;

    //TestSuiteFinished
    void testRunFinished(Result result) throws Exception;
}

