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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: cyberroadie
 * Date: 21/11/2011
 */
public class SpeakerServer {

    final static Logger logger = LoggerFactory.getLogger(SpeakerServer.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ZNodeMonitor monitor;
    private String connectionString;

    private static void printUsage() {
        System.out.println("program [message] [wait between messages in millisecond]");
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            System.exit(1);
        }

        SpeakerServer server = new SpeakerServer();
        server.start(args);

    }

    public void start(String[] args) {
        try {
            readConfig();
        } catch (IOException e) {
            logger.error("Can not read config file", e);
            System.exit(1);
        }

        long delay = Long.parseLong(args[1]);
        Speaker speaker = null;

        try {
            speaker = new Speaker(args[0]);
            monitor = new ZNodeMonitor(connectionString);
            monitor.setListener(speaker);
            monitor.start();
        } catch (Exception e) {
            logger.error("Unrecoverable error", e);
            System.exit(1);
        }
        scheduler.scheduleWithFixedDelay(speaker, 0, delay, TimeUnit.MILLISECONDS);
        logger.info("Speaker server started with fixed time delay of " + delay + " milliseconds.");
    }

    public void readConfig() throws IOException {
        Properties properties = new Properties();
        ClassLoader loader = ClassLoader.getSystemClassLoader ();
        InputStream in = loader.getResourceAsStream ("speaker.config");
        properties.load(in);
        connectionString = properties.getProperty("connectionString");
    }

}
