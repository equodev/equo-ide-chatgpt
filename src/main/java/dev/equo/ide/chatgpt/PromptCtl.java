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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

public class PromptCtl extends ControlWrapper.AroundControl<Composite> {
	private final Combo prefaceCombo, templateCombo;
	private final Text templateTxt;
	private final DragFileCtl dragFileCtl;
	Link switchToBrowser;

	public PromptCtl(Composite parent) {
		super(new Composite(parent, SWT.NONE));
		Layouts.setGrid(wrapped).spacing(0);

		var prefaceLbl = new Label(wrapped, SWT.NONE);
		prefaceLbl.setText("Preface");
		prefaceCombo = new Combo(wrapped, SWT.READ_ONLY | SWT.FLAT);
		prefaceCombo.add("Java expert");
		prefaceCombo.select(0);
		Layouts.setGridData(prefaceCombo).grabHorizontal();

		var promptLbl = new Label(wrapped, SWT.NONE);
		promptLbl.setText("Template");
		Layouts.setGridData(promptLbl).verticalIndent(Layouts.defaultMargin());
		templateCombo = new Combo(wrapped, SWT.READ_ONLY | SWT.FLAT);
		templateCombo.add("Freeform");
		templateCombo.select(0);
		Layouts.setGridData(templateCombo).grabHorizontal();

		templateTxt = new Text(wrapped, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		Layouts.setGridData(templateTxt).heightHint(SwtMisc.scaleByFontHeight(5)).grabHorizontal();

		dragFileCtl = new DragFileCtl(wrapped);
		Layouts.setGridData(dragFileCtl).grabHorizontal();

		var askBar = new Composite(wrapped, SWT.NONE);
		Layouts.setGridData(askBar).grabHorizontal();
		Layouts.setGrid(askBar).margin(0).numColumns(2);

		Layouts.newGridPlaceholder(askBar).grabHorizontal();

		var askBtn = new Button(askBar, SWT.PUSH);
		askBtn.setText("Ask");
		Layouts.setGridData(askBtn)
				.horizontalAlignment(SWT.RIGHT)
				.widthHint(SwtMisc.defaultButtonWidth());

		switchToBrowser = new Link(wrapped, SWT.NONE);
		switchToBrowser.setText("<a>Switch to browser</a>");
		Layouts.setGridData(switchToBrowser).grabAll().verticalAlignment(SWT.BOTTOM);
	}
}
