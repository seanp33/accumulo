/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.accumulo.server.tabletserver;

import java.nio.charset.Charset;
import java.util.Random;

import org.apache.accumulo.core.Constants;
import org.apache.accumulo.core.zookeeper.ZooUtil;
import org.apache.accumulo.server.client.HdfsZooInstance;
import org.apache.accumulo.server.test.FastFormat;
import org.apache.accumulo.server.zookeeper.ZooReaderWriter;

/**
 * Allocates unique names for an accumulo instance. The names are unique for the lifetime of the instance.
 * 
 * This is useful for filenames because it makes caching easy.
 * 
 */

public class UniqueNameAllocator {
  private long next = 0;
  private long maxAllocated = 0;
  private String nextNamePath;
  private Random rand;

  private static final Charset utf8 = Charset.forName("UTF8");
  
  private UniqueNameAllocator() {
    nextNamePath = Constants.ZROOT + "/" + HdfsZooInstance.getInstance().getInstanceID() + Constants.ZNEXT_FILE;
    rand = new Random();
  }
  
  public synchronized String getNextName() {
    
    while (next >= maxAllocated) {
      final int allocate = 100 + rand.nextInt(100);
      
      try {
        byte[] max = ZooReaderWriter.getRetryingInstance().mutate(nextNamePath, null, ZooUtil.PRIVATE, new ZooReaderWriter.Mutator() {
          public byte[] mutate(byte[] currentValue) throws Exception {
            long l = Long.parseLong(new String(currentValue), Character.MAX_RADIX);
            l += allocate;
            return Long.toString(l, Character.MAX_RADIX).getBytes(utf8);
          }
        });
        
        maxAllocated = Long.parseLong(new String(max), Character.MAX_RADIX);
        next = maxAllocated - allocate;
        
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    
    return new String(FastFormat.toZeroPaddedString(next++, 7, Character.MAX_RADIX, new byte[0]));
  }
  
  private static UniqueNameAllocator instance = null;
  
  public static synchronized UniqueNameAllocator getInstance() {
    if (instance == null)
      instance = new UniqueNameAllocator();
    
    return instance;
  }
  
}
