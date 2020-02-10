# Run binaries for android

Compress your binaries and put it in "assets.zip" in assets folder.

Create an Utils class instance in your activity and decompress them then run the binaries:
```aidl
this.utils = new Utils(this);
this.utils.decompress(); // Decompress all file
this.utils.startBinaries("binary name"); // Run binary
```
You can see the exemple app for details