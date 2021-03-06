/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.scan;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IDeviceDependentIterable;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;

/**
 *
 * Most scans are static and therefore they can have their shape and size
 * discovered. Those scans which truely are iterators and on the fly decide
 * the next position, can only have their shapes and sizes estimated.
 *
 * This class is an estimator and not a data holder. Please use ScanInformation
 * to hold data to be sent around.
 *
 * @author Matthew Gerring
 *
 */
public class ScanEstimator {

	/**
	 * Size, number of points in scan
	 */
	private final int   size;

	/**
	 * The rank of the scan
	 */
	private final int rank;

	/**
	 * Estimated time of scan
	 */
	private final long  estimatedScanTime;

	/**
	 *
	 */
	private long  timePerPoint = -1;

	/**
	 *
	 */
	private final Iterable<IPosition> generator;

	/**
	 *
	 */
	private int[] shape;

	/**
	 *
	 * @param pservice
	 * @param bean
	 */
	public ScanEstimator(IPointGeneratorService pservice, ScanRequest<?> request) throws GeneratorException{
		this(pservice, request, 0);
	}

	/**
	 *
	 * @param pservice
	 * @param bean
	 * @param timePerPoint ms
	 * @throws GeneratorException
	 */
	public ScanEstimator(IPointGeneratorService pservice, ScanRequest<?> request, long timePerPoint) throws GeneratorException {
		this(pservice.createCompoundGenerator(request.getCompoundModel()), request.getDetectors(), timePerPoint);
	}

	public ScanEstimator(Iterable<IPosition> positionIterable, List<IRunnableDevice<?>> detectors) throws GeneratorException {
		this(positionIterable, detectorsToModels(detectors), 0);
	}

	private static Collection<Object> detectorsToModels(List<IRunnableDevice<?>> detectors) {
		return detectors == null ? null : detectors.stream().map(d -> d.getModel()).collect(Collectors.toList());
	}

	/**
	 *
	 * @param pservice
	 * @param request
	 * @param timePerPoint
	 * @throws GeneratorException
	 */
	public ScanEstimator(IPointGenerator<?> gen, Map<String, Object> detectors, long timePerPoint) throws GeneratorException {
		this(gen, detectors == null ? null : detectors.values(), timePerPoint);
	}

	/**
	 * Create a scan estimator for the given positions, (optional) detectors and (optional) time per point
	 * @param positionIterable iteratable over positions in the scan
	 * @param detectorModels detector models, may be <code>null</code>
	 * @param timePerPoint time per point, only used if <code> detectorModels</code> is <code>null</code>
	 * @throws GeneratorException if the scan estimator cannot be created
	 */
	public ScanEstimator(Iterable<IPosition> positionIterable, Collection<Object> detectorModels, long timePerPoint) throws GeneratorException {
		// TODO FIXME If some detectors are malcolm, they may have a wait time.
		// If some are malcolm we may wish to ignore the input point time from the user
		// in favour of the malcolm time per point or maybe the device tells us how long it will take?
		if (detectorModels != null && !detectorModels.isEmpty()) {
			timePerPoint = detectorModels.stream().filter(IDetectorModel.class::isInstance)
					.map(m -> Math.round(((IDetectorModel) m).getExposureTime() * 1000))
					.reduce(0l, Math::max);
		}

		this.generator = positionIterable;
		this.size = getEstimatedSize(positionIterable);
		this.rank = positionIterable.iterator().next().getScanRank();
		this.timePerPoint = timePerPoint;
		this.estimatedScanTime = size * timePerPoint;
	}

	private int getEstimatedSize(Iterable<IPosition> gen) throws GeneratorException {

		int size=0;
		if (gen instanceof IDeviceDependentIterable) {
			size = ((IDeviceDependentIterable)gen).size();

		} else if (gen instanceof IPointGenerator) {
			size = ((IPointGenerator<?>)gen).size();
		} else  {
			for (@SuppressWarnings("unused") IPosition unused : gen) size++; // Fast even for large stuff providing they do not check hardware on the next() call.
		}

		return size;
	}

	public int getSize() {
		return size;
	}

	public long getTimePerPoint() {
		return timePerPoint;
	}

	public void setTimePerPoint(long timePerPoint) {
		this.timePerPoint = timePerPoint;
	}

	public long getEstimatedScanTime() {
		return estimatedScanTime;
	}

	public int getRank() {
		return rank;
	}

	/**
	 * The estimated scan shape. Clue is in the name, ScanEstimator
	 * @return
	 * @throws GeneratorException
	 */
	public int[] getShape() throws GeneratorException {
		if (shape == null) {
			if (generator instanceof IPointGenerator<?>) {
				shape =((IPointGenerator<?>) generator).getShape();
			} else {
				// can only get shape from IPointGenerator
				// TODO: refactor ScanModel to have IPointGenerator instead of position iterable?
				throw new IllegalArgumentException("Cannot get scan shape");
			}
		}

		return shape;
	}

}
