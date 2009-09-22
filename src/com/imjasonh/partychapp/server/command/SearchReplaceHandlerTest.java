package com.imjasonh.partychapp.server.command;

import junit.framework.TestCase;

import com.google.appengine.api.xmpp.JID;
import com.imjasonh.partychapp.Channel;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.Member;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.server.SendUtil;

public class SearchReplaceHandlerTest extends TestCase {
  SearchReplaceHandler handler = new SearchReplaceHandler();
  MockXMPPService xmpp = new MockXMPPService();
  PPBHandler ppbHandler = new PPBHandler();
  
  public void setUp() {
    Datastore.setInstance(new FakeDatastore());
    SendUtil.setXMPP(xmpp);
  }
  
  public void testMatches() {
    assertTrue(handler.matches(Message.createForTests("s/foo/bar/")));
    assertTrue(handler.matches(Message.createForTests("s/foo/bar/g")));
    assertTrue(handler.matches(Message.createForTests(" s/foo/bar/")));
    assertTrue(handler.matches(Message.createForTests("s/foo/bar")));
    assertTrue(handler.matches(Message.createForTests(" s/foo/bar")));
  }
  
  public void testSimple() {
    Channel c = FakeDatastore.instance().fakeChannel();
    Member m = c.getMemberByJID(new JID("neil@gmail.com"));
    m.addToLastMessages("foo foo");
    handler.doCommand(Message.createForTests("s/foo/bar/"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[\"neil\"] s/foo/bar/", xmpp.messages.get(0).getBody());
    assertEquals("_neil meant bar foo_", xmpp.messages.get(1).getBody());
  }
  
  public void testMissingTrailingSlash() {
    Channel c = FakeDatastore.instance().fakeChannel();
    Member m = c.getMemberByJID(new JID("neil@gmail.com"));
    m.addToLastMessages("foo foo");
    handler.doCommand(Message.createForTests("s/foo/bag"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[\"neil\"] s/foo/bag", xmpp.messages.get(0).getBody());
    assertEquals("_neil meant bag foo_", xmpp.messages.get(1).getBody());
  }
  
  public void testGreedy() {
    Channel c = FakeDatastore.instance().fakeChannel();
    Member m = c.getMemberByJID(new JID("neil@gmail.com"));
    m.addToLastMessages("foo bar baz foo bar baz");
    handler.doCommand(Message.createForTests("s/foo/bar/g"));
    assertEquals(2, xmpp.messages.size());
    assertEquals("[\"neil\"] s/foo/bar/g", xmpp.messages.get(0).getBody());
    assertEquals("_neil meant bar bar baz bar bar baz_", xmpp.messages.get(1).getBody());
  }
  
  public void testNoPlusPlusesChanged() {
    ppbHandler.doCommand(Message.createForTests("x++ foo"));
    xmpp.messages.clear();
    
    handler.doCommand(Message.createForTests("s/foo/bar/"));
    assertEquals(3, xmpp.messages.size());
    assertEquals("[\"neil\"] s/foo/bar/", xmpp.messages.get(0).getBody());
    assertEquals("Undoing original actions: x++ [back to 0]", xmpp.messages.get(1).getBody());
    assertEquals("_neil meant x++ [woot! now at 1] bar_", xmpp.messages.get(2).getBody());
  }

  public void testOnePlusPlusChanged() {
    ppbHandler.doCommand(Message.createForTests("x++ foo"));
    xmpp.messages.clear();

    handler.doCommand(Message.createForTests("s/x/y/"));
    assertEquals(3, xmpp.messages.size());
    assertEquals("[\"neil\"] s/x/y/", xmpp.messages.get(0).getBody());
    assertEquals("Undoing original actions: x++ [back to 0]", xmpp.messages.get(1).getBody());
    assertEquals("_neil meant y++ [woot! now at 1] foo_", xmpp.messages.get(2).getBody());
  }
  
  public void testManyChanges() {
    String firstMessage = "i am watching psych++ right now. sean-- for being an ass sometimes";
    ppbHandler.doCommand(Message.createForTests(firstMessage));
    xmpp.messages.clear();

    handler.doCommand(Message.createForTests("s/s/t/g"));
    assertEquals(3, xmpp.messages.size());
    assertEquals("[\"neil\"] s/s/t/g", xmpp.messages.get(0).getBody());
    assertEquals("Undoing original actions: psych++ [back to 0], sean-- [back to 0]", xmpp.messages.get(1).getBody());
    assertEquals("_neil meant i am watching ptych++ [woot! now at 1] right now. " +
                 "tean-- [ouch! now at -1] for being an att tometimet_",
                 xmpp.messages.get(2).getBody());    
  }
  
  public void testFixCombine() {
    // To start with, 'jason' is at 1 and 'intern' is at 2. We want to end with 'jason at 3 and 'intern' at 0.
    ppbHandler.doCommand(Message.createForTests("intern++"));
    ppbHandler.doCommand(Message.createForTests("intern++"));
    ppbHandler.doCommand(Message.createForTests("jason++"));
    
    // Oh no, a typo.
    String firstMessage = "jason++ jason++ intren-- intren-- /combine";
    ppbHandler.doCommand(Message.createForTests(firstMessage));
    xmpp.messages.clear();

    // fix the typo!
    handler.doCommand(Message.createForTests("s/intren/intern/g"));
    assertEquals(3, xmpp.messages.size());
    assertEquals("[\"neil\"] s/intren/intern/g", xmpp.messages.get(0).getBody());
    assertEquals("Undoing original actions: jason++ [back to 2], jason++ [back to 1], intren-- [back to -1], intren-- [back to 0]",
                 xmpp.messages.get(1).getBody());
    assertEquals("_neil meant jason++ [woot! now at 2] jason++ [woot! now at 3] intern-- [ouch! now at 1] intern-- [ouch! now at 0] /combine_",
                 xmpp.messages.get(2).getBody());    

  }
}