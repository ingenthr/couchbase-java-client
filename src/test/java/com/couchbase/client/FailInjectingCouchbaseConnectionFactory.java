/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.couchbase.client;

import com.couchbase.client.vbucket.FailInjectingVBucketNodeLocator;
import com.couchbase.client.vbucket.config.Config;
import com.couchbase.client.vbucket.config.ConfigType;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import net.spy.memcached.DefaultHashAlgorithm;
import net.spy.memcached.KetamaNodeLocator;
import net.spy.memcached.MemcachedNode;
import net.spy.memcached.NodeLocator;

/**
 *
 * @author ingenthr
 */
public class FailInjectingCouchbaseConnectionFactory extends CouchbaseConnectionFactory {

  public FailInjectingCouchbaseConnectionFactory(final List<URI> baseList,
      final String bucketName, final String password)
      throws IOException {
        super(baseList, bucketName, password);
  }


  @Override
  public NodeLocator createLocator(List<MemcachedNode> nodes) {
    Config config = getVBucketConfig();

    if (config == null) {
      throw new IllegalStateException("Couldn't get config");
    }

    if (config.getConfigType() == ConfigType.MEMCACHE) {
      return new KetamaNodeLocator(nodes, DefaultHashAlgorithm.KETAMA_HASH);
    } else if (config.getConfigType() == ConfigType.COUCHBASE) {
      return new FailInjectingVBucketNodeLocator(nodes, getVBucketConfig());
    } else {
      throw new IllegalStateException("Unhandled locator type: "
          + config.getConfigType());
    }
  }

}
