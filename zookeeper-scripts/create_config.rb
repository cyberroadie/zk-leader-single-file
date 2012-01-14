#!/usr/bin/env ruby

max = 5
if ARGV.length > 0 
  max = ARGV[0].to_i
  if max == 0
    max = 5 
  end
end

puts "Creating #{max} configurations"
connection_string = String.new

basedir = Dir.pwd
configdir = "#{basedir}/zk/etc/zookeeper/"
puts "Creating configuration directory #{configdir}"

`mkdir -p #{configdir}`
`cp log4j.properties #{configdir}`

(1..max).each do |i| 
 t = i.to_s
 # Delete file if already exists
 File.delete("#{configdir}zk-#{t}.cfg") rescue nil
 # Create data dir
 datadir = "#{basedir}/zk/var/lib/zookeeper/zoo#{t}/" 
 logdir = "#{basedir}/zk/var/log/zookeeper/zk#{t}/"

 `mkdir -p #{datadir}`
 `mkdir -p #{logdir}`
 File.open("#{datadir}myid" , "w") { |f| f.puts i } 
 File.open("#{configdir}zk-#{t}.cfg", 'a') { |f|
   f.puts "tickTime=2000"
   f.puts "initLimit=10"
   f.puts "syncLimit=5"
   f.puts "dataDir=#{datadir}"
   f.puts "clientPort=#{(2180 + i).to_s}"
   #f.puts "electionAlg=0"
   (1..max).each do |n|
      s = n.to_s()
      f.puts "server.#{s}=localhost:288#{s}:388#{s}"
   end
 }
connection_string += "localhost:218#{t}," 
 
end

puts connection_string.chomp
File.open("speaker.config", 'w') { |f| f.puts "connectionString=#{connection_string[0..-2]}" }
