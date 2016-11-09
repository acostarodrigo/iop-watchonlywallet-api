package fermatOrg.wallet.event;

import fermatOrg.network.NetworkType;

import java.io.Serializable;
import java.util.EventObject;

/**
 * Created by rodrigo on 11/3/16.
 */
public class IncomingTransactionEvent extends EventObject implements Serializable{
    //class variables
    private Status status;

    //class constants
    private final String address;
    private final NetworkType networkType;
    private final long value;
    private final String transactionHash;


    public enum Status{
        PENDING_NOTIFICATION, NOTIFICATION_COMPLETED
    }

    public IncomingTransactionEvent(Object source, String address, NetworkType networkType, long value, String transactionHash) {
        super(source);

        this.address = address;
        this.networkType = networkType;
        this.value = value;
        this.transactionHash = transactionHash;
        this.status = Status.PENDING_NOTIFICATION;
    }

    /**
     * The address to where the IoPs where sent to.
     * @return an string representing a Base58 Address on the current network.
     */
    public String getAddress() {
        return address;
    }

    /**
     * The current network type this event was registered on.
     * @return a valid IoP network type.
     */
    public NetworkType getNetworkType() {
        return networkType;
    }

    /**
     * the amount of coins in IoPtoshis sent to the wallet.
     * @return a long value representing the amount of coins.
     */
    public long getValue() {
        return value;
    }

    /**
     * The status of the event notification.
     * @return a pending or already completed notification status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status of the notification on this event. Usually is to marked this event as NOTIFICATION_COMPLETED:
     * @param status a valud notification status.
     */
    public void setStatus(Status status) {
        this.status = status;
    }


    /**
     * The transaction hash on which this event was detected-
     * @return a string representing the transaction hash
     */
    public String getTransactionHash() {
        return transactionHash;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.getAddress().hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (!IncomingTransactionEvent.class.isAssignableFrom(obj.getClass()))
            return false;

        final IncomingTransactionEvent other = (IncomingTransactionEvent) obj;
        if (this.getStatus() == other.getStatus() && this.getValue() == other.getValue() && this.getAddress() == other.getAddress() && this.getTransactionHash() == other.getTransactionHash())
            return true;
        else
            return false;
    }
}

