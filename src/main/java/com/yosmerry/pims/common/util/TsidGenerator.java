package com.yosmerry.pims.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TsidGenerator {

  private static final long CUSTOM_EPOCH = 1_577_836_800_000L;
  private static final int COUNTER_BITS = 12;
  private static final int MAX_COUNTER = (1 << COUNTER_BITS) - 1;

  private static long lastTimestamp = -1L;
  private static int counter;

  public static synchronized long next() {
    long timestamp = System.currentTimeMillis() - CUSTOM_EPOCH;

    if (timestamp == lastTimestamp) {
      counter = (counter + 1) & MAX_COUNTER;
      if (counter == 0) {
        timestamp = waitForNextMillisecond(timestamp);
      }
    } else {
      counter = 0;
    }

    lastTimestamp = timestamp;
    return (timestamp << COUNTER_BITS) | counter;
  }

  private static long waitForNextMillisecond(long timestamp) {
    long nextTimestamp = System.currentTimeMillis() - CUSTOM_EPOCH;
    while (nextTimestamp <= timestamp) {
      Thread.onSpinWait();
      nextTimestamp = System.currentTimeMillis() - CUSTOM_EPOCH;
    }
    return nextTimestamp;
  }
}
