<!--
Copyright (c) 2010 Yahoo! Inc., 2012 - 2015 YCSB contributors. 
All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You
may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. See accompanying
LICENSE file.
-->

Yahoo! Cloud System Benchmark (YCSB)
====================================
[![Build Status](https://travis-ci.org/brianfrankcooper/YCSB.png?branch=master)](https://travis-ci.org/brianfrankcooper/YCSB)

Links
-----
http://wiki.github.com/brianfrankcooper/YCSB/  
http://research.yahoo.com/Web_Information_Management/YCSB/  
ycsb-users@yahoogroups.com  

Getting Started
---------------

1. Download the latest release of YCSB:

    For our experiment we only need to clone this repo. // QIMING CHEN
    
2. Set up a database to benchmark. There is a README file under each binding 
   directory.
    
    We can skip this // QIMING CHEN

3. Run YCSB command. 
    
    For our experiment we use this: // QIMING CHEN
    ```sh
    sudo ./bin/ycsb load redis -s -P workloads/workloada -p "redis.host=50.112.164.180;50.112.164.180;50.112.164.180;50.112.164.180;50.112.164.180;50.112.164.180" -p "redis.port=6379;6380;6381;6382;6383;6384" -p "redis.cluster=y" -p "redis.compress=y" -p "redis.compress-algo=lz4" -p "redis.slave-count=3"    
    ```
    
    The following command runs ycsb in a command line interface:
    ```sh
    sudo ./bin/ycsb shell redis -p "redis.host=50.112.164.180;50.112.164.180;50.112.164.180;50.112.164.180;50.112.164.180;50.112.164.180" -p "redis.port=6379;6380;6381;6382;6383;6384" -p "redis.cluster=y" -p "redis.compress=y" -p "redis.compress-algo=lz4"
    ```
    Those command will rebuild YCSB if you modified it.
    
  Running the `ycsb` command without any argument will print the usage. 
   
  See https://github.com/brianfrankcooper/YCSB/wiki/Running-a-Workload
  for a detailed documentation on how to run a workload.

  See https://github.com/brianfrankcooper/YCSB/wiki/Core-Properties for 
  the list of available workload properties.
