package com.gentics.mesh.changelog.changes;

import static org.apache.tinkerpop.gremlin.structure.Direction.OUT;

import java.util.Iterator;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import com.gentics.mesh.changelog.AbstractChange;

public class PurgeOldJobs extends AbstractChange {

	@Override
	public String getName() {
		return "Purge old jobs";
	}

	@Override
	public String getDescription() {
		return "Purges any listed job in order to allow custom job types.";
	}

	@Override
	public void apply() {

		// 1. Remove jobs
		Vertex meshRoot = getMeshRootVertex();
		Iterator<Vertex> it = meshRoot.vertices(OUT, "HAS_JOB_ROOT");
		if (it.hasNext()) {
			Vertex jobRoot = meshRoot.vertices(OUT, "HAS_JOB_ROOT").next();
			Iterable<Vertex> jobIt = (Iterable<Vertex>) () -> jobRoot.vertices(OUT, "HAS_JOB");
			for (Vertex v : jobIt) {
				v.remove();
			}
		}
		// 2. Remove JobImpl type since we have now specific job vertices
		getDb().removeVertexType("JobImpl");

	}

	@Override
	public String getUuid() {
		return "4AE75B065A4A452CA75B065A4A052CDA";
	}

}
