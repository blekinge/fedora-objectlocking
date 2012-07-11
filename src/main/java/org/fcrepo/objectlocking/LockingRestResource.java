package org.fcrepo.objectlocking;

import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.Server;
import org.fcrepo.server.rest.BaseRestResource;
import org.fcrepo.server.rest.RestParam;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;

//TODO find the correct location of these rest methods
@Path("/")
@Component
public class LockingRestResource extends BaseRestResource {
    public LockingRestResource(Server server) {
        super(server);
    }

    @POST
    @Path(VALID_PID_PART+"/lock")
    @Consumes({XML, FORM})
    public void lockObject(@PathParam(RestParam.PID)
                           String pid) throws IllegalAccessException {
        Context context = getContext();
        //from context, get username
        String username = context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
        LockingDatabaseAccessor database = LockingDatabaseAccessor.getInstance();

        String lockedToUser = database.lockObjectToUser(pid, username);
        if ("".equals(lockedToUser) || username.equals(lockedToUser)) {
          //object locked to you
        } else {
            throw new IllegalAccessException("Object is already locked");
        }
    }

    @POST
    @Path(VALID_PID_PART+"/unlock")
    @Consumes({XML, FORM})
    public void unlockObject(@PathParam(RestParam.PID)
                             String pid) throws IllegalAccessException {
        Context context = getContext();
        //from context, get username
        String username = context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
        LockingDatabaseAccessor database = LockingDatabaseAccessor.getInstance();
        boolean result = database.unlockObjectAsUser(pid, username);
        if (!result){
            throw new IllegalAccessException("Object is already locked");
        }
    }
}
