/**
 * Copyright (c) 2012 YCSB contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */

/**
 * Redis client binding for YCSB.
 *
 * All YCSB records are mapped to a Redis *hash field*.  For scanning
 * operations, all keys are saved (by an arbitrary hash) in a sorted set.
 */

package com.yahoo.ycsb.db;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.StringByteIterator;

import java.util.*;
import java.io.*;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.jpountz.lz4.LZ4SafeDecompressor;

import org.apache.commons.compress.compressors.bzip2.*;
import org.xerial.snappy.Snappy;

public class RedisClient extends DB {

    private Jedis jedis;
    private JedisCluster jedisCluster;
    private boolean cluster;

    static String compress;
    static String compressAlgo;

    static LZ4Factory lz4factory;

    public static final String COMPRESS = "redis.compress"; // y, n
    public static final String COMPRESS_ALGO = "redis.compress-algo"; // lz4, lz4hc, bzip2, snappy
    public static final String COMPRESS_LEVEL = "redis.compression-level"; // 1-17 for lz4
    public static final String CLUSTER = "redis.cluster"; // y, n
    public static final String HOST_PROPERTY = "redis.host";
    public static final String PORT_PROPERTY = "redis.port";
    public static final String PASSWORD_PROPERTY = "redis.password";

    public static final String INDEX_KEY = "_indices";

    public static String compress(String st)
    {
        if (compress != null && compress.equals("y"))
        {
            if (compressAlgo != null && (compressAlgo.equals("lz4") || compressAlgo.equals("lz4hc")))
            {
                byte[] data = st.getBytes();
                LZ4Compressor compressor;
                if (compressAlgo.equals("lz4"))
                {
                    compressor = lz4factory.fastCompressor();
                }
                else
                {
                    compressor = lz4factory.highCompressor();
                }
                final int decompressedLength = data.length;
                int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
                byte[] compressed = new byte[maxCompressedLength];
                int compressedLength = compressor.compress(data, 0, decompressedLength, compressed, 0, maxCompressedLength);
                byte[] compressed2 = Arrays.copyOf(compressed, compressedLength);
                String ret = decompressedLength + "|" + new String(compressed2);
                return ret;
            }
            else if (compressAlgo != null && compressAlgo.equals("bzip2"))
            {
                try
                {
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(st.getBytes());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(baos);
                    final byte[] buffer = new byte[8192];
                    int n = 0;
                    while (-1 != (n = byteArrayInputStream.read(buffer)))
                    {
                        bzOut.write(buffer, 0, n);
                    }
                    bzOut.close();
                    return new String(baos.toByteArray());
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (compressAlgo != null && compressAlgo.equals("snappy"))
            {
                try
                {
                    byte[] compressed = Snappy.compress(st.getBytes());
                    String ret = new String(compressed);
                    return ret;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        return st;
    }

    public static String decompress(String st)
    {
        if (compress != null && compress.equals("y"))
        {
            if (compressAlgo != null && (compressAlgo.equals("lz4") || compressAlgo.equals("lz4hc")))
            {
                int split = st.indexOf('|');
                final int decompressedLength = Integer.parseInt(st.substring(0, split));
                LZ4FastDecompressor decompressor = lz4factory.fastDecompressor();
                byte[] restored = new byte[decompressedLength];
                byte[] compressed = st.substring(split+1, st.length()).getBytes();
                decompressor.decompress(compressed, 0, restored, 0, decompressedLength);
                String ret = new String(restored);
                return ret;
            }
            else if (compressAlgo != null && compressAlgo.equals("bzip2"))
            {
                try
                {
                    InputStream in = new StringBufferInputStream(st);
                    BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
                    byte[] data = st.getBytes();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int n = 0;
                    while (-1 != (n = bzIn.read(data)))
                    {
                        baos.write(data, 0, n);
                    }
                    bzIn.close();
                    return baos.toString();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (compressAlgo != null && compressAlgo.equals("snappy"))
            {
                try
                {
                    byte[] uncompressed = Snappy.uncompress(st.getBytes());
                    String ret = new String(uncompressed);
                    return ret;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        return st;
    }

    public static ByteIterator compress(ByteIterator bi)
    {
        return new StringByteIterator(compress(bi.toString()));
    }

    public static ByteIterator decompress(ByteIterator bi)
    {
        return new StringByteIterator(decompress(bi.toString()));
    }

    public void init() throws DBException {
        Properties props = getProperties();

        String clusterString = props.getProperty(CLUSTER);
        String hostString = props.getProperty(HOST_PROPERTY);
        String portString = props.getProperty(PORT_PROPERTY);
        String password = props.getProperty(PASSWORD_PROPERTY);
        String compress = props.getProperty(COMPRESS);
        String compressAlgo = props.getProperty(COMPRESS_ALGO);
        String compressLevel = props.getProperty(COMPRESS_LEVEL);

        // compress
        lz4factory = LZ4Factory.fastestInstance();


        if (clusterString != null && clusterString.equals("y"))
        {
            cluster = true;
        }
        else
        {
            cluster = false;
        }

        // cluster
        List<Integer> ports = new ArrayList<Integer>();
        List<String> hosts = new ArrayList<String>();

        // single
        int port;
        String host = props.getProperty(HOST_PROPERTY);

        if (cluster)
        {
            for (String st : portString.split(";"))
            {
                if (!st.equals(""))
                {
                    ports.add(Integer.parseInt(st));
                }
                else
                {
                    ports.add(Protocol.DEFAULT_PORT);
                }
            }
            for (String st : hostString.split(";"))
            {
                if (!st.equals(""))
                {
                    hosts.add(st);
                }
            }
            Set<HostAndPort> hostsAndPorts = new HashSet<HostAndPort>();
            for (int i = 0; i < hosts.size(); ++i)
            {
                HostAndPort tmp = new HostAndPort(hosts.get(i), ports.get(i));
                hostsAndPorts.add(tmp);
            }
            jedisCluster = new JedisCluster(hostsAndPorts, 5000, 1000);
            if (password != null) {
                jedisCluster.auth(password);
            }
        }
        else
        {
            if (portString != null) {
                port = Integer.parseInt(portString);
            }
            else
            {
                port = Protocol.DEFAULT_PORT;
            }
            jedis = new Jedis(host, port);
            jedis.connect();
            if (password != null) {
                jedis.auth(password);
            }
        }
    }

    public void cleanup() throws DBException {
        if (cluster)
            jedisCluster.close();
        else
            jedis.disconnect();
    }

    /* Calculate a hash for a key to store it in an index.  The actual return
     * value of this function is not interesting -- it primarily needs to be
     * fast and scattered along the whole space of doubles.  In a real world
     * scenario one would probably use the ASCII values of the keys.
     */
    private double hash(String key) {
        return key.hashCode();
    }

    //XXX jedis.select(int index) to switch to `table`

    @Override
    public int read(String table, String key, Set<String> fields, HashMap<String, ByteIterator> result)
    {
        if (fields == null) {
            StringByteIterator.putAllAsByteIterators(result, jedis.hgetAll(key));
        }
        else {
            String[] fieldArray = (String[])fields.toArray(new String[fields.size()]);
            List<String> values;
            if (cluster)
            {
                values = jedisCluster.hmget(key, fieldArray);
            }
            else
            {
                values = jedis.hmget(key, fieldArray);
            }
            Iterator<String> fieldIterator = fields.iterator();
            Iterator<String> valueIterator = values.iterator();

            while (fieldIterator.hasNext() && valueIterator.hasNext())
            {
                result.put(fieldIterator.next(), decompress(new StringByteIterator(valueIterator.next())));
            }
            assert !fieldIterator.hasNext() && !valueIterator.hasNext();
        }
        return result.isEmpty() ? 1 : 0;
    }

    @Override
    public int insert(String table, String key, HashMap<String, ByteIterator> values)
    {
        for (String st : values.keySet())
        {
            values.put(st, compress(values.get(st)));
        }
        if (cluster)
        {
            if (jedisCluster.hmset(key, StringByteIterator.getStringMap(values)).equals("OK")) {
                jedisCluster.zadd(INDEX_KEY, hash(key), key);
                return 0;
            }
        }
        else
        {
            if (jedis.hmset(key, StringByteIterator.getStringMap(values)).equals("OK")) {
                jedis.zadd(INDEX_KEY, hash(key), key);
                return 0;
            }
        }
        return 1;
    }

    @Override
    public int delete(String table, String key)
    {
        if (cluster)
        {
            return jedisCluster.del(key) == 0 && jedisCluster.zrem(INDEX_KEY, key) == 0 ? 1 : 0;
        }
        else
        {
            return jedis.del(key) == 0 && jedis.zrem(INDEX_KEY, key) == 0 ? 1 : 0;
        }
    }

    @Override
    public int update(String table, String key, HashMap<String, ByteIterator> values)
    {
        for (String st : values.keySet())
        {
            values.put(st, compress(values.get(st)));
        }
        if (cluster)
        {
            return jedisCluster.hmset(key, StringByteIterator.getStringMap(values)).equals("OK") ? 0 : 1;
        }
        else
        {
            return jedis.hmset(key, StringByteIterator.getStringMap(values)).equals("OK") ? 0 : 1;
        }
    }

    @Override
    public int scan(String table, String startkey, int recordcount, Set<String> fields, Vector<HashMap<String, ByteIterator>> result)
    {
        Set<String> keys;
        if (cluster)
        {
            keys = jedisCluster.zrangeByScore(INDEX_KEY, hash(startkey), Double.POSITIVE_INFINITY, 0, recordcount);
        }
        else
        {
            keys = jedis.zrangeByScore(INDEX_KEY, hash(startkey), Double.POSITIVE_INFINITY, 0, recordcount);
        }

        HashMap<String, ByteIterator> values;
        for (String key : keys) {
            values = new HashMap<String, ByteIterator>();
            for (String st : values.keySet())
            {
                values.put(st, decompress(values.get(st)));
            }
            read(table, key, fields, values);
            result.add(values);
        }

        return 0;
    }

}
