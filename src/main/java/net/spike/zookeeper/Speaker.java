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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * User: cyberroadie
 * Date: 07/11/2011
 */
public class Speaker implements Runnable, ZNodeMonitor.ZNodeMonitorListener {

    public LeaderState state = LeaderState.INACTIVE;

    final static Logger logger = LoggerFactory.getLogger(Speaker.class);

    private String message;
    private String processName;
    private long counter = 0;

    public Speaker(String message) throws IOException, InterruptedException, KeeperException {
        this.message = message;
        this.processName = getUniqueIdentifier();
    }

    private static String getUniqueIdentifier() {
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        String processId = processName.substring(0, processName.indexOf("@"));
        return "pid-" + processId + ".";
    }

    public void run() {
        try {
            switch (state) {
                case ACTIVE:
                    handleTask();
                    break;
                case PAUSED:
                    pauseTask();
                    break;
                case INACTIVE:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void handleTask() throws IOException {
        FileWriter fstream = new FileWriter("out.txt");
        BufferedWriter out = new BufferedWriter(fstream);
        String msg = message + ": " + counter++ + " " + processName;
        out.write(msg + "\n");
        logger.debug(msg);
        out.close();
    }

    public void pauseTask() throws IOException {
        String msg = message + ": paused " + processName;
        logger.debug(msg);
    }

    @Override
    public void setState(LeaderState state) {
        this.state = state;
    }

    @Override
    public String getProcessName() {
        return processName;
    }
}
