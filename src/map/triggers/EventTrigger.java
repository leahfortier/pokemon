package map.triggers;

import gui.view.MapView;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Game;
import main.Game.ViewMode;

/**
 * Spawns DialogueSequences and adds character globals
 *
 */
public class EventTrigger extends Trigger{
	private static final Pattern dialoguePattern = Pattern.compile("(?:(dialogue):\\s*(\\w+)|(createDialogue):\\s*(\\w+)\\s+([\\s\\S]+))");
	private static final Pattern dialogueCreationPattern = Pattern.compile("text\\[(\\d+)\\]:\\s*\"([^\"]+)\"");
	
	public String dialogue;
	public String dialogueName;
	public boolean createDialogue;
	private boolean needToCreateDialogue;
	
	public String[] dialogueLines;
	
	public EventTrigger(String name, String contents) {
		super(name, contents);
		
		createDialogue = false;
		needToCreateDialogue = false;
		
		Matcher m = dialoguePattern.matcher(contents);
		while(m.find()){
			if(m.group(1) != null) {
				dialogueName = m.group(2);
			}
			if(m.group(3) != null) {
				createDialogue = true;
				needToCreateDialogue = true;
				
				dialogue = m.group(4);
				dialogueName = name+"_Dialogue_" +dialogue+"_01";
				
				dialogueLines = new String[100];
				int max = -1;
				
				Matcher creationMatcher = dialogueCreationPattern.matcher(m.group(5));
				
				while(creationMatcher.find()) {
					int val = Integer.parseInt(creationMatcher.group(1));
					dialogueLines[val] = creationMatcher.group(2);
					max = Math.max(max, val);
				}
				 
				dialogueLines = Arrays.copyOf(dialogueLines, max+1);
			}
		}
	}
	@Override
	public void execute(Game game){
		
		if(needToCreateDialogue) {
			for(int i = 0; i < dialogueLines.length; ++i) {
				String next = i+1 == dialogueLines.length? "": "next[0]: " +name+"_Dialogue_" +dialogue+String.format("_%02d",i+2);
				game.data.addDialogue(name+"_Dialogue_" +dialogue+String.format("_%02d",i+1), "text: \""+dialogueLines[i]+"\"\n" +next);
			}
			
			needToCreateDialogue = false;
		}
		
		super.execute(game);
		if(dialogueName != null){
			((MapView)game.viewMap.get(ViewMode.MAP_VIEW)).setDialogue(dialogueName);
		}
	}
	
	@Override
	public String toString() {
		return "EventTrigger: "+name+" dialogue:"+dialogueName + " global:"+globals.toString();
	}
	
	@Override
	public String triggerDataAsString() {
		StringBuilder ret = new StringBuilder(super.triggerDataAsString());
		
		if(createDialogue) {
			ret.append("\tcreateDialogue: " +dialogue +"\n");
			
			for(int currDialogue = 0; currDialogue < dialogueLines.length; ++currDialogue) {
				ret.append("\ttext["+currDialogue+"]: \""+dialogueLines[currDialogue].trim() + "\"\n");
			}
		}
		else {
			ret.append("\tdialogue: " +dialogueName);
			ret.append("\n");
		}
		
		return ret.toString();
	}
}
