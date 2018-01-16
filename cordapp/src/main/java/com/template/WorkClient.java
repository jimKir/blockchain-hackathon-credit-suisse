package com.template;

import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCClientConfiguration;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.DataFeed;
import net.corda.core.node.services.Vault;
import net.corda.core.utilities.NetworkHostAndPort;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.concurrent.ExecutionException;

/**
 * Created by pai on 15.01.18.
 */
public class WorkClient {

    private static final Logger logger = LoggerFactory.getLogger(WorkClient.class);


    private static void logState(StateAndRef<WorkState> state) {

        logger.info("{}", state.getState().getData());
    }


    public static void main(String[] args) throws ActiveMQException, InterruptedException, ExecutionException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: TemplateClient <node address>");
        }

        final NetworkHostAndPort nodeAddress = NetworkHostAndPort.parse(args[0]);
        final CordaRPCClient client = new CordaRPCClient(nodeAddress, CordaRPCClientConfiguration.DEFAULT);

        // Can be amended in the Main file.
        final CordaRPCOps proxy = client.start("user1", "test").getProxy();

        // Grab all existing TemplateStates and all future TemplateStates.
        final DataFeed<Vault.Page<WorkState>, Vault.Update<WorkState>> dataFeed = proxy.vaultTrack(WorkState.class);

        final Vault.Page<WorkState> snapshot = dataFeed.getSnapshot();
        final Observable<Vault.Update<WorkState>> updates = dataFeed.getUpdates();

        // Log the existing TemplateStates and listen for new ones.
        snapshot.getStates().forEach(WorkClient::logState);
        updates.toBlocking().subscribe(update -> update.getProduced().forEach(WorkClient::logState));
    }

}
