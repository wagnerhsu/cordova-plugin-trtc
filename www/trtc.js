var exec = require('cordova/exec');
var TRTC = {};

TRTC.init = function (sdkAppId, userInfoUrl, success, failure) {
    exec(
        success,
        failure,
        'TRTC',
        'init',
        [sdkAppId, userInfoUrl]
    );
};

TRTC.enterRoom = function (userId, userSig, roomId, success, failure) {
    exec(
        success,
        failure,
        'TRTC',
        'enterRoom',
        [userId, userSig, roomId]
    );
};

module.exports = TRTC;