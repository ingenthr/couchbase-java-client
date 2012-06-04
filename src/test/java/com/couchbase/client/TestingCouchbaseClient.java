/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.couchbase.client;

import com.couchbase.client.vbucket.Reconfigurable;
import java.io.IOException;
import net.spy.memcached.ops.Operation;

/**
 *
 * @author ingenthr
 */
public class TestingCouchbaseClient extends CouchbaseClient implements CouchbaseClientIF, Reconfigurable {

  public TestingCouchbaseClient(CouchbaseConnectionFactory cf) throws IOException {
    super(cf);
  }

  public void enqueueTestOperation(String key, Operation op) {
    mconn.enqueueOperation(key, op);
  }

}
