package net.spike.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * This code is based on the HBase RecoverableZookeeper:
 * http://svn.apache.org/viewvc/hbase/trunk/src/main/java/org/apache/hadoop/hbase/zookeeper/RecoverableZooKeeper.java?view=markup
 * it's very dumbed down with only the for this example relevant functionality
 * User: cyberroadie
 * Date: 30/11/2011
 */
public class RecoverableZookeeper {

    private final int MAX_RETRIES = 3;
    private long RETRY_INTERVAL = 1000;
    private int sessionTimeout = 1000;

    private ZooKeeper zk;

    public RecoverableZookeeper(String connectionString, Watcher watcher) throws IOException {
        this.zk = new ZooKeeper(connectionString, sessionTimeout, watcher);
    }

    /**
     * Checks if znode exists, retries up to MAX_ENTRIES with a RETRY_INTERVAL in betweeen retries
     * before it gives up.
     */
    public Stat exists(String path, Watcher watcher) throws KeeperException, InterruptedException {
        int remainingRetries = MAX_RETRIES;
        while (true) {
            try {
                return zk.exists(path, watcher);
            } catch (KeeperException e) {
                switch (e.code()) {
                    case CONNECTIONLOSS:
                    case OPERATIONTIMEOUT:
                        if (remainingRetries > 0) {
                            break;
                        }
                    default:
                        throw e;
                }
            }
            TimeUnit.MILLISECONDS.sleep(RETRY_INTERVAL);
            remainingRetries--;
        }
    }

    /**
     * Creation of a non sequential ephemeral znode with retry logic
     */
    public String create(String path, byte[] data) throws KeeperException, InterruptedException {
        int remainingRetries = MAX_RETRIES;
        boolean isRetry = false;
        while (true) {
            try {
                return zk.create(path, data, null, CreateMode.EPHEMERAL);
            } catch (KeeperException e) {
                switch (e.code()) {
                    case NODEEXISTS:
                        if (isRetry) {
                            byte[] currentData = zk.getData(path, false, null);
                            if (currentData != null && Arrays.equals(currentData, data)) {
                                return path;
                            }
                            throw e;
                        }
                        throw e;
                    case CONNECTIONLOSS:
                    case OPERATIONTIMEOUT:
                        if (remainingRetries > 0) {
                            break;
                        }
                    default:
                        throw e;
                }
            }
            TimeUnit.MILLISECONDS.sleep(RETRY_INTERVAL);
            remainingRetries--;
            isRetry = true;
        }
    }
}
