
/*
 * Copryright 2012 Couchbase, Inc.
 * Portions Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
5    * (the "License"); you may not use this file except in compliance with the
6    * License.  You may obtain a copy of the License at:
7    *
8    *    http://www.apache.org/licenses/LICENSE-2.0
9    *
10   * Unless required by applicable law or agreed to in writing, software
11   * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
12   * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
13   * License for the specific language governing permissions and limitations
14   * under the License.
15   */
package com.couchbase.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.base64.Base64;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import sun.misc.BASE64Encoder;

/**
34   * A simple HTTP client that prints out the content of the HTTP response to
35   * {@link System#out} to test {@link HttpServer}.
36   *
37   * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
38   * @author Andy Taylor (andy.taylor@jboss.org)
39   * @author <a href="http://gleamynode.net/">Trustin Lee</a>
40   *
41   * @version $Rev: 2226 $, $Date: 2010-03-31 11:26:51 +0900 (Wed, 31 Mar 2010) $
42   */
public class TestHttpClient {

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println(
              "Usage: " + TestHttpClient.class.getSimpleName()
              + " <URL>");
      return;
    }

    URI uri = new URI(args[0]);
    String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
    String host = uri.getHost() == null ? "localhost" : uri.getHost();
    int port = uri.getPort();
    if (port == -1) {
      if (scheme.equalsIgnoreCase("http")) {
        port = 80;
      } else if (scheme.equalsIgnoreCase("https")) {
        port = 443;
      }
    }

    if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
      System.err.println("Only HTTP(S) is supported.");
      return;
    }

    boolean ssl = scheme.equalsIgnoreCase("https");

    // Configure the client.
    ClientBootstrap bootstrap = new ClientBootstrap(
            new NioClientSocketChannelFactory(
            Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool()));

    // Set up the event pipeline factory.
    //bootstrap.setPipelineFactory(new HttpClientPipelineFactory(ssl));

    // Start the connection attempt.
    ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

    // Wait until the connection attempt succeeds or fails.
    Channel channel = future.awaitUninterruptibly().getChannel();
    if (!future.isSuccess()) {
      future.getCause().printStackTrace();
      bootstrap.releaseExternalResources();
      return;
    }


    // Prepare the HTTP request.
    HttpRequest request = new DefaultHttpRequest(
            HttpVersion.HTTP_1_1, HttpMethod.GET, uri.toASCIIString());
    request.setHeader(HttpHeaders.Names.HOST, host);
    request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
    request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

    // Set some example cookies.
    CookieEncoder httpCookieEncoder = new CookieEncoder(false);
    httpCookieEncoder.addCookie("my-cookie", "foo");
    httpCookieEncoder.addCookie("another-cookie", "bar");
    request.setHeader(HttpHeaders.Names.COOKIE, httpCookieEncoder.encode());

    // Send the HTTP request.
    channel.write(request);

    // Wait for the server to close the connection.
    channel.getCloseFuture().awaitUninterruptibly();

    // Shut down executor threads to exit.
    bootstrap.releaseExternalResources();
  }

  public boolean doDeleteDefault() throws IOException {

    URL url = new URL("http://localhost:8091/pools/default/buckets/default");
    try {
      request(false, "DELETE", url, "Administrator", "password", null, null);
    } catch (FileNotFoundException ex) {
      System.err.println("Warning: bucket doesn't exist, could not be deleted.");
    }

    return true;

  }

  public boolean doCreateDefault256() throws IOException {

    URL url = new URL("http://localhost:8091/pools/default/buckets");
    String args = "parallelDBAndViewCompaction=false&autoCompactionDefined=false&replicaIndex=0&replicaNumber=1&saslPassword=&authType=sasl&ramQuotaMB=256&bucketType=membase&name=default";
    executePost(url, args, "Administrator", "password");

    return true;

  }
//  /**
//   * Oddly, lots of things that do HTTP seem to not know how to do this and
//   * Authenticator caches for the process. Since we only need Basic at the
//   * moment simply, add the header.
//   *
//   * @return a value for an HTTP Basic Auth Header
//   */
//  protected String buildAuthHeader(String username, String password)
//          throws UnsupportedEncodingException {
//    // apparently netty isn't familiar with HTTP Basic Auth
//    StringBuilder clearText = new StringBuilder(username);
//    clearText.append(':');
//    if (password != null) {
//      clearText.append(password);
//    }
//    String headerResult;
//    headerResult = "Basic "
//            + Base64.encodeBase64String(clearText.toString().getBytes("UTF-8"));
//
//    if (headerResult.endsWith("\r\n")) {
//      headerResult = headerResult.substring(0, headerResult.length() - 2);
//    }
//    return headerResult;
//  }

  private Channel getChannel() throws IOException {
    ClientBootstrap client = new ClientBootstrap(
            new NioClientSocketChannelFactory(
            Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool()));

    client.setPipelineFactory(new ChannelPipelineFactory() {

      @Override
      public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("codec", new HttpClientCodec());
        pipeline.addLast("aggregator", new HttpChunkAggregator(5242880));
        pipeline.addLast("authHandler", new ClientMessageHandler());
        return pipeline;
      }
    });

    DefaultHttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.DELETE, "http://localhost:8091/pools/default/buckets/default");

    request.addHeader(HttpHeaders.Names.HOST, "localhost");

    String authString = "Administrator" + ":" + "password";
    ChannelBuffer authChannelBuffer = ChannelBuffers.copiedBuffer(authString, CharsetUtil.UTF_8);
    ChannelBuffer encodedAuthChannelBuffer = Base64.encode(authChannelBuffer);
    request.addHeader(HttpHeaders.Names.AUTHORIZATION, encodedAuthChannelBuffer.toString(CharsetUtil.UTF_8));
    Channel channel = client.connect(new InetSocketAddress("localhost", 8091)).awaitUninterruptibly().getChannel();


    return channel;
  }

  boolean checkPoolsForComplete() {

    try {
      URL url = new URL("http://localhost:8091/pools");
      request(false, "GET", url, null, null, null, null);
    } catch (IOException ex) {
      System.err.println("Couldn't get pools: " + ex.getLocalizedMessage());
      ex.printStackTrace();
      return false;
    }

    return true;
  }

  public static class ClientMessageHandler extends SimpleChannelHandler {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
      e.getCause().printStackTrace();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      HttpResponse httpResponse = (HttpResponse) e.getMessage();
      String json = httpResponse.getContent().toString(CharsetUtil.UTF_8);
      System.out.println(json);
    }
  }

  private static void request(boolean quiet, String method, URL url, String username, String password, InputStream body, String encodedRequest)
          throws IOException {
    // sigh.  openConnection() doesn't actually open the connection,
    // just gives you a URLConnection.  connect() will open the connection.
    if (!quiet) {
      System.out.println("[issuing request: " + method + " " + url + "]");
    }
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod(method);

    if (username != null) {
    // write auth header
    BASE64Encoder encoder = new BASE64Encoder();
    String encodedCredential = encoder.encode((username + ":" + password).getBytes());
    connection.setRequestProperty("Authorization", "Basic " + encodedCredential);
    }


    if (method.equals("POST")) {
      connection.setRequestProperty("Content-Type",
              "application/x-www-form-urlencoded");
      connection.setRequestProperty("Content-Length", ""
              + Integer.toString(encodedRequest.getBytes("UTF-8").length));
      connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches (false);
      connection.setDoInput(true);
      connection.setDoOutput(true);
    }

    // write body if we're doing POST or PUT
    byte buffer[] = new byte[8192];
    int read = 0;
    if (body != null) {
      connection.setDoOutput(true);

      OutputStream output = connection.getOutputStream();
      while ((read = body.read(buffer)) != -1) {
        output.write(buffer, 0, read);
      }
    }

    // do request
    long time = System.currentTimeMillis();
    connection.connect();

    InputStream responseBodyStream = connection.getInputStream();
    StringBuffer responseBody = new StringBuffer();
    while ((read = responseBodyStream.read(buffer)) != -1) {
      responseBody.append(new String(buffer, 0, read));
    }
    connection.disconnect();
    time = System.currentTimeMillis() - time;

    // start printing output
    if (!quiet) {
      System.out.println("[read " + responseBody.length() + " chars in " + time + "ms]");
    }

    // look at headers
    // the 0th header has a null key, and the value is the response line ("HTTP/1.1 200 OK" or whatever)
    if (!quiet) {
      String header = null;
      String headerValue = null;
      int index = 0;
      while ((headerValue = connection.getHeaderField(index)) != null) {
        header = connection.getHeaderFieldKey(index);

        if (header == null) {
          System.out.println(headerValue);
        } else {
          System.out.println(header + ": " + headerValue);
        }

        index++;
      }
      System.out.println("");
    }

    // dump body
    System.out.print(responseBody);
    System.out.flush();
  }


  public static String executePost (URL targetURL, String urlParameters, String username, String password) throws IOException
  {
    HttpURLConnection connection = null;
    try {
      //Create connection
      connection = (HttpURLConnection)targetURL.openConnection();
          // write auth header
    BASE64Encoder encoder = new BASE64Encoder();
    String encodedCredential = encoder.encode((username + ":" + password).getBytes());
    connection.setRequestProperty("Authorization", "Basic " + encodedCredential);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type",
           "application/x-www-form-urlencoded");

      connection.setRequestProperty("Content-Length", "" +
               Integer.toString(urlParameters.getBytes().length));
      connection.setRequestProperty("Content-Language", "en-US");

      connection.setUseCaches (false);
      connection.setDoInput(true);
      connection.setDoOutput(true);

      //Send request
      DataOutputStream wr = new DataOutputStream (
                  connection.getOutputStream ());
      wr.writeBytes (urlParameters);
      wr.flush ();
      wr.close ();

      //Get Response
      InputStream is = connection.getInputStream();
      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
      String line;
      StringBuilder response = new StringBuilder();
      while((line = rd.readLine()) != null) {
        response.append(line);
        response.append('\r');
      }
      rd.close();
      return response.toString();

    } finally {

      if(connection != null) {
        connection.disconnect();
      }
    }
  }

}
