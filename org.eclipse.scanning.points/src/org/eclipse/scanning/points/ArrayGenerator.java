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
package org.eclipse.scanning.points;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.ScanPointIterator;
import org.eclipse.scanning.api.points.models.ArrayModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

public class ArrayGenerator extends AbstractGenerator<ArrayModel> {

	public ArrayGenerator() {
		setLabel("Array Scan");
		setDescription("Creates a scan from an array of positions");
	}

	@Override
	protected void validateModel() throws ValidationException {
		if (getModel().getPositions()==null) throw new ModelValidationException("There are no positions!", model, "positions");
		if (getModel().getName()==null) throw new ModelValidationException("The model must have a name!\nIt is the motor name used for the array of points.", model, "name");
		super.validateModel();
	}

	@Override
	public int sizeOfValidModel() throws GeneratorException {
		if (containers!=null) throw new GeneratorException("Cannot deal with regions in an array scan!");
		if (model.getPositions() == null) {
			return 0;
		}
		return model.getPositions().length;
	}

	@Override
	protected ScanPointIterator iteratorFromValidModel() {
		final ArrayModel model = getModel();
        final JythonObjectFactory<ScanPointIterator> arrayGeneratorFactory = ScanPointGeneratorFactory.JArrayGeneratorFactory();

        final double[] points = model.getPositions();

		final ScanPointIterator pyIterator = arrayGeneratorFactory.createObject(
				model.getName(), "mm", points);
        return new SpgIterator(pyIterator);
	}

	@Override
	public int[] getShape() throws GeneratorException {
		return new int[] { size() };
	}

}
