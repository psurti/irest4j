package com.lotuslabs.rest.interfaces;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Failure {
    final Description description;
    final Throwable throwable;

    public Failure(Description description, Throwable throwable) {
        this.description = description;
        this.throwable = throwable;
    }

    /**
     * @return the raw description of the context of the failure.
     */
    public Description getDescription() {
        return description;
    }

    /**
     * @return the exception thrown
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * @return a user-understandable label for the test
     */
    public String getTestHeader() {
        return description.getDisplayName();
    }

    public String getMessage() {
        if (throwable != null) {
            return throwable.getMessage();
        }
        return null;
    }

    public String getTrace() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        if (throwable != null) {
            throwable.printStackTrace(writer);
        }
        return stringWriter.toString();
    }

    @Override
    public String toString() {
        return getTestHeader() + ": " + throwable.getMessage();
    }
}
