package eu.paasword.adapter.openstack.nova;

import eu.paasword.adapter.openstack.util.ServerAction;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.compute.Action;
import org.openstack4j.model.compute.FloatingIP;
import org.openstack4j.model.compute.RebootType;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.network.NetFloatingIP;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by smantzouratos on 16/09/16.
 */
public class NovaServer {

    // Variables
    private  OSClient.OSClientV2 os2 ;
    private  OSClient.OSClientV3 os3 ;
            
    public NovaServer(OSClient.OSClientV2 os) {
        this.os2 = os;
    }

    public NovaServer(OSClient.OSClientV3 os) {
        this.os3 = os;
    }    
    
//    public NovaServer(OSClient.OSClientV3 os) {
//        this.os = os;
//    }    
    
    /**
     * Create and Boot a Server Instance.
     */
    public Server createNewServerV2(String serverName, String flavorId, String imageId, List<String> networkNames, String keyPairName) {
        // Create a Server Model Object
        ServerCreate sc = Builders.server().name(serverName).flavor(flavorId).image(imageId).networks(networkNames).keypairName(keyPairName).build();
        // Boot the Server
        Server server = os2.compute().servers().boot(sc);
        return server;
    }//EoM
    
    public Server createNewServerV3(String serverName, String flavorId, String imageId, List<String> networkNames, String keyPairName) {
        // Create a Server Model Object
        ServerCreate sc = Builders.server().name(serverName).flavor(flavorId).image(imageId).networks(networkNames).keypairName(keyPairName).build();
        // Boot the Server
        Server server = os3.compute().servers().boot(sc);
        return server;
    }//EoM

    /**
     * Create and Boot a Server Instance.
     * Overloaded method.
     */
    public Server createNewServerV2(String serverName, String flavorId, String imageId, List<String> networkNames, String keyPairName, String path, String contents) {
        // Create a Server Model Object
        ServerCreate sc = Builders.server()
                .name(serverName)
                .flavor(flavorId)
                .image(imageId)
                .networks(networkNames)
                .keypairName(keyPairName)
                .addPersonality(path, contents)
                .build();
        // Boot the Server
        Server server = os2.compute().servers().boot(sc);
        //Server server = client.compute().servers().bootAndWaitActive(sc, 60 * 1000);
        return server;
    }//EoM
    
    public Server createNewServerV3(String serverName, String flavorId, String imageId, List<String> networkNames, String keyPairName, String path, String contents) {
        // Create a Server Model Object
        ServerCreate sc = Builders.server()
                .name(serverName)
                .flavor(flavorId)
                .image(imageId)
                .networks(networkNames)
                .keypairName(keyPairName)
                .addPersonality(path, contents)
                .build();
        // Boot the Server
        Server server = os3.compute().servers().boot(sc);
        //Server server = client.compute().servers().bootAndWaitActive(sc, 60 * 1000);
        return server;
    }//EoM
    
    public Server createNewServerV3(String serverName, String flavorId, String imageId, List<String> networkNames, String keyPairName, String userData) {
        // Create a Server Model Object
        ServerCreate sc = Builders.server()
                .name(serverName)
                .flavor(flavorId)
                .image(imageId)
                .networks(networkNames)
                .keypairName(keyPairName)
                .userData(userData)
                .build();
        // Boot the Server
        Server server = os3.compute().servers().boot(sc);
        //Server server = client.compute().servers().bootAndWaitActive(sc, 60 * 1000);

        return server;

    }    
    

    /**
     * Delete a Serve Instance.
     */
    public void deleteServerV2(String serverId) {
        os2.compute().servers().delete(serverId);
    }
    
    public void deleteServerV3(String serverId) {
        os3.compute().servers().delete(serverId);
    }

    /**
     * Perform Actions on a Server Instance.
     */
    public void performedActionOnServerV2(ServerAction serverAction, String serverId) {
        Scanner keyboard = new Scanner(System.in);

        switch (serverAction) {

            case START:
                // Start a Server
                os2.compute().servers().action(serverId, Action.START);
                break;

            case PAUSE:
                // Pause a Server
                os2.compute().servers().action(serverId, Action.PAUSE);
                break;

            case UNPAUSE:
                // UnPause a Server
                os2.compute().servers().action(serverId, Action.UNPAUSE);
                break;

            case STOP:
                // Stop a Server
                os2.compute().servers().action(serverId, Action.STOP);
                break;

            case LOCK:
                // Lock a Server
                os2.compute().servers().action(serverId, Action.LOCK);
                break;

            case UNLOCK:
                // UnLock a Server
                os2.compute().servers().action(serverId, Action.UNLOCK);
                break;

            case SUSPEND:
                // Suspend a Server
                os2.compute().servers().action(serverId, Action.SUSPEND);
                break;

            case RESUME:
                // Resume a Server
                os2.compute().servers().action(serverId, Action.RESUME);
                break;

            case RESCUE:
                // Rescue a Server
                os2.compute().servers().action(serverId, Action.RESCUE);
                break;

            case UNRESCUE:
                // UnRescue a Server
                os2.compute().servers().action(serverId, Action.UNRESCUE);
                break;

            case SHELVE:
                // Shelve a Server
                os2.compute().servers().action(serverId, Action.SHELVE);
                break;

            case SHELVE_OFFLOAD:
                // Shelve a Server
                os2.compute().servers().action(serverId, Action.SHELVE_OFFLOAD);
                break;

            case UNSHELVE:
                // Shelve a Server
                os2.compute().servers().action(serverId, Action.UNSHELVE);
                break;

            case REBOOT:
                // Reboot a Server
                os2.compute().servers().reboot(serverId, RebootType.SOFT);
                break;

            case RESIZE:
                //TODO: Relocate Resizing to a more appropriate location.
                // Resize a Server
                System.out.println("Enter newFlavorID: ");
                String newFlavorID = keyboard.next();
                os2.compute().servers().resize(serverId, newFlavorID);
                os2.compute().servers().confirmResize(serverId);
                break;

            case REVERTRESIZE:
                // Revert a Resize
                os2.compute().servers().revertResize(serverId);
                break;

            default:
                System.out.println("Not a valid Action argument! Please retry");
                break;

        }

    }

    /**
     * Create a new Snapshot of a given server.
     */
    public String CreateNewServerSnapshotV2(String serverId, String snapshotName) {
        String imageId = os2.compute().servers().createSnapshot(serverId, snapshotName);

        return imageId;

    }

    // Floating IP Addresses ###################################
    /**
     * Getting a List of available Pool Names.
     */
    public List<String> ListAvailablePoolNamesV2() {
        List<String> pools = os2.compute().floatingIps().getPoolNames();

        return pools;

    }

    /**
     * List floating IP addresses.
     */
    public List<FloatingIP>  ListFloatingIPsV2() {
        //TODO: Review code
        List<FloatingIP> ips = (List<FloatingIP>) os2.compute().floatingIps().list();

        return ips;

    }

    /**
     * Allocate a floating IP address from a pool.
     */
    public FloatingIP allocateFloatingIPFromPoolV2(String poolName) {
        //TODO: Maybe requires change to poolID
        FloatingIP ip = os2.compute().floatingIps().allocateIP(poolName);

        return ip;

    }

    /**
     * Deallocate a floating IP address.
     */
    public void deAllocateFloatingIPV2(String floatingIp_id) {
        os2.compute().floatingIps().deallocateIP(floatingIp_id);

    }

    /**
     * Add a floating IP address to a server.
     */
    public ActionResponse addFloatingIPAddressToServerV2(Server server, String fixedIPAddress, String IPAddress) {
        ActionResponse r = os2.compute().floatingIps().addFloatingIP(server, fixedIPAddress, IPAddress);

        return r;

    }

    /**
     * Add a floating IP address to a server if Neutron is installed.
     */
    public ActionResponse addFloatingIPAddressToServerIfNeutronPresentV2(String ipId, String serverId) {
        NetFloatingIP netFloatingIP = os2.networking().floatingip().get(ipId);
        Server server = os2.compute().servers().get(serverId);
        ActionResponse r = os2.compute().floatingIps().addFloatingIP(server, netFloatingIP.getFloatingIpAddress());

        return r;

    }

    /**
     * Remove a floating IP address from a server.
     */
    public ActionResponse removeFloatingIPAddressToServerIfNeutronPresentV2(Server server, String IPAddress) {
        ActionResponse r = os2.compute().floatingIps().removeFloatingIP(server, IPAddress);

        return r;

    }
    //##########################################################

    public void CreateServerMetadata() {
        //TODO: finish Metadata services

    }

    /**
     * Grab the tail of the console log based on the specified number of lines.
     * Type console log
     */
    public String grabConsoleServerOutputV2(String serverId, int numLines) {
        String consoleOutput = os2.compute().servers().getConsoleOutput(serverId, numLines);

        return consoleOutput;

    }

    /**
     * Fetch server diagnostics.
     */
    public  Map<String, ? extends Number> fetchServerDiagnosticsV2(String serverId) {
        Map<String, ? extends Number> diagnostics = os2.compute().servers().diagnostics(serverId);

        return diagnostics;

    }


    /**
     * List all Servers. (Boolean state true)
     * List all servers (light) ID, Name and Links populated. (Boolean state false)
     */
    public List<? extends Server> listServersV2(Boolean state) {
        List<? extends Server> servers;
        if (state == false) {
            servers = os2.compute().servers().list(state);

        }
        else {
            servers = os2.compute().servers().list();

        }

        return servers;

    }

    /**
     * Get a specific Server by ID.
     */
    public Server listServerV2(String serverId) {
        Server server = os2.compute().servers().get(serverId);

        return server;

    }

}
