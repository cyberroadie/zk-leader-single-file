#!/usr/bin/env bash
ps -ef | grep zookeeper | grep -v grep | awk '{print "kill " $2}' | sudo sh
