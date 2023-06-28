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

import com.diffplug.common.base.Errors;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

public class PromptStore {
	static PromptStore get() {
		return new PromptStore(InstanceScope.INSTANCE.getNode("dev.equo.ide.chatgpt"));
	}

	private final Preferences prefaces, templates;

	PromptStore(IEclipsePreferences preferences) {
		this.prefaces = preferences.node("prefaces");
		this.templates = preferences.node("templates");
	}

	public List<String> listPrefaces() {
		return Arrays.asList(Errors.rethrow().get(prefaces::keys));
	}

	public List<String> listTemplates() {
		return Arrays.asList(Errors.rethrow().get(templates::keys));
	}

	public static String TEMPLATE_FREEFORM = "Freeform";

	public void removePreface(String key) {
		prefaces.remove(key);
	}

	public void removeTemplate(String key) {
		templates.remove(key);
	}

	public void putPreface(String key, String value) {
		prefaces.put(key, value);
	}

	public void putTemplate(String key, String value) {
		templates.put(key, value);
	}
}
