<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.imjasonh.partychapp.Configuration" %>
<%@ page import="com.imjasonh.partychapp.Datastore"%>
<%@ page import="com.imjasonh.partychapp.server.json.UserInfoJsonServlet"%>

<%
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<link type="text/css" rel="stylesheet" href="Partychapp.css">
<script type="text/javascript">
      var _sf_startpt=(new Date()).getTime();
      function show(elt) {
        document.getElementById('actionOptions').style.display = 'none';
        document.getElementById(elt).style.display = '';
      }
    </script>
<title>Partychat</title>
<script>
  function displayChannels(userInfo, targetDiv) {
    targetDiv.setAttribute('style', 'display: block');
    var channels = userInfo['channels'];
    for (var i = 0; i < channels.length; i++) {
       var channelName = channels[i].name;
       var nameDiv = document.createElement("div");
       var nameAnchor = document.createElement("a");
       nameAnchor.href = "/channel/" + channelName;
       var nameNode = document.createTextNode(channelName);
       nameAnchor.appendChild(nameNode);
       nameDiv.appendChild(nameAnchor);
       nameDiv.appendChild(document.createTextNode(" (alias: " + channels[i].alias + ")"));
       targetDiv.appendChild(nameDiv);
    }
}
</script>
</head>
<body>
<div id="main">
<div id="loginlogout" style="text-align: right">
<%
	if (user != null) {
%> <a href="<%=userService.createLogoutURL(request.getRequestURI())%>">sign
out of <%=user.getEmail()%></a> <%
 	} else {
 %> <a href="<%=userService.createLoginURL(request.getRequestURI())%>">sign
in</a> <%
 	}
 %>
</div>
<div id="header"><img src="logo.png" width="310" height="150"
	alt="Partychat"></div>

<p>Create chat rooms with your friends or coworkers using Google
Talk or XMPP.</p>

<h3>Why use Partychat?</h3>
<ul>
	<li>Use whatever you use to chat already: GMail, Adium,...</li>
	<li>Catch up on messages you miss while offline.</li>
	<li>Don't have to re-join rooms when you log-out.</li>
	<li>Built on reliable Google App Engine.</li>
	<li>Easy to use, lots of <a href="#nowwhat">silly features</a>.</li>
</ul>
<%
	if (Configuration.isConfidential) {
%>
<h3>Are messages confidential?</h3>
Yup! We're running on an internal instance of AppEngine, so everything
stays safe. <%
  	}
  %>
<h3>How do I create a room?</h3>

<%
	if (user != null) {
		Datastore datastore = Datastore.instance();
		datastore.startRequest();
		com.imjasonh.partychapp.User pchappUser = datastore.getOrCreateUser(user.getEmail());
%>
<div id="actionOptions"><input type="button"
	value="Create a new room" onclick="show('create')" /></div>
<div id="create" style="display: none; border: 1px solid #ccc">
<table cellpadding=10>
	<tr>
		<td>
		<form action="/room" method="post" target="createResults">Pick
		a room name<br>
		<input type="text" name="name"> <br>
		<br>
		Do you only want people who are invited to be able to join?<br>
		<input type="radio" name="inviteonly" value="true" checked="yes">
		yes <input type="radio" name="inviteonly" value="false"> no <br>
		<br>
		Email addresses you would like to invite? (separated by commas)<br>
		<textarea name="invitees"></textarea> <br>
		<br>
		<input type="submit" value="Create!"></form>
		</td>
		<td><iframe frameborder=0 name="createResults"> </iframe></td>
	</tr>
</table>
</div>

<div id="channels" style="display: none">
<h3>My Channels</h3>
</div>
<script>
var userInfo = <%=UserInfoJsonServlet.getJsonFromUser(pchappUser, datastore)%>
displayChannels(userInfo, document.getElementById("channels"));
</script> <%
		datastore.endRequest();
	} else {
%> The easiest way to create a room is to <a style="font-weight: bold"
	href="<%=userService.createLoginURL(request.getRequestURI())%>">sign
in</a> and do it right here. <br />
<br />
Or you can add <tt>[roomname]@<%=Configuration.chatDomain%></tt> to your
buddy list and send it a message to join the room. If a room of that
name doesn't exist, a new one will be created. <%
	}
%>

<h3>How do I join a room?</h3>
The easiest way to join a room is to be invited. If the room has already
been created, have someone in the room type <tt>/invite
youremailaddress@anydomain.com</tt>. <br>
<br>
You should see an invitation from <tt>[roomname]@<%=Configuration.chatDomain%></tt>
in your chat window. Accept the invitation, and then <b>send a
message to your new buddy</b>, such as "hi." This will finish adding you to
the room. <br>
<br>
Alternatively, if a room is not invite-only, you can just add <tt>[roomname]@<%=Configuration.chatDomain%></tt>
to your buddy list and send it a message. <a name="nowwhat">
<h3>Okay, I'm in a room, now what?</h3>
Besides just sending messages and having everyone see them, most of the
things you can do take the form of commands you type as special chat
messages starting with a /.<br>
<br>
<img
	src="http://1.bp.blogspot.com/_qxrodbRnu8Q/SyL57yANfsI/AAAAAAAAD4w/pRdYP3wI_a4/s400/pchapp-shot.png">
<br>
<br>
You can get a full list of commands by sending the chat message <tt>/help</tt>
to the room. Some key ones:
<ul>
	<li><tt>/leave</tt> Leave this chat room. You can rejoin by
	sending another message to the room. If the room is invite-only, you
	may need to be re-invited.</li>
	<li><tt>/list</tt> See who is in the chat room.</li>
	<li><tt>/alias <i>newalias</i></tt> Change what name you show up
	as in the room.</li>
	<li><tt>/inviteonly</tt> Toggle whether this room is invite only.</li>
	<li><tt>/invite <i>someemail</i></tt> Invite someone to the room.</li>
	<li><tt>/me <i>someaction</i></tt> Tell the room what you're up
	to. If you type <tt>/me is rolling his eyes</tt>, everyone sees <tt>[youralias]
	is rolling his eyes</tt>.</li>
	<li><tt>/score <i>something</i></tt> This one's a bit complicated.
	You can give points to things you like by typing ++ at the end of them
	in your message. For example, you might say <tt>partychat++ for
	being so handy</tt>. This adds one to the score for partychat, which you can
	see by typing <tt>/score partychat</tt>. Or you can take points away
	from things you dislike, such as <tt>kushal-- for another bad pun</tt>.

</ul>

<h3>Tell me more about this "partychat"</h3>
Partychat was started by <a href=http://www.q00p.net />Akshay</a> and is
maintained by a motley, ragtag group of current and former Googlers with
names like Neil, Jason, and Kushal, although <i>this is not in any
way associated with Google</i>. You can find the source code on <a
	href="http://code.google.com/p/partychapp/">Google Code</a>. <br>
<br>
For updates, please subscribe to our <a
	href="http://techwalla.blogspot.com/">blog</a> or <a
	href="http://twitter.com/partychat">follow us on Twitter</a>. </div>
<script type="text/javascript">
var _sf_async_config={uid:2197,domain:"partychapp.appspot.com"};
(function(){
  function loadChartbeat() {
    window._sf_endpt=(new Date()).getTime();
    var e = document.createElement('script');
    e.setAttribute('language', 'javascript');
    e.setAttribute('type', 'text/javascript');
    e.setAttribute('src',
       (("https:" == document.location.protocol) ? "https://s3.amazonaws.com/" : "http://") +
       "static.chartbeat.com/js/chartbeat.js");
    document.body.appendChild(e);
  }
  var oldonload = window.onload;
  window.onload = (typeof window.onload != 'function') ?
     loadChartbeat : function() { oldonload(); loadChartbeat(); };
})();

</script>
</body>
</html>