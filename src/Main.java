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
 * http://www.mets-blog.com/java-usb-communication-usb4java/
 * http://zadig.akeo.ie/
 * Gap loi -9 va da fix http://stackoverflow.com/questions/17354891/java-bytebuffer-to-string
 * 
 * @author lamhm
 *
 */
public class Main {
	private static final String HARDWARE_IDS = "VID_13FE&PID_3600";


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
			System.out.println("[INFO] size:" + devices.getSize());

			for (Device device : devices) {
				DeviceDescriptor descriptor = new DeviceDescriptor();
				result = LibUsb.getDeviceDescriptor(device, descriptor);
				if (result != LibUsb.SUCCESS) {
					continue;
				}

				String vendorId = String.format("%04x", descriptor.idVendor());
				String productId = String.format("%04x", descriptor.idProduct());
				if (!resolutionIds[0].equals(vendorId) || !resolutionIds[1].equals(productId)) {
					continue;
				}

				int address = LibUsb.getDeviceAddress(device);
				int busNumber = LibUsb.getBusNumber(device);
				System.out.println(descriptor.dump());
				System.out.format("Bus %03d, Device %03d: Vendor %s, Product %s%n", busNumber, address, vendorId, productId);

				DeviceHandle handle = new DeviceHandle();
				result = LibUsb.open(device, handle);
				if (result != LibUsb.SUCCESS) {
					System.out.println("[ERROR] Unable to open USB device" + result);
					continue;
				}

				try {
					ByteBuffer buffer = ByteBuffer.allocateDirect(8);
					buffer.put(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 });

					int transfered = LibUsb.controlTransfer(handle, LibUsb.ENDPOINT_IN, LibUsb.REQUEST_GET_DESCRIPTOR, (short) 0x0100, (short) 0x0000, buffer,
							timeout);
					if (transfered < 0) {
						System.out.println("[ERROR] Control transfer failed: " + transfered);
					}

					System.out.println("[INFO] product id: " + descriptor.idProduct() + "[int] , " + transfered + " bytes sent");
				} catch (Exception e) {
					e.printStackTrace();
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
