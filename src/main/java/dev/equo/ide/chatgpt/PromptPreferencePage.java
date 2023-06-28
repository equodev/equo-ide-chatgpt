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
import com.diffplug.common.swt.SiliconFix;
import com.diffplug.common.swt.SwtExec;
import io.reactivex.subjects.BehaviorSubject;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PromptPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	@Override
	protected Control createContents(Composite parent) {
		return new Ctl(parent, PromptStore.get()).getRootControl();
	}

	@Override
	public void init(IWorkbench workbench) {}

	public static class Ctl extends ControlWrapper.AroundControl<SashForm> {
		private ToolItem prefacesItem, templatesItem;
		private BehaviorSubject<PromptStore.Type> activeType =
				BehaviorSubject.createDefault(PromptStore.Type.PREFACE);
		private Map<PromptStore.Type, Integer> selection = new HashMap<>();

		public Ctl(Composite parent, PromptStore store) {
			super(new SashForm(parent, SWT.HORIZONTAL));
			var left = new Composite(wrapped, SWT.NONE);
			Layouts.setGrid(left).margin(0).spacing(0);

			var toolbarList = new ToolBar(left, SWT.FLAT);
			Layouts.setGridData(toolbarList).grabHorizontal();

			prefacesItem = new ToolItem(toolbarList, SWT.RADIO);
			prefacesItem.setText("Prefaces");
			prefacesItem.setSelection(true);
			templatesItem = new ToolItem(toolbarList, SWT.RADIO);
			templatesItem.setText("Templates");

			var templates = new List(left, SWT.NONE);
			SiliconFix.fix(templates);
			Layouts.setGridData(templates).grabAll();

			var right = new Composite(wrapped, SWT.NONE);
			Layouts.setGrid(right).numColumns(2).margin(0).spacing(0);

			Layouts.newGridPlaceholder(right).grabHorizontal();
			var toolbarItems = new ToolBar(right, SWT.FLAT);
			var add = new ToolItem(toolbarItems, SWT.PUSH);
			add.setText("Copy");
			var minus = new ToolItem(toolbarItems, SWT.PUSH);
			minus.setText("Delete");
			var rename = new ToolItem(toolbarItems, SWT.PUSH);
			rename.setText("Rename");

			var text = new Text(right, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			Layouts.setGridData(text).horizontalSpan(2).grabAll();
			text.setText("Lorem ipsum");
			text.forceFocus();

			wrapped.setWeights(new int[] {1, 2});

			var guarded = SwtExec.immediate().guardOn(this);
			for (var type : PromptStore.Type.values()) {
				selection.put(type, 0);
				var item = type.prefaceTemplate(prefacesItem, templatesItem);
				final PromptStore.Type finalType = type;
				item.addListener(
						SWT.Selection,
						e -> {
							activeType.onNext(finalType);
						});
			}
			guarded.subscribe(
					activeType,
					type -> {
						type.prefaceTemplate(prefacesItem, templatesItem).setSelection(true);
						type.prefaceTemplate(templatesItem, prefacesItem).setSelection(false);
						templates.removeAll();
						var keys = store.get(type).list();
						keys.forEach(templates::add);
						var selectionIdx = selection.get(type);
						templates.setSelection(selectionIdx);
						var value = store.get(type).get(keys.get(selectionIdx));
						text.setText(value);
					});
			templates.addListener(
					SWT.Selection,
					e -> {
						int idx = templates.getSelectionIndex();
						selection.put(activeType.getValue(), idx);
						var key = templates.getItems()[idx];
						var value = store.get(activeType.getValue()).get(key);
						text.setText(value);
					});
		}
	}
}
