/*
 * Copyright © 2021 92AK
 */
package com.ntak.testfx;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;

public class ExpectationFileVisitor implements FileVisitor<Path> {

    private static final Pattern SSV = Pattern.compile(Pattern.quote("/"));
    private final Map<Integer,Map<String,String[]>> expectations = new HashMap<>();
    private final Path rootDir;

    public ExpectationFileVisitor(Path rootDir) {this.rootDir = rootDir;}

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        System.out.println(dir);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        System.out.println(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.out.println(file);
        return FileVisitResult.TERMINATE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (dir.equals(rootDir)) {
            expectations.put(0, Collections.singletonMap("", new String[]{dir.getFileName().toString()}));
        }

        String parent = dir.toString().replace(String.format("%s/", rootDir.getParent().toString()), "");
        System.out.println(parent);
        int depth = SSV.split(parent).length;

        Map<String,String[]> children = expectations.getOrDefault(depth, new HashMap<>());
        String[] values = children.get(parent);

        LinkedList<String> listValues = new LinkedList<>();
        if (values != null) {
            listValues.addAll(Arrays.asList(values));
        }
        Files.list(dir).map(p -> p.getFileName().toString()).forEach(listValues::add);
        children.put(parent,listValues.toArray(new String[0]));
        expectations.put(depth, children);

        return FileVisitResult.CONTINUE;
    }

    public Map<Integer,Map<String,String[]>> getExpectations() {
        return expectations;
    }
}
