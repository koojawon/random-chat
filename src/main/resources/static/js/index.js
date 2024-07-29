var ws = new WebSocket("ws://" + location.host + "/randomChat");
var oppoVideo;
var myVideo;
var webRtcPeer;
var pc;
var chatBox;
var inputBox;
var sendButton;
var owner;

const MEDIA_CONSTRAINTS = {
  video: true,
  audio: true,
};

window.onload = () => {
  oppoVideo = document.getElementById("video");
  myVideo = document.getElementById("myVideo");
  inputBox = document.getElementById("inputBox");
  sendButton = document.getElementById("sendButton");
  chatBox = document.getElementById("chatBox");
  inputBox.addEventListener("keyup", enterKey);
  recalcChatBoxHeight();
  disableStopButton();
};

window.onresize = () => {
  recalcChatBoxHeight();
};

window.onbeforeunload = () => {
  ws.close();
};

ws.onmessage = (message) => {
  var parsedMessage = JSON.parse(message.data);
  switch (parsedMessage.id) {
    case "sdpgenorder":
      clearChat();
      appendSysMessage("매칭되었습니다!");
      generateOffer();
      break;
    case "sdpOffer":
      clearChat();
      appendSysMessage("매칭되었습니다!");
      handleOffer(parsedMessage);
      break;
    case "sdpAnswer":
      handleAnswer(parsedMessage);
      break;
    case "onIceCandidate":
      webRtcPeer.addIceCandidate(parsedMessage.candidate, (error) => {
        if (error) return console.log("Error adding candidate: " + error);
      });
      break;
    case "stop":
      appendSysMessage("상대방과 연결이 끊어졌습니다.");
      dispose();
      break;
    default:
      console.log("Unrecognized message : " + parsedMessage);
  }
};

function handleOffer(message) {
  createRTC(() =>
    webRtcPeer.processOffer(message.sdpOffer, (e) => {
      if (e) return console.log(e);
    })
  );
}

function handleAnswer(message) {
  webRtcPeer.processAnswer(message.sdpAnswer, function (error) {
    if (error) return console.log(error);
  });
}

function start() {
  var startMessage = {
    id: "start",
  };
  enableStopButton();
  sendMessage(startMessage);
}

function generateOffer() {
  owner = true;
  createRTC();
}

function createRTC(callback) {
  if (!webRtcPeer) {
    showSpinner(myVideo);
    showSpinner(oppoVideo);

    var options = {
      localVideo: myVideo,
      remoteVideo: oppoVideo,
      onicecandidate: onIceCandidate,
      dataChannels: true,
      dataChannelConfig: {
        label: "ranChatData",
        onopen: dataChannelOpen,
        onmessage: dataChannelMessage,
        onclose: dataChannelClose,
        onerror: dataChannelError,
        options:{
            negotiated: true,
            id: 0,
        }
      },
      configuration: {
        iceServers: [
          { urls: "stun:stun.l.google.com:19302" },
          { urls: "stun:stun.l.google.com:5349" },
          { urls: "stun:stun1.l.google.com:3478" },
          { urls: "stun:stun1.l.google.com:5349" },
          { urls: "stun:stun2.l.google.com:19302" },
          { urls: "stun:stun2.l.google.com:5349" },
          { urls: "stun:stun3.l.google.com:3478" },
          { urls: "stun:stun3.l.google.com:5349" },
          { urls: "stun:stun4.l.google.com:19302" },
          { urls: "stun:stun4.l.google.com:5349" },
        ],
      },
    };
    webRtcPeer = new WebRtcPeer(options, (error) => {
      if (error) {
        return console.log(error);
      }
      if (owner) webRtcPeer.generateOffer(onOfferPresenter);
      if (callback) callback();
    });
  }
}

function dataChannelOpen() {
  inputBox.disabled = false;
  sendButton.addEventListener("click", sendChat);
}

function dataChannelClose() {
  inputBox.disabled = true;
  sendButton.removeEventListener("click", sendChat);
}

function dataChannelMessage(e) {
  var data = e.data;
  var newBubble = document.createElement("div");
  var textNode = document.createTextNode(data);
  newBubble.className += "bubble you";
  newBubble.appendChild(textNode);
  chatBox.appendChild(newBubble);
  chatBox.scrollTop = chatBox.scrollHeight;
}

function dataChannelError(e) {
  console.error(e);
}

//webrtcpeer 선언
function WebRtcPeer(options, callback) {
  callback = callback.bind(this);

  var localVideo = options.localVideo;
  var remoteVideo = options.remoteVideo;
  var dataChannelConfig = options.dataChannelConfig;
  var useDataChannels = options.dataChannels;
  this.dataChannel;

  var configuration = options.configuration;
  var candidatesQueueOut = [];
  this.candidategatheringdone = false;

  // Init PeerConnection
  if (!pc) {
    pc = new RTCPeerConnection(configuration);
    if (useDataChannels && !this.dataChannel) {
      var dcId = dataChannelConfig.label || dcId;
      var dcOptions = dataChannelConfig.options;
      this.dataChannel = pc.createDataChannel(dcId, dcOptions);
      this.dataChannel.onopen = dataChannelConfig.onopen;
      this.dataChannel.onclose = dataChannelConfig.onclose;
      this.dataChannel.onmessage = dataChannelConfig.onmessage;
      this.dataChannel.onbufferedamountlow =
        dataChannelConfig.onbufferedamountlow;
      this.dataChannel.onerror = dataChannelConfig.onerror;
    }
  }

  pc.onicecandidate = (event) => {
    var candidate = event.candidate;
    if (candidate) {
      onIceCandidate(candidate);
      this.candidategatheringdone = false;
    } else if (!this.candidategatheringdone) {
      this.candidategatheringdone = true;
    }
    if (!this.candidategatheringdone) {
      candidatesQueueOut.push(candidate);
      if (!candidate) this.candidategatheringdone = true;
    }
  };
  pc.onaddstream = options.onaddstream;
  pc.onnegotiationneeded = options.onnegotiationneeded;

  pc.addEventListener("newListener", (event, listener) => {
    if (event === "icecandidate" || event === "candidategatheringdone") {
      while (candidatesQueueOut.length) {
        var candidate = candidatesQueueOut.shift();
        if (!candidate === (event === "candidategatheringdone")) {
          listener(candidate);
        }
      }
    }
  });

  var addIceCandidate = bufferizeCandidates(pc);

  this.addIceCandidate = (iceCandidate, callback) => {
    var candidate = new RTCIceCandidate(iceCandidate);
    callback = callback.bind(this);
    addIceCandidate(candidate, callback);
  };

  this.generateOffer = (callback) => {
    pc.createOffer()
      .then((offer) => {
        return pc.setLocalDescription(offer);
      })
      .then(callback)
      .catch(callback);
  };

  function setRemoteVideo() {
    if (remoteVideo) {
      remoteVideo.pause();
      var tempStream = new MediaStream();
      pc.getReceivers().forEach((sender) => {
        tempStream.addTrack(sender.track);
      });
      var stream = [tempStream][0];
      remoteVideo.srcObject = stream;
      remoteVideo.load();
    }
  }

  this.send = (data) => {
    if (this.dataChannel && this.dataChannel.readyState === "open") {
      this.dataChannel.send(data);
    } else {
      console.error(
        "Trying to send data over a non-existing or closed data channel"
      );
    }
  };

  this.processAnswer = (sdpAnswer, callback) => {
    callback = callback.bind(this);
    var answer = new RTCSessionDescription(sdpAnswer);

    if (pc.signalingState === "closed") {
      return callback("PeerConnection is closed");
    }

    pc.setRemoteDescription(answer).then(() => {
      setRemoteVideo();
      callback();
    }, callback);
  };

  this.processOffer = (sdpOffer, callback) => {
    callback = callback.bind(this);
    var offer = new RTCSessionDescription(sdpOffer);
    if (pc.signalingState === "closed") {
      return callback("PeerConnection is closed");
    }

    pc.setRemoteDescription(offer)
      .then(() => {
        return setRemoteVideo();
      })
      .then(() => {
        return pc.createAnswer();
      })
      .then((answer) => {
        var message = {
          id: "sdpAnswer",
          sdpAnswer: answer,
        };
        sendMessage(message);
        return pc.setLocalDescription(answer);
      })
      .then(() => {
        var localDescription = pc.localDescription;
        callback(null, localDescription.sdp);
      })
      .catch(callback);
  };

  function start() {
    if (pc.signalingState === "closed") {
      callback(
        'The peer connection object is in "closed" state. This is most likely due to an invocation of the dispose method before accepting in the dialogue'
      );
    }

    if (videoStream && localVideo) {
      localVideo.srcObject = videoStream;
      localVideo.muted = true;
    }

    if (videoStream) {
      videoStream.getTracks().forEach(function (track) {
        console.info("adding track");
        pc.addTrack(track, videoStream);
      });
    }
    callback();
  }

  navigator.mediaDevices
    .getUserMedia(MEDIA_CONSTRAINTS)
    .then((stream) => {
      videoStream = stream;
      start();
    })
    .catch(callback);

  this.dispose = () => {
    if (localVideo) {
      localVideo.pause();
      localVideo.srcObject = null;
      if (typeof AdapterJS === "undefined") {
        localVideo.load();
      }
      localVideo.muted = false;
    }
    if (remoteVideo) {
      remoteVideo.pause();
      remoteVideo.srcObject = null;
      if (typeof AdapterJS === "undefined") {
        remoteVideo.load();
      }
    }
    if (
      typeof window !== "undefined" &&
      window.cancelChooseDesktopMedia !== undefined
    ) {
      window.cancelChooseDesktopMedia(guid);
    }
  };
}

function bufferizeCandidates(pc, onerror) {
  var candidatesQueue = [];

  pc.addEventListener("signalingstatechange", () => {
    if (pc.signalingState === "stable") {
      while (candidatesQueue.length) {
        var entry = candidatesQueue.shift();
        pc.addIceCandidate(entry.candidate, entry.callback, entry.callback);
      }
    }
  });

  return function (candidate, callback) {
    callback = callback || onerror;
    switch (pc.signalingState) {
      case "closed":
        callback(new Error("PeerConnection object is closed"));
        break;
      case "stable":
        if (pc.remoteDescription) {
          pc.addIceCandidate(candidate, callback, callback);
          break;
        }
      default:
        candidatesQueue.push({
          candidate: candidate,
          callback: callback,
        });
    }
  };
}

function onOfferPresenter(error) {
  if (error) return console.log("Error generating the offer" + error);
  var offerSdp = pc.localDescription;
  var message = {
    id: "sdpOffer",
    sdpOffer: offerSdp,
  };
  sendMessage(message);
}

function onIceCandidate(candidate) {
  var message = {
    id: "onIceCandidate",
    candidate: candidate,
  };
  sendMessage(message);
}

function stop() {
  appendSysMessage("연결을 종료했습니다.");
  var message = {
    id: "stop",
  };
  sendMessage(message);
  dispose();
}

function dispose() {
  if (webRtcPeer) {
    webRtcPeer.dispose();
    webRtcPeer = null;
    pc = null;
  }
  hideSpinner(myVideo, oppoVideo);
  disableStopButton();
}

function disableStopButton() {
  enableButton("start", "start()");
  disableButton("stop");
}

function enableStopButton() {
  disableButton("start");
  enableButton("stop", "stop()");
}

function disableButton(id) {
  document.getElementById(id).setAttribute("disabled", true);
  document.getElementById(id).removeAttribute("onclick");
}

function enableButton(id, functionName) {
  document.getElementById(id).setAttribute("disabled", false);
  document.getElementById(id).setAttribute("onclick", functionName);
}

function sendMessage(message) {
  var jsonMessage = JSON.stringify(message);
  ws.send(jsonMessage);
}

function showSpinner() {
  for (var i = 0; i < arguments.length; i++) {
    arguments[i].style.background =
      'center transparent url("./img/spinner.gif") no-repeat';
  }
}

function hideSpinner() {
  for (var i = 0; i < arguments.length; i++) {
    arguments[i].src = "";
    arguments[i].poster = "";
    arguments[i].style.background = "";
  }
}

function recalcChatBoxHeight() {
  chatBox.style.maxHeight =
    document.getElementById("mainRow").clientHeight - 48 + "px";
}

function enterKey() {
  if (event.key == 'Enter') {
    sendChat();
  }
}

function appendSysMessage(str) {
  var newBubble = document.createElement("div");
  var inSpan = document.createElement("span");
  var text = document.createTextNode(str);
  inSpan.appendChild(text);
  newBubble.className += "sysMessage";
  newBubble.appendChild(inSpan);
  chatBox.appendChild(newBubble);
}

function clearChat() {
  if (chatBox.childNodes) chatBox.replaceChildren();
}

function sendChat() {
  var text = inputBox.value;
  if (text == "") return;
  var newBubble = document.createElement("div");
  var textNode = document.createTextNode(text);
  newBubble.className += "bubble me";
  newBubble.appendChild(textNode);
  chatBox.appendChild(newBubble);
  chatBox.scrollTop = chatBox.scrollHeight;
  inputBox.value = "";
  webRtcPeer.send(text);
}
