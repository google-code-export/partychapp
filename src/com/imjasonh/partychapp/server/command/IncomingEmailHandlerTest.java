package com.imjasonh.partychapp.server.command;

import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.Message.MessageType;

public class IncomingEmailHandlerTest extends CommandHandlerTest {
  IncomingEmailHandler handler = new IncomingEmailHandler();

  public void testSimple() {
    Message m = Message.createForTests("Subject: x / Body: blah blah");
    m.messageType = MessageType.EMAIL;
    
    handler.doCommand(m);
    
    assertEquals(1, xmpp.messages.size());
    assertEquals("**via email** [neil] Subject: x / Body: blah blah", xmpp.messages.get(0).getBody());
  }
}