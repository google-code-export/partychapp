package com.imjasonh.partychapp.ppb;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.repackaged.com.google.common.collect.Maps;
import com.google.appengine.repackaged.com.google.common.collect.Sets;
import com.imjasonh.partychapp.Datastore;
import com.imjasonh.partychapp.Message;

public class PlusPlusBot {
  private static Set<Pattern> blacklist = Sets.newHashSet();
  public static final String targetPattern = "[\\w-\\.\\+]+";
  private static final Pattern pattern =
    Pattern.compile("(" + targetPattern + ")(\\+\\+|--)($|\\s+)");

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(PlusPlusBot.class.getName());


  static {
    // c++
    blacklist.add(Pattern.compile("c"));
    // <----
    blacklist.add(Pattern.compile("-*"));
  }

  public enum Action {
    PLUSPLUS, MINUSMINUS;
    
    public boolean isPlusPlus() {
      return equals(PLUSPLUS);
    }
    
    public Action opposite() {
      return ifPlusPlusElse(MINUSMINUS, PLUSPLUS);
    }
    
    public <T extends Object> T ifPlusPlusElse(T then, T otherwise) {
      if (isPlusPlus()) {
        return then;
      } else {
        return otherwise;
      }
    }
  };

  public boolean matches(String content) {
    // use find() instead of matches() because matches() looks for the whole
    // string to match and we're okay with substring matches.
    return pattern.matcher(content.trim()).find();
  }

  public List<Reason> extractReasonsNoCommit(Message msg) {
    return extractReasonsHelper(msg, false);
  }

  public List<Reason> extractReasons(Message msg) {
    return extractReasonsHelper(msg, true);
  }

  public List<Reason> extractReasonsHelper(Message msg, boolean mutateObjects) {
    List<Reason> reasons = Lists.newArrayList();
    Set<Target> targets = Sets.newHashSet();
    Map<String, Target> alreadyFetched = Maps.newHashMap();
    Map<String, Integer> scores = Maps.newHashMap();
    Matcher m = pattern.matcher(msg.content);
    while (m.find()) {
      String target = m.group(1).toLowerCase();
      String action = m.group(2);
      boolean blacklisted = false;
      for (Pattern p : blacklist) {
        if (p.matcher(target).matches()) {
          blacklisted = true;
          break;
        }
      }
      if (blacklisted) {
        continue;
      }
      Target t = alreadyFetched.get(target);
      if (t == null) {
        t = Datastore.instance().getOrCreateTarget(msg.channel, target);
        alreadyFetched.put(target, t);
      }
      Action a = action.equals("--") ? Action.MINUSMINUS : Action.PLUSPLUS;
      targets.add(t);
      if (mutateObjects) {
        reasons.add(t.takeAction(msg.member, a, msg.content));
      } else {
        if (!scores.containsKey(t.key())) {
          scores.put(t.key(), t.score());
        }
        int scoreAfter = scores.get(t.key());
        if (a == Action.PLUSPLUS) {
          ++scoreAfter;
        } else {
          --scoreAfter;
        }
        scores.put(t.key(), scoreAfter);
        reasons.add(new Reason(t, msg.member, a, msg.content, scoreAfter));
      }
    }
    if (mutateObjects) {
      List<Serializable> toSave = Lists.newArrayList();
      toSave.addAll(targets);
      toSave.addAll(reasons);
      Datastore.instance().putAll(toSave);
    }
    return reasons;
  }

  public List<Reason> undoEarlierMessage(Message msg) {
    List<Reason> reasonsBefore = extractReasonsNoCommit(msg);

    List<Serializable> toSave = Lists.newArrayList();
    List<Reason> allUndos = Lists.newArrayList();
    for (Reason r : reasonsBefore) {
      Reason undone = r.undo();
      toSave.add(undone.target());
      toSave.add(undone);
      allUndos.add(undone);
    }

    Datastore.instance().putAll(toSave);
    return allUndos;
  }
}