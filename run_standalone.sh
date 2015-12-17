pkill -f redis
sleep 8
redis-server standalone_redis.conf&
sleep 8
./bin/ycsb load redis -s -P tmp -p "redis.host=127.0.0.1" -p "redis.port=6379" -p "redis.cluster=n" -p "redis.compress=n" -p "redis.algo=none" -p "redis.slave-count=0" -p "redis.sync=n" &&
echo "None"
./bin/ycsb run redis -s -P tmp -p "redis.host=127.0.0.1" -p "redis.port=6379" -p "redis.cluster=n" -p "redis.compress=n" -p "redis.algo=none" -p "redis.slave-count=0" -p "redis.sync=n" &&

pkill -f redis &&
sleep 8
redis-server standalone_redis.conf&
sleep 8
./bin/ycsb load redis -s -P tmp -p "redis.host=127.0.0.1" -p "redis.port=6379" -p "redis.cluster=n" -p "redis.compress=y" -p "redis.algo=snappy" -p "redis.slave-count=0" -p "redis.sync=n" &&
echo "Snappy"
./bin/ycsb run redis -s -P tmp -p "redis.host=127.0.0.1" -p "redis.port=6379" -p "redis.cluster=n" -p "redis.compress=y" -p "redis.algo=snappy" -p "redis.slave-count=0" -p "redis.sync=n" &&

pkill -f redis &&
sleep 8
redis-server standalone_redis.conf&
sleep 8
./bin/ycsb load redis -s -P tmp -p "redis.host=127.0.0.1" -p "redis.port=6379" -p "redis.cluster=n" -p "redis.compress=y" -p "redis.algo=lz4" -p "redis.slave-count=0" -p "redis.sync=n" &&
echo "lz4"
./bin/ycsb run redis -s -P tmp -p "redis.host=127.0.0.1" -p "redis.port=6379" -p "redis.cluster=n" -p "redis.compress=y" -p "redis.algo=lz4" -p "redis.slave-count=0" -p "redis.sync=n" &&

pkill -f redis &&
sleep 8
redis-server standalone_redis.conf&
sleep 8
./bin/ycsb load redis -s -P tmp -p "redis.host=127.0.0.1" -p "redis.port=6379" -p "redis.cluster=n" -p "redis.compress=y" -p "redis.algo=lz4hc" -p "redis.slave-count=0" -p "redis.sync=n" &&
echo "lz4hc"
./bin/ycsb run redis -s -P tmp -p "redis.host=127.0.0.1" -p "redis.port=6379" -p "redis.cluster=n" -p "redis.compress=y" -p "redis.algo=lz4hc" -p "redis.slave-count=0" -p "redis.sync=n" &&

pkill -f redis &&
sleep 8
redis-server standalone_redis.conf&
sleep 8
./bin/ycsb load redis -s -P tmp -p "redis.host=127.0.0.1" -p "redis.port=6379" -p "redis.cluster=n" -p "redis.compress=y" -p "redis.algo=bzip2" -p "redis.slave-count=0" -p "redis.sync=n" &&
echo "bzip2"
./bin/ycsb run redis -s -P tmp -p "redis.host=127.0.0.1" -p "redis.port=6379" -p "redis.cluster=n" -p "redis.compress=y" -p "redis.algo=bzip2" -p "redis.slave-count=0" -p "redis.sync=n" 
