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
package eu.paasword.adapter.openstack.util;



public class CloudCfgPaaSword {
    @Override
    public String toString() {
        return "CloudCfgPaaSword{" +
                "text='" + text + '\'' +
                '}';
    }

    /**
     * Setters & Getters
     */
    public String getText() {
        return text;

    }

    public void setText(String text) {
        this.text = text;

    }

    /**
     * Constructors
     */
    //Default Constructor
    public CloudCfgPaaSword() {
        this.text = "# The top level settings are used as module\n" +
                "# and system configuration.\n" +
                "\n" +
                "# A set of users which may be applied and/or used by various modules\n" +
                "# when a 'default' entry is found it will reference the 'default_user'\n" +
                "# from the distro configuration specified below\n" +
                "users:\n" +
                "   - default\n" +
                "\n" +
                "# If this is set, 'root' will not be able to ssh in and they \n" +
                "# will get a message to login instead as the above $user (ubuntu)\n" +
                "disable_root: true\n" +
                "\n" +
                "# This will cause the set+update hostname module to not operate (if true)\n" +
                "preserve_hostname: false\n" +
                "\n" +
                "# Example datasource config\n" +
                "# datasource: \n" +
                "#    Ec2: \n" +
                "#      metadata_urls: [ 'blah.com' ]\n" +
                "#      timeout: 5 # (defaults to 50 seconds)\n" +
                "#      max_wait: 10 # (defaults to 120 seconds)\n" +
                "\n" +
                "# The modules that run in the 'init' stage\n" +
                "cloud_init_modules:\n" +
                " - migrator\n" +
                " - seed_random\n" +
                " - bootcmd\n" +
                " - write-files\n" +
                " - growpart\n" +
                " - resizefs\n" +
                " - set_hostname\n" +
                " - update_hostname\n" +
                " - update_etc_hosts\n" +
                " - ca-certs\n" +
                " - rsyslog\n" +
                " - users-groups\n" +
                " - ssh\n" +
                "\n" +
                "# The modules that run in the 'config' stage\n" +
                "cloud_config_modules:\n" +
                "# Emit the cloud config ready event\n" +
                "# this can be used by upstart jobs for 'start on cloud-config'.\n" +
                " - emit_upstart\n" +
                " - disk_setup\n" +
                " - mounts\n" +
                " - ssh-import-id\n" +
                " - locale\n" +
                " - set-passwords\n" +
                " - grub-dpkg\n" +
                " - apt-pipelining\n" +
                " - apt-configure\n" +
                " - package-update-upgrade-install\n" +
                " - landscape\n" +
                " - timezone\n" +
                " - puppet\n" +
                " - chef\n" +
                " - salt-minion\n" +
                " - mcollective\n" +
                " - disable-ec2-metadata\n" +
                " - runcmd\n" +
                " - byobu\n" +
                "\n" +
                "# The modules that run in the 'final' stage\n" +
                "cloud_final_modules:\n" +
                " - rightscale_userdata\n" +
                " - scripts-vendor\n" +
                " - scripts-per-once\n" +
                " - scripts-per-boot\n" +
                " - scripts-per-instance\n" +
                " - scripts-user\n" +
                " - ssh-authkey-fingerprints\n" +
                " - keys-to-console\n" +
                " - phone-home\n" +
                " - final-message\n" +
                " - power-state-change\n" +
                "\n" +
                "# System and/or distro specific settings\n" +
                "# (not accessible to handlers/transforms)\n" +
                "system_info:\n" +
                "   # This will affect which distro class gets used\n" +
                "   distro: ubuntu\n" +
                "   # Default user name + that default users groups (if added/used)\n" +
                "   default_user:\n" +
                "     name: ubuntu\n" +
                "     lock_passwd: True\n" +
                "     gecos: Ubuntu\n" +
                "     groups: [adm, audio, cdrom, dialout, dip, floppy, netdev, plugdev, sudo, video]\n" +
                "     sudo: [\"ALL=(ALL) NOPASSWD:ALL\"]\n" +
                "     shell: /bin/bash\n" +
                "   # Other config here will be given to the distro class and/or path classes\n" +
                "   paths:\n" +
                "      cloud_dir: /var/lib/cloud/\n" +
                "      templates_dir: /etc/cloud/templates/\n" +
                "      upstart_dir: /etc/init/\n" +
                "   package_mirrors:\n" +
                "     - arches: [i386, amd64]\n" +
                "       failsafe:\n" +
                "         primary: http://archive.ubuntu.com/ubuntu\n" +
                "         security: http://security.ubuntu.com/ubuntu\n" +
                "       search:\n" +
                "         primary:\n" +
                "           - http://%(ec2_region)s.ec2.archive.ubuntu.com/ubuntu/\n" +
                "           - http://%(availability_zone)s.clouds.archive.ubuntu.com/ubuntu/\n" +
                "           - http://%(region)s.clouds.archive.ubuntu.com/ubuntu/\n" +
                "         security: []\n" +
                "     - arches: [armhf, armel, default]\n" +
                "       failsafe:\n" +
                "         primary: http://ports.ubuntu.com/ubuntu-ports\n" +
                "         security: http://ports.ubuntu.com/ubuntu-ports\n" +
                "   ssh_svcname: ssh\n" +
                "\n" +
                "scripts-user:\n" +
                "#!/bin/bash\n" +
                "# Example script to run at first boot via Openstack\n" +
                "# using the user_data and cloud-init.\n" +
                "# This example installs Ansible and deploys your \n" +
                "# org's example App.\n" +
                "\n" +
                "sudo -u postgres bash -c \"psql -c \\\"CREATE USER vagrant WITH PASSWORD 'vagrant';\\\"\"\n" +
                "sudo -u postgres bash -c \"psql -c \\\"ALTER ROLE postgres WITH ENCRYPTED PASSWORD 'postgres';\\\"\"\n" +
                "sudo -u postgres bash -c \"psql -c \\\"CREATE DATABASE databaseName;\\\"\"\n" +
                "exit 0";

    }

    public CloudCfgPaaSword(String text) {
        this.text = text;

    }

    String text;

}