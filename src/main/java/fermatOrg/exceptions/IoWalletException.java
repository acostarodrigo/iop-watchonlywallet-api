package fermatOrg.exceptions;

/**
 * Created by rodrigo on 11/3/16.
 * Any IO error from the Wallet. Might happen after trying to load an existing wallet, or during a save operation.
 */
public class IoWalletException extends Exception {
    public IoWalletException(String message, Throwable cause) {
        super(message, cause);
    }

    public IoWalletException(String message) {
        super(message);
    }
}
