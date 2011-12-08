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

(1..findmax()).each do |i| 
  File.delete("/etc/zookeeper/zk-" << i.to_s() << ".cfg") 
  FileUtils.rm_rf(datadir = "/var/lib/zookeeper/zoo" + i.to_s)
  FileUtils.rm_rf(logdir = "/var/log/zookeeper/zk" + i.to_s)
end


