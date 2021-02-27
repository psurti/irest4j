package com.lotuslabs.rest.model.actions;

import com.lotuslabs.rest.interfaces.IRestClient;
import com.lotuslabs.rest.model.RestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.util.Map;

@Slf4j
public abstract class RestAction {
    private final String name;

    public RestAction(String name) {
        super();
        this.name = name;
   }

    public abstract Map<String,?> execute(RestContext restContext,
                                          IRestClient<Map<String,?>, String> restClient) throws IOException;

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .toString();
    }
}
