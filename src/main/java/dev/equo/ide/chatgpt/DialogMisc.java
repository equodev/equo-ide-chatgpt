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

import com.diffplug.common.base.Box;
import com.diffplug.common.swt.Layouts;
import com.diffplug.common.swt.Shells;
import com.diffplug.common.swt.SwtMisc;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class DialogMisc {
	/** Opens a dialog and waits for an answer to the given question. */
	public static boolean blockForQuestion(String title, String message, Shell parent) {
		Box<Boolean> clickedYes = Box.of(false);
		Shells.builder(
						SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL,
						cmp -> {
							Layouts.setGrid(cmp).numColumns(2);

							var icon = new Label(cmp, SWT.NONE);
							icon.setImage(SwtMisc.getSystemIcon(SWT.ICON_QUESTION));

							var label = new Label(cmp, SWT.WRAP);
							label.setText(message);
							Layouts.setGridData(label).grabHorizontal();

							Layouts.newGridRow(
									cmp,
									buttons -> {
										Layouts.newGridPlaceholder(buttons).grabHorizontal();
										var ok = new Button(buttons, SWT.PUSH);
										ok.setText("No");
										ok.addListener(
												SWT.Selection,
												e -> {
													cmp.getShell().dispose();
												});
										var cancel = new Button(buttons, SWT.PUSH);
										cancel.setText("Yes");
										cancel.addListener(
												SWT.Selection,
												e -> {
													clickedYes.set(true);
													cmp.getShell().dispose();
												});
										cancel.setFocus();
										cancel.forceFocus();

										Layouts.setGridData(ok).widthHint(SwtMisc.defaultButtonWidth());
										Layouts.setGridData(cancel).widthHint(SwtMisc.defaultButtonWidth());
									});
						})
				.setTitle(title)
				.setSize(SwtMisc.defaultDialogWidth(), SWT.DEFAULT)
				.openOnBlocking(parent);
		return clickedYes.get();
	}

	public static void blockForError(String title, String message, Shell parent) {
		Shells.builder(
						SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL,
						cmp -> {
							Layouts.setGrid(cmp).numColumns(2);

							var icon = new Label(cmp, SWT.NONE);
							icon.setImage(SwtMisc.getSystemIcon(SWT.ICON_ERROR));

							var label = new Label(cmp, SWT.WRAP);
							label.setText(message);
							Layouts.setGridData(label).grabHorizontal();

							Layouts.newGridRow(
									cmp,
									buttons -> {
										Layouts.newGridPlaceholder(buttons).grabHorizontal();
										var ok = new Button(buttons, SWT.PUSH);
										ok.setText("OK");
										ok.addListener(
												SWT.Selection,
												e -> {
													cmp.getShell().dispose();
												});
										Layouts.setGridData(ok).widthHint(SwtMisc.defaultButtonWidth());
									});
						})
				.setTitle(title)
				.setSize(SwtMisc.defaultDialogWidth(), SWT.DEFAULT)
				.openOnBlocking(parent);
	}

	public static String blockForRename(String activeKey, Shell parent) {
		Box.Nullable<String> newName = Box.Nullable.ofNull();
		Shells.builder(
						SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL,
						cmp -> {
							Layouts.setGrid(cmp);

							var label = new Label(cmp, SWT.WRAP);
							label.setText("Rename " + activeKey + " to:");
							Layouts.setGridData(label).grabHorizontal();

							var text = new Text(cmp, SWT.BORDER | SWT.SINGLE);
							text.setText(activeKey);
							Layouts.setGridData(text).grabHorizontal();

							Layouts.newGridRow(
									cmp,
									buttons -> {
										Layouts.newGridPlaceholder(buttons).grabHorizontal();
										var cancel = new Button(buttons, SWT.PUSH);
										cancel.setText("Cancel");
										cancel.addListener(
												SWT.Selection,
												e -> {
													cmp.getShell().dispose();
												});
										var rename = new Button(buttons, SWT.PUSH);
										rename.setText("Rename");
										rename.addListener(
												SWT.Selection,
												e -> {
													newName.set(text.getText());
													cmp.getShell().dispose();
												});
										rename.setFocus();
										rename.forceFocus();

										Layouts.setGridData(cancel).widthHint(SwtMisc.defaultButtonWidth());
										Layouts.setGridData(rename).widthHint(SwtMisc.defaultButtonWidth());
									});
						})
				.setTitle("Rename " + activeKey)
				.setSize(SwtMisc.defaultDialogWidth(), SWT.DEFAULT)
				.openOnBlocking(parent);
		return newName.get();
	}
}
