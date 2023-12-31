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
import com.diffplug.common.base.Unhandled;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

abstract class PromptStore {
	public static final String NONE = "(None)";
	public static final String FREEFORM = "(Freeform)";
	public static final String DIALOG = "(Dialog...)";

	public enum Type {
		PREFACE,
		TEMPLATE;

		public <T> T prefaceTemplate(T preface, T template) {
			return this == PREFACE ? preface : template;
		}
	}

	abstract Sub get(Type type);

	abstract void save();

	public boolean isDefault(Type type, String key) {
		return type.prefaceTemplate(defaultPrefaces, defaultTemplates).containsKey(key);
	}

	private int selectionPreface = 0;
	private int selectionTemplate = 0;

	public int getSelection(Type type) {
		return type.prefaceTemplate(selectionPreface, selectionTemplate);
	}

	public void setSelection(Type type, int selection) {
		switch (type) {
			case PREFACE:
				this.selectionPreface = selection;
				break;
			case TEMPLATE:
				this.selectionTemplate = selection;
				break;
			default:
				throw Unhandled.enumException(type);
		}
	}

	public String getCurrentPrompt(Optional<File> file) {
		var keyPreface = get(Type.PREFACE).list().get(selectionPreface);
		var preface = get(Type.PREFACE).get(keyPreface);

		var keyTemplate = get(Type.TEMPLATE).list().get(selectionTemplate);
		var template = get(Type.TEMPLATE).get(keyTemplate);

		String fileContent = null;
		if (file != null && file.isPresent()) {
			try {
				fileContent = new String(Files.readAllBytes(file.get().toPath()), StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw Errors.asRuntime(e);
			}
		}

		String prompt = (ensureEndsWithBlankline(preface) + ensureEndsWithBlankline(template)).trim();
		if (fileContent != null) {
			fileContent = fileContent.replace("\r", "");
			if (!fileContent.endsWith("\n")) {
				fileContent = fileContent + "\n";
			}
			prompt = prompt + "\n\n```" + gfmType(file.get().getName()) + "\n" + fileContent + "\n```\n";
		}
		return prompt;
	}

	private static String ensureEndsWithBlankline(String str) {
		if (str.endsWith("\n\n")) {
			return str;
		} else if (str.endsWith("\n")) {
			return str + "\n";
		} else {
			return str + "\n\n";
		}
	}

	private static String gfmType(String filename) {
		if (filename.endsWith(".java")) {
			return "java";
		} else {
			var lastDot = filename.lastIndexOf('.');
			var lastSlash = filename.replace('\\', '/').lastIndexOf('/');
			if (lastDot > 0 && lastSlash >= 0) {
				return filename.substring(lastSlash + 1, lastDot);
			} else {
				return "";
			}
		}
	}

	private static final Map<String, String> defaultPrefaces =
			Map.of(
					NONE,
					"",
					"Java terse",
					"You are an expert Java developer. For the question below, please think carefully, work step by step, and provide a concise answer. Wherever possible, respond only in code without any explanation.",
					"Java verbose",
					"You are an expert Java developer. For the question below, please think carefully, work step by step, and provide a detailed answer. Describe the reasoning for your answer.");

	private static final Map<String, String> defaultTemplates =
			Map.of(
					FREEFORM,
					"",
					"Test JUnit 5",
					"Write a test for the following class using JUnit 5.",
					"Test JUnit 4",
					"Write a test for the following class using JUnit 4.",
					"Modernize",
					"Rewrite the following class using the latest syntax constructs from Java 11. Examples to modernize:\n\n"
							+ "- use `var` instead of explicit type declarations where possible\n"
							+ "- use `List<T>` instead of raw arrays `T[]`\n"
							+ "- use collection literals such as `List.of()`, `Set.of()`, and `Map.of()` when appropriate",
					"Describe",
					"Describe the functionality of the following class. Call special attention to any unusual aspects of the design if they are present.");

	public static class Sub {
		final TreeMap<String, String> values = new TreeMap<>();

		public List<String> list() {
			return List.copyOf(values.keySet());
		}

		public String get(String key) {
			return values.get(key);
		}

		public void put(String key, String value) {
			values.put(key, value);
		}

		public void remove(String key) {
			values.remove(key);
		}

		public String newKey(String keySimilarTo, String content) {
			if (!values.containsKey(keySimilarTo)) {
				values.put(keySimilarTo, content);
				return keySimilarTo;
			}
			String keyPrefix;
			int startIdx;
			var matcher = V_MATCHER.matcher(keySimilarTo);
			if (matcher.matches()) {
				keyPrefix = matcher.group(1) + " v";
				startIdx = Integer.parseInt(matcher.group(2)) + 1;
			} else {
				keyPrefix = keySimilarTo + " v";
				startIdx = 2;
			}
			for (int i = startIdx; i <= 99; ++i) {
				String candidate = keyPrefix + i;
				if (!values.containsKey(candidate)) {
					values.put(candidate, content);
					return candidate;
				}
			}
			throw new IllegalStateException("Too many versions of " + keyPrefix);
		}
	}

	private static final Pattern V_MATCHER = Pattern.compile("(.*) v(\\d+)$");

	static PromptStore get() {
		if (store == null) {
			store = new BackedByEclipse(InstanceScope.INSTANCE.getNode("dev.equo.ide.chatgpt"));
		}
		return store;
	}

	private static PromptStore store;

	public static class BackedByEclipse extends PromptStore {
		private final IEclipsePreferences rootPreferences;
		private final Sub prefaces, templates;

		BackedByEclipse(IEclipsePreferences preferences) {
			this.rootPreferences = preferences;
			prefaces = parse(preferences.node("prefaces"));
			templates = parse(preferences.node("templates"));
			prefaces.values.putAll(defaultPrefaces);
			templates.values.putAll(defaultTemplates);

			int defaultPreface = prefaces.list().indexOf("Java terse");
			int defaultTemplate = templates.list().indexOf("Test JUnit 5");

			setSelection(Type.PREFACE, preferences.getInt("prefaces-selection", defaultPreface));
			setSelection(Type.TEMPLATE, preferences.getInt("templates-selection", defaultTemplate));
		}

		private Sub parse(Preferences preferences) {
			var sub = new Sub();
			for (String key : Errors.rethrow().get(preferences::keys)) {
				sub.put(key, preferences.get(key, null));
			}
			return sub;
		}

		@Override
		void save() {
			try {
				save(rootPreferences.node("prefaces"), prefaces, defaultPrefaces);
				save(rootPreferences.node("templates"), templates, defaultTemplates);
				rootPreferences.putInt("prefaces-selection", getSelection(Type.PREFACE));
				rootPreferences.putInt("templates-selection", getSelection(Type.TEMPLATE));
				rootPreferences.flush();
			} catch (Exception e) {
				Errors.dialog().accept(e);
			}
		}

		private void save(Preferences preferences, Sub sub, Map<String, String> defaults)
				throws BackingStoreException {
			for (String key : preferences.keys()) {
				if (!sub.values.containsKey(key)) {
					preferences.remove(key);
				}
			}
			for (Map.Entry<String, String> entry : sub.values.entrySet()) {
				if (!defaults.containsKey(entry.getKey())) {
					preferences.put(entry.getKey(), entry.getValue());
				}
			}
		}

		@Override
		Sub get(Type type) {
			return type.prefaceTemplate(prefaces, templates);
		}
	}
}
