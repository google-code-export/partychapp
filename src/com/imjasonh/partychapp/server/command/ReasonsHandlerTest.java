package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.server.SendUtil;

import junit.framework.TestCase;

public class ReasonsHandlerTest extends TestCase {
  ReasonsHandler handler = new ReasonsHandler();
  MockXMPPService xmpp = new MockXMPPService();
  PPBHandler ppb = new PPBHandler();

  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
    SendUtil.setXMPP(xmpp);
  }
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("/reasons x")));
    assertTrue(handler.matches(Message.createForTests(" /reasons x")));
    assertTrue(handler.matches(Message.createForTests("/reasons xyz")));
    assertTrue(handler.matches(Message.createForTests("/reasons a_b-c.d")));
    assertFalse(handler.matches(Message.createForTests("x /reasons")));
  }
  
  public void testReasons() {
    String content = "x++ for being awesome";
    ppb.doCommand(Message.createForTests(content));
    xmpp.messages.clear();

    handler.doCommand(Message.createForTests("/reasons x"));
    assertEquals(1, xmpp.messages.size());
    assertEquals("x: 1\n" +
                 "increment by neil@gmail.com (" + content + ")",
                 xmpp.messages.get(0).getBody());
  }
}