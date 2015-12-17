echo ""
echo ""
echo "Cluster"
echo ""
echo ""

cd ../redis-3.0.5/utils/create-cluster/ && 
./auto-public.sh &&
cd - &&
./bin/ycsb load redis -s -P tmpc -p "redis.host=52.10.155.147;52.10.155.147;52.10.155.147;52.10.155.147;52.10.155.147;52.10.155.147" -p "redis.port=6379;6380;6381;6382;6383;6384" -p "redis.cluster=y" -p "redis.compress=n" -p "redis.algo=none" -p "redis.slave-count=1" -p "redis.sync=y" &&
echo "None"
./bin/ycsb run redis -s -P tmpc -p "redis.host=52.10.155.147;52.10.155.147;52.10.155.147;52.10.155.147;52.10.155.147;52.10.155.147" -p "redis.port=6379;6380;6381;6382;6383;6384" -p "redis.cluster=y" -p "redis.compress=n" -p "redis.algo=none" -p "redis.slave-count=1" -p "redis.sync=y" &&

cd ../redis-3.0.5/utils/create-cluster/ && 
./auto-public.sh &&
cd - &&
./bin/ycsb load redis -s -P tmpc -p "redis.host=52.10.155.147;52.10.155.147;52.10.155.147;52.10.155.147;52.10.155.147;52.10.155.147" -p "redis.port=6379;6380;6381;6382;6383;6384" -p "redis.cluster=y" -p "redis.compress=y" -p "redis.algo=snappy" -p "redis.slave-count=1" -p "redis.sync=y" &&
echo "Snappy"
./bin/ycsb run redis -s -P tmpc -p "redis.host=52.10.155.147;52.10.155.147;52.10.155.147;52.10.155.147;52.10.155.147;52.10.155.147" -p "redis.port=6379;6380;6381;6382;6383;6384" -p "redis.cluster=y" -p "redis.compress=y" -p "redis.algo=snappy" -p "redis.slave-count=1" -p "redis.sync=y" 

echo ""
echo ""
echo "Standalone"
echo ""
echo ""

pkill -f redis
sleep 8
redis-server standalone_redis.conf&
sleep 8
./bin/ycsb load redis -s -P tmpc -p "redis.host=127.0.0.1" -p "redis.port=6379" -p "redis.cluster=n" -p "redis.compress=n" -p "redis.algo=none" -p "redis.slave-count=0" -p "redis.sync=n" &&
echo "None"
./bin/ycsb run redis -s -P tmpc -p "redis.host=127.0.0.1" -p "redis.port=6379" -p "redis.cluster=n" -p "redis.compress=n" -p "redis.algo=none" -p "redis.slave-count=0" -p "redis.sync=n" &&

pkill -f redis
sleep 8
redis-server standalone_redis.conf&
sleep 8
./bin/ycsb load redis -s -P tmpc -p "redis.host=127.0.0.1" -p "redis.port=6379" -p "redis.cluster=n" -p "redis.compress=y" -p "redis.algo=snappy" -p "redis.slave-count=0" -p "redis.sync=n" &&
echo "Snappy"
./bin/ycsb run redis -s -P tmpc -p "redis.host=127.0.0.1" -p "redis.port=6379" -p "redis.cluster=n" -p "redis.compress=y" -p "redis.algo=snappy" -p "redis.slave-count=0" -p "redis.sync=n" 
