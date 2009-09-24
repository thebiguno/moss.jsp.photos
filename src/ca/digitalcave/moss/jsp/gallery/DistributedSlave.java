package ca.digitalcave.moss.jsp.gallery;

import com.hazelcast.core.Hazelcast;

public class DistributedSlave {
	
	/**
	 * Starts the hazelcast remote execution listener
	 * @param args
	 */
	public static void main(String[] args) {
		Hazelcast.getCluster();
	}
}
