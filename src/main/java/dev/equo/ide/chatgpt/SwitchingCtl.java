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
import com.diffplug.common.swt.SwtMisc;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class SwitchingCtl extends ControlWrapper.AroundWrapper<CoatMux> {

	private final CoatMux.Layer<GptWrapperCtl> gpt;
	private final CoatMux.Layer<PromptCtl> prompt;

	public SwitchingCtl(Composite parent) {
		super(new CoatMux(parent, SWT.NONE));
		gpt = wrapped.addWrapper(GptWrapperCtl::new);
		prompt = wrapped.addWrapper(PromptCtl::new);

		gpt.getHandle()
				.switchTemplates
				.addListener(
						SWT.Selection,
						e -> {
							prompt.bringToTop();
						});
		prompt
				.getHandle()
				.switchToBrowser
				.addListener(
						SWT.Selection,
						e -> {
							gpt.bringToTop();
						});
		prompt.bringToTop();
	}

	static class GptWrapperCtl extends ControlWrapper.AroundControl<Composite> {
		final ChatGptCtl ctl;
		final Button switchTemplates;

		public GptWrapperCtl(Composite parent) {
			super(new Composite(parent, SWT.NONE));
			Layouts.setGrid(wrapped).numColumns(2);

			ctl = new ChatGptCtl(wrapped);
			Layouts.setGridData(ctl).horizontalSpan(2).grabAll();

			Layouts.newGridPlaceholder(wrapped).grabHorizontal();

			switchTemplates = new Button(wrapped, SWT.PUSH | SWT.FLAT);
			switchTemplates.setText("New question");
			Layouts.setGridData(switchTemplates).widthHint(SwtMisc.defaultButtonWidth());
		}
	}
}
