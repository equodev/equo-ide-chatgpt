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
import com.diffplug.common.swt.Shells;
import com.diffplug.common.swt.SiliconFix;
import com.diffplug.common.swt.SwtMisc;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PromptPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private static Ctl dialog;

	public static void openDialog(PromptStore.Type type) {
		if (dialog != null) {
			dialog.getShell().open();
			dialog.getShell().forceActive();
		} else {
			Shell dialogShell =
					Shells.builder(
									SWT.DIALOG_TRIM | SWT.RESIZE,
									cmp -> {
										Layouts.setFill(cmp);
										dialog = new Ctl(cmp, PromptStore.get());
									})
							.setTitle("ChatGPT Prompts")
							.setSize(SwtMisc.defaultDialogWidth(), SwtMisc.defaultDialogWidth())
							.openOnActive();
			dialogShell.addListener(
					SWT.Dispose,
					e -> {
						dialog = null;
					});
		}
		dialog.matchSelectionToView(type);
	}

	@Override
	protected Control createContents(Composite parent) {
		return new Ctl(parent, PromptStore.get()).getRootControl();
	}

	@Override
	public void init(IWorkbench workbench) {}

	public static class Ctl extends ControlWrapper.AroundControl<SashForm> {
		private PromptStore store;
		private ToolItem prefacesItem, templatesItem;
		private ToolBar toolbar;
		private List templates;
		private Text text;
		private PromptStore.Type activeType;
		private String activeKey;
		private Boolean activeIsDefault;

		private Map<PromptStore.Type, String> selection = new HashMap<>();

		public Ctl(Composite parent, PromptStore store) {
			super(new SashForm(parent, SWT.HORIZONTAL));
			this.store = store;
			var left = new Composite(wrapped, SWT.NONE);
			Layouts.setGrid(left).margin(0).spacing(0);

			ToolBar kindToolbar = new ToolBar(left, SWT.FLAT);
			Layouts.setGridData(kindToolbar).grabHorizontal();

			prefacesItem = new ToolItem(kindToolbar, SWT.RADIO);
			prefacesItem.setText("Prefaces");
			templatesItem = new ToolItem(kindToolbar, SWT.RADIO);
			templatesItem.setText("Templates");

			templates = new List(left, SWT.NONE);
			templates.addListener(
					SWT.KeyDown,
					e -> {
						if (e.keyCode == SWT.DEL) {
							delete();
						}
					});
			SiliconFix.fix(templates);
			Layouts.setGridData(templates).grabAll();

			var right = new Composite(wrapped, SWT.NONE);
			Layouts.setGrid(right).numColumns(2).margin(0).spacing(0);

			Layouts.newGridPlaceholder(right).grabHorizontal();
			toolbar = new ToolBar(right, SWT.FLAT);

			text = new Text(right, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			Layouts.setGridData(text).horizontalSpan(2).grabAll();
			text.forceFocus();

			wrapped.setWeights(new int[] {1, 2});

			for (var type : PromptStore.Type.values()) {
				var sub = store.get(type);
				selection.put(type, sub.list().get(store.getSelection(type)));
				var item = type.prefaceTemplate(prefacesItem, templatesItem);
				final PromptStore.Type finalType = type;
				item.addListener(
						SWT.Selection,
						e -> {
							setActive(finalType, selection.get(finalType));
						});
			}
			templates.addListener(
					SWT.Selection,
					e -> {
						String[] selected = templates.getSelection();
						if (selected.length == 1) {
							setActive(activeType, selected[0]);
						}
					});
			setActive(PromptStore.Type.PREFACE, selection.get(PromptStore.Type.PREFACE));
			wrapped.addListener(
					SWT.Dispose,
					e -> {
						PromptStore.get().save();
					});
		}

		private void storeCurrent() {
			var sub = store.get(activeType);
			if (sub.get(activeKey) != null) {
				sub.put(activeKey, text.getText());
				selection.put(activeType, sub.list().get(templates.getSelectionIndex()));
			}
		}

		private void setActive(PromptStore.Type type, String key) {
			// store the last value
			if (activeType != null) {
				storeCurrent();
			}
			activeType = type;
			type.prefaceTemplate(prefacesItem, templatesItem).setSelection(true);
			type.prefaceTemplate(templatesItem, prefacesItem).setSelection(false);
			templates.removeAll();
			var keys = store.get(type).list();
			keys.forEach(templates::add);

			activeKey = key;
			int selectionIdx = keys.indexOf(key);
			templates.setSelection(selectionIdx);
			text.setText(store.get(type).get(activeKey));

			boolean isDefault = store.isDefault(type, key);
			if (activeIsDefault == null || activeIsDefault.booleanValue() != isDefault) {
				activeIsDefault = isDefault;
				for (var child : toolbar.getItems()) {
					child.dispose();
				}
				if (isDefault) {
					var cantModify = new ToolItem(toolbar, SWT.PUSH);
					cantModify.setText("Can't modify built-in");
					cantModify.setEnabled(false);
				} else {
					var delete = new ToolItem(toolbar, SWT.PUSH);
					delete.setText("Delete");
					delete.addListener(
							SWT.Selection,
							e -> {
								delete();
							});
					var rename = new ToolItem(toolbar, SWT.PUSH);
					rename.setText("Rename");
					rename.addListener(
							SWT.Selection,
							e -> {
								rename();
							});
				}
				var copy = new ToolItem(toolbar, SWT.PUSH);
				copy.setText("Copy");
				copy.addListener(
						SWT.Selection,
						e -> {
							String newKey = store.get(activeType).newKey(activeKey, text.getText());
							setActive(activeType, newKey);
						});
				toolbar.getParent().layout();
			}
		}

		private void delete() {
			if (DialogMisc.blockForQuestion(
					"Confirm delete", "Are you sure you want to delete " + activeKey + "?", getShell())) {
				store.get(activeType).remove(activeKey);
				String[] items = templates.getItems();
				int selectionIdx = templates.getSelectionIndex();
				String toSelect;
				if (selectionIdx == items.length - 1) {
					toSelect = items[items.length - 2];
				} else {
					toSelect = items[selectionIdx + 1];
				}
				setActive(activeType, toSelect);
			}
		}

		private void rename() {
			String newName = DialogMisc.blockForRename(activeKey, getShell());
			if (newName == null) {
				return;
			}
			var sub = store.get(activeType);
			var content = sub.get(activeKey);
			var existing = sub.get(newName);
			if (existing != null && !existing.equals(content)) {
				DialogMisc.blockForError(
						"Duplicate name " + newName,
						"There is already a "
								+ activeType.name().toLowerCase(Locale.ROOT)
								+ " named "
								+ newName,
						getShell());
			}
			sub.remove(activeKey);
			sub.put(newName, content);
			setActive(activeType, newName);
		}

		void matchSelectionToView(PromptStore.Type type) {
			var selectionIdx = store.getSelection(type);
			setActive(type, store.get(type).list().get(selectionIdx));
		}
	}
}
