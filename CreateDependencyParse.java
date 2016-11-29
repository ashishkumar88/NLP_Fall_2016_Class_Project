import java.io.*;
import java.util.*;
import java.net.*;

//import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;import opennlp.tools.coref.DefaultLinker;
import opennlp.tools.coref.DiscourseEntity;
import opennlp.tools.coref.Linker;
import opennlp.tools.coref.LinkerMode;
import opennlp.tools.coref.mention.DefaultParse;
import opennlp.tools.coref.mention.Mention;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import opennlp.tools.coref.mention.MentionContext;

class Node{
	String verb;
	String rel;
}
class NodePair{
	 int key;
	 Node first;
	 Node second;
	 public void toPString(){
		 if (first != null){
			System.out.print("first Node : "+first.verb+" : "+first.rel);
		 }
		 if (second != null){
			System.out.print(" second Node : "+second.verb+" : "+second.rel);
		 }else{
			 System.out.print(" second pair is no there for entity");
		 }
		 System.out.println("\n");
		 
	 }
}
public class CreateDependencyParse {
	private static Linker mylinker;
	
	public static int checkPartOfKey(Map<String,Integer> entity_map,Set<String> keys,String key){
	   for(String str : keys){
		  if(str.indexOf(key)!=-1)
				return entity_map.get(str);
	   }
	   return -1;
    }
	public static void main(String[] args) {
		Map<String,Integer> entity_map = new HashMap<String,Integer>();
		Map<String,NodePair> node_map = null; ;
		ArrayList< List<String> > list_res_whole = new ArrayList< List<String> >(); 
		List<String> list_res = null;
		try {			
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("LDC2003T05\\test.txt")));
			String line = "";
			
			InputStream is = new FileInputStream("model-1.5-en\\en-token.bin"); 
			TokenizerModel model = new TokenizerModel(is);
			Tokenizer tokenizer = new TokenizerME(model);	
			InputStream io = new FileInputStream(new File("model-1.5-en\\en-parser-chunking.bin"));
			ParserModel parseModel = new ParserModel(io);
			
			boolean sectionStart = false;
			String tempSection = "";
			MaxentTagger tagger = new MaxentTagger("models/wsj-0-18-left3words-nodistsim.tagger");
			
			while((line = br.readLine()) != null) {	 
				
				if(line.indexOf("---") != -1) {	
					sectionStart = true;
							
					String[] sentences = tempSection.split("#");
					
					final List<Mention> document = new ArrayList<Mention>();
					for (int i=0; i < sentences.length; i++) {
						final Parse parse = parseSentence(sentences[i], tokenizer, parseModel);
						
						final DefaultParse parseWrapper = new DefaultParse(parse, i);
						final Mention[] extents = linker().getMentionFinder().getMentions(parseWrapper);
						
						//Note: taken from TreebankParser source...
						for (int ei=0, en=extents.length; ei<en; ei++) {
							//System.out.println("i : "+ei+" "+extents[ei].getNp());
							// construct parses for mentions which don't have constituents
							if (extents[ei].getParse() == null) {
								// not sure how to get head index, but it doesn't seem to be used at this point
								final Parse snp = new Parse(parse.getText(), 
									  extents[ei].getSpan(), "NML", 1.0, 0);
								parse.insert(snp);
								// setting a new Parse for the current extent
								extents[ei].setParse(new DefaultParse(snp, i));
							}
						}
						document.addAll(Arrays.asList(extents));
					}
					
					ArrayList< ArrayList<String> > verbs = new ArrayList< ArrayList<String> >();
					if (!document.isEmpty()) {
						final DiscourseEntity[] entities = linker().getEntities(document.toArray(new Mention[document.size()]));
						
						for(String sentence : sentences) {
							ArrayList<String> verb_sen = new ArrayList<String>();
							String tagged = tagger.tagString(sentence);
							System.out.println(tagged);
							String[] posTags = tagged.split(" ");
							for (String tags : posTags) {
								if(tags.indexOf("VB") != -1) {
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
								System.out.print("[" + mc.toString() + "]");
								if(entity_map.get(mc.toString().toLowerCase().trim()) == null)
									entity_map.put(mc.toString().trim().toLowerCase(),i+1);
							}
							System.out.println();
						}
					}
					tempSection = "";
					/*
					for(ArrayList<String> s : verbs) {
						String[] verb_arr = new String[s.size()];
						verb_arr = s.toArray(verb_arr);
						for(String str : verb_arr)
							System.out.print(str+" ");
						System.out.println();
					}*/
					int index = 0;
					for(String sentence : sentences){
						System.out.println("sentences : "+sentence);
						list_res = new ArrayList<String>();
						node_map = new HashMap<String,NodePair>();
						String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
						LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);
						
						TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
						edu.stanford.nlp.process.Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new StringReader(sentence));
						List<CoreLabel> rawWords2 = tok.tokenize();
						Tree parse = lp.apply(rawWords2);

						TreebankLanguagePack tlp = new PennTreebankLanguagePack(); // PennTreebankLanguagePack for English
						GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
						GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
						Collection<TypedDependency> tdl = gs.typedDependencies();
						ArrayList<String> temp_verb = verbs.get(index);	

						for(TypedDependency td: tdl) {
							String rel = td.reln().toString();
							String gov = td.gov().toString().split("/")[0].toLowerCase();
							String dep = td.dep().toString().split("/")[0].toLowerCase();
							//System.out.println("rel : "+rel+" gov : "+gov+" dep : "+dep);
							if(temp_verb.contains(gov)){
								if(checkPartOfKey(entity_map,entity_map.keySet(),dep) != -1){
									list_res.add(checkPartOfKey(entity_map,entity_map.keySet(),dep)+" "+gov+" "+rel+" "+index);
								}
							}else if (temp_verb.contains(dep)){
								if(checkPartOfKey(entity_map,entity_map.keySet(),gov) != -1){
									list_res.add(checkPartOfKey(entity_map,entity_map.keySet(),gov)+" "+dep+" "+rel+" "+index);
								}
							}
						}
						list_res_whole.add(list_res);
						index++;
					}
					System.out.println("#################################################");
					for(int i=0; i<list_res_whole.size();i++){
						List<String> list_res_t = list_res_whole.get(i);
						for(String str : list_res_t){
							
							System.out.println(str);
							String[] ar = str.split(" ");
							if (node_map.get(ar[0]) == null){
								NodePair np = new NodePair();
								np.key = Integer.parseInt(ar[3]);
								np.first = new Node();
								np.first.verb = ar[1];
								np.first.rel = ar[2];
								node_map.put(ar[0],np);
							}else{
								NodePair np = node_map.get(ar[0]);
								int val = Integer.parseInt(ar[3]);
								if (!np.first.verb.equals(ar[1]) && val == np.key){
									np.second = new Node();
									np.second.verb = ar[1];
									np.second.rel = ar[2];
								}
							}
						}
					}
					
					for (Map.Entry<String, NodePair> entry : node_map.entrySet()) {
						String key = entry.getKey();
						System.out.print("Key : " + entry.getKey() + " Value : ");
						node_map.get(key).toPString();
					}
					//return;
				} else {					
					tempSection =  tempSection + "#" + line;
				}
				
				
				// Stanford dependency parser and tagger
				/*String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
				LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);
				
				TokenizerFactory<CoreLabel> tokenizerFactory =
				PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
				Tokenizer<CoreLabel> tok =
					tokenizerFactory.getTokenizer(new StringReader(line));
				List<CoreLabel> rawWords2 = tok.tokenize();
				Tree parse = lp.apply(rawWords2);

				TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
				GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
				GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
				List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
				System.out.println(tdl);
				System.out.println();

				// You can also use a TreePrint object to print trees and dependencies
				TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
				tp.printTree(parse);
				
				MaxentTagger tagger = new MaxentTagger("models/wsj-0-18-left3words-nodistsim.tagger");
				String tagged = tagger.tagString(line);
				 
				// Output the result
				 
				System.out.println(tagged);
				System.out.println("\n\n\n");*/
			}
		} catch(Exception exp) {
			exp.printStackTrace();
		}
	}
	
	public static Parse parseSentence(final String text, Tokenizer tokenizer, ParserModel parserModel) throws IOException {
      
      final Parse p = new Parse(text,
            // a new span covering the entire text
            new Span(0, text.length()),
            AbstractBottomUpParser.INC_NODE, 1, 0);

      final Span[] spans = tokenizer.tokenizePos(text);

      for (int idx=0; idx < spans.length; idx++) {
         final Span span = spans[idx];
         // flesh out the parse with token sub-parses
         p.insert(new Parse(text, span,
               AbstractBottomUpParser.TOK_NODE,
               0,
               idx));
      }

      return ParserFactory.create(parserModel).parse(p);
   }
   
   private static Linker linker() throws IOException {
	   if(mylinker == null) {
			mylinker = new DefaultLinker(
                  "lib/opennlp-1.5-en/coref", LinkerMode.TEST);
	   }
	   return mylinker;
   }
   
   
}

