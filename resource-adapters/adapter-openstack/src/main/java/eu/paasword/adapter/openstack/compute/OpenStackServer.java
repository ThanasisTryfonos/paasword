/*
 *  Copyright 2016 PaaSword Framework, http://www.paasword.eu/
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.paasword.adapter.openstack.compute;

import eu.paasword.adapter.openstack.util.ServerAction;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.compute.*;
import org.openstack4j.model.network.NetFloatingIP;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by John Tsantilis on 20/07/16.
 */
public class OpenStackServer {


    private final OSClient.OSClientV2 os;

    public OpenStackServer(OSClient.OSClientV2 os) {
        this.os = os;
    }

    /**
     * Create and Boot a Server Instance.
     */
    public Server createNewServer(String serverName, String flavorId, String imageId, List<String> networkNames, String keyPairName) {

        // Create a Server Model Object
        ServerCreate sc = Builders.server().name(serverName).flavor(flavorId).image(imageId).networks(networkNames).keypairName(keyPairName).build();

        // Boot the Server
        Server server = os.compute().servers().boot(sc);

        return server;

    }

    /**
     * Create and Boot a Server Instance.
     * Overloaded method.
     */
    public Server createNewServer(String serverName, String flavorId, String imageId, List<String> networkNames, String keyPairName, String path, String contents) {
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
        Server server = os.compute().servers().boot(sc);

        return server;

    }

    /**
     * Delete a Serve Instance.
     */
    public void deleteServer(String serverId) {
        os.compute().servers().delete(serverId);

    }

    /**
     * Perform Actions on a Server Instance.
     */
    public void performeActionOnServer(ServerAction serverAction, String serverId) {
        Scanner keyboard = new Scanner(System.in);

        switch (serverAction) {

            case START:
                // Start a Server
                os.compute().servers().action(serverId, Action.START);
                break;

            case PAUSE:
                // Pause a Server
                os.compute().servers().action(serverId, Action.PAUSE);
                break;

            case UNPAUSE:
                // UnPause a Server
                os.compute().servers().action(serverId, Action.UNPAUSE);
                break;

            case STOP:
                // Stop a Server
                os.compute().servers().action(serverId, Action.STOP);
                break;

            case LOCK:
                // Lock a Server
                os.compute().servers().action(serverId, Action.LOCK);
                break;

            case UNLOCK:
                // UnLock a Server
                os.compute().servers().action(serverId, Action.UNLOCK);
                break;

            case SUSPEND:
                // Suspend a Server
                os.compute().servers().action(serverId, Action.SUSPEND);
                break;

            case RESUME:
                // Resume a Server
                os.compute().servers().action(serverId, Action.RESUME);
                break;

            case RESCUE:
                // Rescue a Server
                os.compute().servers().action(serverId, Action.RESCUE);
                break;

            case UNRESCUE:
                // UnRescue a Server
                os.compute().servers().action(serverId, Action.UNRESCUE);
                break;

            case SHELVE:
                // Shelve a Server
                os.compute().servers().action(serverId, Action.SHELVE);
                break;

            case SHELVE_OFFLOAD:
                // Shelve a Server
                os.compute().servers().action(serverId, Action.SHELVE_OFFLOAD);
                break;

            case UNSHELVE:
                // Shelve a Server
                os.compute().servers().action(serverId, Action.UNSHELVE);
                break;

            case REBOOT:
                // Reboot a Server
                os.compute().servers().reboot(serverId, RebootType.SOFT);
                break;

            case RESIZE:
                //TODO: Relocate Resizing to a more appropriate location.
                // Resize a Server
                System.out.println("Enter newFlavorID: ");
                String newFlavorID = keyboard.next();
                os.compute().servers().resize(serverId, newFlavorID);
                os.compute().servers().confirmResize(serverId);
                break;

            case REVERTRESIZE:
                // Revert a Resize
                os.compute().servers().revertResize(serverId);
                break;

            default:
                System.out.println("Not a valid Action argument! Please retry");
                break;

        }

    }

    /**
     * Create a new Snapshot of a given server.
     */
    public String CreateNewServerSnapshot(String serverId, String snapshotName) {
        String imageId = os.compute().servers().createSnapshot(serverId, snapshotName);

        return imageId;

    }

    // Floating IP Addresses ###################################
    /**
     * Getting a List of available Pool Names.
     */
    public List<String> ListAvailablePoolNames() {
        List<String> pools = os.compute().floatingIps().getPoolNames();

        return pools;

    }

    /**
     * List floating IP addresses.
     */
    public List<FloatingIP>  ListFloatingIPs() {
        //TODO: Review code
        List<FloatingIP> ips = (List<FloatingIP>) os.compute().floatingIps().list();

        return ips;

    }

    /**
     * Allocate a floating IP address from a pool.
     */
    public FloatingIP allocateFloatingIPFromPool(String poolName) {
        //TODO: Maybe requires change to poolID
        FloatingIP ip = os.compute().floatingIps().allocateIP(poolName);

        return ip;

    }

    /**
     * Deallocate a floating IP address.
     */
    public void deAllocateFloatingIP(String floatingIp_id) {
        os.compute().floatingIps().deallocateIP(floatingIp_id);

    }

    /**
     * Add a floating IP address to a server.
     */
    public ActionResponse addFloatingIPAddressToServer(Server server, String fixedIPAddress, String IPAddress) {
        ActionResponse r = os.compute().floatingIps().addFloatingIP(server, fixedIPAddress, IPAddress);

        return r;

    }

    /**
     * Add a floating IP address to a server if Neutron is installed.
     */
    public ActionResponse addFloatingIPAddressToServerIfNeutronPresent(String ipId, String serverId) {
        NetFloatingIP netFloatingIP = os.networking().floatingip().get(ipId);
        Server server = os.compute().servers().get(serverId);
        ActionResponse r = os.compute().floatingIps().addFloatingIP(server, netFloatingIP.getFloatingIpAddress());

        return r;

    }

    /**
     * Remove a floating IP address from a server.
     */
    public ActionResponse removeFloatingIPAddressToServerIfNeutronPresent(Server server, String IPAddress) {
        ActionResponse r = os.compute().floatingIps().removeFloatingIP(server, IPAddress);

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
    public String grabConsoleServerOutput(String serverId, int numLines) {
        String consoleOutput = os.compute().servers().getConsoleOutput(serverId, numLines);

        return consoleOutput;

    }

    /**
     * Fetch server diagnostics.
     */
    public Map<String, ? extends Number> fetchServerDiagnostics(String serverId) {
        Map<String, ? extends Number> diagnostics = os.compute().servers().diagnostics(serverId);

        return diagnostics;

    }


    /**
     * List all Servers. (Boolean state true)
     * List all servers (light) ID, Name and Links populated. (Boolean state false)
     */
    public List<? extends Server> listServers(Boolean state) {
        List<? extends Server> servers;
        if (state == false) {
            servers = os.compute().servers().list(state);

        }
        else {
            servers = os.compute().servers().list();

        }

        return servers;

    }

    /**
     * Get a specific Server by ID.
     */
    public Server listServer(String serverId) {
        Server server = os.compute().servers().get(serverId);

        return server;

    }

}
