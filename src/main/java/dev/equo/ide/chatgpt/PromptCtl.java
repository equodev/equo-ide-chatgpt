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
import com.diffplug.common.swt.SwtMisc;
import java.util.Arrays;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

class PromptCtl extends ControlWrapper.AroundControl<Composite> {
	private final Combo prefaceCombo, templateCombo;
	private final Link saveAs;
	private final Text templateTxt;
	final DragFileCtl dragFileCtl;
	final Link switchToBrowser;
	final Button askBtn;
	private PromptStore store = PromptStore.get();

	public PromptCtl(Composite parent) {
		super(new Composite(parent, SWT.NONE));
		Layouts.setGrid(wrapped).spacing(0);

		var prefaceLbl = new Label(wrapped, SWT.NONE);
		prefaceLbl.setText("Preface");

		prefaceCombo = new Combo(wrapped, SWT.READ_ONLY | SWT.FLAT);
		Layouts.setGridData(prefaceCombo).grabHorizontal();

		var templateRow = new Composite(wrapped, SWT.NONE);
		Layouts.setGridData(templateRow).grabHorizontal().verticalIndent(Layouts.defaultMargin());
		Layouts.setGrid(templateRow).margin(0).numColumns(2);

		var templateLbl = new Label(templateRow, SWT.NONE);
		templateLbl.setText("Template");
		Layouts.setGridData(templateLbl).grabHorizontal();

		saveAs = new Link(templateRow, SWT.NONE);
		saveAs.setText("<a>Save as</a>");
		saveAs.addListener(
				SWT.Selection,
				e -> {
					saveAs();
				});

		templateCombo = new Combo(wrapped, SWT.READ_ONLY | SWT.FLAT);
		Layouts.setGridData(templateCombo).grabHorizontal();

		var templateContainer = new Composite(wrapped, SWT.BORDER);
		templateTxt = new Text(templateContainer, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		Layouts.setSingleNoMargin(templateContainer);
		Layouts.setGridData(templateContainer).grabAll();
		templateTxt.setFocus();

		dragFileCtl = new DragFileCtl(wrapped);
		Layouts.setGridData(dragFileCtl).grabHorizontal().verticalIndent(Layouts.defaultMargin());

		var askBar = new Composite(wrapped, SWT.NONE);
		Layouts.setGridData(askBar).grabHorizontal();
		Layouts.setGrid(askBar).margin(0).numColumns(2);

		switchToBrowser = new Link(askBar, SWT.NONE);
		switchToBrowser.setText("<a>Open browser</a>");
		Layouts.setGridData(switchToBrowser).grabHorizontal().verticalAlignment(SWT.BOTTOM);

		askBtn = new Button(askBar, SWT.PUSH | SWT.FLAT);
		askBtn.setText("Get answer");
		Layouts.setGridData(askBtn)
				.horizontalAlignment(SWT.RIGHT)
				.verticalIndent(Layouts.defaultMargin())
				.widthHint(SwtMisc.defaultButtonWidth());

		dragFileCtl.applyDropTo(
				prefaceLbl,
				prefaceCombo,
				templateLbl,
				templateCombo,
				templateTxt,
				askBar,
				switchToBrowser,
				askBtn);

		// bind the data model
		for (var type : PromptStore.Type.values()) {
			final PromptStore.Type finalType = type;
			final PromptStore.Sub sub = store.get(finalType);
			var combo = type.prefaceTemplate(prefaceCombo, templateCombo);
			combo.addListener(
					SWT.Selection,
					e -> {
						var idx = combo.getSelectionIndex();
						saveAs.setVisible(idx == freeformIdx);
						if (idx == sub.list().size()) {
							PromptPreferencePage.openDialog(finalType);
							combo.select(store.getSelection(finalType));
							return;
						}
						store.setSelection(finalType, idx);
						if (finalType == PromptStore.Type.TEMPLATE) {
							templateTxt.setText(sub.get(sub.list().get(idx)));
						}
					});
			refreshData();
		}
		templateTxt.addListener(
				SWT.Modify,
				e -> {
					var idx = templateCombo.getSelectionIndex();
					if (idx != freeformIdx) {
						String key = templateCombo.getItem(idx);
						String storeContent = store.get(PromptStore.Type.TEMPLATE).get(key);
						String uiContent = templateTxt.getText();
						if (storeContent.equals(uiContent)) {
							return;
						}
						store.setSelection(PromptStore.Type.TEMPLATE, freeformIdx);
						templateCombo.select(freeformIdx);
						store.get(PromptStore.Type.TEMPLATE).put(PromptStore.FREEFORM, uiContent);
						saveAs.setVisible(true);
					} else {
						store.get(PromptStore.Type.TEMPLATE).put(PromptStore.FREEFORM, templateTxt.getText());
					}
				});
		wrapped.addListener(SWT.Dispose, e -> store.save());
	}

	private int freeformIdx;

	private void saveAs() {
		String newName = DialogMisc.blockForSaveAs(getShell());
		if (newName != null) {
			String newKey = store.get(PromptStore.Type.TEMPLATE).newKey(newName, templateTxt.getText());
			refreshData();
			int newKeyIdx = Arrays.asList(templateCombo.getItems()).indexOf(newKey);
			templateCombo.select(newKeyIdx);
			saveAs.setVisible(false);
		}
	}

	private void refreshData() {
		for (var type : PromptStore.Type.values()) {
			var combo = type.prefaceTemplate(prefaceCombo, templateCombo);
			combo.removeAll();
			var sub = store.get(type);
			sub.list().forEach(combo::add);
			combo.add(PromptStore.DIALOG);

			int selectionIdx = store.getSelection(type);
			combo.select(selectionIdx);
			if (type == PromptStore.Type.TEMPLATE) {
				templateTxt.setText(sub.get(sub.list().get(selectionIdx)));
			}
		}
		freeformIdx = Arrays.asList(templateCombo.getItems()).indexOf(PromptStore.FREEFORM);
		saveAs.setVisible(templateCombo.getSelectionIndex() == freeformIdx);
	}

	String prompt() {
		return store.getCurrentPrompt(dragFileCtl.file.getValue());
	}
}
