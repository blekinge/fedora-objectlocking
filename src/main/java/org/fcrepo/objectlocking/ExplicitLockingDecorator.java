package org.fcrepo.objectlocking;

import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.proxy.AbstractInvocationHandler;

import java.lang.reflect.Method;

public class ExplicitLockingDecorator extends AbstractInvocationHandler {

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

        //check pid and username against databaseAccessor
        if (database.isAvailableForThisUser(pid,username)) {
            return method.invoke(target, args);
        } else {
            throw new IllegalAccessException("Attempted to use a method on a locked object");
        }
    }
}
