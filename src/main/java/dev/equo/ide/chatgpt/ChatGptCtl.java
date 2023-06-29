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
import com.equo.chromium.swt.Browser;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.widgets.Composite;

class ChatGptCtl extends ControlWrapper.AroundControl<Browser> {
	private BehaviorSubject<Boolean> isReady = BehaviorSubject.createDefault(false);

	public ChatGptCtl(Composite parent) {
		super(new Browser(parent, SWT.NONE));
		tryLogin();
		wrapped.addLocationListener(
				new LocationListener() {
					@Override
					public void changing(LocationEvent event) {}

					@Override
					public void changed(LocationEvent event) {
						isReady.onNext(
								event.location.startsWith(CHAT_URL) && !event.location.startsWith(LOGIN_URL));
					}
				});
	}

	public void tryLogin() {
		wrapped.setUrl(CHAT_URL);
	}

	public Observable<Boolean> isReady() {
		return isReady.distinctUntilChanged();
	}

	private static final String CHAT_URL = "https://chat.openai.com/";
	private static final String LOGIN_URL = "https://chat.openai.com/auth";

	public String getPrompt() {
		return (String)
				wrapped.evaluate(
						"var promptBox = document.getElementById('prompt-textarea');\n"
								+ "return promptBox == null ? '' : promptBox.value;");
	}

	public void sendPrompt(String prompt) {
		wrapped.evaluate(
				"var promptBox = document.getElementById('prompt-textarea')\n"
						+ "if (promptBox == null) return;\n"
						+ "promptBox.value = '"
						+ escapeForJs(prompt)
						+ "';\n"
						+ "// send event so that button is enabled\n"
						+ "var event = new Event('input', {\n"
						+ "  bubbles: true,\n"
						+ "  cancelable: true,\n"
						+ "});\n"
						+ "promptBox.dispatchEvent(event);\n"
						+ "// now we can click the button and submit\n"
						+ "var button = promptBox.nextElementSibling;\n"
						+ "button.click();");
		wrapped.evaluate(
				"var promptBox = document.getElementById('prompt-textarea');\n"
						+ "var button = promptBox.nextElementSibling;\n"
						+ "button.click();");
	}

	private static String escapeForJs(String in) {
		return in.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r");
	}
}
