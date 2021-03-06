/*
 * Copyright (c) 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chimpler.example;

import java.io.File;
import java.util.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.params.FacetSearchParams;
import org.apache.lucene.facet.search.CountFacetRequest;
import org.apache.lucene.facet.search.FacetRequest;
import org.apache.lucene.facet.search.FacetResult;
import org.apache.lucene.facet.search.FacetResultNode;
import org.apache.lucene.facet.search.FacetsCollector;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * @author Frederic Dang Ngoc
 */
class FacetLuceneSearcher {
	private static Version LUCENE_VERSION = Version.LUCENE_45;
	public static void main(String args[]) throws Exception {
//		if (args.length != 3) {
//			System.err.println("Parameters: [index directory] [taxonomy directory] [query]");
//			System.exit(1);
//		}
		
		String indexDirectory = "index";
		String taxonomyDirectory = "taxonomy";
		String query = "story";
		
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File(indexDirectory)));
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);

		TaxonomyReader taxonomyReader = new DirectoryTaxonomyReader(FSDirectory.open(new File(taxonomyDirectory)));

		ArrayList<FacetRequest> facetRequests = new ArrayList<FacetRequest>();
		facetRequests.add(new CountFacetRequest(new CategoryPath("author"), 100));
		facetRequests.add(new CountFacetRequest(new CategoryPath("book_category"), 100));
		
		FacetSearchParams searchParams = new FacetSearchParams(facetRequests);
		

		ComplexPhraseQueryParser queryParser = new ComplexPhraseQueryParser(LUCENE_VERSION, "title", new StandardAnalyzer(LUCENE_VERSION));
		Query luceneQuery = queryParser.parse(query);

		// Collectors to get top results and facets
		TopScoreDocCollector topScoreDocCollector = TopScoreDocCollector.create(10, true);
		FacetsCollector facetsCollector = FacetsCollector.create(searchParams, indexReader, taxonomyReader);
		indexSearcher.search(luceneQuery, MultiCollector.wrap(topScoreDocCollector, facetsCollector));
		System.out.println("Found:");
		
		for(ScoreDoc scoreDoc: topScoreDocCollector.topDocs().scoreDocs) {
			Document document = indexReader.document(scoreDoc.doc);
			System.out.printf("- book: id=%s, title=%s, book_category=%s, authors=%s, score=%f\n",
					document.get("id"), document.get("title"),
					document.get("book_category"),
					document.get("authors"),
					scoreDoc.score);
		}

		System.out.println("Facets:");
		for(FacetResult facetResult: facetsCollector.getFacetResults()) {
			System.out.println("- " + facetResult.getFacetResultNode().label);
			for(FacetResultNode facetResultNode: facetResult.getFacetResultNode().subResults) {
				System.out.printf("    - %s (%f)\n", facetResultNode.label.toString(),
						facetResultNode.value);
				for(FacetResultNode subFacetResultNode: facetResultNode.subResults) {
					System.out.printf("        - %s (%f)\n", subFacetResultNode.label.toString(),
							subFacetResultNode.value);
				}
			}
		}
		taxonomyReader.close();
		indexReader.close();
	}
}