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
import com.diffplug.common.swt.Fonts;
import com.diffplug.common.swt.Layouts;
import com.diffplug.common.swt.SwtExec;
import com.diffplug.common.swt.dnd.DndOp;
import com.diffplug.common.swt.dnd.StructuredDrop;
import io.reactivex.subjects.BehaviorSubject;
import java.io.File;
import java.util.Optional;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

class DragFileCtl extends ControlWrapper.AroundControl<Composite> {
	private final StructuredDrop drop = new StructuredDrop();
	private final Label path;
	private final ToolItem close;

	private final BehaviorSubject<Optional<File>> file =
			BehaviorSubject.createDefault(Optional.empty());

	public DragFileCtl(Composite parent) {
		super(new Composite(parent, SWT.BORDER));
		Layouts.setGrid(wrapped).numColumns(2);

		path = new Label(wrapped, SWT.NONE);
		Layouts.setGridData(path).grabHorizontal();

		var toolbar = new ToolBar(wrapped, SWT.FLAT);
		close = new ToolItem(toolbar, SWT.NONE);
		close.setText("clear");
		close.addListener(SWT.Selection, e -> file.onNext(Optional.empty()));

		drop.addFile(
				StructuredDrop.handler(
						DndOp.COPY,
						files -> {
							if (files.isEmpty()) {
								file.onNext(Optional.empty());
							} else {
								file.onNext(Optional.of(files.get(0)));
							}
						}));
		drop.applyTo(wrapped, path, toolbar);

		SwtExec.immediate()
				.guardOn(wrapped)
				.subscribe(
						file,
						f -> {
							if (f.isPresent()) {
								path.setText(f.get().getName());
								path.setFont(Fonts.systemMonospace());
							} else {
								path.setText("Drag a file here");
								path.setFont(Fonts.system());
							}
							close.getParent().setVisible(f.isPresent());
						});
	}

	public void applyDropTo(Control... otherControls) {
		drop.applyTo(otherControls);
	}
}
