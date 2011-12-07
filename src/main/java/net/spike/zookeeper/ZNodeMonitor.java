package net.spike.zookeeper;

import com.sun.tools.hat.internal.model.Root;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
        switch(watchedEvent.getType()) {
            case None:
                processNoneEvent(watchedEvent);
                break;
            case NodeDeleted:
                listener.stopSpeaking();
                try {
                    zk.create(ROOT, listener.getProcessName().getBytes());
                    listener.startSpeaking();
                } catch (Exception e) {
                    // Something went wrong, lets try set a watch first before
                    // we take any action
                }
            case NodeChildrenChanged:
            case NodeCreated:
            case NodeDataChanged:
                try {
                    zk.exists(ROOT, this);
                } catch (Exception e) {
                    shutdown(e);
                }
        }
    }

    public void shutdown(Exception e) {
        logger.error("Unrecoverable error whilst trying to set a watch on election znode, shutting down client", e);
        System.exit(1);
    }

    /**
     * Something changed related to the connection or session
     * @param event
     */
    public void processNoneEvent(WatchedEvent event) {
        switch (event.getState()) {
            case AuthFailed:
            case Disconnected:
            case Expired:
                listener.stopSpeaking();
                break;
        }
    }

    public interface ZNodeMonitorListener {
        public void startSpeaking();

        public void stopSpeaking();

        public String getProcessName();
    }

}
