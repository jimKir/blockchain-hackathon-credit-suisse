package com.template;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

/**
 * Created by pai on 15.01.18.
 */
public class WorkContract implements Contract {

    public static class Create implements CommandData {

    }

    @Override
    public void verify(LedgerTransaction ledgerTransaction) throws IllegalArgumentException {

    }
}
