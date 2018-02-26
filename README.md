# cordova-plugin-camerapicturebackground
Automatically take Picture from Android Smartphones without any User Interactions.

### Supported Platform

* Android

### Installation

Plugin can be installed using the [Command Line Interface](http://cordova.apache.org/docs/en/4.0.0/guide_cli_index.md.html#The%20Command-Line%20Interface):

````
cordova plugin add https://github.com/netinhoteixeira/cordova-plugin-camerapicturebackground.git
````

### Plugin Usage

````
var options = {
    name: "Image", // image suffix
    dirName: "CameraPictureBackground", // foldername
    orientation: "landscape", // or portrait
    cameraDirection: "back" // or front
    quality: 60,
    width: 800,
    height: 1200
};

window.plugins.CameraPictureBackground.takePicture(success, error, options);

function success(imgurl) {
    console.log("Imgurl = " + imgurl);
}
````

##### Here options are

Option | Description
------- | -------
**name** | Suffix to be added while saving the image for .e.g Image-yyyymmddhhmmss
**dirName** | Folder will be created with this name in Pictures directory of external storage.
**orientation** | while taking the picture, camera should be in landscape or portrait mode.
**cameraDirection** | Either to use front or bank camera.
**quality** | Quality of output image (optional).
**width** | Width of output image (optional).
**height** | Height of output image (optional).


### Credits

Stackoverflow user Sam - http://stackoverflow.com/a/27083867/1584921
