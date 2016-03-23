# setpropex in Scala

This is an experiment of doing something low level in Android world use Scala language.

### what is setpropex

We can only use setprop to change Android system properties at some restriction, for example: you cann't change `ro.debuggable` propertie (actually all properties who's name starts with ro), setpropex use ptrace to write init process's memory directly to bypass all restrictions (of course need root privilege and at the risk of crash the system). This idea is come from the [original](https://github.com/poliva/rootadb) setpropex.

### benefits of use Scala:

1. All code is pure scala, make the coding process more enjoyable than c/c++.
2. Intellj for coding assistant & scalatest for TDD.
3. Needn't install ndk.
4. CPU architecture independent. The same program can run on different CPU architecture eg: `arm,arm64,x86_64,mips`. In contrast to c/c++ counterpart, which need rebuild with toolchains of the exactly target CPU architecture.

### drawbacks:

1. the size of result program is very huge(5M+, mostly Scala lib), this may reduced by proguard or [add Scala lib to `BOOTCLASSPATH`](https://zegoggl.es/2011/07/how-to-preinstall-scala-on-your-android-phone.html)(seems workable, haven't try yet)
2. the startup time is comparable slow, especially at the first time the program been run, which need to do AOT compilation.


### How to use

``` shell
git clone https://github.com/wuhx/setpropex
cd setpropex
sbt dalvik

adb push setpropex /data/local/tmp
adb shell 
su
cd /data/local/tmp/
./setpropex ro.debuggable 1
```







