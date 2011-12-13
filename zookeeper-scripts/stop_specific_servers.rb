#!/usr/bin/env ruby

require 'fileutils'

if ARGV.length == 0
  puts "specify number(s) of server(s) to kill"
  Kernel.exit
end

basedir = `pwd`[0..-2] 
datadir = "#{basedir}/zk/var/lib/zookeeper/zoo" 

ARGV.each do |n| 
  pid = 0
  File.open("#{datadir}#{n}/pid", 'r') { |f| pid = f.gets.to_i }
  puts "Killing process with id #{pid}"
  Process.kill("HUP", pid)
  FileUtils.rm("#{datadir}#{n}/pid")
end
