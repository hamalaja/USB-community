import java.nio.ByteBuffer;

import org.apache.commons.lang3.StringUtils;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

/**
 * https://github.com/usb4java/usb4java-examples/blob/master/src/main/java/org/
 * usb4java/examples/AsyncBulkTransfer.java
 * 
 * http://usb4java.org/quickstart/libusb.html
 * http://masters.donntu.org/2013/fknt/hryhoriev/library/java_usb.pdf
 * 
 * @author lamhm
 *
 */
public class Main {
	private static final String HARDWARE_IDS = "VID_0BB4&PID_0C97";


	public static void main(String[] args) {
		Context context = new Context();
		int result = LibUsb.init(context);
		if (result != LibUsb.SUCCESS) {
			throw new LibUsbException("Unable to initialize libusb.", result);
		}

		String[] resolutionIds = resolutionHardwareIds(HARDWARE_IDS);
		DeviceList devices = null;
		try {
			devices = findDevices();
			int timeout = 1000;
			System.out.println("[ERROR] size:" + devices.getSize());

			for (Device device : devices) {
				DeviceDescriptor descriptor = new DeviceDescriptor();
				result = LibUsb.getDeviceDescriptor(device, descriptor);
				if (result != LibUsb.SUCCESS) {
					continue;
				}

				int address = LibUsb.getDeviceAddress(device);
				int busNumber = LibUsb.getBusNumber(device);
				String vendorId = String.format("%04x", descriptor.idVendor());
				String productId = String.format("%04x", descriptor.idProduct());
				System.out.format("Bus %03d, Device %03d: Vendor %s, Product %s%n", busNumber, address, vendorId, productId);
				if (resolutionIds[0].equals(vendorId) && resolutionIds[1].equals(productId)) {
					System.out.println("[FATAL] ----------------------> HTC");
				}

				DeviceHandle handle = new DeviceHandle();
				result = LibUsb.open(device, handle);
				if (result != LibUsb.SUCCESS) {
					continue;
				}

				try {
					ByteBuffer buffer = ByteBuffer.allocateDirect(8);
					buffer.put(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 });
					int transfered = LibUsb.controlTransfer(handle, (byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE), (byte) 0x09, (short) 2,
							(short) 1, buffer, timeout);
					if (transfered < 0) {
						System.out.println("[ERROR] Control transfer failed: " + transfered);
					}

					System.out.println("[INFO] product id: " + descriptor.idProduct() + "[int] , " + transfered + " bytes sent");
				} finally {
					LibUsb.close(handle);
				}
			}

		} finally {
			if (devices != null) {
				LibUsb.freeDeviceList(devices, true);
			}
			LibUsb.exit(context);
		}

	}


	public static DeviceList findDevices() {
		// Read the USB device list
		DeviceList list = new DeviceList();
		int result = LibUsb.getDeviceList(null, list);
		if (result < 0)
			throw new LibUsbException("Unable to get device list", result);

		return list;
	}


	public static String[] resolutionHardwareIds(String value) {
		String[] items = StringUtils.split(value, "&");
		return new String[] { items[0].replace("VID_", "").toLowerCase(), items[1].replace("PID_", "").toLowerCase() };
	}

}
