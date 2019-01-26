package com.app;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.*;
import org.xml.sax.InputSource;
import org.w3c.dom.*;
import org.w3c.dom.Node;

import java.io.*;
import jess.*;


@Path("/api")
public class Bright {

	int amountSent;
	Rete engine;
	String result;
	
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String checkJess(String reqData) {
	
//	Read xml
	try {
		DocumentBuilderFactory dbf =
			DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(reqData)); //xml string is entered
		Document doc = db.parse(is);	
		NodeList transaction = doc.getElementsByTagName("transaction");
		Element element = (Element) transaction.item(0);
		NodeList amount = element.getElementsByTagName("amount");  
		Element line = (Element) amount.item(0);   
		amountSent = Integer.parseInt(getCharacterDataFromElement(line));
							
	}
	catch (Exception e) {
		e.printStackTrace();
	}
//end Read xml
	
//	Initiate Jess engine
	engine = new Rete();
	
//	Store the value from Java in Jess
	
	engine.store("AMOUNT", amountSent);
	String rule = "(defrule amount-greater-than-50000\n" + 
			"    (amount ?a&:(> ?a 50000))" + 
			"    =>" + 
			"    (store  RESULT \"<resMsg>Jess says the amount is greater than allowed limit</resMsg>"
			+ "<continue>false</continue>\")" + 
			"    " +  
			")";
	String rule1 = "(defrule amount-less-than-50000" + 
			"    (amount ?a&:(< ?a 50000))" + 
			"    =>" + 
			"    (store  RESULT \"<resMsg>Jess says the amount is less than 50000</resMsg>"
			+ "<continue>true</continue>\")" + 
			"    " +  
			")";
	//Create a fact and assert it
	try {
	    
		engine.executeCommand("(assert (amount (fetch AMOUNT)))");
		engine.executeCommand(rule);
		engine.executeCommand(rule1);
		engine.executeCommand("(run)");
		Value resValue = engine.fetch("RESULT");
	 result = resValue.stringValue(engine.getGlobalContext());
	} catch (JessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}



	
	


	return "<data>"+result+
	"</data>";	        

	
	//CheckJess function ends here
	}
	  
	private String getCharacterDataFromElement(Element e){
		// TODO Auto-generated method stub
	
		
		Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
           CharacterData cd = (CharacterData) child;
           return cd.getData();
        }
        return "?";
		
	}
}
