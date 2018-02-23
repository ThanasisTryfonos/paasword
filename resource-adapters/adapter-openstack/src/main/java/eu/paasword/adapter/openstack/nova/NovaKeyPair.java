package eu.paasword.adapter.openstack.nova;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Keypair;

import java.util.List;

/**
 * Created by smantzouratos on 16/09/16.
 */
public class NovaKeyPair {

    // Variables
    private OSClient.OSClientV2 os2;
    private OSClient.OSClientV3 os3;

    public NovaKeyPair(OSClient.OSClientV2 os) {
        this.os2 = os;
    }
    
    public NovaKeyPair(OSClient.OSClientV3 os) {
        this.os3 = os;
    }

    // Query operations for KeyPairObj’s
    /**
     * Get all Keypairs the current account making the request has access to.
     */
    public List<? extends Keypair> getAllKeypairsV2() {
        List<? extends Keypair> kps = os2.compute().keypairs().list();

        return kps;

    }

    /**
     * Get a KeyPairObj by Name.
     */
    public Keypair getKeypairByNameV2(String name) {
        Keypair kp = os2.compute().keypairs().get(name);

        return kp;

    }

    // CRUD operations for KeyPairObj’s
    /**
     * Creating a KeyPairObj with a pre-generated public key.
     */
    public Keypair createKeypairPreGenPubKeyV2(String name, String publicKey) {
        Keypair kp = os2.compute().keypairs().create(name, publicKey);
        return kp;
    }//EoM

    public Keypair createKeypairPreGenPubKeyV3(String name, String publicKey) {
        Keypair kp = os3.compute().keypairs().create(name, publicKey);
        return kp;
    }//EoM

    /**
     * Creating a KeyPairObj with a compute (nova) generated public key.
     */
    public Keypair createKeypairNoPreGenPubKeyV2(String name) {
        Keypair kp = os2.compute().keypairs().create(name, null);

        return kp;

    }

    /**
     * Deleting a KeyPairObj.
     */
    public void deleteKeypairV2(String name) {
        os2.compute().keypairs().delete(name);

    }

}
