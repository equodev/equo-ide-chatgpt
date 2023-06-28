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
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PromptPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	@Override
	protected Control createContents(Composite parent) {
		return new Ctl(parent).getRootControl();
	}

	@Override
	public void init(IWorkbench workbench) {}

	public static class Ctl extends ControlWrapper.AroundControl<SashForm> {
		public Ctl(Composite parent) {
			super(new SashForm(parent, SWT.HORIZONTAL));
			var tabs = new TabFolder(wrapped, SWT.BORDER);
			var prefacesTab = new TabItem(tabs, SWT.NONE);
			prefacesTab.setText("Prefaces");

			var prefaces = new List(tabs, SWT.NONE);
			prefaces.add("(None)");
			prefaces.add("Java expert");
			prefacesTab.setControl(prefaces);

			var templatesTab = new TabItem(tabs, SWT.NONE);
			templatesTab.setText("Templates");

			var templates = new List(tabs, SWT.NONE);
			templates.add("Freeform");
			templates.add("Java expert");
			templates.add("Java beginner");
			templatesTab.setControl(templates);

			var text = new Text(wrapped, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			text.setText("Lorem ipsum");
		}
	}
}
