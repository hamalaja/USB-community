import java.nio.ByteBuffer;

import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Context context = new Context();
		int result = LibUsb.init(context);
		
		if (result != LibUsb.SUCCESS) {
			throw new LibUsbException("Unable to initialize libusb.", result);
		}
		
		
		DeviceList list = null;
		
		try {
			
			list = findDevices();
			int timeout = 1000;
			
			for(Device device : list) {
				
				DeviceDescriptor descriptor = new DeviceDescriptor();
				int address = LibUsb.getDeviceAddress(device);
                int busNumber = LibUsb.getBusNumber(device);
				
	            result = LibUsb.getDeviceDescriptor(device, descriptor);
	            if (result != LibUsb.SUCCESS) {
	            	continue;
	            }	            
                System.out.format(
                    "Bus %03d, Device %03d: Vendor %04x, Product %04x%n",
                    busNumber, address, descriptor.idVendor(), descriptor.idProduct());               
				
				DeviceHandle handle = new DeviceHandle();
				result = LibUsb.open(device, handle);
				if (result != LibUsb.SUCCESS) {
	            	continue;
	            }
				
				try
				{
					ByteBuffer buffer = ByteBuffer.allocateDirect(8);
					buffer.put(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 });
					int transfered = LibUsb.controlTransfer(handle, 
					    (byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE),
					    (byte) 0x09, (short) 2, (short) 1, buffer, timeout);
					if (transfered < 0) {
						System.out.println("Control transfer failed: " + transfered);	
					}
					
					System.out.println("product id: " + descriptor.idProduct() + " , " + transfered + " bytes sent");
				}
				finally
				{
				    LibUsb.close(handle);
				}
				
			}
			
		} finally {
			if(list != null) {
				LibUsb.freeDeviceList(list, true);
			}			
			LibUsb.exit(context);			
		}
	
		
	}
	
	
	public static Device findDevice(short vendorId, short productId)
	{
	    // Read the USB device list
	    DeviceList list = new DeviceList();
	    int result = LibUsb.getDeviceList(null, list);
	    if (result < 0) throw new LibUsbException("Unable to get device list", result);

	    try
	    {
	        // Iterate over all devices and scan for the right one
	        for (Device device: list)
	        {
	            DeviceDescriptor descriptor = new DeviceDescriptor();
	            result = LibUsb.getDeviceDescriptor(device, descriptor);
	            if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to read device descriptor", result);
	            if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) return device;
	        }
	    }
	    finally
	    {
	        // Ensure the allocated device list is freed
	        LibUsb.freeDeviceList(list, true);
	    }

	    // Device not found
	    return null;
	}
	
	
	public static DeviceList findDevices()
	{
	    // Read the USB device list
	    DeviceList list = new DeviceList();
	    int result = LibUsb.getDeviceList(null, list);
	    if (result < 0) throw new LibUsbException("Unable to get device list", result);
	   
	    return list;
	}

}
