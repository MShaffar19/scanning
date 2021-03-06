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
package org.eclipse.scanning.api.event.alive;

import org.eclipse.scanning.api.event.bean.IBeanClassListener;

public interface IHeartbeatListener extends IBeanClassListener<HeartbeatBean> {

	default void heartbeatPerformed(HeartbeatEvent evt) {
		// default implementation does nothing, subclasses should override as necessary
	}

	@Override
	default Class<HeartbeatBean> getBeanClass() {
		return HeartbeatBean.class;
	}
}
