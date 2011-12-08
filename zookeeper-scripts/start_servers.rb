#!/usr/bin/env ruby

require 'fileutils'

def findmax()
  max = 0
  Dir["/etc/zookeeper/zk-*"].each do |f|
    f =~ /([0-9])/
      if max < $1.to_i
        max = $1.to_i
      end
  end

  return max
end

max = findmax()
puts "Starting #{max.to_s} servers"
CLASSPATH = "/etc/zookeeper:/usr/share/java/jline.jar:/usr/share/java/log4j-1.2.jar:/usr/share/java/xercesImpl.jar:/usr/share/java/xmlParserAPIs.jar:/usr/share/java/zookeeper.jar"
ZOO_LOG4J_PROP="INFO,ROLLINGFILE"
ZOOMAIN="org.apache.zookeeper.server.quorum.QuorumPeerMain"
JVMFLAGS="-Dzookeeper.log.threshold=INFO"

(1..max).each do |i|
  zooCfg = "/etc/zookeeper/zk-#{i.to_s}.cfg"
  zooDataDir = "/var/lib/zookeeper/zoo#{i.to_s}/"
  zooLogDir = "/var/log/zookeeper/zk#{i.to_s}/"
  job = []

  io = IO.popen("java  -Dzookeeper.log.dir=" + zooLogDir + 
                " -Dzookeeper.root.logger=" + ZOO_LOG4J_PROP +  
                " -cp " + CLASSPATH + " " +
                JVMFLAGS + " " +
                ZOOMAIN + " " +
                zooCfg)
  File.open("#{zooDataDir}pid", 'w') { |f| f.puts io.pid }
  puts "Started server #{zooCfg} with pid #{io.pid}"  
  sleep 1
end



