/*
 * Copyright (c) 2011, Olivier Van Acker.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.spike.zookeeper;

import net.spike.zookeeper.retry.RetryCounter;
import net.spike.zookeeper.retry.RetryCounterFactory;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    final static Logger logger = LoggerFactory.getLogger(RecoverableZookeeper.class);
    private final int MAX_RETRIES = 3;
    private int RETRY_INTERVAL = 1000;
    private int sessionTimeout = 1000;

    private RetryCounterFactory retryCounterFactory = new RetryCounterFactory(MAX_RETRIES, RETRY_INTERVAL);

    private ZooKeeper zk;

public RecoverableZookeeper(String connectionString, Watcher watcher) throws IOException {
    this.zk = new ZooKeeper(connectionString, sessionTimeout, watcher);
}

    /**
     * Checks if znode exists, retries up to MAX_ENTRIES with a RETRY_INTERVAL in betweeen retries
     * before it gives up.
     */
    /**
     *
     * @param path
     * @param watcher
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public Stat exists(String path, Watcher watcher) throws KeeperException, InterruptedException {
        RetryCounter retryCounter = retryCounterFactory.create();
        while (true) {
            try {
                return zk.exists(path, watcher);
            } catch (KeeperException e) {
                switch (e.code()) {
                    case CONNECTIONLOSS:
                    case OPERATIONTIMEOUT:
                        if (!retryCounter.shouldRetry()) {
                            break;
                        }
                    default:
                        throw e;
                }
            }
            retryCounter.sleepUntilNextRetry();
            retryCounter.useRetry();
        }
    }

    /**
     * Creation of a non sequential ephemeral znode with retry logic
     */
public String create(String path, byte[] data) throws KeeperException, InterruptedException {
    RetryCounter retryCounter = retryCounterFactory.create();
    while (true) {
        try {
            return zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (KeeperException e) {
            logger.debug("Error code: " + e.code());
            switch (e.code()) {
                case NODEEXISTS:
                    if (retryCounter.shouldRetry()) {
                        byte[] currentData = zk.getData(path, false, null);
                        if (currentData != null && Arrays.equals(currentData, data)) {
                            return path;
                        }
                        throw e;
                    }
                    throw e;
                case CONNECTIONLOSS:
                case OPERATIONTIMEOUT:
                    if (!retryCounter.shouldRetry()) {
                        break;
                    }
                default:
                    throw e;
            }
        }
        retryCounter.sleepUntilNextRetry();
        retryCounter.useRetry();
    }
    }
}
