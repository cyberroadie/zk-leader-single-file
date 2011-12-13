#!/usr/bin/env ruby

require 'fileutils'

max = 5
if ARGV.length > 0 
  max = ARGV[0].to_i
  if max == 0
    max = 5 
  end
end

puts "creating #{max.to_s} configurations"
connection_string = ""

basedir = `pwd`[0..-2] 
configdir = "#{basedir}/zk/etc/zookeeper/"
puts "Creating configuration directory #{configdir}"

FileUtils.mkdir_p(configdir)
FileUtils.copy("log4j.properties", configdir)

(1..max).each do |i| 
 t = i.to_s
 # Delete file if already exists
 File.delete("#{configdir}zk-#{t}.cfg") rescue nil
 # Create data dir
 datadir = "#{basedir}/zk/var/lib/zookeeper/zoo#{t}/" 
 logdir = "#{basedir}/zk/var/log/zookeeper/zk#{t}/"

 FileUtils.mkdir_p(datadir) rescue nil
 FileUtils.mkdir_p(logdir) rescue nil
 File.open("#{datadir}myid" , "w") { |f| f.puts i } 
 File.open("#{configdir}zk-#{t}.cfg", 'a') { |f|
   f.puts "tickTime=2000"
   f.puts "initLimit=10"
   f.puts "syncLimit=5"
   f.puts "dataDir=#{datadir}"
   f.puts "clientPort=#{(2880 + i).to_s}"
   (1..max).each do |n|
      s = n.to_s()
      f.puts "server.#{s}=localhost:289#{s}:389#{s}\n"
   end
 }
connection_string += "localhost:289#{t}," 
 
end

puts connection_string[0..-2]
File.open("speaker.config", 'w') { |f| f.puts "connectionString=#{connection_string[0..-2]}" }


