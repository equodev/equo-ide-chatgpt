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

import com.diffplug.common.swt.CoatMux;
import com.diffplug.common.swt.ControlWrapper;
import com.diffplug.common.swt.Layouts;
import com.diffplug.common.swt.SwtExec;
import com.diffplug.common.swt.SwtMisc;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class SwitchingCtl extends ControlWrapper.AroundWrapper<CoatMux> {

	private final CoatMux.Layer<GptWrapperCtl> gpt;
	final CoatMux.Layer<PromptCtl> prompt;

	public SwitchingCtl(Composite parent) {
		super(new CoatMux(parent, SWT.NONE));
		gpt = wrapped.addWrapper(GptWrapperCtl::new);
		prompt = wrapped.addWrapper(PromptCtl::new);

		gpt.getHandle().parent = this;
		prompt
				.getHandle()
				.switchToBrowser
				.addListener(
						SWT.Selection,
						e -> {
							gpt.bringToTop();
						});
		gpt.bringToTop();
	}

	static class GptWrapperCtl extends ControlWrapper.AroundControl<Composite> {
		final ChatGptCtl ctl;
		final Button switchTemplates;
		SwitchingCtl parent;

		public GptWrapperCtl(Composite parentCmp) {
			super(new Composite(parentCmp, SWT.NONE));
			Layouts.setGrid(wrapped).margin(0);

			ctl = new ChatGptCtl(wrapped);
			Layouts.setGridData(ctl).horizontalSpan(2).grabAll();

			var bottomPanel = new Composite(wrapped, SWT.NONE);
			Layouts.setGridData(bottomPanel).grabHorizontal();
			Layouts.setGrid(bottomPanel).numColumns(2);
			Layouts.newGridPlaceholder(bottomPanel).grabHorizontal();

			switchTemplates = new Button(bottomPanel, SWT.PUSH | SWT.FLAT);
			Layouts.setGridData(switchTemplates).widthHint(SwtMisc.defaultButtonWidth());
			switchTemplates.setText(NEW_QUESTION);

			SwtExec.async()
					.guardOn(ctl)
					.subscribe(
							ctl.isReady(),
							isReady -> {
								if (isReady) {
									switchTemplates.setText(NEW_QUESTION);
									parent.prompt.bringToTop();
								} else {
									switchTemplates.setText(RESET_LOGIN);
								}
							});
			switchTemplates.addListener(
					SWT.Selection,
					e -> {
						if (switchTemplates.getText().equals(RESET_LOGIN)) {
							ctl.tryLogin();
						} else {
							parent.prompt.bringToTop();
						}
					});
		}

		private static final String NEW_QUESTION = "New question";
		private static final String RESET_LOGIN = "Reset login";
	}
}
