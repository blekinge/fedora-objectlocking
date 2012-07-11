package org.fcrepo.objectlocking;

import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.proxy.AbstractInvocationHandler;

import java.lang.reflect.Method;

public class LockingDecorator extends AbstractInvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //TODO only hook the changing methods
        //args[0] = context
        //args[1] = pid
        Context context = (Context) args[0];
        String pid = args[1].toString();
        //from context, get username
        String username = context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
        LockingDatabaseAccessor database = LockingDatabaseAccessor.getInstance();

        //check pid and username against databaseAccessor
        String lockedToUser = database.lockObjectToUser(pid,username);
        if (lockedToUser.equals("")){  //We have just locked to object
            try {
                return method.invoke(target,args);
            }finally {
                database.unlockObjectAsUser(pid,username);
            }
        } else {
            if (lockedToUser.equals(username)){//The object was already locked
                return method.invoke(target,args);
            } else {// To somebody else
                throw new IllegalAccessException("Attempted to use a method on a locked object");

            }
        }
    }
}
