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
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.index.FacetFields;
import org.apache.lucene.facet.params.FacetSearchParams;
import org.apache.lucene.facet.search.CountFacetRequest;
import org.apache.lucene.facet.search.FacetRequest;
import org.apache.lucene.facet.search.FacetResult;
import org.apache.lucene.facet.search.FacetResultNode;
import org.apache.lucene.facet.search.FacetsCollector;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Frederic Dang Ngoc
 */
class FacetLuceneIndexer {
	
	private static Version LUCENE_VERSION = Version.LUCENE_45;
	
	public static void main(String args[]) throws Exception {
//		if (args.length != 3) {
//			System.err.println("Parameters: [index directory] [taxonomy directory] [json file]");
//			System.exit(1);
//		}
		
	
		String indexDirectory = "index";
		String taxonomyDirectory = "taxonomy";
		String jsonFileName = "/home/qiuqiang/workspace/facet-lucene-example/books.json";
		
		IndexWriterConfig writerConfig = new IndexWriterConfig(LUCENE_VERSION, new WhitespaceAnalyzer(LUCENE_VERSION));
		writerConfig.setOpenMode(OpenMode.APPEND);
		IndexWriter indexWriter = new IndexWriter(FSDirectory.open(new File(indexDirectory)), writerConfig);

		
		TaxonomyWriter taxonomyWriter = new DirectoryTaxonomyWriter(MMapDirectory.open(new File(taxonomyDirectory)), OpenMode.APPEND);
		
		TaxonomyReader taxonomyReader = new DirectoryTaxonomyReader(FSDirectory.open(new File(taxonomyDirectory)));
		
		String content = IOUtils.toString(new FileInputStream(jsonFileName));
		JSONArray bookArray = new JSONArray(content);
		
		Field idField = new IntField("id", 0, Store.YES);
		Field titleField = new TextField("title", "", Store.YES);
		Field authorsField = new TextField("authors", "", Store.YES);
		Field bookCategoryField = new TextField("book_category", "", Store.YES);
		
		
		indexWriter.deleteAll();
		
		FacetFields facetFields = new FacetFields(taxonomyWriter);
		
		for(int i = 0 ; i < bookArray.length() ; i++) {
			Document document = new Document();

			JSONObject book = bookArray.getJSONObject(i);
			int id = book.getInt("id");
			String title = book.getString("title");
			String bookCategory = book.getString("book_category");
			
    		List<CategoryPath> categoryPaths = new ArrayList<CategoryPath>();
			
    		String authorsString = "";
			JSONArray authors = book.getJSONArray("authors");
			for(int j = 0 ; j < authors.length() ; j++) {
				String author = authors.getString(j);
				if (j > 0) {
					authorsString += ", ";
				}
				categoryPaths.add(new CategoryPath("author", author));
				authorsString += author;
			}
			categoryPaths.add(new CategoryPath("book_category" + bookCategory, '/'));
			
			
			idField.setIntValue(id);
			titleField.setStringValue(title);
			authorsField.setStringValue(authorsString);
			bookCategoryField.setStringValue(bookCategory);
			
			facetFields.addFields(document, categoryPaths);
			
			document.add(idField);
			document.add(titleField);
			document.add(authorsField);
			document.add(bookCategoryField);
			
			indexWriter.addDocument(document);
			
			System.out.printf("Book: id=%d, title=%s, book_category=%s, authors=%s\n",
				id, title, bookCategory, authors);
		}
		
		taxonomyWriter.prepareCommit();
		try{
			taxonomyWriter.commit();
		}catch(Exception e){
			taxonomyWriter.rollback();
		}
		
//		taxonomyWriter.close();
//		
//		indexWriter.commit();
//		indexWriter.close();
		
		
		
		String query = "story";
		
		IndexReader indexReader = DirectoryReader.open(indexWriter, false);
		IndexReader indexReader2 = DirectoryReader.open(indexWriter, false);
		System.out.println(indexReader==indexReader2);
		
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		
		TaxonomyReader newTaxonomyReader = DirectoryTaxonomyReader.openIfChanged(taxonomyReader);
		if(newTaxonomyReader!=null){
			TaxonomyReader tmp = taxonomyReader;
			taxonomyReader = newTaxonomyReader;
			tmp.close();
		}else{
			System.out.println("null");
		}
		
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
		
		taxonomyWriter.commit();
		taxonomyWriter.close();
		
		indexWriter.commit();
		indexWriter.close();
		
	}
}