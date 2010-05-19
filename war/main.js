// TODO: figure out if we can still use goog.require
//goog.require('goog.dom');
//goog.require('goog.dom.classes');
//goog.require('goog.net.XhrIo');
//goog.require('goog.string');
//goog.require('partychapp.templates');

function showCreateForm() {
  goog.dom.classes.add(goog.dom.$('create-button-container'), 'hidden');
  goog.dom.classes.remove(goog.dom.$('create-table'), 'hidden');
}

function submitCreateRoom() {
  var roomName = goog.dom.$('room-name').value;
  var inviteOnly = goog.dom.$('inviteonly-true').checked;
  var invitees = goog.dom.$('invitees').value;

  if (goog.string.isEmptySafe(roomName)) {
    alert('Please enter a room name.');
    return false;
  }

  goog.net.XhrIo.send(
      '/channel/create',
      function(e) {
        var resultNode = goog.dom.$('create-result');
        goog.dom.classes.remove(resultNode, 'hidden');
        var xhr = e.target;
        resultNode.innerHTML = xhr.getResponseText();
      },
      'POST',
      'name=' + encodeURIComponent(roomName) +
          '&inviteonly=' + inviteOnly +
          '&invitees=' + encodeURIComponent(invitees));

  return false;
}

function acceptInvitation(channelName) {
  window.location.href =
      '/channel/invitation/accept?name=' + encodeURIComponent(channelName);
}

function declineInvitation(channelName) {
  window.location.href =
      '/channel/invitation/decline?name=' + encodeURIComponent(channelName);
}

function formatDate(date) {
 function pad(n) {return n < 10 ? '0' + n : n}
 return pad(date.getMonth() + 1) + '/' +
        pad(date.getDate())+ '/' +
        date.getFullYear();
}

function addTargetDetails(targetName, targetCellNode, data) {
	var reasonsNode = goog.dom.$dom('ul', 'reasons');

  for (var i = 0, reason; reason = data.reasons[i]; i++) {
    var reasonNode = goog.dom.$dom('li');

    var actionNode = goog.dom.$dom('span', 'action');
    goog.dom.classes.enable(actionNode, 'plusplus', reason.action == '++');
    goog.dom.classes.enable(actionNode, 'minusminus', reason.action != '++');
    actionNode.innerHTML = reason.action;
    reasonNode.appendChild(actionNode);

    var senderConnectorNode = goog.dom.$dom('span', {}, '\'ed by ');
    reasonNode.appendChild(senderConnectorNode);

    var senderNode = goog.dom.$dom('span', 'sender', reason.sender);
    reasonNode.appendChild(senderNode);

    // Only show the short version of the reason (if any), so strip out
    // everything before the action...
    var targetLocation = reason.reason.toLowerCase().indexOf(
        targetName.toLowerCase() + reason.action);
    if (targetLocation != -1) {
      var reasonDetails = reason.reason.substring(
          targetLocation + targetName.length + 2);

      if (!goog.string.isEmptySafe(reasonDetails)) {
        // ...but still have the full reason line as a tooltip.
        var reasonDetailsNode = goog.dom.$dom('span', {
          'title': reason.reason
        }, reasonDetails);
        reasonNode.appendChild(reasonDetailsNode);
      }
    }

    var dateNode = goog.dom.$dom(
        'span',
        'date',
        ' on ' + formatDate(new Date(reason.timestampMsec)));
    reasonNode.appendChild(dateNode);

    reasonsNode.appendChild(reasonNode);
  }

  var graphNode = goog.dom.$dom('img', {
    'src': data.graph
  });

	var detailsNode =
	    goog.dom.$dom('div', 'target-details', graphNode, reasonsNode);
	targetCellNode.appendChild(detailsNode);
}

function toggleTargetDetails(targetNameNode, channelName, targetName) {
  var targetCellNode = targetNameNode.parentNode;
  var targetRowNode = targetCellNode.parentNode;

  goog.dom.classes.toggle(targetRowNode, 'target-expanded');

  // Check if we've already populated the details node
  if (goog.dom.$$('div', 'target-details', targetCellNode).length != 0) {
    return;
  }

  goog.dom.classes.add(targetRowNode, 'target-loading');

  // Otherwise fill it in (this will only happen when expanding the first time)
	var url = '/targetdetailsjson/' + channelName + '/' + targetName;
  goog.net.XhrIo.send(url, function(e) {
    goog.dom.classes.remove(targetRowNode, 'target-loading');
    var xhr = e.target;
    addTargetDetails(targetName, targetCellNode, xhr.getResponseJson());
  });
}

function displayChannels(userInfo, targetNode) {
  targetNode.setAttribute('style', 'display: block');
  if (userInfo.error) {
    targetDiv.innerHTML = "ERROR: " + userInfo.error;
    return;
  }

  var channelListNode = goog.dom.$dom('ul', 'channel-list');

  var channels = userInfo['channels'];
  for (var i = 0, channel; channel = channels[i]; i++) {
    var linkNode = goog.dom.$dom(
        'a',
        {'href': '/channel/' + channel.name},
        channel.name);
    var descriptionNode = goog.dom.$dom(
        'span',
        'description',
        ' as ',
        goog.dom.$dom('b', {}, channel.alias),
        channel.memberCount > 1
            ? ' with ' + (channel.memberCount - 1) +
                (channel.memberCount == 2 ? ' other' : ' others')
            : '');
    var channelNode = goog.dom.$dom('li', {}, linkNode, descriptionNode);
    channelListNode.appendChild(channelNode);
  }

  targetNode.appendChild(channelListNode);
}

function printEmail(opt_anchorText) {
  var a = [112, 97, 114, 116, 121, 99, 104, 97, 112, 112, 64, 103, 111, 111,
      103, 108, 101, 103, 114, 111, 117, 112, 115, 46, 99, 111, 109];
  var b = [];
  for (var i = 0; i < a.length; i++) {
    b.push(String.fromCharCode(a[i]));
  }
  b = b.join('');
  document.write('<' + 'a href="mailto:' + b + '">' +
                 (opt_anchorText || b) +
                 '<' + '/a>');
}

/**
 * @enum {number}
 */
var SortOrder = {
  BY_NAME: 1,
  BY_SCORE: 2
};

var UP_ARROW = '&#8679;';
var DOWN_ARROW = '&#8681;';

/**
 * @constructor
 * @param {string} channelName
 * @param {Array.<object>} targetList
 */
function ScoreTable(channelName, targetList) {
  this.channelName = channelName;
  this.targetList = targetList;
  this.sortOrder = undefined;
  this.sortByName();
}

ScoreTable.prototype.sortByName = function() {
  if (this.sortOrder == SortOrder.BY_NAME) {
    this.targetList.reverse();
    this.toggleArrow();
  } else {
    this.sortOrder = SortOrder.BY_NAME;
    this.arrow = UP_ARROW;
    this.targetList.sort(function(a, b) { return a['name'].localeCompare(b['name']); });
  }

  this.draw();
}

ScoreTable.prototype.toggleArrow = function() {
  if (this.arrow == DOWN_ARROW) {
    this.arrow = UP_ARROW;
  } else {
    this.arrow = DOWN_ARROW;
  }
}

ScoreTable.prototype.sortByScore = function() {
  if (this.sortOrder == SortOrder.BY_SCORE) {
    this.targetList.reverse();
    this.toggleArrow();
  } else {
    this.sortOrder = SortOrder.BY_SCORE;
    this.arrow = DOWN_ARROW;
    this.targetList.sort(function(a, b) { return b['score'] - a['score']; });
  }

  this.draw();
}

ScoreTable.prototype.draw = function() {
  soy.renderElement(
      goog.dom.$('score-table'),
      partychapp.templates.scoreTable,
      { 'channelName': this.channelName,
        'targets': this.targetList });

  var nameHeader = goog.dom.$('target-name-header');
  var scoreHeader = goog.dom.$('target-score-header');

  nameHeader.onclick = goog.bind(this.sortByName, this);
  scoreHeader.onclick = goog.bind(this.sortByScore, this);

  if (this.sortOrder == SortOrder.BY_NAME) {
    nameHeader.innerHTML = this.arrow + nameHeader.innerHTML;
  } else {
    scoreHeader.innerHTML = this.arrow + scoreHeader.innerHTML;
  }
}
