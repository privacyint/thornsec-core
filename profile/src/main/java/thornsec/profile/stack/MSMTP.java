/*
 * This code is part of the ThornSec project.
 *
 * To learn more, please head to its GitHub repo: @privacyint
 *
 * Pull requests encouraged.
 */
package profile.stack;

import java.util.ArrayList;
import java.util.Collection;
import core.exception.data.InvalidPortException;
import core.exception.runtime.InvalidMachineModelException;
import core.iface.IUnit;
import core.model.machine.ServerModel;
import core.profile.AStructuredProfile;
import core.unit.pkg.InstalledUnit;
import inet.ipaddr.HostName;

/**
 * This profile installs and configures msmtp (https://marlam.de/msmtp/).
 *
 * This allows servers to send authenticated emails (and otherwise)
 */
public class MSMTP extends AStructuredProfile {

	public MSMTP(ServerModel me) {
		super(me);
	}

	@Override
	public Collection<IUnit> getInstalled() {
		final Collection<IUnit> units = new ArrayList<>();

		units.add(new InstalledUnit("msmtp_mta", "proceed", "msmtp-mta"));

		return units;
	}

	@Override
	public Collection<IUnit> getPersistentFirewall() throws InvalidMachineModelException, InvalidPortException {
		final Collection<IUnit> units = new ArrayList<>();

		getServerModel().addEgress(new HostName("*:25,465"));

		return units;
	}
}