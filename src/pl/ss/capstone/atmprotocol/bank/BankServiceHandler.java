package pl.ss.capstone.atmprotocol.bank;

import pl.ss.capstone.atmprotocol.common.utils.CryptoTool;
import t.BankService;
import t.W;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;


public class BankServiceHandler implements BankService.Iface {

    private HashMap<String, Account> accounts;
    private DecimalFormat df = new DecimalFormat("0.##");
    private DecimalFormat parameterCheckerDF = new DecimalFormat("0.00");

    private HashMap<String, Set<Long>> transactions = new HashMap<>();

    private KeyPair keypair;
    private int seed;
    private SecureRandom rng;

    public BankServiceHandler(KeyPair keypair) throws NoSuchAlgorithmException {
        accounts = new HashMap<>();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
        otherSymbols.setDecimalSeparator('.');
        this.df.setDecimalFormatSymbols(otherSymbols);
        this.parameterCheckerDF.setDecimalFormatSymbols(otherSymbols);
        this.keypair = keypair;
        this.seed = this.keypair.hashCode();
        rng = SecureRandom.getInstance("SHA1PRNG");
        rng.setSeed(seed);
    }

    public Long createAccount(String account, double initialBalance) {
        if (accounts.containsKey(account)) {
            return null;
        }
        if (initialBalance < 10.00) {
            return null;
        }
        Account acc = new Account(account, rng.nextLong() >>> 1, initialBalance);
        accounts.put(account, acc);
        return acc.getAccountCard();
    }

    public boolean deposit(String account, double amount, Long card) {
        if (!accounts.containsKey(account)) {
           return false;
        }
        return accounts.get(account).deposit(card, amount);
    }

    public boolean withdraw(String account, double amount, Long card) {
        if (!accounts.containsKey(account)) {
            return false;
        }
        return accounts.get(account).withdraw(card, amount);
    }

    public double balance(String account, Long card) {
        if (!accounts.containsKey(account)) {
            return -1;
        }
        return accounts.get(account).balance(card);
    }


    private boolean isDuplicateTransaction(Long id, String account){
        if (true) return false;
        if (transactions.get(account) != null && transactions.get(account).contains(id)){
            return true;
        }
        if (transactions.get(account) == null){
            transactions.put(account, new HashSet<Long>());
        }
        transactions.get(account).add(id);
        return false;
    }

    @Override
    public long c(W w) {
        if (isDuplicateTransaction(w.getT(),w.getA())){
            System.err.println("Duplicate transaction id");
            System.out.println("protocol_error");
            return -1;
        }

        Long result = createAccount(w.getA(), w.getV());
        if (result == null) {
            return -1;
        }
        System.out.println(String.format("{\"account\":\"%s\",\"initial_balance\":%s}", w.getA(), df.format(w.getV())));
        return result;
    }

    @Override
    public boolean d(W w) {
        if (isDuplicateTransaction(w.getT(),w.getA())){
            System.err.println("Duplicate transaction id");
            System.out.println("protocol_error");
            return false;
        }
        boolean isValid = deposit(w.getA(), w.getV(), w.getC());
        if (!isValid){
            return false;
        }
        System.out.println(String.format("{\"account\":\"%s\",\"deposit\":%s}", w.getA(), df.format(w.getV())));
        return true;
    }

    @Override
    public boolean w(W w) {
        if (isDuplicateTransaction(w.getT(),w.getA())){
            System.err.println("Duplicate transaction id");
            System.out.println("protocol_error");
            return false;
        }
        boolean isValid = withdraw(w.getA(), w.getV(), w.getC());
        if (!isValid){
            return false;
        }
        System.out.println(String.format("{\"account\":\"%s\",\"withdraw\":%s}", w.getA(), df.format(w.getV())));
        return true;
    }

    @Override
    public double b(W w) {
        if (isDuplicateTransaction(w.getT(),w.getA())){
            System.err.println("Duplicate transaction id");
            System.out.println("protocol_error");
            return -2;
        }
        double result = balance(w.getA(), w.getC());
        if (result < 0){
            return -1;
        }
        System.out.println(String.format("{\"account\":\"%s\",\"balance\":%s}", w.getA(), df.format(result)));
        return result;
    }

}
