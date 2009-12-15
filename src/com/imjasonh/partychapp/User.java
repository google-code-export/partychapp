package com.imjasonh.partychapp;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.repackaged.com.google.common.collect.Lists;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class User implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 89437432538532985L;

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(User.class.getName());

  @Persistent
  @PrimaryKey
  private String jid;

  @Persistent
  List<String> channelNames;
  
  @Persistent
  String phoneNumber;
  
  @Persistent
  String carrier;
  
  @Persistent
  Date lastSeen;

  // I stole from http://en.wikipedia.org/wiki/List_of_carriers_providing_SMS_transit
  public enum Carrier {
    ATT("at&t", "txt.att.net", false),
    VERIZON("verizon", "vtext.net", true),
    TMOBILE("tmobile", "tmomail.net", true),
    SPRINT("sprint", "messaging.sprintpcs.com", true),
    VIRGIN("virgin", "vmobl.com", true)
    ;

    public final String shortName;
    public final String emailToSmsDomain;
    public final boolean wantsLeadingOne;

    private Carrier(String shortName, String emailToSmsDomain, boolean wantsLeadingOne) {
      this.shortName = shortName;
      this.emailToSmsDomain = emailToSmsDomain;
      this.wantsLeadingOne = wantsLeadingOne;
    }

    public String emailAddress(String phoneNumber) {
      if (phoneNumber == null) {
        return null;
      }
      if (wantsLeadingOne) {
        if (!phoneNumber.startsWith("1")) {
          phoneNumber = "1" + phoneNumber;
        }
      } else {
        if (phoneNumber.startsWith("1")) {
          phoneNumber = phoneNumber.substring(1);
        }
      }
      return phoneNumber + "@" + emailToSmsDomain;
    }
  }

  public User(String jid) {
    this.jid = jid;
    this.channelNames = Lists.newArrayList();
  }

  public User(User other) { 
    this.channelNames = other.channelNames;
    this.jid = other.jid;
    this.phoneNumber = other.phoneNumber;
    this.carrier = other.carrier;
    this.lastSeen = other.lastSeen;
  }

  public String getJID() {
    return jid;
  }
  
  public String getEmail() {
    // TODO(nsanch): this isn't quite right because it's possible to have a
    // jabber account that doesn't accept email, but this is good enough until
    // we have a web UI for this.
    return jid;
  }

  public String phoneNumber() {
    return phoneNumber;
  }
  
  public void setPhoneNumber(String phone) {
    phoneNumber = phone;
  }
  
  public User.Carrier carrier() {
    if (carrier == null) {
      return null;
    } 
    return Carrier.valueOf(carrier);
  }
  
  public void setCarrier(Carrier carrier) {
    this.carrier = carrier.name();
  }

  public boolean canReceiveSMS() {
    return carrier != null && phoneNumber != null;
  }
  
  public Date lastSeen() {
    return lastSeen;
  }
  
  public void markSeen() {
    lastSeen = new Date();
  }
  
  public List<String> channelNames() {
    return Collections.unmodifiableList(channelNames);
  }
  
  public void addChannel(String c) {
    if (!channelNames.contains(c)) {
      channelNames.add(c);
      
      // I feel dirty doing this! There is some opaque JDO bug that makes
      // this not save.
      JDOHelper.makeDirty(this, "channelNames");
    }
  }

  public void fixUp() {
  }
  
  public void put() {
    Datastore.instance().put(this);
  }

  public String toString() {
    return "[User: jid: " + jid + ", phoneNumber: " + phoneNumber +
      ", carrier: " + carrier + ", channelNames: " + channelNames +
      "]";
  }
}