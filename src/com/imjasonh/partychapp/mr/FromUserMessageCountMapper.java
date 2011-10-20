package com.imjasonh.partychapp.mr;
import java.util.logging.Logger;


import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
public class FromUserMessageCountMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable> {
  private static final Logger log = Logger.getLogger(FromUserMessageCountMapper.class.getName());

  public FromUserMessageCountMapper() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void taskSetup(Context context) {
    ;
  }

  @Override
  public void taskCleanup(Context context) {
    ;
  }

  @Override
  public void setup(Context context) {
    ;
  }

  @Override
  public void cleanup(Context context) {
  }

  @Override
  public void map(Key key, Entity value, Context context) {
    final String from = (String)value.getProperty("from");
    final String to = (String)value.getProperty("to");
    final long num_r = ((Long)value.getProperty("num_recipients")).longValue();
    context.getCounter("channel", to).increment(num_r);
    context.getCounter("user", from).increment(num_r);
  }
}
