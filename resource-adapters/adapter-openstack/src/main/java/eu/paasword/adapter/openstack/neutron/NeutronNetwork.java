package eu.paasword.adapter.openstack.neutron;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.identity.v2.Tenant;
import org.openstack4j.model.network.AttachInterfaceType;
import org.openstack4j.model.network.IPVersionType;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Port;
import org.openstack4j.model.network.Router;
import org.openstack4j.model.network.RouterInterface;
import org.openstack4j.model.network.Subnet;

import java.util.List;

/**
 * Created by smantzouratos on 16/09/16.
 */
public class NeutronNetwork {

    private OSClient.OSClientV2 os2;    
    private OSClient.OSClientV3 os3;    
    
    public NeutronNetwork(OSClient.OSClientV2 os) {
        this.os2 = os;
    }    
    
    public NeutronNetwork(OSClient.OSClientV3 os) {
        this.os3 = os;
    }    
    
    public Network createNetworkV2(String name, Tenant tenant) {
        Network network = os2.networking().network()
                .create(Builders.network().name(name).tenantId(tenant.getId()).build());

        return network;

    }

    //Querying for Networks
    /**
     * List all Networks
     */
    public List<? extends Network> listNetworksV2() {
        //List the networks which the current tenant has access to
        List<? extends Network> networks = os2.networking().network().list();
        return networks;
    }//EoM
    
    public List<? extends Network> listNetworksV3() {
        //List the networks which the current tenant has access to
        List<? extends Network> networks = os3.networking().network().list();
        return networks;
    }//EoM
    

    /**
     * Get a network by ID
     */
    public Network getNetworkByIDV2(String networkId) {
        Network network = os2.networking().network().get(networkId);

        return network;

    }

    /**
     * Delete a Network
     */
    public void deleteNetworkByIDV2(String networkId) {
        os2.networking().network().delete(networkId);

    }

    //Subnets
    //===============================================================================================
    //===============================================================================================
    /**
     * Create a Subnet
     */
    public Subnet createSubnetIPV4V2(String name, String networkId, String tenantId, String startIP, String endIP, String cidr) {
        Subnet subnet = os2.networking().subnet().create(Builders.subnet()
                .name(name)
                .networkId(networkId)
                .tenantId(tenantId)
                .addPool(startIP, endIP)
                .ipVersion(IPVersionType.V4)
                .cidr(cidr)
                .enableDHCP(true)
                .build());

        return subnet;

    }

    //Querying for Subnets
    /**
     * List all subnets
     */
    public List<? extends Subnet> listSubnetsV2() {
        //List all subnets which the current authorized tenant has access to
        List<? extends Subnet> subnets = os2.networking().subnet().list();

        return subnets;

    }

    /**
     * Get a Subnet by ID
     */
    public Subnet getSubnetByIDV2(String subnetId) {
        Subnet subnet = os2.networking().subnet().get(subnetId);

        return subnet;

    }

    /**
     * Delete a Subnet
     */
    public void deleteSubnetByIDV2(String subnetId) {
        os2.networking().subnet().delete(subnetId);

    }

    //Ports
    //===============================================================================================
    //===============================================================================================
    /**
     * Create a Port
     */
    public Port createPortV2(String name, String networkId, String address, String subnetId) {
        Port port = os2.networking().port().create(Builders.port()
                .name(name)
                .networkId(networkId)
                .fixedIp(address, subnetId)
                .build());

        return port;

    }

    /**
     * Updating the Port
     */
    public Port updatePortV2(Port port, String name) {
        //TODO: See code for errors. Original: "port.tobuilder().name("port-1-1").build()"
        Port updatedPort = os2.networking().port().update(port.toBuilder().name(name).build());

        return updatedPort;

    }

    //Querying for Ports
    /**
     * List all Ports
     */
    public List<? extends Port> listPortsV2() {
        //List all Ports which the current authorized tenant has access to
        List<? extends Port> ports = os2.networking().port().list();

        return ports;

    }

    /**
     * Get a Port by ID
     */
    public Port getPortByIDV2(String portId) {
        Port port = os2.networking().port().get(portId);

        return port;

    }

    /**
     * Delete a Port
     */
    public void deletePortByIDV2(String portId) {
        os2.networking().port().delete(portId);

    }

    //Routers
    //===============================================================================================
    //===============================================================================================
    /**
     * Create a Router
     */
    public Router createRouterV2(String name, String externalNetworkId) {
        Router router = os2.networking().router().create(Builders.router()
                .name(name)
                .adminStateUp(true)
                .externalGateway(externalNetworkId)
                .build());

        return router;

    }

    /**
     * Create a Router with internal routes
     */
    public Router createRouterV2(String name, String externalNetworkId, String destination, String nextHop) {
        Router router = os2.networking().router().create(Builders.router()
                .name(name)
                .adminStateUp(true)
                .externalGateway(externalNetworkId)
                .route(destination, nextHop)
                .build());

        return router;

    }

    /**
     * Update a Router
     */
    public Router updateRouterV2(Router router, String name) {
        //TODO: See code for errors.
        Router updatedrouter = os2.networking().router().update(router.toBuilder().name(name).build());

        return updatedrouter;

    }

    /**
     * Toggle Administrative State
     */
    public Router toggleAdministrativeStateV2(String routerId) {
        Router router = os2.networking().router().toggleAdminStateUp(routerId, true);

        return router;

    }

    //Attaching / Detaching External Interfaces
    /**
     * Attach an External Interface using subnet name
     */
    public RouterInterface attachExternalSubnetInterfaceToRouterV2(String routerId, String subnetId) {
        RouterInterface iface = os2.networking().router()
                .attachInterface(routerId, AttachInterfaceType.SUBNET, subnetId);

        return iface;

    }

    /**
     * Attach an External Interface using port name
     */
    public RouterInterface attachExternalPortInterfaceToRouterV2(String routerId, String portId) {
        RouterInterface iface = os2.networking().router()
                .attachInterface(routerId, AttachInterfaceType.PORT, portId);

        return iface;

    }

    /**
     * Detach an External Interface
     */
    public RouterInterface detachExternalInterfaceToRouterV2(String routerId, String subnetId) {
        RouterInterface iface = os2.networking().router()
                .detachInterface(routerId, subnetId, null);

        return iface;

    }

    //Querying for Routers
    /**
     * List all Routers
     */
    public List<? extends Router> listRoutersV2() {
        //List all Routers which the current authorized tenant has access to
        List<? extends Router> routers = os2.networking().router().list();

        return routers;

    }

    /**
     * Get a Router by ID
     */
    public Router getRouterByIDV2(String routerId) {
        Router router = os2.networking().router().get(routerId);

        return router;

    }

    /**
     * Delete a Router
     */
    public void deleteRouterByIDV2(String routerId) {
        os2.networking().router().delete(routerId);

    }

}
