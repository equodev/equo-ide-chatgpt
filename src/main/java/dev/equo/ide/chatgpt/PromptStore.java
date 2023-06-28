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
import java.util.Collection;
import java.util.TreeMap;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.Preferences;

public abstract class PromptStore {
	public enum Type {
		PREFACE,
		TEMPLATE
	}

	static PromptStore get() {
		return null;
	}

	abstract Sub get(Type type);

	public static class BackedByEclipse extends PromptStore {
		private final Sub prefaces, templates;

		BackedByEclipse(IEclipsePreferences preferences) {
			prefaces = parse(preferences.node("prefaces"));
			templates = parse(preferences.node("templates"));
		}

		private Sub parse(Preferences preferences) {
			var sub = new Sub();
			for (String key : Errors.rethrow().get(preferences::keys)) {
				sub.put(key, preferences.get(key, null));
			}
			return sub;
		}

		@Override
		Sub get(Type type) {
			return type == Type.PREFACE ? prefaces : templates;
		}
	}

	public static class Defaults extends PromptStore {
		private final Sub prefaces =
				new Sub()
						.put(
								"Java terse",
								"You are an expert Java developer. For the question below, please think carefully, work step by step, and provide a concise answer. Wherever possible, respond only in code without any explanation.")
						.put(
								"Java verbose",
								"You are an expert Java developer. For the question below, please think carefully, work step by step and provide a detailed answer. Describe the reasoning for your answer.")
						.put("(None)", "");

		private final Sub templates =
				new Sub()
						.put("Freeform", "")
						.put("Test JUnit 5", "Write a test for the following class using JUnit 5.")
						.put("Test JUnit 4", "Write a test for the following class using JUnit 4.")
						.put(
								"Modernize",
								"Rewrite the following class using the latest syntax constructs from Java 11. Examples to modernize:\n"
										+ "- use `var` instead of explicit type declarations where possible\n"
										+ "- use `List<T>` instead of `Array<T>`\n"
										+ "- use collection literals such as `List.of()`, `Set.of()`, and `Map.of()` when appropriate")
						.put(
								"Describe",
								"Describe the functionality of the following class. Call special attention to any unusual aspects of the design if they are present.");

		@Override
		Sub get(Type type) {
			return type == Type.PREFACE ? prefaces : templates;
		}
	}

	public static class Sub {
		private final TreeMap<String, String> values = new TreeMap<>();

		public Collection<String> list() {
			return values.keySet();
		}

		public String get(String key) {
			return values.get(key);
		}

		public Sub put(String key, String value) {
			values.put(key, value);
			return this;
		}
	}
}
