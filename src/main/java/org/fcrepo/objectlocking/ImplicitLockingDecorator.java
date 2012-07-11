package org.fcrepo.objectlocking;

import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.proxy.AbstractInvocationHandler;

import java.lang.reflect.Method;

public class ImplicitLockingDecorator extends AbstractInvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //TODO only hook the changing methods
        //args[0] = context
        //args[1] = pid
        Context context = (Context) args[0];
        String pid = args[1].toString();
        //from context, get username
        String username = context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
        sharedLockResource database = sharedLockResource.getInstance();

        boolean implicitlyLocked = database.lockObjectToUserIfNotAlreadyLocked(pid, username);
        //check pid and username against databaseAccessor

        if (implicitlyLocked){
            try {
                return method.invoke(target, args);
            } finally {
                database.unlockObjectAsUser(pid, username);
            }
        } else {
            return method.invoke(target, args);
        }
    }
}
