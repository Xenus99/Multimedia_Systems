# Image Rotation and Zooming
An image can be rotated and zoomed in/out as a video by pixel manipulation using the given parameters. It takes 4 inputs: A .rgb file, ZoomFactor, RotationAngle, FPS
1. .rgb file: It is an image file represented as rgb pixel values.
2. ZoomFactor: This defines the amount of zoom per second. Values above 1 are for zoom in. Values between 0 and 1 are zoom out.
3. RotationAngle: This defines the angle of rotation per second. Positive values rotate clockwise and negative rotate ani-clockwise.
4. FPS: The framerate for the transition of the image.

# Requirements
1. Java

# How To Run
1. javac ImageDisplay.java
2. java ImageDisplay FILE Z R F
3. In the above command, the parameters FILE, Z, R, F stand for .rgb file(Lena.rgb), Zoom(0 to ∞), Rotation(0 to 360 degrees), FPS(1 to ∞)
