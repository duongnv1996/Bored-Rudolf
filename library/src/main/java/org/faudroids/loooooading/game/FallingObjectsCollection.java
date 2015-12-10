package org.faudroids.loooooading.game;

import org.faudroids.loooooading.utils.RandomUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


abstract class FallingObjectsCollection implements Iterable<FallingObject> {

	private final int maxObjectsCount;
	private final List<FallingObject> objects = new ArrayList<>();
	private final int minObjectCreationTime, maxObjectCreationTime;

	private long nextObjectCreationTime = 0; // ms until the next object should be created

	public FallingObjectsCollection(int maxObjectsCount, int minObjectCreationTime, int maxObjectCreationTime) {
		this.maxObjectsCount = maxObjectsCount;
		this.minObjectCreationTime = minObjectCreationTime;
		this.maxObjectCreationTime = maxObjectCreationTime;
	}

	public void onTimePassed(long passedTimeInMs) {
		if (nextObjectCreationTime <= 0 && objects.size() < maxObjectsCount) {
			objects.add(createObject());
			nextObjectCreationTime = RandomUtils.randomInt(minObjectCreationTime, maxObjectCreationTime);
		} else {
			nextObjectCreationTime -= passedTimeInMs;
		}
	}

	protected abstract FallingObject createObject();

	public List<FallingObject> getObjects() {
		return objects;
	}

	public Iterator<FallingObject> iterator() {
		return objects.iterator();
	}

}
