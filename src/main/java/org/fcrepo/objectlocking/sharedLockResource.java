package org.fcrepo.objectlocking;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;

import java.io.FileNotFoundException;
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
public class sharedLockResource {

    private final String mapName = "fedora.objects.writelocks.map";

    private static sharedLockResource instance = null;

    public static String configFileLocation = "/home/abr/temp/hazelcast.xml";

    private final HazelcastInstance hazelcast;

    public synchronized static sharedLockResource getInstance() {

        if (instance == null) {
            instance = new sharedLockResource();
        }
        return instance;
    }

    public sharedLockResource()  {
        Config cfg = null;
        try {
            cfg = new XmlConfigBuilder(configFileLocation).build();
        } catch (FileNotFoundException e) {
            throw new Error(e);
        }
        hazelcast = Hazelcast.newHazelcastInstance(cfg);
    }

    public boolean lockObjectToUserIfNotAlreadyLocked(String pid, String user) {
        ILock lock = hazelcast.getLock(mapName);
        lock.lock();
        try {
            Map<String, String> locksMap = hazelcast.getMap(mapName);
            String lockedToUser = locksMap.get(pid);
            if (lockedToUser == null) {
                locksMap.put(pid, user);
                return true;
            } else {
                return false;
            }

        } finally {
            lock.unlock();
        }
    }

    public boolean lockObjectToUser(String pid, String user) {
        ILock lock = hazelcast.getLock(mapName);
        lock.lock();
        try {
            Map<String, String> locksMap = hazelcast.getMap(mapName);
            String lockedToUser = locksMap.get(pid);
            if (lockedToUser == null) {
                locksMap.put(pid, user);
                return true;
            } else if (lockedToUser.equals(user)){
                return true;
            } else {
                return false;
            }

        } finally {
            lock.unlock();
        }
    }


    public boolean isAvailableForThisUser(String pid, String user){
        ILock lock = hazelcast.getLock(mapName);
        lock.lock();
        try {
            Map<String, String> locksMap = hazelcast.getMap(mapName);
            String lockedToUser = locksMap.get(pid);
            if (lockedToUser == null || lockedToUser.equals(user)) {
                return true;
            } else {
                return false;
            }
        } finally {
            lock.unlock();
        }

    }

    public synchronized boolean unlockObjectAsUser(String pid, String user) {
        ILock lock = hazelcast.getLock(mapName);
        lock.lock();
        try {
            Map<String, String> locksMap = hazelcast.getMap(mapName);
            String lockedToUser = locksMap.get(pid);
            if (lockedToUser == null) {
                return true;
            }
            if (lockedToUser.equals(user)) {
                locksMap.remove(pid);
                return true;
            }

        } finally {
            lock.unlock();
        }
        return false;
    }

}
