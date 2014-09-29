package org.multibit.hd.hardware.core;

/**
 * <p>Utility interface to provide the following to hardware wallet clients and devices:</p>
 * <ul>
 * <li>Provision of standard lifecycle entry points</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public interface Connectable {

  /**
   * <p>Verify the supporting environment before attempting a connection</p>
   *
   * <p>Typically this would involve initialising native libraries and verifying their communications</p>
   *
   * @return True if the native libraries initialised successfully
   */
  boolean verifyEnvironment();

  /**
   * <p>Attempt a connection to the underlying hardware to establish communication only (no higher level messages)</p>
   *
   * <p>Implementers must ensure the following behaviour:</p>
   * <ul>
   * <li>The device is assumed to be connected and discoverable</li>
   * <li>Method will return false if no matching device is found and further queries are safe to repeat</li>
   * <li>A DEVICE_FAILED event will be generated if subsequent USB HID communication fails (i.e. device is broken)</li>
   * </ul>
   *
   * @return True if the connection was successful
   */
  boolean connect();

  /**
   * <p>Break the connection to the device</p>
   *
   * <p>Implementers must ensure the following behaviour:</p>
   * <ul>
   * <li>All non-persistent state associated with the device is reset</li>
   * <li>A connect is required to restart communications</li>
   * </ul>
   */
  void disconnect();

}
