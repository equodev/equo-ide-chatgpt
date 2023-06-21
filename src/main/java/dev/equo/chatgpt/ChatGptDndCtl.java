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
package dev.equo.chatgpt;

import com.diffplug.common.swt.ControlWrapper;
import com.diffplug.common.swt.Layouts;
import com.diffplug.common.swt.dnd.DndOp;
import com.diffplug.common.swt.dnd.StructuredDrop;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class ChatGptDndCtl extends ControlWrapper.AroundControl<Composite> {
	private final ChatGptCtl ctl;

	public ChatGptDndCtl(Composite parent) {
		super(new Composite(parent, SWT.NONE));
		Layouts.setGrid(wrapped).margin(0);
		var group = new Group(wrapped, SWT.BORDER | SWT.SHADOW_ETCHED_IN);
		Layouts.setGridData(group).grabHorizontal();
		Layouts.setFill(group);
		var label = new Label(group, SWT.NONE);
		label.setText("Drag and drop a file here to copy it into the chat window.");

		ctl = new ChatGptCtl(wrapped);
		Layouts.setGridData(ctl).grabAll();

		var drop = new StructuredDrop();
		drop.addText(
				StructuredDrop.handler(
						DndOp.COPY,
						text -> {
							ctl.setPrompt(ensureEndsWithBlankline(ctl.getPrompt()) + text.trim());
						}));
		drop.applyTo(label, group);
	}

	private static String ensureEndsWithBlankline(String str) {
		if (str.endsWith("\n\n")) {
			return str;
		} else if (str.endsWith("\n")) {
			return str + "\n";
		} else {
			return str + "\n\n";
		}
	}
}
