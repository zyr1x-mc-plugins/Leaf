package ru.lewis.point;

import java.util.List;

public class CompositeClassLoader extends ClassLoader {

    private final List<ClassLoader> classLoaders;

    public CompositeClassLoader(
        ClassLoader parent,
        List<ClassLoader> classLoaders
    ) {
        super(parent);
        this.classLoaders = classLoaders;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        for (ClassLoader classLoader : classLoaders) {
            try {
                return Class.forName(name, false, classLoader);
            } catch (ClassNotFoundException ignored) {
            }
        }

        throw new ClassNotFoundException(name);
    }
}
