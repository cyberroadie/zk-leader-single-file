#!/usr/bin/env ruby

basedir = Dir.pwd 
configdir = "#{basedir}/zk/etc/zookeeper/"

max = 0
Dir["#{configdir}/zk-*"].each do |f|
  f =~ /([0-9])/
    if max < $1.to_i
      max = $1.to_i
    end
end

puts "Starting #{max} servers"
# CLASSPATH = "#{configdir}:/usr/share/java/jline.jar:/usr/share/java/log4j-1.2.jar:/usr/share/java/xercesImpl.jar:/usr/share/java/xmlParserAPIs.jar:/usr/share/java/zookeeper.jar"

classpath = "#{configdir}:"
Dir['/opt/zookeeper/lib/*.jar'].each do |jar|
  classpath += "#{jar}:"
end

Dir['/usr/share/java/zookeeper*.jar'].each do |jar|
  classpath += "#{jar}:"
end

Dir['/opt/zookeeper/*.jar'].each do |jar|
  classpath += "#{jar}:"
end

puts "Classpath #{classpath}"

ZOO_LOG4J_PROP="INFO,ROLLINGFILE"
ZOOMAIN="org.apache.zookeeper.server.quorum.QuorumPeerMain"
JVMFLAGS="-Dzookeeper.log.threshold=INFO"

(1..max).each do |i|
  zooCfg = "#{configdir}zk-#{i}.cfg"
  zooDataDir = "#{basedir}/zk/var/lib/zookeeper/zoo#{i}/"
  zooLogDir = "#{basedir}/zk/var/log/zookeeper/zk#{i}/"
  job = []

  io = IO.popen("java  -Dzookeeper.log.dir=#{zooLogDir}" + 
                " -Dzookeeper.root.logger=#{ZOO_LOG4J_PROP}" +  
                " -cp #{classpath} #{JVMFLAGS} #{ZOOMAIN} #{zooCfg}")
  File.open("#{zooDataDir}pid", 'w') { |f| f.puts io.pid }
  puts "Started server #{zooCfg} with pid #{io.pid}" 
  sleep 1
end
