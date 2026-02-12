package com.microp.snowflake;


public class Snowflake {

    private final long workerIdBits = 5L;
    private final long datacenterIdBits = 5L;
    private final long maxWorkerId = -1L ^(-1L << workerIdBits);
    private final long maxDatacenterId = -1L ^(-1L << datacenterIdBits);
    private final long sequenceBits = 12L;
    private final long workerIdShift = sequenceBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private final long sequenceMask =  -1L ^ (-1L << sequenceBits);


    private long tmepoch = 1770907248L;
    private long workerId;
    private long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;


    private final long startNano = System.nanoTime();
    private final long startMillis = System.currentTimeMillis();

    private long currentTime() {
        return (System.nanoTime() - startNano) / 1_000_000 + startMillis;
    }


    public Snowflake(long workerId, long datacenterId){
        if(workerId > maxWorkerId || workerId < 0){
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if(datacenterId > maxDatacenterId || datacenterId < 0){
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }
    public Snowflake(long workerId, long datacenterId,long tmepoch){
        if(workerId > maxWorkerId || workerId < 0){
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if(datacenterId > maxDatacenterId || datacenterId < 0){
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.tmepoch = tmepoch;
    }

    public synchronized long nextId(){
        long timestamp = currentTime();
        if(timestamp < lastTimestamp){
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        if(lastTimestamp == timestamp){
            sequence = (sequence + 1) & sequenceMask;
            if(sequence == 0){
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else{
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - tmepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift) | (workerId << workerIdShift) | sequence;
    }

    private long tilNextMillis(long lastTimestamp){
        long timestamp = currentTime();
        while(timestamp <= lastTimestamp){
            timestamp = currentTime();
        }
        return timestamp;
    }
}
