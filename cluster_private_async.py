import os
import time
import sys

def get_avg_value(file_name):
    total = 0.0
    count = 0
    try:
        with open(file_name, 'r') as f:
            for line in f:
                total += float(line)
                count += 1
        return total / count
    except:
        return 0

def get_compression_time():
    return get_avg_value('compress_time.txt')

def get_decompression_time():
    return get_avg_value('decompress_time.txt')

def get_compression_rate():
    return get_avg_value('compress_rate.txt')

def get_latency():
    op_insert = 0.0
    op_read = 0.0
    lat_insert = 0.0
    lat_read = 0.0
    try:
        with open("result.txt", 'r') as f:
            for line in f:
                if line.startswith("[INSERT], Operations,"):
                    op_insert = float(line.rsplit(' ')[2])
                elif line.startswith("[READ], Operations,"):
                    op_read = float(line.rsplit(' ')[2])
                elif line.startswith("[INSERT], AverageLatency(us),"):
                    lat_insert = float(line.rsplit(' ')[2])
                elif line.startswith("[READ], AverageLatency(us),"):
                    lat_read = float(line.rsplit(' ')[2])
        return (op_insert * lat_insert + op_read * lat_read) / (op_insert + op_read)
    except:
        return 0

def get_throughput():
    with open("result.txt", 'r') as f:
        for line in f:
            if line.startswith("[OVERALL], Throughput(ops/sec)"):
                return float(line.rsplit(' ')[2])
    return 0

def create_workload_file(tmp_file, fieldlength, r_perc_str, i_perc_str, txt_file):
    cmd = "cp workloads/template " + tmp_file
    os.system(cmd)
    cmd = "sed -i s,fieldlength=VAL,fieldlength=" + str(fieldlength) + ", " + tmp_file
    os.system(cmd)
    cmd = "sed -i s,readproportion=VAL,readproportion=" + str(r_perc_str) + ", " + tmp_file
    os.system(cmd)
    cmd = "sed -i s,insertproportion=VAL,insertproportion=" + str(i_perc_str) + ", " + tmp_file
    os.system(cmd)
    cmd = "sed -i s,filebyteiterator=VAL,filebyteiterator=text/" + txt_file + ", " + tmp_file
    os.system(cmd)

def restart_redis():
    cmd = "cd /home/ec2-user/redis-3.0.5/utils/create-cluster && ./auto-private.sh"
    os.system(cmd)
    time.sleep(5)

if __name__ == "__main__":
    name = "cluster_private_async"
    dirname = "result_" + name
    os.system("mkdir " + dirname)

    text = ["wiki_long_all.txt"]
    work_load_read_percent = [0, 25, 50, 75, 100]
    conf_algo = ["none", "lz4", "lz4hc", "bzip2", "snappy"]
    conf_compress = ["n", "y", "y", "y", "y"]
    record_size = [100, 1000, 10000, 100000]
    repeat_time = 1

    chart = {}

    cmd = "pkill -f redis-server"
    os.system(cmd)
    cmd = "rm " + name + ".log"
    os.system(cmd)
    testmode = False

    for txt_file in text:
        for r_perc in work_load_read_percent:

            chart_name_prefix = name + "_" + txt_file + "_read_" + str(r_perc)
            chart[chart_name_prefix] = {}
            chart[chart_name_prefix]["compression_time"] = {}
            chart[chart_name_prefix]["decompression_time"] = {}
            chart[chart_name_prefix]["compression_rate"] = {}
            chart[chart_name_prefix]["latency"] = {}
            chart[chart_name_prefix]["throughput"] = {}

            for i in range(len(conf_algo)):

                algo = conf_algo[i]
                yes_no = conf_compress[i]
                chart[chart_name_prefix]["compression_time"][algo] = []
                chart[chart_name_prefix]["decompression_time"][algo] = []
                chart[chart_name_prefix]["compression_rate"][algo] = []
                chart[chart_name_prefix]["latency"][algo] = []
                chart[chart_name_prefix]["throughput"][algo] = []

                for fieldlength in record_size:
                    total_compression_time = 0.0
                    total_decompression_time = 0.0
                    total_compression_rate = 0.0
                    total_latency = 0.0
                    total_throughput = 0.0

                    for repeat in range(repeat_time):
                        r_perc_str = "{0:.2f}".format(float(r_perc)/100)
                        i_perc_str = "{0:.2f}".format(float(100-r_perc)/100)
                        tmp_file = "tmp"

                        create_workload_file(tmp_file, fieldlength, r_perc_str, i_perc_str, txt_file)
                        if not testmode:
                            restart_redis()

                        cmd = "./bin/ycsb load redis -s -P " + tmp_file + " -p \"redis.host=127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1\" -p \"redis.port=6379;6380;6381;6382;6383;6384\" -p \"redis.cluster=y\" -p \"redis.compress=" + yes_no + "\" -p \"redis.algo=" + algo + "\" -p \"redis.slave-count=1\" -p \"redis.sync=n\" > trash.txt"
                        if not testmode:
                            os.system(cmd)

                        cmd = "rm compress_rate.txt compress_time.txt decompress_time.txt"
                        if not testmode:
                            os.system(cmd)

                        print ""
                        print ""
                        print ""
                        with open(name + ".log", 'a') as logfile:
                            print >> logfile, "Running " + chart_name_prefix + " " + algo + " " + str(fieldlength) + " " + str(repeat)
                        cmd = "./bin/ycsb run redis -s -P " + tmp_file + " -p \"redis.host=127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1;127.0.0.1\" -p \"redis.port=6379;6380;6381;6382;6383;6384\" -p \"redis.cluster=y\" -p \"redis.compress=" + yes_no + "\" -p \"redis.algo=" + algo + "\" -p \"redis.slave-count=1\" -p \"redis.sync=n\" > result.txt"
                        print cmd
                        if not testmode:
                            os.system(cmd)

                        total_compression_time += get_compression_time()
                        total_decompression_time += get_decompression_time()
                        total_compression_rate += get_compression_rate()
                        total_latency += get_latency()
                        total_throughput += get_throughput()

                    avg_compression_time = total_compression_time / repeat_time
                    avg_decompression_time = total_decompression_time / repeat_time
                    avg_compression_rate = total_compression_rate / repeat_time
                    avg_latency = total_latency / repeat_time
                    avg_throughput = total_throughput / repeat_time

                    with open(name + ".log", 'a') as logfile:
                        print >> logfile, "avg_compression_time", avg_compression_time
                        print >> logfile, "avg_decompression_time", avg_decompression_time
                        print >> logfile, "avg_compression_rate", avg_compression_rate
                        print >> logfile, "avg_latency", avg_latency
                        print >> logfile, "avg_throughput", avg_throughput

                    chart[chart_name_prefix]["compression_time"][algo].append(avg_compression_time)
                    chart[chart_name_prefix]["decompression_time"][algo].append(avg_decompression_time)
                    chart[chart_name_prefix]["compression_rate"][algo].append(avg_compression_rate)
                    chart[chart_name_prefix]["latency"][algo].append(avg_latency)
                    chart[chart_name_prefix]["throughput"][algo].append(avg_throughput)

                with open(name + ".log", 'a') as logfile:
                    print >> logfile, chart_name_prefix, "compression_time", algo,chart[chart_name_prefix]["compression_time"][algo]
                    print >> logfile, chart_name_prefix, "decompression_time", algo, chart[chart_name_prefix]["decompression_time"][algo]
                    print >> logfile, chart_name_prefix, "compression_rate", algo, chart[chart_name_prefix]["compression_rate"][algo]
                    print >> logfile, chart_name_prefix, "latency", algo, chart[chart_name_prefix]["latency"][algo]
                    print >> logfile, chart_name_prefix, "throughput", algo, chart[chart_name_prefix]["throughput"][algo]

            # Put vectors into chart_name_prefix_metrix file
            with open(dirname + "/" + chart_name_prefix + "_" + "compression_time" + str(time.time()), 'w') as f:
                for algo in chart[chart_name_prefix]["compression_time"]:
                    print >> f, algo + "," + \
                        ",".join(str(v) for v in chart[chart_name_prefix]["compression_time"][algo])

            with open(dirname + "/" + chart_name_prefix + "_" + "decompression_time" + str(time.time()), 'w') as f:
                for algo in chart[chart_name_prefix]["decompression_time"]:
                    print >> f, algo + "," + \
                        ",".join(str(v) for v in chart[chart_name_prefix]["decompression_time"][algo])

            with open(dirname + "/" + chart_name_prefix + "_" + "compression_rate" + str(time.time()), 'w') as f:
                for algo in chart[chart_name_prefix]["compression_rate"]:
                    print >> f, algo + "," + \
                        ",".join(str(v) for v in chart[chart_name_prefix]["compression_rate"][algo])

            with open(dirname + "/" + chart_name_prefix + "_" + "latency" + str(time.time()), 'w') as f:
                for algo in chart[chart_name_prefix]["latency"]:
                    print >> f, algo + "," + \
                        ",".join(str(v) for v in chart[chart_name_prefix]["latency"][algo])

            with open(dirname + "/" + chart_name_prefix + "_" + "throughput" + str(time.time()), 'w') as f:
                for algo in chart[chart_name_prefix]["throughput"]:
                    print >> f, algo + "," + \
                        ",".join(str(v) for v in chart[chart_name_prefix]["throughput"][algo])

    cmd = "pkill -f redis-server"
    os.system(cmd)
