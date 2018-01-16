package com.template;

import com.google.common.collect.ImmutableList;
import com.template.vo.WorkStateValue;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by pai on 15.01.18.
 */
public class WorkState implements ContractState {

    private final Party proposer;
    private final Party acceptor;
    private final WorkStateValue workStateValue;



    public WorkState(Party proposer, Party acceptor, WorkStateValue workStateValue) {
        this.proposer = proposer;
        this.acceptor = acceptor;
        this.workStateValue = workStateValue;
    }

    public Party getProposer() {
        return proposer;
    }

    public Party getAcceptor() {
        return acceptor;
    }



    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(proposer, acceptor);
    }
}
