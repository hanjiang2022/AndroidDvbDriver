package info.martinmarinov.drivers.usb.cxusb;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import info.martinmarinov.drivers.DeviceFilter;
import info.martinmarinov.drivers.DvbException;
import info.martinmarinov.drivers.tools.SleepUtils;
import info.martinmarinov.drivers.usb.DvbFrontend;
import info.martinmarinov.drivers.usb.DvbTuner;
import info.martinmarinov.drivers.usb.DvbUsbIds;
import info.martinmarinov.drivers.usb.silabs.Si2157;
import info.martinmarinov.drivers.usb.silabs.Si2168;

import static info.martinmarinov.drivers.usb.silabs.Si2157.Type.SI2157_CHIPTYPE_SI2141;


/**
 * @author dmgouriev
 */
public class MygicaT230A extends CxUsbDvbDevice {
    public final static String MYGICA_NAME = "Mygica T230A DVB-T/T2/C";
    final static DeviceFilter MYGICA_T230A = new DeviceFilter(DvbUsbIds.USB_VID_CONEXANT, DvbUsbIds.USB_PID_MYGICA_T230A, MYGICA_NAME);

    private Si2168 frontend;

    private final static byte CMD_GPIO_WRITE = 0x0e;

    MygicaT230A(UsbDevice usbDevice, Context context) throws DvbException {
        super(usbDevice, context, MYGICA_T230A);
    }

    @Override
    public String getDebugString() {
        return MYGICA_NAME;
    }

    @Override protected void identifyStateBeforeOpen() throws DvbException {
        SleepUtils.mdelay(100);
        cxusb_ctrl_msg(CMD_GPIO_WRITE, new byte[]{(byte)0x87, 0}, 2, new byte[1], 1);
        SleepUtils.mdelay(20);
        cxusb_ctrl_msg(CMD_GPIO_WRITE, new byte[]{(byte)0x86, 1}, 2, new byte[1], 1);
        cxusb_ctrl_msg(CMD_GPIO_WRITE, new byte[]{(byte)0x80, 0}, 2, new byte[1], 1);
        SleepUtils.mdelay(100);
        cxusb_ctrl_msg(CMD_GPIO_WRITE, new byte[]{(byte)0x80, 1}, 2, new byte[1], 1);
        SleepUtils.mdelay(50);		
    }

    @Override
    synchronized protected void powerControl(boolean onoff) throws DvbException {
        cxusb_d680_dmb_power_ctrl(onoff);
        cxusb_streaming_ctrl(onoff);
    }

    private void cxusb_d680_dmb_power_ctrl(boolean onoff) throws DvbException {
        cxusb_power_ctrl(onoff);
        if (!onoff) return;

        SleepUtils.mdelay(128);
        cxusb_ctrl_msg(CMD_DIGITAL, new byte[0], 0, new byte[1], 1);
        SleepUtils.mdelay(100);
    }

    @Override
    protected void readConfig() throws DvbException {
        // no-op
    }

    @Override
    protected DvbFrontend frontendAttatch() throws DvbException {
        return frontend = new Si2168(this, resources, i2CAdapter, 0x64,
                SI2168_TS_PARALLEL, true, SI2168_TS_CLK_MANUAL, false);
    }

    @Override
    protected DvbTuner tunerAttatch() throws DvbException {
        return new Si2157(resources, i2CAdapter, frontend.gateControl(), 0x60, false, SI2157_CHIPTYPE_SI2141);
    }

    @Override
    protected void init() throws DvbException {
        // no-op
    }


}
