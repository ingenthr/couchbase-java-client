/**
 * Copyright (C) 2009-2011 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */
package com.couchbase.client;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import net.spy.memcached.ClientBaseCase;
import net.spy.memcached.TestConfig;
import net.spy.memcached.tapmessage.ResponseMessage;

// TBD - Uncomment this line when the TAP tests are complete
// import net.spy.memcached.tapmessage.ResponseMessage;
/**
 * A TapTest.
 */
public class BucketManagementTest extends ClientBaseCase {

  @Override
  protected void setUp() throws Exception {
    // delete the bucket if it exists, recreate it.
    TestHttpClient hclient = new TestHttpClient();

    assertTrue(hclient.doDeleteDefault());
    Thread.sleep(1000);
    assertTrue(hclient.doCreateDefault256());
    Thread.sleep(5000); /* TODO: get the server to do this correctly */

    super.setUp();

  }

  public void testSomething() {
    return;
  }
}
