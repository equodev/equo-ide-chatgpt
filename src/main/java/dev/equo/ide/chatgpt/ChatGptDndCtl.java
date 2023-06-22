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
import com.diffplug.common.swt.dnd.DndOp;
import com.diffplug.common.swt.dnd.StructuredDrop;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class ChatGptDndCtl extends ControlWrapper.AroundControl<Composite> {
	private final ChatGptCtl ctl;

	public ChatGptDndCtl(Composite parent) {
		super(new Composite(parent, SWT.NONE));
		Layouts.setGrid(wrapped).margin(0);
		var group = new Composite(wrapped, SWT.BORDER);
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
		drop.addFile(
				StructuredDrop.handler(
						DndOp.COPY,
						files -> {
							for (var file : files) {
								try {
									String content =
											new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)
													.replace("\r", "")
													.trim();
									ctl.setPrompt(
											ensureEndsWithBlankline(ctl.getPrompt())
													+ "```"
													+ gfmType(file.getName())
													+ "\n"
													+ content
													+ "\n```\n");
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							}
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

	private static String gfmType(String filename) {
		if (filename.endsWith(".java")) {
			return "java";
		} else {
			var lastDot = filename.lastIndexOf('.');
			var lastSlash = filename.replace('\\', '/').lastIndexOf('/');
			if (lastDot > 0 && lastSlash >= 0) {
				return filename.substring(lastSlash + 1, lastDot);
			} else {
				return "";
			}
		}
	}
}
