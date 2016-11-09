# IoP WatchOnly Wallet -API

Convenient API to create a public key wallet only and interact with the IoP blockchain written in Java.

## Version 1.0.0

Even the IoP core wallet is an  [HD Wallet](https://en.bitcoin.it/wiki/Deterministic_wallet), it is very basic. It has the minimun functionality to be able to derive keys from a seed so that only one backup is needed.

But the hierarchy structure on the wallet derives **hardened** keys only on a single account, in a structure similar to: m/0'/0'/n

This means that there is no way to derive public keys (and addresses) from an extended public key, so there is no direct way to create a **WatchOnly Wallet** that will generate the same address than the IoP core wallet without using private keys.

Since the expected usage of this watchonly api is for unsecure servers, storing private keys from the IoP core Wallet is not an option.

Version 1.0.0 of the API only supports **importing** public keys from a file or manually. Is not the best option but will work as an initial version.

After the IoP core wallet code has been improved to support non hardened hierarchies, an extended public key will be able to be imported into a new API version and automatically derive public keys only.

## Usage

### WatchOnlyWallet class

Main class used to create a new or load an existing wallet. You can import public keys from a file or manually and then connect to the IoP network to download blocks and verify incoming transactions.


You can get IoP coin balances per address:

~~~
long getAddressBalance(String address) throws AddressFormatException
~~~

With addresses imported in the wallet, you can connect to the blockchain and start monitoring for transactions. The Wallet triggers IncomingTransaction events and stores them until you confirm that you have received them

With methods:

```
List<IncomingTransactionEvent> getPendingNotificationEvents();
void confirmEventReception(IncomingTransactionEvent incomingTransactionEvent);
```

You get notified of pending events and confirm reception of them. If your language program allows it, you can add a **listener** to the events to get notified directly.

### IoPBlockchain class

Conneting from the wallet, returns an IoPBlockchain object which will connect to  available IoP peers and download block headers from the blockchain.

Once we are up-to-date with the blockchain, it will automatically disconnect. 

When we found a transaction that sends IoP coins to any of the imported addresses, an Incoming Transaction event will be triggered and stored until reception is confirmed.

For additional functionality, read the internal **API Java docs.**

## Example

```
private WatchOnlyWallet wallet;
private  final File walletFile;

// create wallet.
walletFile = new File("walletTest");
wallet = new WatchOnlyWallet(walletFile, NetworkType.PRODUCTION);

// import valid addresses
wallet.importAddress("pRo2xwwWF2mKC78kwUCZiYaLeVUn4yCpbo");
wallet.importAddress("p6YdVrQFGfp8AuhpnaqgAVHibNVd3LxbTU"); 

// private listener class
private class Listener implements IncomingTransactionListener{
        @Override
        public void incomingEvent(IncomingTransactionEvent incomingTransactionEvent) {
        //do something with the transaction
        }
    }

// create and add the listener    
Listener listener = new Listener();
wallet.addIncomingTransactionListener(listener);

// connect the wallet and sync with the blockchain
this.wallet.connect();

```

##Build
To compilethe project with Maven and generate the JAR file execute:

```
$ mvn package
```

All test will be executed. The tests include creating a wallet that import some real addresses and trigger events, so if you want to avoid them, you can also execute:

```
$ mvn -Dmaven.test.skip=true -Djacoco.skip=true package	
```

The JAR package with all dependencies is located at **IoPWatchOnlyWallet/target** directory.

## Author

* **Rodrigo Acosta**  - [acostarodrigo](https://github.com/acostarodrigo)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details