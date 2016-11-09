package fermatOrg.exceptions;

/**
 * Created by rodrigo on 11/3/16.
 * Raised when the address is not valid in the current IoP network.
 */
public class AddressFormatException extends Exception {
    public AddressFormatException(Throwable cause) {
        super(cause);
    }

    public AddressFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
