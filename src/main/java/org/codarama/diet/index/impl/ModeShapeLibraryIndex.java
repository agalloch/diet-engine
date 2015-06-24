package org.codarama.diet.index.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import org.codarama.diet.index.LibraryIndex;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.ClassStream;
import org.codarama.diet.util.Tokenizer;
import org.infinispan.schematic.document.ParsingException;
import org.modeshape.common.collection.Problems;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.RepositoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A {@link org.codarama.diet.index.LibraryIndex} utilizing the ModeShape JCR implementation.
 *
 * More info here:
 *   https://docs.jboss.org/author/display/MODE40/Home
 *
 * Created by ayld on 20.06.15.
 */
public class ModeShapeLibraryIndex implements LibraryIndex{

    private static final Logger LOG = LoggerFactory.getLogger(ModeShapeLibraryIndex.class);

    private static final String DEFAULT_REPO_SESSION_NAME = "default";

    private static final String JCR_QUERY_LANGUAGE = "xpath";
    private static final String JCR_SYSTEM_NODE_ID = "/jcr:system";
    private static final String JCR_ROOT_NODE_ID = "/jcr:root";
    private static final String JCR_BINARY_NODE_PROPERTY = "binary";

    private static final String XPATH_QUERY_SEPARATOR = "/";
    private static final String XPATH_DOLLAR_SIGN_REPLACEMENT = "___";

    private static final String INNER_CLASS_SEPARATOR_REGEX = "\\" + ClassName.INNER_CLASS_SEPARATOR;

    private final Session repoSession;

    private ModeShapeLibraryIndex(String libLocation) throws ParsingException, RepositoryException {
        final RepositoryConfiguration conf = RepositoryConfiguration.read(Resources.getResource(libLocation));

        final Problems problems = conf.validate();
        if (problems.hasErrors()) {
            throw new ParsingException(problems.toString(), -1, -1); // -1's as we don't know the line and char numbers
        }

        final ModeShapeEngine engine = new ModeShapeEngine();
        engine.start();

        final Repository repo = engine.deploy(conf);
        this.repoSession = repo.login(DEFAULT_REPO_SESSION_NAME);
    }

    public static ModeShapeLibraryIndex withConfig(String relativeRepositoryConfigLocation) throws ParsingException, RepositoryException {
        return new ModeShapeLibraryIndex(relativeRepositoryConfigLocation);
    }

    @Override
    public LibraryIndex index(Set<JarFile> libs) {

        final Node rootNode;
        try {
            rootNode = repoSession.getRootNode();
        } catch (RepositoryException e) {
            throw new IllegalStateException("could not get root node for index, cause: ", e);
        }

        for (JarFile jar: libs) {

            final Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {

                final JarEntry entry = entries.nextElement();
                final String entryName = entry.getName();

                final List<String> packagesAndClassname = Tokenizer.delimiter("/").tokenize(entryName).tokens();
                try {
                    addChildrenDepthFirst(packagesAndClassname, rootNode, entry, jar);
                } catch (RepositoryException | IOException e) {
                    throw new IllegalStateException("could not add nodes: " + packagesAndClassname + ", to parent: " + rootNode);
                }
            }
        }

        try {
            repoSession.save();
        } catch (RepositoryException e) {
            throw new IllegalStateException("could not save changes for index, cause: ", e);
        }

        return this;
    }

    private void addChildrenDepthFirst(Collection<String> newChildren, Node to, JarEntry entry, JarFile jar) throws RepositoryException, IOException {
        if (newChildren.isEmpty()) {
            return;
        }
        final Iterator<String> childrenIterator = newChildren.iterator();
        if (childrenIterator.hasNext()) {

            String childName = childrenIterator.next();

            final boolean childContainsInnerClass = childName.contains(ClassName.INNER_CLASS_SEPARATOR);
            if (childContainsInnerClass) {
                childName = childName.replaceAll(INNER_CLASS_SEPARATOR_REGEX, XPATH_DOLLAR_SIGN_REPLACEMENT);
            }

            Node child;
            if (!to.hasNode(childName)) {
                child = to.addNode(childName);
            }
            else {
                child = to.getNode(childName);
            }

            final boolean childIsClassFile = childName.endsWith(ClassFile.EXTENSION);
            if (childIsClassFile) {
                child.setProperty(JCR_BINARY_NODE_PROPERTY, toBinary(jar.getInputStream(entry)));
            }

            childrenIterator.remove();
            addChildrenDepthFirst(newChildren, child, entry, jar);
        }
    }

    private Binary toBinary(InputStream stream) throws RepositoryException {
        return repoSession.getValueFactory().createBinary(stream);
    }

    @Override
    public LibraryIndex index(JarFile lib) {
        throw new UnsupportedOperationException("we don't support single lib addition here as we'd need to open a session" +
                "for every change, which would cause tremendous overhead");
    }

    @Override
    public boolean contains(ClassName className) {
        return find(className) != null;
    }

    @Override
    public ClassStream get(ClassName name) {
        final ClassStream result = find(name);
        if (result == null) {
            throw new IllegalStateException("no file with name: " + name + ", found in repository");
        }
        return result;
    }

    @Override
    public ClassStream find(ClassName name) {
        String query = Joiner.on(XPATH_QUERY_SEPARATOR)
                .join(
                        JCR_ROOT_NODE_ID,
                        name.toString()
                                .replaceAll("\\.", XPATH_QUERY_SEPARATOR)
                                .replaceAll(INNER_CLASS_SEPARATOR_REGEX, XPATH_DOLLAR_SIGN_REPLACEMENT)
                );
        query += "." + ClassFile.EXTENSION; // because library jar entries end with .class

        final NodeIterator nodeIterator;
        try {
            nodeIterator = queryForNodes(query);
        } catch (RepositoryException e) {
            throw new IllegalArgumentException("could not execute query: " + query, e);
        }
        if (!nodeIterator.hasNext()) {
            return null;
        }
        if (nodeIterator.getSize() > 1) {
            throw new IllegalArgumentException("found more than one node for name: " + name + " with query: " + query);
        }
        final Node node = nodeIterator.nextNode();

        final Binary binary;
        try {
            binary = node.getProperty(JCR_BINARY_NODE_PROPERTY).getBinary();
        } catch (RepositoryException e) {
            throw new IllegalStateException("could not get binary property for file with name: " + name, e);
        }

        try {
            return ClassStream.fromStream(binary.getStream());
        } catch (RepositoryException e) {
            throw new IllegalStateException("could not get binary stream for node with name: " + name, e);
        }
    }

    @Override
    public long size() {
        final NodeIterator allClassNodes;
        try {
            allClassNodes = queryForNodes("//element(*, nt:base)[jcr:contains(., '" + ClassFile.EXTENSION + "')]");
        } catch (RepositoryException e) {
            throw new IllegalStateException(e);
        }
        return allClassNodes.getSize();
    }

    @Override
    public String toString() {

        final StringBuffer result = new StringBuffer();
        try {
            final String queryStr = "//*"; // all nodes

            final NodeIterator resultNodes = queryForNodes(queryStr);
            while (resultNodes.hasNext()) {

                final String nodePath = resultNodes.nextNode().getPath();

                // omit JCR system
                if (nodePath.contains(JCR_SYSTEM_NODE_ID)) {
                    continue;
                }

                result.append(nodePath + "\n");
            }

        } catch (RepositoryException e) {
            throw new RuntimeException("could not execute query: " + "", e);
        }

        return Strings.isNullOrEmpty(result.toString()) ? "empty" : result.toString();
    }

    private NodeIterator queryForNodes(String queryStr) throws RepositoryException {
        final QueryManager queryManager = repoSession.getWorkspace().getQueryManager();
        final QueryResult queryResult = queryManager.createQuery(queryStr, JCR_QUERY_LANGUAGE).execute();

        return queryResult.getNodes();
    }
}
