package com.lotuslabs.rest.model.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

public class RestActions {

    private final Collection<RestAction> actions;

    public RestActions(Properties p) {
        actions = new ArrayList<>();
        //-- Actions
        final String[] propertyNames = p.getProperty("actions").split(",");
        final StringBuilder builder = new StringBuilder();
        for (String action : propertyNames) {
            RestAction restAction = null;
            //-- Special handling of Login Action
            if (LoginAction.NAME.equalsIgnoreCase(action)) {
                restAction = (new LoginAction(p));
            }

            if (restAction == null) {
                builder.setLength(0);
                for (char c : action.toCharArray()) {
                    if (c == '_' || Character.isUpperCase(c)) {
                        break;
                    } else {
                        builder.append(c);
                    }
                }
                String method = builder.toString();
                if (method.equalsIgnoreCase(GetAction.NAME)) {
                    restAction = new GetAction(action, p);
                } else if (method.equalsIgnoreCase(PostAction.NAME)) {
                    restAction = new PostAction(action, p);
                } else if (method.equalsIgnoreCase(PutAction.NAME)) {
                    restAction = new PutAction(action, p);
                } else if (method.equalsIgnoreCase(DeleteAction.NAME)) {
                    restAction = new DeleteAction(action, p);
                } else if (method.equalsIgnoreCase(FormPostAction.NAME)) {
                    restAction = new FormPostAction(action, p);
                }
            }
            if (restAction != null) {
                actions.add(restAction);
            }
        }
    }

    public RestAction[] getActions() {
        return actions.toArray(new RestAction[0]);
    }
}
