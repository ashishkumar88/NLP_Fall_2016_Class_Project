package edu.asu.nlp.fall;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import opennlp.tools.coref.DefaultLinker;
import opennlp.tools.coref.DiscourseEntity;
import opennlp.tools.coref.Linker;
import opennlp.tools.coref.LinkerMode;
import opennlp.tools.coref.mention.DefaultParse;
import opennlp.tools.coref.mention.Mention;
import opennlp.tools.coref.mention.MentionContext;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

/*
 * Author : Ashish Kumar, Vatsal Mahajan, Saurabh Singh, Vivek Singh
 * References : Demo codes provided by Stanford Parser library
 * 				Demo codes provided by Open NLP library
 *				http://blog.dpdearing.com/2012/11/
 * Dataset : https://catalog.ldc.upenn.edu/LDC2003T05 - Sample corpus (Available in the LDC2003T05 folder)
 * Pretrained models by Stanford Parser and Wordnet for Java have also been used
 */
public class CreateDependencyParse {
	private final static Logger slf4jLogger = LoggerFactory.getLogger(CreateDependencyParse.class);
	private Linker linker;
	private Parser parser;
	private String parserFile;
	private LexicalizedParser lp;
	private TokenizerFactory<CoreLabel> tokenizerFactory;
	private Tokenizer tokenizer;
	private static String EMPTY_STRING = "";
	private static String PARAGRAPH_SEPARATOR = "---";
	private static boolean DEBUG_LOGS = Boolean.FALSE;
	private static boolean INFO_LOGS = Boolean.TRUE;
	private Set<VerbDependency> allVerbDependency;
	private ArrayList<VerbsDependenciesCount> allEventPairs;
	private StanfordCoreNLP pipeline;
	private Statement stmt;
	private Connection conn;

	public Set<VerbDependency> getAllVerbDependency() {
		return allVerbDependency;
	}

	public void setAllVerbDependency(Set<VerbDependency> allVerbDependency) {
		this.allVerbDependency = allVerbDependency;
	}

	public ArrayList<VerbsDependenciesCount> getAllEventPairs() {
		return allEventPairs;
	}

	public void setAllEventPairs(ArrayList<VerbsDependenciesCount> allEventPairs) {
		this.allEventPairs = allEventPairs;
	}

	/*
	 * Constructs the CreateDependencyParse class with the required linker,
	 * parser, tokenizer objects
	 */
	public CreateDependencyParse() {
		try {
			linker = new DefaultLinker("lib" + File.separator + "opennlp-1.5-en" + File.separator + "coref",
					LinkerMode.TEST);
			InputStream io = new FileInputStream(new File("model-1.5-en" + File.separator + "en-parser-chunking.bin"));
			ParserModel parserModel = new ParserModel(io);
			parser = ParserFactory.create(parserModel);

			parserFile = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
			lp = LexicalizedParser.loadModel(parserFile);

			tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");

			InputStream is = new FileInputStream("model-1.5-en" + File.separator + "en-token.bin");
			TokenizerModel model = new TokenizerModel(is);
			tokenizer = new TokenizerME(model);
			allEventPairs = new ArrayList<>();
			Properties props;
			props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, lemma");
			pipeline = new StanfordCoreNLP(props);
			allVerbDependency = new HashSet<>();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int checkPartOfKey(Map<String, Integer> entity_map, Set<String> keys, String key) {
		for (String str : keys) {
			if (str.indexOf(key) != -1) {
				return entity_map.get(str);
			}
		}
		return -1;
	}

	/*
	 * parses the document paragraph by paragraph
	 */
	public void startParse() {
		Map<String, Integer> entity_map = new HashMap<String, Integer>();

		ArrayList<List<String>> list_res_whole = null;
		List<String> list_res = null;
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream("LDC2003T05" + File.separator + "single.txt")));
			String line = EMPTY_STRING;

			String tempSection = EMPTY_STRING;
			MaxentTagger tagger = new MaxentTagger("models" + File.separator + "wsj-0-18-left3words-nodistsim.tagger");

			while ((line = br.readLine()) != null) {
				list_res_whole = new ArrayList<List<String>>();
				Map<String, NodePair> node_map = null;
				list_res = new ArrayList<String>();
				if (line.indexOf(PARAGRAPH_SEPARATOR) != -1) {
					String[] sentences = tempSection.split("#");

					final List<Mention> document = new ArrayList<Mention>();
					for (int i = 0; i < sentences.length; i++) {
						final Parse parse = parseSentence(sentences[i], tokenizer);

						final DefaultParse parseWrapper = new DefaultParse(parse, i);
						final Mention[] extents = linker.getMentionFinder().getMentions(parseWrapper);

						// Note: taken from TreebankParser source...
						for (int ei = 0, en = extents.length; ei < en; ei++) {
							// construct parses for mentions which don't have
							// constituents
							if (extents[ei].getParse() == null) {
								// not sure how to get head index, but it
								// doesn't seem to be used at this point
								final Parse snp = new Parse(parse.getText(), extents[ei].getSpan(), "NML", 1.0, 0);
								parse.insert(snp);
								// setting a new Parse for the current extent
								extents[ei].setParse(new DefaultParse(snp, i));
							}
						}
						document.addAll(Arrays.asList(extents));
					}

					ArrayList<ArrayList<String>> verbs = new ArrayList<ArrayList<String>>();
					if (!document.isEmpty()) {
						final DiscourseEntity[] entities = linker
								.getEntities(document.toArray(new Mention[document.size()]));

						for (String sentence : sentences) {
							ArrayList<String> verb_sen = new ArrayList<String>();
							String tagged = tagger.tagString(sentence);
							writeConsole(tagged, DEBUG_LOGS);
							String[] posTags = tagged.split(" ");
							for (String tags : posTags) {
								if (tags.indexOf("VB") != -1) {
									verb_sen.add(tags.split("_")[0]);
								}
							}
							verbs.add(verb_sen);
						}

						for (int i = 0; i < entities.length; i++) {
							final DiscourseEntity ent = entities[i];
							final Iterator<MentionContext> mentions = ent.getMentions();
							while (mentions.hasNext()) {
								final MentionContext mc = mentions.next();
								writeConsoleInline("[" + mc.toString() + "]", DEBUG_LOGS);
								if (entity_map.get(mc.toString().toLowerCase().trim()) == null) {
									entity_map.put(mc.toString().trim().toLowerCase(), i + 1);
								}
							}
							writeConsoleInline("\n", DEBUG_LOGS);
						}
					}
					writeConsole("\nVerbs extracted from the sentences and coreferences found.", INFO_LOGS);
					if (DEBUG_LOGS) {
						for (ArrayList<String> verbList : verbs) {
							for (String verb : verbList) {
								writeConsoleInline(verb + ", ", DEBUG_LOGS);
							}
							writeConsole(EMPTY_STRING, DEBUG_LOGS);
						}
					}

					int index = 0;

					for (String sentence : sentences) {
						list_res = new ArrayList<String>();
						// node_map = new HashMap<String, NodePair>();
						edu.stanford.nlp.process.Tokenizer<CoreLabel> tok = tokenizerFactory
								.getTokenizer(new StringReader(sentence));
						List<CoreLabel> rawWords2 = tok.tokenize();
						Tree parse = lp.apply(rawWords2);

						TreebankLanguagePack tlp = new PennTreebankLanguagePack(); // PennTreebankLanguagePack
																					// for
																					// English
						GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
						GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
						Collection<TypedDependency> tdl = gs.typedDependencies();
						ArrayList<String> temp_verb = verbs.get(index);

						for (TypedDependency td : tdl) {
							String rel = td.reln().toString();
							String gov = td.gov().toString().split("/")[0].toLowerCase();
							String dep = td.dep().toString().split("/")[0].toLowerCase();
							writeConsole("rel : " + rel + ", gov : " + gov + ", dep : " + dep, DEBUG_LOGS);
							if (temp_verb.contains(gov)) {
								if (checkPartOfKey(entity_map, entity_map.keySet(), dep) != -1) {
									list_res.add(checkPartOfKey(entity_map, entity_map.keySet(), dep) + " " + gov + " "
											+ rel + " " + index + " " + dep);
								}
							} else if (temp_verb.contains(dep)) {
								if (checkPartOfKey(entity_map, entity_map.keySet(), gov) != -1) {
									list_res.add(checkPartOfKey(entity_map, entity_map.keySet(), gov) + " " + dep + " "
											+ rel + " " + index + " " + gov);
								}
							}
						}
						for (String s : list_res) {
							writeConsole(s, DEBUG_LOGS);
						}
						list_res_whole.add(list_res);
						index++;
					}
					writeConsole("#################################################", INFO_LOGS);
					node_map = new HashMap<String, NodePair>();
					for (int i = 0; i < list_res_whole.size(); i++) {
						List<String> list_res_t = list_res_whole.get(i);
						for (String str : list_res_t) {
							writeConsole(str, DEBUG_LOGS);
							String[] ar = str.split(" ");
							if (ar[2].equals("nsubj") || ar[2].equals("dobj")) {
								if (node_map.get(ar[0]) == null) {
									NodePair np = new NodePair();
									np.setHeadWord(ar[4]);
									np.setKey(Integer.parseInt(ar[3]));
									np.setFirst(new Node(ar[1], Dependency.parseString(ar[2])));
									if (ar[2].equals("nsubj")) {
										np.getFirst().setSubject(ar[4]);
									} else {
										np.getFirst().setObject(ar[4]);
									}
									node_map.put(ar[0], np);
								} else {
									NodePair np = node_map.get(ar[0]);
									int val = Integer.parseInt(ar[3]);
									if (!np.getFirst().getVerb().equals(ar[1]) && (val == np.getKey())) {
										np.setSecond(new Node(ar[1], Dependency.parseString(ar[2])));
										if (ar[2].equals("nsubj")) {
											np.getSecond().setSubject(np.getHeadWord());
										} else {
											np.getSecond().setObject(np.getHeadWord());
										}
									}

								}
							}
						}
					}
					for (Map.Entry<String, NodePair> entry : node_map.entrySet()) {
						String key = entry.getKey();
						NodePair nodePair = node_map.get(key);
						int sen_indx = nodePair.getKey();
						if (nodePair.getFirst() != null) {
							String first_verb = nodePair.getFirst().getVerb();
							List<String> list_res_t = list_res_whole.get(sen_indx);
							for (String str : list_res_t) {
								String[] tmp = str.split(" ");
								if (first_verb.equals(tmp[1]) && (nodePair.getFirst().getSubject() == null)
										&& tmp[2].equals("nsubj")) {
									nodePair.getFirst().setSubject(tmp[4]);
								} else if (first_verb.equals(tmp[1]) && (nodePair.getFirst().getObject() == null)
										&& tmp[2].equals("dobj")) {
									nodePair.getFirst().setObject(tmp[4]);
								}
							}
						}
						if (nodePair.getSecond() != null) {
							String second_verb = nodePair.getSecond().getVerb();
							List<String> list_res_t = list_res_whole.get(sen_indx);
							for (String str : list_res_t) {
								String[] tmp = str.split(" ");
								if (second_verb.equals(tmp[1]) && (nodePair.getSecond().getSubject() == null)
										&& tmp[2].equals("nsubj")) {
									nodePair.getSecond().setSubject(tmp[4]);
								} else if (second_verb.equals(tmp[1]) && (nodePair.getSecond().getObject() == null)
										&& tmp[2].equals("dobj")) {
									nodePair.getSecond().setObject(tmp[4]);
								}
							}
						}
					}
					for (Map.Entry<String, NodePair> entry : node_map.entrySet()) {
						String key = entry.getKey();
						NodePair nodePair = node_map.get(key);
						if ((nodePair.getFirst() != null) && (nodePair.getSecond() != null)) {
							writeConsole(node_map.get(key).toString(), DEBUG_LOGS);
							allEventPairs.add(VerbsDependenciesCount.createNew(nodePair, this, allVerbDependency));
						}
					}
					list_res_whole = null;
					tempSection = "";
				} else {
					tempSection = tempSection + "#" + line;
				}
			}
			br.close();
		} catch (

		Exception exp)

		{
			exp.printStackTrace();
		}

	}

	public void updateDataStructures() {
		System.gc();

		Set<VerbDependency> temp = new HashSet<>();
		for (VerbDependency dependency : allVerbDependency) {
			int count = 0;
			for (VerbsDependenciesCount dependenciesCount : allEventPairs) {
				if (dependency.equals(dependenciesCount.getVerb1())
						|| dependency.equals(dependenciesCount.getVerb2())) {
					count++;
				}
			}
			dependency.setCount(count);
			temp.add(dependency);
		}
		allVerbDependency = temp;

		try {
			createDatabaseConnection();
			dropTables();
			createVerbDependencyTable();

			for (VerbDependency dependency : allVerbDependency) {
				insertIntoVerbDetailTable(dependency);
			}

			for (VerbsDependenciesCount dependenciesCount : allEventPairs) {
				insertIntoVerbDependencyTable(dependenciesCount);
			}

			allEventPairs.clear();

			ResultSet rs = stmt.executeQuery(
					"SELECT verb1, dependency1, verb2, dependency2, head_word, count(*) as total_count FROM verbs_dependency group by verb1, dependency1, verb2, dependency2, head_word ;");
			while (rs.next()) {
				String verb1 = rs.getString("verb1");
				String dependency1 = rs.getString("dependency1");
				String verb2 = rs.getString("verb2");
				String dependency2 = rs.getString("dependency2");
				String headWord = rs.getString("head_word");
				int totalCount = rs.getInt("total_count");

				int count1 = 1;
				String subject1 = "";
				String object1 = "";
				Statement st1 = conn.createStatement();
				ResultSet rs1 = st1.executeQuery("SELECT count, subject, object FROM verb_details where verb1='" + verb1
						+ "' and dependency1='" + dependency1 + "';");
				if (rs1.next()) {
					count1 = rs1.getInt("count");
					subject1 = rs1.getString("subject");
					object1 = rs1.getString("object");
				}

				int count2 = 1;
				String subject2 = "";
				String object2 = "";
				Statement st2 = conn.createStatement();
				ResultSet rs2 = st1.executeQuery("SELECT count, subject, object FROM verb_details where verb1='" + verb2
						+ "' and dependency1='" + dependency2 + "';");
				if (rs2.next()) {
					count2 = rs2.getInt("count");
					subject2 = rs2.getString("subject");
					object2 = rs2.getString("object");
				}

				allEventPairs.add(new VerbsDependenciesCount(verb1, dependency1, count1, subject1, object1, verb2,
						dependency2, count2, subject2, object2, headWord, totalCount));
			}

			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void insertIntoVerbDetailTable(VerbDependency dependency) throws Exception {
		String query = "insert into verb_details values ('" + dependency.getVerb() + "','"
				+ dependency.getDependency().name() + "'," + dependency.getCount() + ", '" + dependency.getSubject()
				+ "', '" + dependency.getObject() + "');";
		stmt.executeUpdate(query);
	}

	public void insertIntoVerbDependencyTable(VerbsDependenciesCount dependenciesCount) throws Exception {
		String query = "insert into verbs_dependency values ('" + dependenciesCount.getVerb1().getVerb() + "','"
				+ dependenciesCount.getVerb1().getDependency().name() + "','" + dependenciesCount.getVerb2().getVerb()
				+ "','" + dependenciesCount.getVerb2().getDependency().name() + "','"
				+ dependenciesCount.getCoreferringEntity() + "');";
		stmt.executeUpdate(query);
	}

	public void dropTables() {
		try {
			String query = "drop table verbs_dependency";
			stmt.executeUpdate(query);
		} catch (Exception e) {

		}
		try {
			String query = "drop table verb_details";
			stmt.executeUpdate(query);
		} catch (Exception e) {

		}
	}

	public void createVerbDependencyTable() throws Exception {
		String query = "create table verbs_dependency (verb1 varchar(50) not null, dependency1 varchar(50) not null, verb2 varchar(50) not null, dependency2 varchar(50) not null, head_word varchar(50) not null)";
		stmt.executeUpdate(query);
		query = "create table verb_details (verb1 varchar(50) not null, dependency1 varchar(50) not null, count int, subject varchar(50), object varchar(50) default 0)";
		stmt.executeUpdate(query);
	}

	public void createDatabaseConnection() {
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:test.db");
			stmt = conn.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		CreateDependencyParse createDependencyParse = new CreateDependencyParse();
		createDependencyParse.startParse();
		createDependencyParse.updateDataStructures();
		VerbDependencyGraph verbDependencyGraph = new VerbDependencyGraph();
		verbDependencyGraph.publishDependencies(createDependencyParse.allEventPairs,
				createDependencyParse.allVerbDependency);
		verbDependencyGraph.createGraph();
		EventChains eventChains = verbDependencyGraph.getEventChains();
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(new File("output" + File.separator + "file.json"), eventChains);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Parse parseSentence(final String text, Tokenizer tokenizer) throws IOException {
		final Parse p = new Parse(text, new Span(0, text.length()), AbstractBottomUpParser.INC_NODE, 1, 0);
		final Span[] spans = tokenizer.tokenizePos(text);
		for (int idx = 0; idx < spans.length; idx++) {
			final Span span = spans[idx];
			p.insert(new Parse(text, span, AbstractBottomUpParser.TOK_NODE, 0, idx));
		}
		return parser.parse(p);
	}

	private static void writeConsoleInline(String text, boolean writeFlag) {
		if (writeFlag) {
			System.out.print(text);
		}
	}

	private static void writeConsole(String text, boolean writeFlag) {
		if (writeFlag) {
			slf4jLogger.info(text);
		}
	}

	public String lemmatize(String word) {
		Annotation tokenAnnotation = new Annotation(word);
		pipeline.annotate(tokenAnnotation); // necessary for the LemmaAnnotation
											// to be set.
		List<CoreMap> list = tokenAnnotation.get(SentencesAnnotation.class);
		String tokenLemma = list.get(0).get(TokensAnnotation.class).get(0).get(LemmaAnnotation.class);
		return tokenLemma;
	}
}
