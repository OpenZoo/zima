/**
 * Copyright (c) 2020, 2021, 2022 Adrian Siekierka
 *
 * This file is part of zima.
 *
 * zima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * zima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with zima.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.asie.zima.util;

import pl.asie.zima.gui.ZimaLicenseWindow;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public final class FileUtils {
    // TODO: move elsewhere
    public static String readAllTextFromClasspath(String classFile) throws IOException {
        try (InputStream stream = ZimaLicenseWindow.class.getClassLoader().getResourceAsStream(classFile)) {
            if (stream == null) {
                throw new IOException();
            }
            return new String(readAll(stream), StandardCharsets.UTF_8);
        }
    }

    @FunctionalInterface
	public interface ThrowingSupplier<T> {
		T get() throws Exception;
	}

	private static File projectDirectory;

	private FileUtils() {

	}

	public static String getExtension(File file) {
		String[] parts = file.getName().split("\\.");
		return parts[parts.length - 1];
	}

	public static void setProjectDirectory(File directory) {
		projectDirectory = directory;
	}

	public static byte[] readAll(InputStream stream) throws IOException {
		try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
			stream.transferTo(byteStream);
			return byteStream.toByteArray();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T serializeOrCreate(String filename, ThrowingSupplier<T> supplier) throws Exception {
		if (projectDirectory == null) {
			throw new RuntimeException("No projectDirectory set!");
		}
		File file = new File(projectDirectory, filename + ".dat");
		if (file.exists()) {
			try (FileInputStream fileIn = new FileInputStream(file); ObjectInputStream in = new ObjectInputStream(fileIn)) {
				return (T) in.readObject();
			}
		} else {
			T obj = supplier.get();
			try (FileOutputStream fileOut = new FileOutputStream(file); ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
				out.writeObject(obj);
			}
			return obj;
		}
	}
}
