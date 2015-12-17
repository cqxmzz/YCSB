cd ../redis-3.0.5/utils/create-cluster/ && 
./auto-private.sh &&
cd - &&
./bin/ycsb load redis -s -P tmp -p "redis.host=127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1" -p "redis.port=6379;6380;6381;6382;6383;6384" -p "redis.cluster=y" -p "redis.compress=n" -p "redis.algo=none" -p "redis.slave-count=1" -p "redis.sync=y" &&
echo "None"
./bin/ycsb run redis -s -P tmp -p "redis.host=127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1" -p "redis.port=6379;6380;6381;6382;6383;6384" -p "redis.cluster=y" -p "redis.compress=n" -p "redis.algo=none" -p "redis.slave-count=1" -p "redis.sync=y" &&

cd ../redis-3.0.5/utils/create-cluster/ && 
./auto-private.sh &&
cd - &&
./bin/ycsb load redis -s -P tmp -p "redis.host=127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1" -p "redis.port=6379;6380;6381;6382;6383;6384" -p "redis.cluster=y" -p "redis.compress=y" -p "redis.algo=snappy" -p "redis.slave-count=1" -p "redis.sync=y" &&
echo "snappy"
./bin/ycsb run redis -s -P tmp -p "redis.host=127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1" -p "redis.port=6379;6380;6381;6382;6383;6384" -p "redis.cluster=y" -p "redis.compress=y" -p "redis.algo=snappy" -p "redis.slave-count=1" -p "redis.sync=y" &&

cd ../redis-3.0.5/utils/create-cluster/ && 
./auto-private.sh &&
cd - &&
./bin/ycsb load redis -s -P tmp -p "redis.host=127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1" -p "redis.port=6379;6380;6381;6382;6383;6384" -p "redis.cluster=y" -p "redis.compress=y" -p "redis.algo=lz4" -p "redis.slave-count=1" -p "redis.sync=y" &&
echo "lz4"
./bin/ycsb run redis -s -P tmp -p "redis.host=127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1" -p "redis.port=6379;6380;6381;6382;6383;6384" -p "redis.cluster=y" -p "redis.compress=y" -p "redis.algo=lz4" -p "redis.slave-count=1" -p "redis.sync=y" &&

cd ../redis-3.0.5/utils/create-cluster/ && 
./auto-private.sh &&
cd - &&
./bin/ycsb load redis -s -P tmp -p "redis.host=127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1" -p "redis.port=6379;6380;6381;6382;6383;6384" -p "redis.cluster=y" -p "redis.compress=y" -p "redis.algo=lz4hc" -p "redis.slave-count=1" -p "redis.sync=y" &&
echo "lz4hc"
./bin/ycsb run redis -s -P tmp -p "redis.host=127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1" -p "redis.port=6379;6380;6381;6382;6383;6384" -p "redis.cluster=y" -p "redis.compress=y" -p "redis.algo=lz4hc" -p "redis.slave-count=1" -p "redis.sync=y" &&

cd ../redis-3.0.5/utils/create-cluster/ && 
./auto-private.sh &&
cd - &&
./bin/ycsb load redis -s -P tmp -p "redis.host=127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1" -p "redis.port=6379;6380;6381;6382;6383;6384" -p "redis.cluster=y" -p "redis.compress=y" -p "redis.algo=bzip2" -p "redis.slave-count=1" -p "redis.sync=y" &&
echo "bzip2"
./bin/ycsb run redis -s -P tmp -p "redis.host=127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1" -p "redis.port=6379;6380;6381;6382;6383;6384" -p "redis.cluster=y" -p "redis.compress=y" -p "redis.algo=bzip2" -p "redis.slave-count=1" -p "redis.sync=y" 
