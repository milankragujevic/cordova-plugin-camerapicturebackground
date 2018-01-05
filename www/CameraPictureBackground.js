var exec = require('cordova/exec');

exports.takePicture = function (success, error, options) {
	var defaults = {
		name: "Image",
		dirName: "CameraPictureBackground",
		orientation: "landscape",
		quality: 100,
		targetWidth: null,
		targetHeight: null,
		cameraDirection: "back"
	};

	if (options) {
		for (var key in defaults) {
			if (options[key] != undefined) {
				defaults[key] = options[key];
			}
		}
	}

	exec(success, error, "CameraPictureBackground", "takePicture", [defaults]);
};

exports.hasPermission = function (success, error) {
	exec(success, error, "CameraPictureBackground", "hasPermission", []);
};

exports.requestPermission = function (success, error) {
	exec(success, error, "CameraPictureBackground", "requestPermission", []);
};
