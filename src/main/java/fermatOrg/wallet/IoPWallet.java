package fermatOrg.wallet;

import fermatOrg.exceptions.AddressFormatException;
import fermatOrg.exceptions.IoPBlockchainException;
import fermatOrg.exceptions.IoWalletException;
import fermatOrg.wallet.event.IncomingTransactionEvent;

import fermatOrg.network.IoPBlockchain;
import fermatOrg.network.NetworkType;
import fermatOrg.wallet.event.IncomingTransactionListener;

import java.io.File;
import java.util.List;

/**
 * Created by rodrigo on 11/3/16.
 */
interface IoPWallet {
    /**
     * Imports new addresses from the input file into the Watch Only wallet. If any address on the file is already imported, then it will be skipped.
     * When new addresses are added to an existing input file, you need to call this method to add the new added addresses.
     * @param inputFile The file with the list of addresses to monitor on the network.
     * @throws IoWalletException if the specified file doesn't exists or can't be read.
     * @throws AddressFormatException if one or many addresses in the input file is not valid in the wallet network.
     */
    void importAddresses(File inputFile) throws IoWalletException, AddressFormatException;

    /**
     * Manually imports a watch only address into the wallet. This will trigger an autosave of the wallet.
     * @param address a valid Base58 string address on the selected network
     * @throws AddressFormatException in case the provided address is not valid in the network.
     */
    void importAddress(String address) throws AddressFormatException;

    /**
     * Calculates and return the amount of IoPs sent to the specified address.
     * @param address the address we want to get the balance from. Must already be imported in the wallet and be valid.
     * @return the amount of IoPs or zero if the address doesn't exists or is invalid. The long value express IoP-satoshis 10000000 = 1 IoP
     */
    long getAddressBalance(String address) throws AddressFormatException;

    /**
     * Calculates and returns the total amount of IoPs sent to any of the addresses imported into the wallet.
     * @return a long value with the total amounf of IoP-satoshis on the wallet. The long value express IoP-satoshis 10000000 = 1 IoP
     */
    long getWalletBalance();

    /**
     * It returns all the succesfully imported address on the wallet. All the returned addresses are being monitored on the network.
     * @return a list of string representing the imported addresses
     */
    List<String> getAddresses();

    /**
     * Used to calculate how many addresses this wallet is monitoring. It returns the amount ot imported addresses.
     * Just the size of the array returned at {@link #getAddresses()}
     * @return an integer value that counts how many addresses we are monitoring.
     */
    int getAddressesSize();


    /**
     * Confirms and received and consumed an incoming transaction event. If you respond to the Java Event or where notified on the incoming
     * transaction by requesting the list to {@link #getPendingNotificationEvents()}
     * @param incomingTransactionEvent
     */
    void confirmEventReception(IncomingTransactionEvent incomingTransactionEvent);

    /**
     * The file where this wallet is stored. The wallet will auto save when needed, so there are no actions to perform.
     * @return the File object representing the wallet file.
     */
    File getWalletFile();

    /**
     * The WatchOnly wallet supports all three IoP blockchain types. It returns the current network this wallet is listening on.
     * @return the current network this wallet is listening on. Production, Testnet or RegTest.
     */
    NetworkType getNetworkType();

    /**
     * Connects the current WatchOnly wallet to the network. The wallet will connect to nodes on the IoP blockchain, download any pending block
     * and monitor for transactions sent to any of the watched addresses. If new addresses are incorporated into the wallet with {@link #importAddresses(File)} they will be checked automatically.
     * @return an IoP Blockchain object
     * @throws IoPBlockchainException if there is an error connecting to the IoP blockchain.
     */
    IoPBlockchain connect() throws IoPBlockchainException;

    /**
     * Adds a new Listener for the Incoming Transaction Event. Listeners will be notified when a new transaction is detected on the network.
     * @param incomingTransactionListener
     */
    void addIncomingTransactionListener(IncomingTransactionListener incomingTransactionListener);

    /**
     * Removes an existing listener from the list. After been removed, the listener will no longer be notified of new incoming transactions detected on the network.
     * @param incomingTransactionListener
     */
    void removeIncomingTransactionListener(IncomingTransactionListener incomingTransactionListener);

    /**
     * {@link IncomingTransactionEvent} event is triggered when a new transaction is detected that sends IoPs to any of the imported addresses on the wallet.
     * Those events must be confirmed by {@link #confirmEventReception(IncomingTransactionEvent)}. Events that have not been yet confirmed reception are returned by this method.
     * @return the list of unconfirmed events. To confirm them, call {@link #confirmEventReception(IncomingTransactionEvent)}
     */
    List<IncomingTransactionEvent> getPendingNotificationEvents();
}
