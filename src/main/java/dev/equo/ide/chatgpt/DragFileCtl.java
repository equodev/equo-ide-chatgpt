/*******************************************************************************
 * Copyright (c) 2023 EquoTech, Inc. and others.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Contributors:
 *     EquoTech, Inc. - initial API and implementation
 *******************************************************************************/
package dev.equo.ide.chatgpt;

import com.diffplug.common.swt.ControlWrapper;
import com.diffplug.common.swt.Layouts;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

class DragFileCtl extends ControlWrapper.AroundControl<Composite> {
	public DragFileCtl(Composite parent) {
		super(new Composite(parent, SWT.BORDER));
		Layouts.setGrid(wrapped).numColumns(2);

		Label label = new Label(wrapped, SWT.NONE);
		label.setText("Drag file here");
	}
}
