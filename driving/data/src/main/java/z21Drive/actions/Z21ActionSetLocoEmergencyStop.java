package z21Drive.actions;

import z21Drive.LocoAddressOutOfRangeException;

/**
 * Sent to z21 to immediately stop a locomotive. Supports loco addresses up to 63.
 */
public class Z21ActionSetLocoEmergencyStop extends Z21Action {
    /**
     * Default constructor for this.
     *
     * @param locoAddress Address of the loco that this action targets.
     * @throws LocoAddressOutOfRangeException Thrown when loco address is too big or negative.
     */
    public Z21ActionSetLocoEmergencyStop(int locoAddress) throws LocoAddressOutOfRangeException {
        byteRepresentation.add(Byte.decode("0x40"));
        byteRepresentation.add(Byte.decode("0x00"));
        if (locoAddress < 1)
            throw new LocoAddressOutOfRangeException(locoAddress);
        addDataToByteRepresentation(new Object[]{locoAddress});
        addLenByte();
    }

    @Override
    public void addDataToByteRepresentation(Object[] objs) {
        byteRepresentation.add((byte) 0x92);

        byte Adr_MSB = (byte) (((Integer) objs[0]) >> 8);
        byte Adr_LSB = (byte) (((Integer) objs[0]) & 0b11111111);
        if (Adr_MSB != 0) {
            Adr_MSB |= 0b11000000;
        }

        byteRepresentation.add(Adr_MSB);
        byteRepresentation.add(Adr_LSB);
    }
}
