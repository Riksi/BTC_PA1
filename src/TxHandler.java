import java.util.*;
public class TxHandler {

	/* Creates a public ledger whose current UTXOPool (collection of unspent 
	 * transaction outputs) is utxoPool. This should make a defensive copy of 
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
    private UTXOPool utxoPool;
    
	public TxHandler(UTXOPool utxoPool) {
		this.utxoPool = new UTXOPool(utxoPool);
	}

	/* Returns true if 
	 * (1) all outputs claimed by tx are in the current UTXO pool, 
	 * (2) the signatures on each input of tx are valid, 
	 * (3) no UTXO is claimed multiple times by tx, 
	 * (4) all of tx’s output values are non-negative, and
	 * (5) the sum of tx’s input values is greater than or equal to the sum of   
	        its output values;
	   and false otherwise.
	 */

	public boolean isValidTx(Transaction tx) {
		// IMPLEMENT THIS
	    Map<UTXO,Boolean> claims = new HashMap<>();
	    for (UTXO utxo:utxoPool.getAllUTXO()){
	        claims.put(utxo, false);
	    }
	    
	    double inputSum = 0;
	    double outputSum = 0;
	    
	    
	    List<Transaction.Input> inputs  = tx.getInputs();
	    for(Transaction.Input input:inputs){
	        
	        int index = inputs.indexOf(input);
	        //validate signature
	        UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
	        
	        if(!utxoPool.contains(utxo)) return false; // UTXO not in pool
	        Transaction.Output unspentOutput = utxoPool.getTxOutput(utxo);
	        boolean verified = unspentOutput.address.verifySignature(tx.getRawDataToSign(index), input.signature);
	        if(!verified) return false;
	        if(claims.get(utxo)) return false; // tx claims UTXO more than once
	        inputSum+=unspentOutput.value; //update input sum
	        claims.put(utxo,true);
	    }
	    
	    for(Transaction.Output output: tx.getOutputs()){
	        double value = output.value;
	        if(value < 0) return false; //non-negative output
	        outputSum+=value; //update output sum
	    }
		return (inputSum >= outputSum);
	}

	
	
	
	/* Handles each epoch by receiving an unordered array of proposed 
	 * transactions, checking each transaction for correctness, 
	 * returning a mutually valid array of accepted transactions, 
	 * and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
	    
	    Transaction[] validTxs = {};
	    while(true){
	        int added = 0;
    	    for(Transaction tx: possibleTxs){
    	        if(isValidTx(tx)){
    	            validTxs[validTxs.length] = tx;
    	            System.out.println(tx);
    	            //claimed outputs should be deleted
    	            for(Transaction.Input input:tx.getInputs()){
    	                utxoPool.removeUTXO(new UTXO(input.prevTxHash, input.outputIndex));
    	            }
    	            
    	            //outputs of this transaction should be added to the pool
    	            for(int outIndex = 0; outIndex < tx.numOutputs(); outIndex++){
    	                utxoPool.addUTXO(new UTXO(tx.getHash(),outIndex), tx.getOutput(outIndex));
    	            }
    	            
    	            added+=1;
    	        }
    	    }
    	    
    	    if(added == 0) break;
	    }
		return validTxs;
	}

} 
