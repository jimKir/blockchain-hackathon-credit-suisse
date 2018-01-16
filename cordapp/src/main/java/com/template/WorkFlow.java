package com.template;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;


import com.template.vo.WorkStateValue;
import net.corda.core.contracts.AttachmentResolutionException;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.TransactionResolutionException;
import net.corda.core.contracts.TransactionVerificationException;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.Nullable;

import java.security.PublicKey;
import java.util.List;

/**
 * Created by pai on 15.01.18.
 */
public class WorkFlow {

    private static final ProgressTracker.Step ID_OTHER_NODES = new ProgressTracker.Step("Identifying other nodes on the network.");
    private static final ProgressTracker.Step SENDING_AND_RECEIVING_DATA = new ProgressTracker.Step("Sending data between parties.");
    private static final ProgressTracker.Step EXTRACTING_VAULT_STATES = new ProgressTracker.Step("Extracting states from the vault.");
    private static final ProgressTracker.Step OTHER_TX_COMPONENTS = new ProgressTracker.Step("Gathering a transaction's other components.");
    private static final ProgressTracker.Step TX_BUILDING = new ProgressTracker.Step("Building a transaction.");
    private static final ProgressTracker.Step TX_SIGNING = new ProgressTracker.Step("Signing a transaction.");
    private static final ProgressTracker.Step TX_VERIFICATION = new ProgressTracker.Step("Verifying a transaction.");
    private static final ProgressTracker.Step SIGS_GATHERING = new ProgressTracker.Step("Gathering a transaction's signatures.") {
        // Wiring up a child progress tracker allows us to see the
        // subflow's progress steps in our flow's progress tracker.
        @Override
        public ProgressTracker childProgressTracker() {
            return CollectSignaturesFlow.tracker();
        }
    };
    private static final ProgressTracker.Step VERIFYING_SIGS = new ProgressTracker.Step("Verifying a transaction's signatures.");
    private static final ProgressTracker.Step FINALISATION = new ProgressTracker.Step("Finalising a transaction.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return FinalityFlow.tracker();
        }
    };






    /**
     * You can add a constructor to each FlowLogic subclass to pass objects into the flow.
     */
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<Void> {
        private final WorkStateValue workStateValue;
        private final Party acceptor;
        private String desciption;

        public Initiator(Party acceptor, String desciption) {
            this.acceptor = acceptor;
            this.workStateValue = new WorkStateValue(desciption);

        }

        private final ProgressTracker progressTracker = new ProgressTracker(
                ID_OTHER_NODES,
                SENDING_AND_RECEIVING_DATA,
                EXTRACTING_VAULT_STATES,
                OTHER_TX_COMPONENTS,
                TX_BUILDING,
                TX_SIGNING,
                TX_VERIFICATION,
                SIGS_GATHERING,
                FINALISATION
        );

        @Nullable
        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        /**
         * Define the initiator's flow logic here.
         */
        @Suspendable
        @Override public Void call() {
            final Party me = getServiceHub().getMyInfo().getLegalIdentities().get(0);

            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("Controller"));

            TransactionBuilder transactionBuilder = new TransactionBuilder();
            transactionBuilder.setNotary(notary);

            WorkState workState = new WorkState(me, this.acceptor, this.workStateValue);
            List<PublicKey> requiredSigners = ImmutableList.of(me.getOwningKey(), acceptor.getOwningKey());
            Command cmd = new Command(new WorkContract.Create(), requiredSigners);
            transactionBuilder.withItems(workState, cmd);
            progressTracker.setCurrentStep(ID_OTHER_NODES);
            try {
                transactionBuilder.verify(getServiceHub());
            } catch (AttachmentResolutionException e) {
                e.printStackTrace();
            } catch (TransactionResolutionException e) {
                e.printStackTrace();
            } catch (TransactionVerificationException e) {
                e.printStackTrace();
            }
            final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);
            try {
                subFlow( new FinalityFlow(signedTransaction));
            } catch (FlowException e) {
                e.printStackTrace();
            }
            System.out.println("Done");
            progressTracker.setCurrentStep(FINALISATION);

            return null;
        }
    }

    @InitiatedBy(Initiator.class)
    public static class Responder extends FlowLogic<Void> {
        private FlowSession counterpartySession;

        public Responder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        /**
         * Define the acceptor's flow logic here.
         */
        @Suspendable
        @Override
        public Void call() { return null; }
    }

}
