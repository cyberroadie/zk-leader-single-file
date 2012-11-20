#!/usr/bin/env ruby

basedir = Dir.pwd 
configdir = "#{basedir}/zk/etc/zookeeper/"

# Change me to location of zookeeper installation
zookeeperdir = "#{basedir}/../target/zookeeper"

puts "Checking directory #{basedir}/../target/zookeeper"
if !File.directory?("#{basedir}/../target/zookeeper/")
    require 'net/http'

    `wget http://mirror.ox.ac.uk/sites/rsync.apache.org/zookeeper/zookeeper-3.4.3/zookeeper-3.4.3.tar.gz`
    `gunzip zookeeper-3.4.3.tar.gz`

    if !File.directory?("#{basedir}/../target/")
        `mkdir #{basedir}/../target`
    end

    `tar -xvf zookeeper-3.4.3.tar -C #{basedir}/../target/`
    `ln -s #{basedir}/../target/zookeeper-3.4.3 #{basedir}/../target/zookeeper`
    `rm zookeeper-3.4.3.tar*`
end

max = 0
Dir["#{configdir}/zk-*"].each do |f|
  f =~ /([0-9]+)/
    if max < $1.to_i
      max = $1.to_i
    end
end

puts "Starting #{max} servers"

classpath = "#{configdir}:"

Dir["#{zookeeperdir}/lib/*.jar"].each do |jar|
  puts "Adding #{jar} to classpath"
  classpath += "#{jar}:"
end

Dir["#{zookeeperdir}/zookeeper*.jar"].each do |jar|
  puts "Adding #{jar} to classpath"
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
