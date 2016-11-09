package fermatOrg.wallet;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.common.base.Preconditions;
import fermatOrg.exceptions.AddressFormatException;
import fermatOrg.exceptions.IoPBlockchainException;
import fermatOrg.exceptions.IoWalletException;
import fermatOrg.wallet.event.EventNotificationManager;
import fermatOrg.wallet.event.IncomingTransactionEvent;
import fermatOrg.network.IoPBlockchain;
import fermatOrg.network.NetworkType;
import fermatOrg.wallet.event.IncomingTransactionListener;
import org.blockchainj.core.*;
import org.blockchainj.params.IoP.IoP_MainNetParams;
import org.blockchainj.params.IoP.IoP_RegTestParams;
import org.blockchainj.params.IoP.IoP_TestNet3Params;
import org.blockchainj.wallet.UnreadableWalletException;
import org.blockchainj.wallet.Wallet;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by rodrigo on 11/1/16.
 * <p>
 *     Creates a Watch Only wallet in the specified network of the IoP blockchain.
 *     A watch only wallet consists of a group of public keys and no private keys to spend funds in the wallet.
 *     The wallet keeps tracks of the blockchain transactions and keeps the balance of the addresses imported.
 * </p>
 * <p>
 *     You can create a new wallet that will be automatically saved to disk when needed or load from an existing file. {@link #getWalletFile()}.
 *     The Wallet can be filled with addresses from an input file with plain addresses separated by new lines.
 * </p>
 * <p>
 *     Each time a new transaction is detected on the network a new {@link IncomingTransactionEvent} event is triggered. Additionally, to make sure no event is missed,
 *     the wallet keep tracks of fired events until reception is confirmed with {@link #confirmEventReception(IncomingTransactionEvent)}.
 * </p>
 */
public class WatchOnlyWallet implements IoPWallet{
    //class variables
    private IoPBlockchain ioPBlockchain; //the IoP blockchain connection
    private EventNotificationManager eventNotificationManager; // deals with event and event notification

    // static members
    private static Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    // class constants
    private final Wallet wallet; // the actual blockchainj wallet
    private final Context context; // the context to be used that will be maintained throught the entire life cycle of the wallet.
    private final File walletFile; // the wallet file we used to save it's data.

    /**
     * @apiDescription
     * Private constructor used for loading existing wallets.
     * @param walletFile the File where the wallet is stored.
     * @param wallet the blockchainj wallet loaded from file.
     */
    private WatchOnlyWallet(File walletFile, Wallet wallet){
        //check preconditions
        Preconditions.checkNotNull(walletFile);
        Preconditions.checkNotNull(wallet);

        //set constants values
        this.walletFile = walletFile;
        this.wallet = wallet;
        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.INTERNET_OF_PEOPLE);
        this.context = wallet.getContext();

        // initiate the event manager
        this.eventNotificationManager = new EventNotificationManager();

        //default log level to OFF
        logger.setLevel(Level.OFF);
    }

    /**
     * Creates a new empty IoP watch only wallet. To load an existing wallet from a previous file, use static method {@link #loadFromFile(File)}.
     * @param walletFile the file that will be use to store this wallet data. The wallet implements and autosave functionality to save it changes when needed.
     * @param networkType the network type this wallet will be working on. It supports all three IoP network types, production, testnet and regtest.
     * @throws IoWalletException if the file is incorrect, already exists or is unreachable.
     */
    public WatchOnlyWallet(File walletFile, NetworkType networkType) throws IoWalletException{
        //check Preconditions
        Preconditions.checkNotNull(walletFile);
        Preconditions.checkNotNull(networkType);

        // make sure we are not overwriting any wallet.
        if (walletFile.exists())
            throw new IoWalletException("The specified walletFile " + walletFile.toString() + " already exists. Can't overwrite wallets. Use static method loadFromFile instead.", null);

        // set constants
        this.walletFile = walletFile;
        this.context = new Context(getNetworkParameters(networkType));
        this.wallet = new Wallet(context);

        // set wallet properties and save
        try {
            this.wallet.autosaveToFile(walletFile, 1, TimeUnit.SECONDS, null);
            this.wallet.saveToFile(walletFile);
        } catch (Exception e) {
            throw new IoWalletException("There was an error executing the initial save of your wallet. Check that you have permissions on the specified walletFile", e);
        }

        // initiate the event manager
        this.eventNotificationManager = new EventNotificationManager();

        //default log level to OFF
        logger.setLevel(Level.OFF);
    }

    /**
     * Loads from an existing file a previously created IoP watch only wallet.
     * @param walletFile the file used to load the wallet from. It must exists and be reachable.
     * @return an IoP WatchOnly wallet. To import addresses call {@link #importAddresses(File)}
     * @throws IoWalletException if the file doesn't exists or there was an error while trying to load the wallet. If we can't load the wallet, you may try to manually delete
     * the walletFile and create a new one reimporting the addresses.
     */
    public static WatchOnlyWallet loadFromFile(File walletFile) throws IoWalletException{
        //pre validations
        Preconditions.checkNotNull(walletFile);
        Preconditions.checkArgument(walletFile.exists());

        Wallet wallet = null;
        try {
            NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.INTERNET_OF_PEOPLE);
            NetworkParameters.setSupportedBlockchain(SupportedBlockchain.INTERNET_OF_PEOPLE);
            wallet = Wallet.loadFromFile(walletFile);
        } catch (UnreadableWalletException e) {
            throw new IoWalletException("There was an error trying to load an existing wallet. Consider creating a new Wallet and reimporting the addresses", e);
        } catch (Exception exception){
            throw new IoWalletException("Unknown error while trying to load an existing wallet.", exception);
        }

        // at this point the wallet was loaded sucessfully
        return new WatchOnlyWallet(walletFile, wallet);
    }

    /**
     * From the network type selected, we get the real blockchainj parameters over the IoP blockchain
     * @param networkType any valid enum value, production, testnet or regtest.
     * @return the matching blockchainj network type of IoP.
     */
    private static NetworkParameters getNetworkParameters(NetworkType networkType){
        NetworkParametersGetter.setSupportedBlockchain(SupportedBlockchain.INTERNET_OF_PEOPLE);
        switch (networkType){
            case PRODUCTION:
                return IoP_MainNetParams.get();
            case TEST:
                return IoP_TestNet3Params.get();
            case REGTEST:
                return IoP_RegTestParams.get();
            default:
                throw new RuntimeException("The specified Network type (" + networkType.name() + ") does not exists");
        }
    }

    /**
     * changes the log level of the output-
     * @param level default is ERROR
     */
    public static void setLogLevel(Level level){
        logger.setLevel(level);
    }

    /**
     * gets the current log level for the wallet output.
     * @return the current log level.
     */
    public static Level getLogLevel(){
        return logger.getLevel();
    }

    /**
     * translates the IoP blockchain parameter into a valid NetworkType
     * @param networkParameters a valid blockchainj networkparameter on IoP network.
     * @return a value from the NetworkType enum.
     */
    public static NetworkType getNetworkType(NetworkParameters networkParameters){
        if (networkParameters == IoP_MainNetParams.get())
            return NetworkType.PRODUCTION;

        if (networkParameters == IoP_TestNet3Params.get())
            return NetworkType.TEST;

        if (networkParameters == IoP_RegTestParams.get())
            return NetworkType.REGTEST;

        // this should never happen.
        return null;

    }

    /**
     * Imports new addresses from the input file into the Watch Only wallet. If any address on the file is already imported, then it will be skipped.
     * When new addresses are added to an existing input file, you need to call this method to add the new added addresses.
     * @param inputFile The file with the list of addresses to monitor on the network.
     * @throws IoWalletException if the specified file doesn't exists or can't be read.
     * @throws AddressFormatException if one or many addresses in the input file is not valid in the wallet network.
     */
    public void importAddresses(File inputFile) throws IoWalletException, AddressFormatException{
        if (!inputFile.exists())
            throw new IoWalletException("Provided input file does not exists. Verify you have access.");

        AddressesFileReader fileReader = new AddressesFileReader(inputFile);

        try {
            for (String line : fileReader.importAddresses()){
                try{
                    this.wallet.addWatchedAddress(getAddress(line), 1475280000);
                } catch (AddressFormatException e){
                    throw new AddressFormatException("Imported address " + line + " is not a valid base58 IoP address." , e);
                }
            }
        } catch (Exception e) {
            throw new IoWalletException("There was an IO error importing addresses from the input file. Verify the input file is readable.", e);
        }
    }

    /**
    * Manually imports a watch only address into the wallet. This will trigger an autosave of the wallet.
     * @param address a valid Base58 string address on the selected network
     * @throws AddressFormatException in case the provided address is not valid in the network.
     */
    public void importAddress(String address) throws AddressFormatException{
        this.wallet.addWatchedAddress(getAddress(address), 1475280000);
    }

    /**
     * Calculates and return the amount of IoPs sent to the specified address.
     * @param address the address we want to get the balance from. Must already be imported in the wallet and be valid.
     * @return the amount of IoPs or zero if the address doesn't exists or is invalid. The long value express IoP-satoshis 10000000 = 1 IoP
     * @throws AddressFormatException if the passed base58 string address is not valid on the current network.
     */
    public long getAddressBalance(String address) throws AddressFormatException {
        long balance = 0;
        // if not valid address then 0 is the balance.
        Address blockchainAddress =  getAddress(address);

        // we iterate the recorded transactions searching for coins sent to the specified address to calculate the balance.
        for (Transaction transaction : wallet.getTransactions(false)){
            for (TransactionOutput output : transaction.getOutputs()){
                if (output.isWatched(this.wallet) && output.getAddressFromP2PKHScript(wallet.getNetworkParameters()).equals(blockchainAddress))
                    balance = balance + output.getValue().getValue();
            }
        }

        return balance;
    }

    /**
     * returns a blockchainj address valid in the current IoP network.
     * @param address a base58 String representing an address
     * @return a valid address or null if not valid.
     */
    private Address getAddress(String address) throws AddressFormatException{
        Address add = null;
        try{
            add = Address.fromBase58(this.context.getParams(), address);
            return add;
        } catch (Exception e){
            throw new AddressFormatException(e);
        }
    }

    /**
     * Calculates and returns the total amount of IoPs sent to any of the addresses imported into the wallet.
     * @return a long value with the total amounf of IoP-satoshis on the wallet. The long value express IoP-satoshis 10000000 = 1 IoP
     */
    public long getWalletBalance(){
        return wallet.getBalance(Wallet.BalanceType.ESTIMATED).getValue();
    }

    /**
     * It returns all the successfully imported address on the wallet. All the returned addresses are being monitored on the network.
     * @return a list of string representing the imported addresses
     */
    public List<String> getAddresses(){
        List<String> addressesList = new ArrayList<>();
        for (Address address : wallet.getWatchedAddresses()){
            addressesList.add(address.toBase58());
        }

        return addressesList;
    }

    /**
     * Used to calculate how many addresses this wallet is monitoring. It returns the amount ot imported addresses.
     * Just the size of the array returned at {@link #getAddresses()}
     * @return an integer value that counts how many addresses we are monitoring.
     */
    public int getAddressesSize(){
        return getAddresses().size();
    }


    /**
     * Confirms and received and consumed an incoming transaction event. If you respond to the Java Event or where notified on the incoming
     * transaction by requesting the list to {@link #getPendingNotificationEvents()}
     * @param incomingTransactionEvent
     */
    public void confirmEventReception(IncomingTransactionEvent incomingTransactionEvent){
        eventNotificationManager.confirmEventNotification(incomingTransactionEvent);
    }

    /**
     * The file where this wallet is stored. The wallet will auto save when needed, so there are no actions to perform.
     * @return the File object representing the wallet file.
     */
    public File getWalletFile(){
        return this.walletFile;
    }

    /**
     * The WatchOnly wallet supports all three IoP blockchain types. It returns the current network this wallet is listening on.
     * @return the current network this wallet is listening on. Production, Testnet or RegTest.
     */
    public NetworkType getNetworkType(){
        return getNetworkType(context.getParams());
    }

    /**
     * Connects the current WatchOnly wallet to the network. The wallet will connect to nodes on the IoP blockchain, download any pending block
     * and monitor for transactions sent to any of the watched addresses. If new addresses are incorporated into the wallet with {@link #importAddresses(File)} they will be checked automatically.
     * @return an IoP Blockchain object
     * @throws IoPBlockchainException if there is an error connecting to the IoP blockchain.
     */
    public IoPBlockchain connect() throws IoPBlockchainException {
        if (ioPBlockchain == null)
            ioPBlockchain = new IoPBlockchain(this.wallet, eventNotificationManager);

        if (!ioPBlockchain.isConnected())
            ioPBlockchain.connect();

        return ioPBlockchain;
    }

    /**
     * Adds a new Listener for the Incoming Transaction Event. Listeners will be notified when a new transaction is detected on the network.
     * @param incomingTransactionListener
     */
    public void addIncomingTransactionListener(IncomingTransactionListener incomingTransactionListener){
        eventNotificationManager.addIncomingTransactionListener(incomingTransactionListener);
    }

    /**
     * Removes an existing listener from the list. After been removed, the listener will no longer be notified of new incoming transactions detected on the network.
     * @param incomingTransactionListener
     */
    public void removeIncomingTransactionListener(IncomingTransactionListener incomingTransactionListener){
        eventNotificationManager.removeIncomingTransactionListener(incomingTransactionListener);
    }

    /**
     * {@link IncomingTransactionEvent} event is triggered when a new transaction is detected that sends IoPs to any of the imported addresses on the wallet.
     * Those events must be confirmed by {@link #confirmEventReception(IncomingTransactionEvent)}. Events that have not been yet confirmed reception are returned by this method.
     * @return the list of unconfirmed events. To confirm them, call {@link #confirmEventReception(IncomingTransactionEvent)}
     */
    public List<IncomingTransactionEvent> getPendingNotificationEvents(){
     return eventNotificationManager.getPendingNotificationEvents();
    }
}
