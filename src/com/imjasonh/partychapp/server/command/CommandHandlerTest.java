package com.imjasonh.partychapp.server.command;

import junit.framework.TestCase;

import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.FakeDatastore;
import com.imjasonh.partychapp.MockXMPPService;
import com.imjasonh.partychapp.server.SendUtil;

public class CommandHandlerTest extends TestCase {
	BroadcastHandler handler = new BroadcastHandler();
	MockXMPPService xmpp = new MockXMPPService();

	public void setUp() {
		FakeDatastore datastore = new FakeDatastore();
		Datastore.setInstance(datastore);
		datastore.setUp();
		SendUtil.setXMPP(xmpp);
	}
}