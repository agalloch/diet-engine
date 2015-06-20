package org.codarama.diet.index.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.io.Resources;
import org.codarama.diet.index.LibraryIndex;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;
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

    private static final String XPATH_QUERY_SEPARATOR = "/";

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
                    addChildrenDepthFirst(packagesAndClassname, rootNode);
                } catch (RepositoryException e) {
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

    private void addChildrenDepthFirst(Collection<String> newChildren, Node to) throws RepositoryException {
        if (newChildren.isEmpty()) {
            return;
        }
        final Iterator<String> childrenIterator = newChildren.iterator();
        if (childrenIterator.hasNext()) {

            final String childName = childrenIterator.next();

            Node child;
            if (!to.hasNode(childName)) {
                child = to.addNode(childName);
            }
            else {
                child = to.getNode(childName);
            }
            childrenIterator.remove();
            addChildrenDepthFirst(newChildren, child);
        }
    }

    @Override
    public LibraryIndex index(JarFile lib) {
        throw new UnsupportedOperationException("we don't support single lib addition here as we'd need to open a session" +
                "for every change, which would cause tremendous overhead");
    }

    @Override
    public boolean contains(ClassName className) {

        String query = Joiner.on(XPATH_QUERY_SEPARATOR)
                .join(
                        JCR_ROOT_NODE_ID,
                        className.toString().replaceAll("\\.", XPATH_QUERY_SEPARATOR)
                );
        query += "." + ClassFile.EXTENSION; // because library jar entries end with .class
        query = normalizeForXpath(query);

        final NodeIterator nodeIterator;
        try {
            nodeIterator = queryForNodes(query);
        } catch (RepositoryException e) {
            throw new IllegalArgumentException("could not execute query: " + query, e);
        }
        return nodeIterator.hasNext();
    }

    private String normalizeForXpath(String query) {

        // we have to quote the class shot name
        // because class name can contain $
        // when they do the xpath quety fails as $ is a special char
        // so we want to replace:
        //   org/company/something/MethodRule$MethodBindingMetadata.class
        // with:
        //   org/company/something/*[\"MethodRule$MethodBindingMetadata.class\"]

        final Tokenizer queryTokenizer = Tokenizer.delimiter(XPATH_QUERY_SEPARATOR).tokenize(query);
        final int tokensCount = queryTokenizer.tokens().size();

        final String nodeName = "*[\"" + queryTokenizer.lastToken() + "\"]";
        final List<String> nodePath = queryTokenizer.tokensIn(Range.range(1, BoundType.CLOSED, tokensCount - 1, BoundType.CLOSED));

        final String pathStr = Joiner.on(XPATH_QUERY_SEPARATOR).join(nodePath);
        return Joiner.on(XPATH_QUERY_SEPARATOR).join(JCR_ROOT_NODE_ID, pathStr, nodeName);
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
