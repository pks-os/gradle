/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.changedetection.state.mirror;

import org.gradle.api.internal.changedetection.state.DirContentSnapshot;
import org.gradle.api.internal.changedetection.state.FileContentSnapshot;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

@SuppressWarnings("Since15")
public abstract class PhysicalFileVisitor implements HierarchicalFileTreeVisitor {
    private final Deque<String> relativePath = new ArrayDeque<String>();
    private boolean visitedRootDir;

    abstract void visit(Path path, String name, Iterable<String> relativePath, FileContentSnapshot content);

    @Override
    public void preVisitDirectory(Path path, String name) {
        if (!visitedRootDir) {
            visitedRootDir = true;
            return;
        }
        relativePath.addLast(name);
        visit(path, name, relativePath, DirContentSnapshot.INSTANCE);
    }

    @Override
    public void visit(Path path, String name, FileContentSnapshot content) {
        relativePath.addLast(name);
        visit(path, name, relativePath, content);
        relativePath.removeLast();
    }

    @Override
    public void postVisitDirectory() {
        if (!relativePath.isEmpty()) {
            relativePath.removeLast();
        }
    }
}