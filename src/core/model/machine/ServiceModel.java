/*
 * This code is part of the ThornSec project.
 *
 * To learn more, please head to its GitHub repo: @privacyint
 *
 * Pull requests encouraged.
 */
package core.model.machine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.json.stream.JsonParsingException;
import javax.mail.internet.AddressException;
import core.data.machine.configuration.DiskData;
import core.data.machine.configuration.DiskData.Format;
import core.data.machine.configuration.DiskData.Medium;
import core.exception.AThornSecException;
import core.exception.data.machine.InvalidServerException;
import core.iface.IUnit;
import core.model.machine.configuration.DiskModel;
import core.model.network.NetworkModel;

/**
 * This model represents a Service on our network.
 *
 * A service is a machine which is run on a HyperVisor
 */
public class ServiceModel extends ServerModel {
	
	private Map<String, DiskModel> disks;
	private String hypervisor;
	
	public ServiceModel(String label, NetworkModel networkModel)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, ClassNotFoundException, URISyntaxException, AddressException,
			IOException, JsonParsingException, AThornSecException {
		super(label, networkModel);
		setHypervisor(getNetworkModel().getData().getService(getLabel()).getHypervisor());

		Map<String, DiskData> disks = getNetworkModel().getData().getDisks(getLabel());

		if (disks != null) {
			disks.values().forEach(diskData -> {
				DiskModel disk = new DiskModel(diskData);
				try {
					File diskPath = new File(getNetworkModel().getData().getHypervisorThornsecBase(hypervisor) + "/disks/"+disk.getLabel()+"/" + getLabel() + "/" + disk.getLabel() + "." + disk.getFormat().toString());
					disk.setFilename(diskPath);
					
					if (disk.getLabel().equals("boot")) {
						if (disk.getSize() == null) {
							disk.setSize(getNetworkModel().getData().getBootDiskSize(getLabel()));
						}
					}
					
					if (disk.getSize() == null) {
						disk.setSize(getNetworkModel().getData().getDataDiskSize(getLabel()));
					}
				} catch (InvalidServerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				addDisk(disk);
			});
		}
		
		if (getDisk("boot") == null) {
			File bootDiskPath = new File(getNetworkModel().getData().getHypervisorThornsecBase(hypervisor) + "/disks/boot/" + getLabel() + "/boot.vmdk");
			DiskModel bootDisk = new DiskModel("boot", Medium.DISK, Format.VMDK, bootDiskPath, getNetworkModel().getData().getBootDiskSize(getLabel()), null, "autogenerated boot disk");
			addDisk(bootDisk);
		}
		if (getDisk("data") == null) {
			File dataDiskPath = new File(getNetworkModel().getData().getHypervisorThornsecBase(hypervisor) + "/disks/data/" + getLabel() + "/data.vmdk");
			DiskModel dataDisk = new DiskModel("data", Medium.DISK, Format.VMDK, dataDiskPath, getNetworkModel().getData().getDataDiskSize(getLabel()), null, "autogenerated data disk");
			addDisk(dataDisk);
		}
		if (getDisk("debian") == null) {
			File dataDiskPath = new File(getNetworkModel().getData().getHypervisorThornsecBase(hypervisor) + "/isos/" + getLabel() + "/" + getLabel() + ".iso");
			DiskModel dataDisk = new DiskModel("debian", Medium.DVD, null, dataDiskPath, 666, null, "autogenerated iso disk");
			addDisk(dataDisk);
		}

	}

	@Override
	public Collection<IUnit> getUnits() throws AThornSecException {
		final Collection<IUnit> units = new ArrayList<>();
		
		units.addAll(super.getUnits());

		return units;
	}

	public void setHypervisor(String hypervisor) {
		this.hypervisor = hypervisor;
	}
	
	public void addDisk(DiskModel disk) {
		if (this.disks == null) {
			this.disks = new LinkedHashMap<>();
		}
		
		this.disks.put(disk.getLabel(), disk);
	}

	public Map<String, DiskModel> getDisks() {
		return this.disks;
	}
	
	public DiskModel getDisk(String label) {
		try {
			return this.getDisks().getOrDefault(label, null);
		}
		catch (NullPointerException e) {
			return null;
		}
	}
}
