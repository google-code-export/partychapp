package com.imjasonh.partychapp.server.command;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.imjasonh.partychapp.Message;
import com.imjasonh.partychapp.ppb.PlusPlusBot;
import com.imjasonh.partychapp.server.SendUtil;

public class SearchReplaceHandler implements CommandHandler {
  private static Pattern pattern = Pattern.compile("^s/([^/]+)/([^/]+)(/?)(g?)$");

  PlusPlusBot ppb = new PlusPlusBot();
  PPBHandler ppbHandler = new PPBHandler();

  private void sendNoMatchError(Message msg) {
    SendUtil.broadcastIncludingSender("No message found that matches that pattern.",
                                      msg.channel,
                                      msg.serverJID);
  }
  
  public void doCommand(Message msg) {
    List<String> lastMessages = Lists.newArrayList(msg.member.getLastMessages()); 
    
    msg.member.addToLastMessages(msg.content);
    SendUtil.broadcast(msg.member.getAliasPrefix() + msg.content,
                       msg.channel,
                       msg.serverJID,
                       msg.userJID);

    Matcher m = pattern.matcher(msg.content.trim());
    if (!m.matches()) {
      sendNoMatchError(msg);
      return;
    }

    String toReplace = m.group(1);
    String replacement = m.group(2);
    String trailingSlash = m.group(3);
    boolean replaceAll = false;
    if (trailingSlash.isEmpty()) {
      replacement += m.group(4);
    } else {
      replaceAll = m.group(4).equals("g");
    }
    
    String messageToChange = null;
    // TODO(nsanch): should I do anything special since this is user-supplied?
    Pattern p = Pattern.compile(toReplace);
    for (String curr : lastMessages) {
      if (p.matcher(curr).find()) {
        messageToChange = curr;
        break;
      }
    }
    if (messageToChange == null) {
      sendNoMatchError(msg);
      return;
    }

    String after = null;
    if (replaceAll) {
      after = messageToChange.replaceAll(toReplace, replacement);
    } else {
      after = messageToChange.replaceFirst(toReplace, replacement);
    }
    Message originalMsg = new Message(messageToChange, msg.userJID,
                                      msg.serverJID, msg.member, msg.channel);
    if (ppbHandler.matches(originalMsg)) {
      ppbHandler.undoEarlierMessage(originalMsg);
    }
    Message afterMsg = new Message(after, msg.userJID, msg.serverJID, msg.member, msg.channel);
    if (ppbHandler.matches(afterMsg)) {
      ppbHandler.doCommandAsCorrection(afterMsg);
    } else {
      msg.member.addToLastMessages(after);
      SendUtil.broadcastIncludingSender("_" + msg.member.getAlias() + " meant " + after + "_",
                                        msg.channel,
                                        msg.serverJID);
    }
  }

  public String documentation() {
    return "search and replace handler - use s/foo/bar to replace foo with bar";
  }

  public boolean matches(Message msg) {
    return pattern.matcher(msg.content.trim()).matches();
  }

}
