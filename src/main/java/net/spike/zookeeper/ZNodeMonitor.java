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

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * User: cyberroadie
 * Date: 30/11/2011
 */
public class ZNodeMonitor implements Watcher {

    final Logger logger = LoggerFactory.getLogger(ZNodeMonitor.class);
    private final String ROOT = "/SPEAKER";
    private ZNodeMonitorListener listener;
    private RecoverableZookeeper zk;
    private String connectionString;

    public void setListener(ZNodeMonitorListener listener) {
        this.listener = listener;
    }

    public ZNodeMonitor(String connectionString) {
        this.connectionString = connectionString;
    }

    public void start() throws IOException {
        this.zk = new RecoverableZookeeper(connectionString, this);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                processNoneEvent(watchedEvent);
                break;
            case NodeDeleted:
                listener.setState(ZNodeMonitorListener.LeaderState.INACTIVE);
                createZNode();
        }
        try {
            zk.exists(ROOT, this);
        } catch (KeeperException.ConnectionLossException e) {
            // keep going disconnects happen all the time, application is in paused state
        } catch (Exception e) {
            shutdown(e);
        }
    }

    private void createZNode() {
        try {
            zk.create(ROOT, listener.getProcessName().getBytes());
            listener.setState(ZNodeMonitorListener.LeaderState.ACTIVE);
        } catch (Exception e) {
            // Something went wrong, lets try set a watch first before
            // we take any action
        }
    }

    public void shutdown(Exception e) {
        logger.error("Unrecoverable error whilst trying to set a watch on election znode, shutting down client", e);
        System.exit(1);
    }

    /**
     * Something changed related to the connection or session
     *
     * @param event
     */
    public void processNoneEvent(WatchedEvent event) {
        switch (event.getState()) {
            case SyncConnected:
                createZNode();
                break;
            case AuthFailed:
                listener.setState(ZNodeMonitorListener.LeaderState.INACTIVE);
                break;
            case Disconnected:
                listener.setState(ZNodeMonitorListener.LeaderState.PAUSED);
                break;
            case Expired:
                listener.setState(ZNodeMonitorListener.LeaderState.INACTIVE);
                break;
        }
    }


    public interface ZNodeMonitorListener {

        public enum LeaderState {
            ACTIVE,
            INACTIVE,
            PAUSED
        }

        public void setState(LeaderState state);

        public String getProcessName();

    }

}
