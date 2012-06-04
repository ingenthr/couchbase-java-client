/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.couchbase.client.vbucket;

import com.couchbase.client.vbucket.config.Config;
import java.util.ArrayList;
import java.util.List;
import net.spy.memcached.MemcachedNode;
import org.junit.AfterClass;

/**
 *
 * @author ingenthr
 */
public class FailInjectingVBucketNodeLocator extends VBucketNodeLocator {

  ArrayList<String> bogused; // chosen for size over speed

  public FailInjectingVBucketNodeLocator(List<MemcachedNode> nodes, Config jsonConfig) {
    super(nodes, jsonConfig);
    bogused = new ArrayList<String>();
  }

  /**
   * Returns a vbucket index for the given key, but returns a bogus vbucket
   * number if the key begins with "bogus".
   *
   * @param key the key
   * @return vbucket index
   */
  @Override
  public int getVBucketIndex(String key) {

    int vBucketIndex = super.getVBucketIndex(key);
    
    if (bogused.contains(key)) {
      System.err.println("Already bogused once: " + key);
      return vBucketIndex;
    }

    if (key.startsWith("bogus")) {
      vBucketIndex = 1025; // make the vbucket index bogus first time through
      System.err.println("BOGUS!!!!! " + key );
      bogused.add(key);
    }

    return vBucketIndex;
  }

}
