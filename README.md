Status: [![Build Status](https://travis-ci.org/bitcoin-solutions/mbhd-hardware.png?branch=master)](https://travis-ci.org/bitcoin-solutions/mbhd-hardware)

### Project status

Alpha: Expect bugs and API changes. Not suitable for production, but early adopter developers should get on board.

### MultiBit Hardware

MultiBit HD (MBHD) supports hardware wallets through a common API which is provided here under the MIT license.

Hardware wallet implementers can free themselves of the burden of writing their own wallet software by using MBHD
or they can use the API provided here to create their own and take advantage of the code and utilities provided.

One example of a supported hardware wallet is the Trezor and full examples and documentation is available for review.

### Technologies

* [Java HID API](https://code.google.com/p/javahidapi/) - Java library providing USB Human Interface Device (HID) native interface
* [Google Protocol Buffers](https://code.google.com/p/protobuf/) (protobuf) - for the most efficient and flexible wire protocol
* [Google Guava](https://code.google.com/p/guava-libraries/wiki/GuavaExplained) - for excellent Java support features
* Java 7+ - to remove dependencies on JVMs that have reached end of life

### Frequently asked questions (FAQ)

#### Why Google Protocol Buffers ?

We believe that the use of Google Protocol Buffers for "on the wire" data transmission provides the best balance between an efficient
binary format and the flexibility to allow a wide range of other languages to access the device. Having your device speak "protobuf"
means that someone can rip out the `.proto` files and use them to generate the appropriate accessor code for their language (e.g. Ruby).

#### Do you support git submodules for `.proto` files ?

Yes. While you can have treat your module within the MultiBit Hardware project as the single point of (Java) reference, you don't have to.

You are also free to offer up your `.proto` files as common libraries that remain in your repo and under your control. We then include
 them as a [git submodule](http://git-scm.com/book/en/Git-Tools-Submodules). This is the approach taken by the Trezor development team
 and enables them to retain complete control over the wire protocol, while allowing MultiBit Hardware to implement support in a phased
 approach.

#### Why no listeners ?

The Guava library offers the [EventBus](https://code.google.com/p/guava-libraries/wiki/EventBusExplained) which is a much simpler way to
 manage events within an application. Since MultiBit HD uses this internally and will be the primary consumer of this library it makes
 a lot of sense to mandate its use.

#### How do I get my hardware included ?

While we welcome a wide variety of hardware wallet devices into the Bitcoin ecosystem supporting them all through MultiBit HD gives rise 
to some obvious problems:

* maintaining compatibility for legacy versions (variants, quirks, deprecated functionality etc)
* verifying the security of the wallet (robust enough for mainstream users, entropy source etc)
* simplicity of use (mainstream users must find it easy to obtain and use) 

Thus the current situation is that the MultiBit Hardware development team is only supporting the Trezor device, but in time we would like to
open up support for other leading hardware wallet vendors.

### Getting started

MultiBit Hardware uses the standard Maven build process and can be used without having external hardware attached. Just do the usual

```
$ cd <project directory>
$ mvn clean install
```

and you're good to go.

#### Collaborators and the protobuf files

If you are a collaborator (i.e. you have commit access to the repo) then you will need to perform an additional stage to ensure you have
the correct version of the protobuf files:

```
$ cd <project directory>
$ git submodule init
$ git submodule update
```
This will bring down the `.proto` files referenced in the submodules and allow you to select which tagged commit to use when generating
the protobuf files. See the "Updating protobuf files" section later.

MultiBit Hardware does not maintain `.proto` files other than for our emulator. Periodically we will update the protobuf files through
 the following process (assuming an update to the Trezor protobuf):
```
$ cd trezor/src/main/trezor-common
$ git checkout master
$ git pull origin master
$ cd ../../..
$ mvn -DupdateProtobuf=true clean compile
$ cd ..
$ git add trezor
$ git commit -m "Updating protobuf files for 'trezor'"
$ git push
```
We normally expect the HEAD of the submodule origin master branch to [represent the latest production release](http://nvie.com/posts/a-successful-git-branching-model/), but that's up to the
owner of the repo.

#### Read the wiki for detailed instructions

Have a read of [the wiki pages](https://github.com/bitcoin-solutions/mbhd-hardware/wiki/_pages) which gives comprehensive
instructions for a variety of environments.

### Working with a production Trezor device (recommended)

After [purchasing a production Trezor device](https://www.buytrezor.com/) do the following (assuming a completely new :

Plug in the device to the USB port and wait for initialisation to complete.

Attempt to discover the device using the `UsbMonitoringExample` through the command line not the IDE:
```
cd examples
mvn clean compile exec:java -Dexec.mainClass="org.multibit.hd.hardware.examples.trezor.usb.UsbMonitoringExample"
```

This will list available devices on the USB and select a Trezor if present. It relies on the MultiBit Hardware project
JARs being installed into the local repository (e.g. built with `mvn clean install`).

### Working with a Raspberry Pi emulation device

A low-cost introduction to the Trezor is the use of a Raspberry Pi and the Trezor Shield development hardware available from [Satoshi Labs](http://satoshilabs.com/news/2013-07-15-raspberry-pi-shield-for-developers/).
Please read the [instructions on how to set up your RPi + Shield hardware](https://github.com/bitcoin-solutions/multibit-hardware/wiki/Trezor-on-Raspberry-Pi-from-scratch).

#### Configuring a RPi for socket emulation

Note your RPi's IP address so that you can open an SSH connection to it.

If you're using a laptop enable DHCP and plug in the device's network cable.

On Unix you can issue the following command to map the local network (install with brew for OSX):
```
$ nmap 192.168.0.0/24
```
Replace the IP address range with what IP address your laptop

Change the standard `rpi-serial.sh` script to use the following:

```
$ python trezor/__init__.py -s -t socket -p 0.0.0.0:3000 -d -dt socket -dp 0.0.0.0:2000
```
This will ensure that the Shield is serving over port 3000 with a debug socket on port 2000.

**Warning** Do not use this mode with real private keys since it is unsafe!

Run it up with
```
$ sudo ./rpi-serial.sh
```

You should see the Shield OLED show the Trezor logo.

#### Configuring a RPi for USB emulation

Apply power to the RPi through the USB on the Trezor Shield board. Connect a network cable as normal.

After the blinking lights have settled, test the USB device is connected:

* `lsusb` for Linux
* `system_profiler SPUSBDataType` for Mac
* `reg query hklm\system\currentcontrolset\enum\usbstor /s` for Windows (untested so might be a better way)

You should see a CP2110 HID USB-to-UART device.

Establish the IP address of the RPi and SSH on to it as normal.

Use the standard `rpi-serial.sh` script but ensure that RPi Getty is turned off:
```
$ sudo nano /etc/inittab
$ #T0:23:respawn:/sbin/getty -L ttyAMA0 115200 vt100
```
This will ensure that the Shield is using the UART reach the RPi GPIO serial port with a debug socket on port 2000.

**Warning** Do not use this mode with real private keys since it is unsafe!

Run it up with
```
sudo ./rpi-serial.sh
```
You should see the Shield OLED show the Trezor logo.

If you want your Raspberry Pi to be a dedicated USB Trezor device then adding the following commands will cause the
emulator software to be run on boot up

```
$ sudo ln -s /home/pi/trezor-emu/rpi-init /etc/init.d/trezor
$ sudo update-rc.d trezor defaults
```

### Troubleshooting

The following are known issues and their solutions or workarounds.

#### My production Trezor doesn't work on Ubuntu

Out of the box Ubuntu classifies HID devices as belonging to root. You can override this rule by creating your own under `/etc/udev/rules.d`:

```
$ sudo gedit /etc/udev/rules.d/99-trezorhid.rules
```

Make the content of this file as below:

```
# Trezor HID device
ATTRS{idProduct}=="0001", ATTRS{idVendor}=="534c", MODE="0660", GROUP="plugdev"
```

Save and exit from root, then unplug and replug your production Trezor. The rules should take effect immediately. If they're still not running it may that you're not a member of the `plugdev` group. You can fix this as follows (assuming that `plugdev` is not present on your system):

```
$ sudo addgroup plugdev
$ sudo addgroup yourusername plugdev 
```

#### When running the examples I get errors indicating `iconv` is missing or broken

The `iconv` library is used to map character sets and is usually provided as part of the operating system. MultiBit Hardware will work with version 1.11+. We have seen problems with running the code through an IDE where `iconv` responds with a failure code of -1.

#### My production Trezor doesn't work on Mac OS X

Correct. After 4 days of trying everything we could think of we just could not get the HID interface to a production Trezor to work on Mac OS X Mavericks. Our conclusion was the following:

* A device declaring itself as USB HID is claimed by Mac OS X and is not released to anything using `libusb` so usb4java is ineffective
* A signed kernel extension (kext) can be used to trick OS X into releasing this claim removing the "Access denied" message but this cannot be part of the MultiBit Hardware project because it's really the responsibility of the device manufacturers to provide
* it could be that an update to the `javahidapi` wrapper for the Signal 11 `hidapi` library would work but to go down that road would  mean the MultiBit Hardware project forking `javahidapi` and that's just too much effort for us to maintain
 
Possible workarounds are:
 
* buy a cheap Windows laptop and use that for your Trezor 
* use a Raspberry Pi as a "bridge" between the production Trezor and an Ethernet socket connection 
* invest time and money into getting Mac OS X support integrated into MultiBit Hardware (donations and pull requests accepted)

### Closing notes

All trademarks and copyrights are acknowledged.
