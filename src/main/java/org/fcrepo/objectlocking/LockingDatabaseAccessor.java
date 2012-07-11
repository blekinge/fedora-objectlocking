package org.fcrepo.objectlocking;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: abr
 * Date: 7/9/12
 * Time: 10:01 AM
 * To change this template use File | Settings | File Templates.
 */
//TODO Make this into something not memory backed
public class LockingDatabaseAccessor {

    private final Map<String,String> locks = Collections.synchronizedMap(new HashMap<String, String>());

    private static LockingDatabaseAccessor instance = null;

    public synchronized static LockingDatabaseAccessor getInstance(){
        if (instance == null){
            instance = new LockingDatabaseAccessor();
        }
        return instance;
    }


    /**
     * If the object is already locked
     *   - return name of locking user
     * else
     *   - return ""
     * @param pid
     * @param user
     * @return
     */
    public  String lockObjectToUser(String pid, String user) {
        synchronized (locks){
            String lockedToUser = locks.get(pid);
            if (lockedToUser == null) {
                locks.put(pid,user);
                return "";
            } else {
                return lockedToUser;
            }
        }
    }

    public synchronized boolean unlockObjectAsUser(String pid, String user){
        synchronized (locks){
            String lockedToUser = locks.get(pid);
            if (lockedToUser == null){
                return true;
            }
            if (lockedToUser.equals(user)) {
                locks.remove(pid);
                return true;
            }
        }
        return false;

    }

}
