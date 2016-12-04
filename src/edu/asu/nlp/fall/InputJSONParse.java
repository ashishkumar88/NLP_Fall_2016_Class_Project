package edu.asu.nlp.fall;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class InputJSONParse {
	public static void main(String[] args) {
		ObjectMapper mapper = new ObjectMapper();

		ArrayList<EventChain> eventChains = new ArrayList<>();
		try {
			EventChains eventChain = mapper.readValue(new File("output" + File.separator + "unordered_event.json"),
					EventChains.class);
			for (EventChain chain : eventChain.getEventChains()) {
				Event event1 = chain.getEventChain().get(0);
				Event event2 = chain.getEventChain().get(1);
				if ((event1.getObject().equals("null")) || (event1.getSubject().equals("null"))
						|| (event1.getVerb().equals("null")) || (event2.getObject().equals("null"))
						|| (event2.getSubject().equals("null")) || (event2.getVerb().equals("null"))) {
					System.out.println(event1);
					System.out.println(event2);
				} else {
					eventChains.add(chain);
				}
			}

			EventChains chains = new EventChains();
			chains.setEventChains(eventChains);
			mapper.writeValue(new File("output" + File.separator + "unordered_event_without_null.json"), eventChains);
		} catch (JsonParseException e) {
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
}
