package ie.cit.r00158694.soft8023.lab1.model.monitor;

import java.io.File;
import java.util.Objects;

public class SharedFile implements Comparable<SharedFile> {

	private final File file;
	private final String name;

	public SharedFile(File file, String name) {
		this.file = file;
		this.name = name;
	}

	public File getFile() { return file; }

	public String getName() { return name; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SharedFile)) return false;
		SharedFile that = (SharedFile) o;
		return Objects.equals(getFile(), that.getFile()) && Objects.equals(getName(), that.getName());
	}

	@Override
	public int hashCode() { return Objects.hash(getFile(), getName()); }

	@Override
	public String toString() { return "SharedFile{" + "file=" + file + ", name='" + name + '\'' + '}'; }

	@Override
	public int compareTo(SharedFile o) { return name.compareToIgnoreCase(o.name); }
}
