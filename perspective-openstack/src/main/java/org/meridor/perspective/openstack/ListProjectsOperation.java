package org.meridor.perspective.openstack;

import org.meridor.perspective.agent.Operation;
import org.meridor.perspective.agent.OperationType;
import org.meridor.perspective.agent.annotation.Cache;
import org.meridor.perspective.beans.User;

import javax.ws.rs.core.Response;

@Cache(collection = "projects")
public class ListProjectsOperation implements Operation<User> {
    @Override
    public OperationType getType() {
        return OperationType.PROJECTS_LIST;
    }

    @Override
    public Response apply(User user) {
        //TODO: implement it!
        return null;
    }
}
