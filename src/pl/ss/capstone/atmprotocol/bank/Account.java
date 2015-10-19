package pl.ss.capstone.atmprotocol.bank;

import java.math.BigDecimal;

/**
 * Created by nulon on 09.10.15.
 */
public class Account {
    private String accountName;
    private Long accountCard;
    private BigDecimal balance;

    public Account(String accountName, Long accountCard, double initialBalance) {
        this.accountName = accountName;
        this.accountCard = accountCard;
        this.balance = BigDecimal.ZERO;
        this.balance = this.balance.add(BigDecimal.valueOf(initialBalance));
    }

    public String getAccountName() {
        return accountName;
    }

    public long getAccountCard() {
        return accountCard;
    }

    public double balance(long card) {
        if (!accountCard.equals(card)){
            return -1;
        }
        return balance.doubleValue();
    }

    public boolean deposit(long card, double value) {
        if (!accountCard.equals(card)){
            return false;
        }
        if (value <= 0){
            return false;
        }else{
            this.balance = this.balance.add(BigDecimal.valueOf(value));
            return true;
        }
    }

    public boolean withdraw(long card, double value) {
        if (!accountCard.equals(card)){
            return false;
        }
        if (value <= 0){
            return false;
        }else if (this.balance.compareTo(BigDecimal.valueOf(value))>=0){
            this.balance = this.balance.subtract(BigDecimal.valueOf(value));
            return true;
        }else{
            return false;
        }
    }

}
