# FaceBlock - HackMobile 2018 Demo

This project includes code for the FaceBlock demo app that won first place at HackMobile 2018. 
<br>
<p align="center">
<img src="https://github.qualcomm.com/raw/oandrien/faceblock-demo-app/master/thumbnail.gif">
</p>
<br>

#### Compatibility

In order to have the detection model run in real-time, GPU acceleration must be used. This is only supported on the QRD 8998, QRD 710, Galaxy S8/S8+ and Galaxy S9/S9+.

The project will likely work on any Android M+ phone. DSP acceleration is not available in this version of the project.

## Dependencies

+ Android SDK v27
+ Android NDK v16b
+ TensorFlow v1.8 *(optional)*
+ TensorFlow Object Detection API (November 17, 2017 Release) *(optional)*
+ OpenCV4Android v3.2.4 *(optional)*
+ Qualcomm's SNPE SDK v1.15 *(optional)*

The optional items are listed for users who wish to train and deploy their own TensorFlow based models onto the app. They are not required if a user simply wants to use the defaults provided.

## Build Instructions

Begin by setting up your Android development environment and installing the above dependencies. Ensure you have the correct version of the Android SDK, Android NDK and Android Studio installed. The converted DLC models used are supplied in this project, so the portion of this guide will be about the build process.

After setting up your environment, first clone the project to your desired location with
```
git clone https://github.qualcomm.com/oandrien/faceblock-demo-app
```
Then, import the project through Android Studio as an existing project.

#### Building OpenCV4Android

If you are not using the prebuilt APK, you must then build OpenCV for Android in order to get access to the `tracking` module. The high level overview of this process is as follows:

Clone the OpenCV project and the OpenCV extra modules repository
```
git clone https://github.com/opencv/opencv.git
git clone https://github.com/opencv/opencv_contrib.git
```
From here, you must build OpenCV4Android using the supplied build script. This can be found in `opencv/platforms/android`. For this project, Android SDK Build-tools 27.0.3 and Android API Level 27 were used. For this step, make sure you have the Ninja build system and the Apache Ant command line tool installed. An example of running the build script is as follows 

```
export ANDROID_NATIVE_API_LEVEL=27 
export ANDROID_PROJECTS_BUILD_TYPE=ANT
./build_sdk.py \ 
	--sdk_path YOUR_SDK_LOCATION \
	--ndk_path YOUR_NDK_LOCATION \
	--extra_modules_path YOUR_OPENCV_CONTRIB_LOCATION/modules \
	--config ndk-16.config.py YOUR_OPENCV_LOCATION/build_android \
	YOUR_OPENCV_LOCATION
```
#### Adding OpenCV4Android to the project

After the OpenCV Android build is complete, we can now import OpenCV as a module in Android Studio. 

To import OpenCV4Android as a module, go to `Menu -> "File" -> "New" -> "Module" -> "Import Gradle project"`. Select the `sdk` directory in the final build location that you specified, and name the module `opencv`.

Then, add the dependency into the application module by going to `"Open Module Settings" (F4) -> "Dependencies" tab`.

#### Building the complete project

After syncing has completed, build the project and deploy to one of the devices listed above.

## Advanced Instructions

This repository contains the required TensorFlow Checkpoints and SNPE DLC files required to run the app. However, if you wish to train your own model, please refer to the instructions located in the TensorFlow Models Object Detection API repository. 

We trained a MobileNetV1 SSD model on the WIDER dataset. Your dataset of choice must be converted to a TF record format for training. To convert the model using the SNPE converter, you must first freeze your model with the Object Detection export script. Some helpful links are found below:

+ Object Detection API Installation Documentation: https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/running_locally.md
+ WIDER Dataset Download Instructions: http://mmlab.ie.cuhk.edu.hk/projects/WIDERFace/
+ Preparing TFRecords For Training Model https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/using_your_own_dataset.md
+ Converting from Tensorflow checkpoint to frozen model:
https://github.com/tensorflow/tensorflow/blob/master/tensorflow/python/tools/freeze_graph.py
+ Converting from Frozen Tensorflow Model to SNPE DLC format:
https://developer.qualcomm.com/docs/snpe/model_conv_tensorflow.html

## Contributors
+ Oles Andrienko ([oandrien@uwaterloo.ca](mailto:[oandrien@uwaterloo.ca))
+ Zhiyu Liang ([edward.liang@mail.utoronto.ca](edward.liang@mail.utoronto.ca))
+ Alexander Li ([alexli@cs.toronto.edu](alexli@cs.toronto.edu))
